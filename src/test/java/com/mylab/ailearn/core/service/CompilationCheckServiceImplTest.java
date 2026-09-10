package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.CompileCheckReport;
import com.mylab.ailearn.core.model.commonmodel.CompileStatus;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationCheckServiceImplTest {

    private final CompilationCheckServiceImpl service = new CompilationCheckServiceImpl();

    @Test
    void compilesValidJava() {
        String content = "public class Main {\n"
                + "  public static void main(String[] args) {\n"
                + "    System.out.println(\"hi\");\n"
                + "  }\n"
                + "}\n";

        CompileCheckReport report = service.check(java("Main.java", content));

        assertThat(report.status()).isEqualTo(CompileStatus.PASSED);
        assertThat(report.errors()).isEmpty();
    }

    @Test
    void reportsJavaCompileErrors() {
        String content = "public class Main {\n"
                + "  public static void main(String[] args) {\n"
                + "    int x = ;\n"
                + "  }\n"
                + "}\n";

        CompileCheckReport report = service.check(java("Main.java", content));

        assertThat(report.status()).isEqualTo(CompileStatus.FAILED);
        assertThat(report.errors()).isNotEmpty();
        assertThat(report.errors().get(0).line()).isGreaterThan(0);
    }

    private SourceFile java(String filename, String content) {
        return new SourceFile(null, 1L, filename, ProgrammingLanguage.JAVA, null, content, LocalDateTime.now());
    }
}
