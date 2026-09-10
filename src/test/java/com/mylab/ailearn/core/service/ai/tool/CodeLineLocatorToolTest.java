package com.mylab.ailearn.core.service.ai.tool;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 行号定位工具的行为约束：只在构造时绑定的服务端源码中定位，模型无法替换被审查的文件。
 */
class CodeLineLocatorToolTest {

    private static final String SOURCE = "int a = 1;\nint b = 2;\nint a = 3;\n";

    @Test
    void locatesAllOccurrencesInBoundSource() {
        CodeLineLocatorTool tool = new CodeLineLocatorTool(SOURCE);

        assertThat(tool.locate("int a")).containsExactly(1, 3);
    }

    @Test
    void returnsEmptyWhenSnippetAbsent() {
        CodeLineLocatorTool tool = new CodeLineLocatorTool(SOURCE);

        assertThat(tool.locate("never appears")).isEmpty();
    }

    @Test
    void returnsEmptyForBlankSnippet() {
        CodeLineLocatorTool tool = new CodeLineLocatorTool(SOURCE);

        assertThat(tool.locate(null)).isEmpty();
        assertThat(tool.locate("   ")).isEmpty();
    }

    @Test
    void cannotLocateInSourceSuppliedByCaller() {
        // 模型只能提交片段：即便片段来自另一份源码，也定位不到本次权威源码里
        CodeLineLocatorTool tool = new CodeLineLocatorTool(SOURCE);

        assertThat(tool.locate("int c = 99;  // 另一份源码")).isEmpty();
    }

    @Test
    void returnsEmptyWhenNoSourceBound() {
        CodeLineLocatorTool tool = new CodeLineLocatorTool(null);

        assertThat(tool.locate("int a")).isEmpty();
    }

    @Test
    void capsReturnedLineCount() {
        CodeLineLocatorTool tool = new CodeLineLocatorTool("int x;\n".repeat(CodeLineLocatorTool.MAX_RESULTS + 50));

        assertThat(tool.locate("int x;")).hasSize(CodeLineLocatorTool.MAX_RESULTS);
    }

    @Test
    void rejectsOversizedSnippet() {
        CodeLineLocatorTool tool = new CodeLineLocatorTool(SOURCE);

        assertThat(tool.locate("x".repeat(CodeLineLocatorTool.MAX_SNIPPET_LENGTH + 1))).isEmpty();
    }

    @Test
    void stopsAnsweringAfterCallBudgetExhausted() {
        CodeLineLocatorTool tool = new CodeLineLocatorTool(SOURCE);

        for (int i = 0; i < CodeLineLocatorTool.MAX_CALLS; i++) {
            assertThat(tool.locate("int a")).isNotEmpty();
        }

        assertThat(tool.locate("int a")).isEmpty();
    }
}
