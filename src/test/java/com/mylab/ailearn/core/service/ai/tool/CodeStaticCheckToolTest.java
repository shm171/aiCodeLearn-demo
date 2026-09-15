package com.mylab.ailearn.core.service.ai.tool;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.enums.RuleType;
import com.mylab.ailearn.core.model.commonmodel.RuleViolation;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.StaticCheckService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 静态检查工具的行为约束：只检查构造时绑定的服务端源码，模型无法替换被审查的文件。
 */
class CodeStaticCheckToolTest {

    @Test
    void checksTheSourceBoundAtConstruction() {
        StaticCheckReport expected = new StaticCheckReport(List.of(
                new RuleViolation(RuleType.INDENTATION, ErrorCategory.FORMAT_ERROR, "缩进不一致", 2, "调整缩进")));
        AtomicReference<SourceFile> received = new AtomicReference<>();
        StaticCheckService staticCheckService = file -> {
            received.set(file);
            return expected;
        };

        SourceFile bound = source();
        CodeStaticCheckTool tool = new CodeStaticCheckTool(staticCheckService, bound);

        assertThat(tool.check()).isSameAs(expected);
        assertThat(received.get()).isSameAs(bound);
    }

    @Test
    void rejectsMissingSource() {
        StaticCheckService staticCheckService = file -> new StaticCheckReport(List.of());

        assertThatThrownBy(() -> new CodeStaticCheckTool(staticCheckService, null))
                .isInstanceOf(NullPointerException.class);
    }

    private SourceFile source() {
        return new SourceFile(null, 1L, "main.cpp", ProgrammingLanguage.CPP, CourseChapter.COMPREHENSIVE,
                "int main() {\n    return 0;\n}\n", LocalDateTime.now());
    }
}
