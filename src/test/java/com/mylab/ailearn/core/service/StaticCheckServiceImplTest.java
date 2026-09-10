package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.enums.RuleType;
import com.mylab.ailearn.core.model.commonmodel.RuleViolation;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaticCheckServiceImplTest {

    private final StaticCheckServiceImpl checker = new StaticCheckServiceImpl();

    @Test
    void detectsBraceMismatch() {
        SourceFile file = cpp("int main() {\n    return 0;\n");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.BRACE_MISMATCH);
    }

    @Test
    void detectsParenMismatch() {
        SourceFile file = cpp("int main() {\n    int x = (1 + 2;\n    return 0;\n}");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.PAREN_MISMATCH);
    }

    @Test
    void ignoresBracketsInsideCommentsAndStrings() {
        String content = """
                int main() {
                    // print '(' and '{'
                    std::cout << "(" << "{" << std::endl;
                    return 0;
                }
                """;
        assertThat(ruleTypes(checker.check(cpp(content))))
                .doesNotContain(RuleType.BRACE_MISMATCH, RuleType.PAREN_MISMATCH);
    }

    @Test
    void detectsCppArrayOutOfBounds() {
        SourceFile file = cpp("int a[3];\nint main() {\n    std::cout << a[3];\n    return 0;\n}");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.ARRAY_OUT_OF_BOUNDS);
    }

    @Test
    void detectsJavaArrayOutOfBounds() {
        String content = """
                public class Main {
                    public static void main(String[] args) {
                        int[] a = new int[3];
                        System.out.println(a[3]);
                    }
                }
                """;
        assertThat(ruleTypes(checker.check(java(content)))).contains(RuleType.ARRAY_OUT_OF_BOUNDS);
    }

    @Test
    void detectsMemoryLeakInCpp() {
        SourceFile file = cpp("int* p = new int;\nint main() { return 0; }");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.MEMORY_LEAK);
    }

    @Test
    void detectsNullPointerDereferenceInCpp() {
        SourceFile file = cpp("int* p;\nint main() {\n    *p = 5;\n    return 0;\n}");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.NULL_POINTER_DEREFERENCE);
    }

    @Test
    void detectsJavaNullDereference() {
        String content = """
                import java.util.Scanner;
                public class Main {
                    public static void main(String[] args) {
                        Scanner sc;
                        sc.nextInt();
                    }
                }
                """;
        assertThat(ruleTypes(checker.check(java(content)))).contains(RuleType.NULL_POINTER_DEREFERENCE);
    }

    @Test
    void detectsUninitializedVariable() {
        SourceFile file = cpp("int value;\nint main() {\n    printf(\"%d\", value);\n    return 0;\n}");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.UNINITIALIZED_VARIABLE);
    }

    @Test
    void detectsInfiniteLoop() {
        SourceFile file = cpp("int main() {\n    while (true) {\n    }\n    return 0;\n}");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.INFINITE_LOOP);
    }

    @Test
    void detectsInconsistentIndentation() {
        SourceFile file = cpp("int main() {\n\tint a = 1;\n    int b = 2;\n}");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.INDENTATION);
    }

    @Test
    void detectsNamingConventionViolation() {
        SourceFile file = cpp("int main() {\n    int x = 1;\n    return x;\n}");
        assertThat(ruleTypes(checker.check(file))).contains(RuleType.NAMING_CONVENTION);
    }

    private SourceFile cpp(String content) {
        return file("main.cpp", ProgrammingLanguage.CPP, content);
    }

    private SourceFile java(String content) {
        return file("Main.java", ProgrammingLanguage.JAVA, content);
    }

    private SourceFile file(String filename, ProgrammingLanguage language, String content) {
        return new SourceFile(null, 1L, filename, language, null, content, LocalDateTime.now());
    }

    private List<RuleType> ruleTypes(StaticCheckReport report) {
        return report.violations().stream().map(RuleViolation::ruleType).toList();
    }
}