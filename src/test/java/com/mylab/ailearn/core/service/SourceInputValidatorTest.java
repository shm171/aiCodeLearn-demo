package com.mylab.ailearn.core.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 输入预算与文件名校验的行为约束（S08 验收）：奇怪的输入必须是明确的 400，且不回显原始输入。
 */
class SourceInputValidatorTest {

    @Test
    void acceptsOrdinaryFilenames() {
        assertThat(SourceInputValidator.normalizeFilename("main.cpp")).isEqualTo("main.cpp");
        assertThat(SourceInputValidator.normalizeFilename("Main.java")).isEqualTo("Main.java");
        assertThat(SourceInputValidator.normalizeFilename("作业-1_2.cpp")).isEqualTo("作业-1_2.cpp");
    }

    @Test
    void stripsSurroundingWhitespace() {
        assertThat(SourceInputValidator.normalizeFilename("  main.cpp  ")).isEqualTo("main.cpp");
    }

    @Test
    void rejectsBlankFilename() {
        badRequestFrom(() -> SourceInputValidator.normalizeFilename(null));
        badRequestFrom(() -> SourceInputValidator.normalizeFilename("   "));
    }

    @Test
    void rejectsBlankContent() {
        badRequestFrom(() -> SourceInputValidator.requireContentWithinBudget(null));
        badRequestFrom(() -> SourceInputValidator.requireContentWithinBudget("  \n "));
    }

    @Test
    void rejectsPathSeparators() {
        badRequestFrom(() -> SourceInputValidator.normalizeFilename("a/b.cpp"));
        badRequestFrom(() -> SourceInputValidator.normalizeFilename("a\\b.cpp"));
        badRequestFrom(() -> SourceInputValidator.normalizeFilename("../../etc/passwd"));
    }

    @Test
    void rejectsWhitespaceInsideFilename() {
        badRequestFrom(() -> SourceInputValidator.normalizeFilename("Main Bad.java"));
    }

    @Test
    void rejectsControlCharactersAndDrivePrefix() {
        badRequestFrom(() -> SourceInputValidator.normalizeFilename("main\u0000.cpp"));
        badRequestFrom(() -> SourceInputValidator.normalizeFilename("C:main.cpp"));
    }

    @Test
    void rejectsOverlongFilename() {
        String tooLong = "a".repeat(SourceInputValidator.MAX_FILENAME_LENGTH) + ".cpp";

        badRequestFrom(() -> SourceInputValidator.normalizeFilename(tooLong));
    }

    @Test
    void rejectsTooManyBytes() {
        // 字符数未超 256K，但 UTF-8 字节数超了：必须按字节判定
        String multibyte = "中".repeat(100_000);
        assertThat(multibyte.length()).isLessThanOrEqualTo(SourceInputValidator.MAX_SOURCE_BYTES);

        badRequestFrom(() -> SourceInputValidator.requireContentWithinBudget(multibyte));
    }

    @Test
    void enforcesLineBudgetAtTheBoundary() {
        String atLimit = "x\n".repeat(SourceInputValidator.MAX_SOURCE_LINES - 1) + "x";
        String overLimit = "x\n".repeat(SourceInputValidator.MAX_SOURCE_LINES);

        assertThatCode(() -> SourceInputValidator.requireContentWithinBudget(atLimit)).doesNotThrowAnyException();
        badRequestFrom(() -> SourceInputValidator.requireContentWithinBudget(overLimit));
    }

    @Test
    void errorMessageDoesNotEchoTheInput() {
        String hostile = "../../etc/passwd";

        ResponseStatusException exception = badRequestFrom(() -> SourceInputValidator.normalizeFilename(hostile));

        assertThat(exception.getReason()).doesNotContain(hostile).doesNotContain("etc");
    }

    @Test
    void countsLinesConsistentlyWithLineNumbers() {
        assertThat(SourceInputValidator.countLines("a\nb")).isEqualTo(2);
        assertThat(SourceInputValidator.countLines("a\nb\n")).isEqualTo(3);
        assertThat(SourceInputValidator.countLines("a")).isEqualTo(1);
        assertThat(SourceInputValidator.countLines("")).isEqualTo(1);
    }

    @Test
    void requireWithinBudgetReturnsNormalizedFilename() {
        assertThat(SourceInputValidator.requireWithinBudget(" main.cpp ", "int main(){}"))
                .isEqualTo("main.cpp");
    }

    private ResponseStatusException badRequestFrom(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        Throwable thrown = catchThrowable(callable);
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException exception = (ResponseStatusException) thrown;
        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        return exception;
    }
}
