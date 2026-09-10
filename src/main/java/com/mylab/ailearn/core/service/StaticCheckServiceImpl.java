package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.enums.RuleType;
import com.mylab.ailearn.core.model.commonmodel.RuleViolation;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则静态检查的默认实现。
 *
 * <p>覆盖大一高频错误：括号不匹配、缩进风格、变量命名、使用未初始化变量、内存泄漏、
 * 空指针 / 野指针解引用、数组越界与死循环。规则都是基于正则的启发式筛查，只做硬性判定，
 * 筛不出的逻辑错误交给 LLM 深度批改。</p>
 *
 * <p><b>语言差异</b>：空指针检查分两套——C++ 走指针声明与解引用分析，Java 走对象引用
 * 调用分析；其余规则两种语言共用。</p>
 */
@Service
public class StaticCheckServiceImpl implements StaticCheckService {

    /** 允许保留的单字母变量名：循环变量 i / j / k 不算命名不规范。 */
    private static final Set<String> COMMON_LOOP_NAMES = Set.of("i", "j", "k");

    /**
     * 对源码做静态检查。
     *
     * <p>匹配前会先剔除注释与字符串字面量，因此不会把注释里的括号或关键字误判为代码；
     * 返回的行号仍是原始源码中的行号。{@code sourceFile.language()} 为 null 时按 C++ 处理。</p>
     *
     * <p>永不返回 null；{@code sourceFile} 为空时抛 400。</p>
     */
    @Override
    public StaticCheckReport check(SourceFile sourceFile) {
        if (sourceFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceFile 不能为空");
        }

        String raw = Objects.toString(sourceFile.content(), "");
        ProgrammingLanguage language = sourceFile.language() == null
                ? ProgrammingLanguage.CPP
                : sourceFile.language();
        // 先剔除注释与字符串/字符字面量，避免把内容里的括号或关键字误判为代码结构。
        String code = stripCommentsAndStrings(raw);

        List<RuleViolation> violations = new ArrayList<>();

        checkBraceMismatch(code, violations);
        checkParenMismatch(code, violations);
        // 缩进只关心每行行首空白，应基于原始文本判断，避免注释剔除的干扰。
        checkIndentation(raw, violations);
        checkNamingConvention(code, violations);
        if (language == ProgrammingLanguage.CPP) {
            checkMemoryLeak(code, violations);
            checkNullPointerDereference(code, violations);
        } else {
            checkJavaNullDereference(code, violations);
        }
        checkArrayOutOfBounds(code, language, violations);
        checkUninitializedVariable(code, violations);
        checkInfiniteLoop(code, violations);

        return new StaticCheckReport(violations);
    }

    private void checkBraceMismatch(String code, List<RuleViolation> out) {
        int open = countMatches(code, "\\{");
        int close = countMatches(code, "\\}");
        if (open != close) {
            Integer line = open > close
                    ? lineOfFirst(code, "\\{")
                    : lineOfFirst(code, "\\}");
            out.add(new RuleViolation(
                    RuleType.BRACE_MISMATCH,
                    RuleType.BRACE_MISMATCH.category(),
                    "花括号不匹配：左括号 " + open + " 个，右括号 " + close + " 个",
                    line,
                    "检查缺少或多余的花括号 { } 以保证代码块闭合完整"));
        }
    }

    private void checkParenMismatch(String code, List<RuleViolation> out) {
        int open = countMatches(code, "\\(");
        int close = countMatches(code, "\\)");
        if (open != close) {
            Integer line = open > close
                    ? lineOfFirst(code, "\\(")
                    : lineOfFirst(code, "\\)");
            out.add(new RuleViolation(
                    RuleType.PAREN_MISMATCH,
                    RuleType.PAREN_MISMATCH.category(),
                    "圆括号不匹配：左括号 " + open + " 个，右括号 " + close + " 个",
                    line,
                    "检查缺少或多余的圆括号 ( )"));
        }
    }

    private void checkIndentation(String code, List<RuleViolation> out) {
        String[] lines = code.split("\\r?\\n");
        int tabStarts = 0;
        int spaceStarts = 0;
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            char first = line.charAt(0);
            if (first == '\t') {
                tabStarts++;
            } else if (first == ' ') {
                spaceStarts++;
            }
        }

        // 以「行首用 tab 的行更多，还是用空格的行更多」推断整份文件的缩进风格
        boolean useTabs = tabStarts > spaceStarts;
        // 空格缩进的判定单位：用空格时，行首空格数必须是它的整数倍才算对齐
        int unit = 4;
        int badLines = 0;
        int firstBadLine = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                continue;
            }
            char first = line.charAt(0);
            int leadingSpaces = leadingSpaces(line);
            boolean inconsistent;
            if (useTabs) {
                inconsistent = first == ' ';
            } else {
                inconsistent = first == '\t' || (leadingSpaces > 0 && leadingSpaces % unit != 0);
            }
            if (inconsistent) {
                badLines++;
                if (firstBadLine < 0) {
                    firstBadLine = i + 1;
                }
            }
        }

        if (badLines > 0) {
            out.add(new RuleViolation(
                    RuleType.INDENTATION,
                    RuleType.INDENTATION.category(),
                    "缩进风格不一致，共 " + badLines + " 行",
                    firstBadLine,
                    "统一使用一种缩进（推荐 4 个空格），并保持每层嵌套递增一个单位"));
        }
    }

    private void checkNamingConvention(String code, List<RuleViolation> out) {
        Pattern declaration = Pattern.compile("\\b(int|double|float|char|long|short|boolean|String|byte|auto)\\s+([A-Za-z_]\\w*)");
        Matcher matcher = declaration.matcher(code);
        while (matcher.find()) {
            String name = matcher.group(2);
            if (name.length() == 1 && !COMMON_LOOP_NAMES.contains(name)) {
                out.add(new RuleViolation(
                        RuleType.NAMING_CONVENTION,
                        RuleType.NAMING_CONVENTION.category(),
                        "变量名 \"" + name + "\" 过短，含义不清",
                        lineAt(code, matcher.start()),
                        "使用能表达含义的变量名，循环变量可保留常见的 i / j / k"));
            }
        }
    }

    private void checkMemoryLeak(String code, List<RuleViolation> out) {
        int allocations = countMatches(code, "\\bnew\\b") + countMatches(code, "\\bmalloc\\s*\\(");
        int deallocations = countMatches(code, "\\bdelete\\b") + countMatches(code, "\\bfree\\s*\\(");

        if (allocations > deallocations) {
            Integer line = lineOfFirst(code, "\\bnew\\b|\\bmalloc\\s*\\(");
            out.add(new RuleViolation(
                    RuleType.MEMORY_LEAK,
                    RuleType.MEMORY_LEAK.category(),
                    "检测到 " + allocations + " 处内存分配，但仅 " + deallocations + " 处释放",
                    line,
                    "确保每个 new / malloc 都有对应的 delete / free，避免内存泄漏"));
        }
    }

    private void checkNullPointerDereference(String code, List<RuleViolation> out) {
        // 匹配「int *p;」「int *p = NULL;」「int *p = nullptr;」等未初始化 / 空指针声明。
        Pattern declaration = Pattern.compile("\\b\\w+\\s*\\*\\s*(\\w+)\\s*(?:=\\s*(NULL|nullptr|0))?\\s*;");
        Matcher matcher = declaration.matcher(code);

        while (matcher.find()) {
            String name = matcher.group(1);
            String rest = code.substring(matcher.end());

            Pattern assign = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*=(?!=)");
            Pattern deref = Pattern.compile("\\*" + Pattern.quote(name) + "\\b|" + Pattern.quote(name) + "\\s*->");

            Matcher derefMatcher = deref.matcher(rest);
            int derefIndex = derefMatcher.find() ? derefMatcher.start() : -1;
            Matcher assignMatcher = assign.matcher(rest);
            int assignIndex = assignMatcher.find() ? assignMatcher.start() : -1;

            if (derefIndex >= 0 && (assignIndex < 0 || derefIndex < assignIndex)) {
                out.add(new RuleViolation(
                        RuleType.NULL_POINTER_DEREFERENCE,
                        RuleType.NULL_POINTER_DEREFERENCE.category(),
                        "指针 \"" + name + "\" 在赋有效地址前被解引用",
                        lineAt(code, matcher.end() + derefIndex),
                        "解引用前先为指针赋有效地址（new / & / malloc），并做空指针判断"));
            }
        }
    }


    private void checkArrayOutOfBounds(String code, ProgrammingLanguage language, List<RuleViolation> out) {
        if (language == ProgrammingLanguage.JAVA) {
            checkJavaArrayOutOfBounds(code, out);
        } else {
            checkCppArrayOutOfBounds(code, out);
        }
    }

    private void checkCppArrayOutOfBounds(String code, List<RuleViolation> out) {
        // 匹配「int a[10];」这类定长数组声明。
        Pattern declaration = Pattern.compile("\\b\\w+\\s+(\\w+)\\s*\\[(\\d+)\\]\\s*;");
        Matcher matcher = declaration.matcher(code);

        while (matcher.find()) {
            String name = matcher.group(1);
            int size = Integer.parseInt(matcher.group(2));
            checkArrayOutOfBounds(code, name, size, matcher.end(), out);
        }
    }

    private void checkJavaArrayOutOfBounds(String code, List<RuleViolation> out) {
        // 匹配「int[] a = new int[10];」或「int a[] = new int[10];」。
        Pattern declaration = Pattern.compile(
                "\\b\\w+\\s*\\[\\s*\\]\\s*(\\w+)\\s*=\\s*new\\s+\\w+\\s*\\[(\\d+)\\]"
                        + "|\\b\\w+\\s+(\\w+)\\s*\\[\\s*\\]\\s*=\\s*new\\s+\\w+\\s*\\[(\\d+)\\]");
        Matcher matcher = declaration.matcher(code);

        while (matcher.find()) {
            String name;
            int size;
            if (matcher.group(1) != null) {
                name = matcher.group(1);
                size = Integer.parseInt(matcher.group(2));
            } else {
                name = matcher.group(3);
                size = Integer.parseInt(matcher.group(4));
            }
            checkArrayOutOfBounds(code, name, size, matcher.end(), out);
        }
    }

    /**
     * 对已定位到的定长数组做越界检查，C++ 与 Java 共用同一套判断逻辑。
     *
     * <p>两种情况：1) 直接使用字面量下标 a[size]（合法下标应为 0..size-1）；
     * 2) 形如 for(...; i &lt;= size; ...) 的循环再访问 a[i] 造成的 off-by-one。</p>
     */
    private void checkArrayOutOfBounds(String code, String name, int size, int declEnd, List<RuleViolation> out) {
        Pattern literalAccess = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*\\[\\s*" + size + "\\s*\\]");
        Matcher accessMatcher = literalAccess.matcher(code);
        if (accessMatcher.find(declEnd)) {
            out.add(new RuleViolation(
                    RuleType.ARRAY_OUT_OF_BOUNDS,
                    RuleType.ARRAY_OUT_OF_BOUNDS.category(),
                    "数组 \"" + name + "\" 下标 " + size + " 越界，合法范围是 0.." + (size - 1),
                    lineAt(code, accessMatcher.start()),
                    "把下标控制在 0.." + (size - 1) + " 之间"));
            return;
        }

        Pattern boundaryLoop = Pattern.compile("for\\s*\\([^;]*;[^;]*<=\\s*" + size + "\\s*;");
        Pattern variableAccess = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*\\[\\s*\\w+\\s*\\]");
        Matcher loopMatcher = boundaryLoop.matcher(code);
        Matcher accessMatcher2 = variableAccess.matcher(code);
        if (loopMatcher.find(declEnd) && accessMatcher2.find(declEnd)) {
            out.add(new RuleViolation(
                    RuleType.ARRAY_OUT_OF_BOUNDS,
                    RuleType.ARRAY_OUT_OF_BOUNDS.category(),
                    "疑似 off-by-one：存在 \"" + name + "[i]\" 且循环条件使用 <= " + size,
                    lineAt(code, loopMatcher.start()),
                    "长度为 " + size + " 的数组应使用 < " + size + " 作为循环条件"));
        }
    }

    private void checkJavaNullDereference(String code, List<RuleViolation> out) {
        // 匹配「Scanner sc;」这类未初始化的对象引用声明（类型名以大写字母开头）。
        Pattern declaration = Pattern.compile("\\b([A-Z][A-Za-z0-9_]*)\\s+(\\w+)\\s*;");
        Matcher matcher = declaration.matcher(code);

        while (matcher.find()) {
            String name = matcher.group(2);
            String rest = code.substring(matcher.end());

            Pattern assign = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*=(?!=)");
            Pattern use = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*\\.\\s*\\w+");

            Matcher useMatcher = use.matcher(rest);
            int useIndex = useMatcher.find() ? useMatcher.start() : -1;
            Matcher assignMatcher = assign.matcher(rest);
            int assignIndex = assignMatcher.find() ? assignMatcher.start() : -1;

            if (useIndex >= 0 && (assignIndex < 0 || useIndex < assignIndex)) {
                out.add(new RuleViolation(
                        RuleType.NULL_POINTER_DEREFERENCE,
                        RuleType.NULL_POINTER_DEREFERENCE.category(),
                        "对象引用 \"" + name + "\" 在赋值前被调用，可能引发 NullPointerException",
                        lineAt(code, matcher.end() + useIndex),
                        "使用前先 new 实例化或做 null 判断"));
            }
        }
    }

    private void checkUninitializedVariable(String code, List<RuleViolation> out) {
        // 匹配「int a;」这类无初始化的声明。
        Pattern declaration = Pattern.compile("\\b(int|double|float|char|long|short|boolean|String|byte)\\s+(\\w+)\\s*;");
        Matcher matcher = declaration.matcher(code);

        while (matcher.find()) {
            String name = matcher.group(2);
            String rest = code.substring(matcher.end());

            Pattern assign = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*=(?!=)");
            Pattern use = Pattern.compile("\\b" + Pattern.quote(name) + "\\b(?!\\s*=)");

            Matcher useMatcher = use.matcher(rest);
            int useIndex = useMatcher.find() ? useMatcher.start() : -1;
            Matcher assignMatcher = assign.matcher(rest);
            int assignIndex = assignMatcher.find() ? assignMatcher.start() : -1;

            if (useIndex >= 0 && (assignIndex < 0 || useIndex < assignIndex)) {
                out.add(new RuleViolation(
                        RuleType.UNINITIALIZED_VARIABLE,
                        RuleType.UNINITIALIZED_VARIABLE.category(),
                        "变量 \"" + name + "\" 在使用前可能未初始化",
                        lineAt(code, matcher.end() + useIndex),
                        "声明变量时给定初值，使用前确保已赋值"));
            }
        }
    }

    private void checkInfiniteLoop(String code, List<RuleViolation> out) {
        Pattern header = Pattern.compile("\\bwhile\\s*\\(\\s*(?:true|1)\\s*\\)|\\bfor\\s*\\(\\s*;\\s*;\\s*\\)");
        Matcher matcher = header.matcher(code);

        while (matcher.find()) {
            int headerEnd = matcher.end();
            String tail = code.substring(headerEnd);
            String body;
            int open = tail.indexOf('{');
            if (open < 0) {
                int semi = tail.indexOf(';');
                body = semi < 0 ? tail : tail.substring(0, semi);
            } else {
                int depth = 0;
                int close = -1;
                for (int i = open; i < tail.length(); i++) {
                    char c = tail.charAt(i);
                    if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) {
                            close = i;
                            break;
                        }
                    }
                }
                body = close < 0 ? tail.substring(open + 1) : tail.substring(open + 1, close);
            }

            if (!body.contains("break") && !body.contains("return")) {
                out.add(new RuleViolation(
                        RuleType.INFINITE_LOOP,
                        RuleType.INFINITE_LOOP.category(),
                        "疑似死循环：循环体内没有 break / return 退出",
                        lineAt(code, matcher.start()),
                        "为循环设置可满足的终止条件或补充 break / return"));
            }
        }
    }


    /**
     * 把注释、字符串与字符字面量替换成空格，让后续规则只匹配真正的代码。
     *
     * <p>用空格替换而不是删除，是为了保持每个字符的下标不变，从而让 {@link #lineAt}
     * 算出的行号与原始源码完全一致。换行符本身保留，避免行数变化。</p>
     */
    private String stripCommentsAndStrings(String code) {
        StringBuilder cleaned = new StringBuilder(code);
        removeMatches(cleaned, Pattern.compile("(?s)/\\*.*?\\*/"));
        removeMatches(cleaned, Pattern.compile("//[^\\r\\n]*"));
        removeMatches(cleaned, Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\""));
        removeMatches(cleaned, Pattern.compile("'(?:\\\\.|[^'\\\\])'"));
        return cleaned.toString();
    }

    private void removeMatches(StringBuilder text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        List<int[]> spans = new ArrayList<>();
        while (matcher.find()) {
            spans.add(new int[]{matcher.start(), matcher.end()});
        }
        for (int[] span : spans) {
            for (int i = span[0]; i < span[1]; i++) {
                char c = text.charAt(i);
                if (c != '\n' && c != '\r') {
                    text.setCharAt(i, ' ');
                }
            }
        }
    }

    private int countMatches(String input, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int leadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    /** 返回下标 {@code index} 所在的行号，从 1 开始计数。 */
    private int lineAt(String code, int index) {
        String prefix = code.substring(0, Math.min(index, code.length()));
        return 1 + (int) prefix.chars().filter(c -> c == '\n').count();
    }

    /** 返回第一个匹配所在的行号；没有任何匹配时返回 null。 */
    private Integer lineOfFirst(String code, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(code);
        return matcher.find() ? lineAt(code, matcher.start()) : null;
    }
}
