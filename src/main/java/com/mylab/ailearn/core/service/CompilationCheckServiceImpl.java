package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.CompileCheckReport;
import com.mylab.ailearn.core.model.commonmodel.CompileError;
import com.mylab.ailearn.core.model.commonmodel.CompileStatus;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import org.springframework.stereotype.Service;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编译检查的默认实现。
 *
 * <p>Java 用进程内 {@link JavaCompiler} 编译；C++ 用子进程 g++ / clang++ 做语法检查。
 * 两者都只编译、不运行。编译器不可用时降级为「未检查」（不产生错误，不阻断批改）。</p>
 */
@Service
public class CompilationCheckServiceImpl implements CompilationCheckService {

    private static final long CPP_TIMEOUT_SECONDS = 15;
    private static final List<String> CPP_COMPILER_CANDIDATES = List.of("g++", "clang++");
    private static final Pattern GCC_ERROR = Pattern.compile(":(\\d+):\\d+:\\s*error:\\s*(.*)");

    /** 缓存的可用 C++ 编译器；null 表示尚未探测到可用编译器。 */
    private volatile String cppCompiler;
    private volatile boolean cppCompilerResolved;

    @Override
    public CompileCheckReport check(SourceFile sourceFile) {
        if (sourceFile == null || sourceFile.content() == null || sourceFile.content().isBlank()) {
            return new CompileCheckReport(CompileStatus.SKIPPED, List.of());
        }
        if (sourceFile.language() == ProgrammingLanguage.JAVA) {
            return compileJava(sourceFile);
        }
        return compileCpp(sourceFile);
    }

    private CompileCheckReport compileJava(SourceFile sourceFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            // 运行在 JRE 上（无 javac），降级跳过
            return new CompileCheckReport(CompileStatus.SKIPPED, List.of());
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);

        JavaFileObject source = new SimpleJavaFileObject(
                URI.create("string:///" + sourceFile.filename()), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceFile.content();
            }
        };

        Path outDir = null;
        try {
            outDir = Files.createTempDirectory("javac_out");
            List<String> options = List.of("-d", outDir.toString(), "-proc:none", "-encoding", "UTF-8");
            boolean ok = compiler.getTask(null, fileManager, diagnostics, options, null, List.of(source)).call();

            List<CompileError> errors = diagnostics.getDiagnostics().stream()
                    .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                    .sorted(Comparator.comparingLong(Diagnostic::getLineNumber))
                    .map(d -> new CompileError((int) d.getLineNumber(), d.getMessage(Locale.getDefault()), null))
                    .toList();
            return new CompileCheckReport(
                    ok && errors.isEmpty() ? CompileStatus.PASSED : CompileStatus.FAILED, errors);
        } catch (IOException | RuntimeException e) {
            // 编译器不可用或内部异常（如模块/环境问题），降级跳过
            return new CompileCheckReport(CompileStatus.SKIPPED, List.of());
        } finally {
            deleteRecursively(outDir);
            closeQuietly(fileManager);
        }
    }

    private CompileCheckReport compileCpp(SourceFile sourceFile) {
        String compiler = resolveCppCompiler();
        if (compiler == null) {
            // g++ / clang++ 均未安装：降级跳过编译检查
            return new CompileCheckReport(CompileStatus.SKIPPED, List.of());
        }
        Path tmp = null;
        try {
            tmp = Files.createTempFile("student_", ".cpp");
            Files.writeString(tmp, sourceFile.content());

            Process process = new ProcessBuilder(compiler, "-fsyntax-only", "-x", "c++", tmp.toString()).start();
            boolean finished = process.waitFor(CPP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CompileCheckReport(CompileStatus.FAILED,
                        List.of(new CompileError(0, "编译超时（超过 " + CPP_TIMEOUT_SECONDS + " 秒）",
                                "检查是否存在死循环或过深的递归展开")));
            }

            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            int exit = process.exitValue();
            return new CompileCheckReport(
                    exit == 0 ? CompileStatus.PASSED : CompileStatus.FAILED, parseGccErrors(stderr));
        } catch (IOException e) {
            // 编译器启动失败：降级跳过编译检查
            return new CompileCheckReport(CompileStatus.SKIPPED, List.of());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CompileCheckReport(CompileStatus.SKIPPED, List.of());
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /** 探测可用的 C++ 编译器（g++ 优先，其次 clang++），结果缓存。 */
    private String resolveCppCompiler() {
        if (cppCompilerResolved) {
            return cppCompiler;
        }
        synchronized (this) {
            if (!cppCompilerResolved) {
                cppCompiler = firstAvailable(CPP_COMPILER_CANDIDATES);
                cppCompilerResolved = true;
            }
        }
        return cppCompiler;
    }

    private String firstAvailable(List<String> candidates) {
        for (String cmd : candidates) {
            if (isAvailable(cmd)) {
                return cmd;
            }
        }
        return null;
    }

    private boolean isAvailable(String cmd) {
        try {
            Process p = new ProcessBuilder(cmd, "--version").redirectErrorStream(true).start();
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private List<CompileError> parseGccErrors(String stderr) {
        List<CompileError> errors = new ArrayList<>();
        for (String line : stderr.split("\\R")) {
            Matcher matcher = GCC_ERROR.matcher(line);
            if (matcher.find()) {
                errors.add(new CompileError(
                        Integer.parseInt(matcher.group(1)),
                        matcher.group(2).trim(),
                        null));
            }
        }
        return errors;
    }

    private void deleteRecursively(Path dir) {
        if (dir == null) {
            return;
        }
        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private void closeQuietly(StandardJavaFileManager fileManager) {
        try {
            fileManager.close();
        } catch (IOException ignored) {
        }
    }
}
