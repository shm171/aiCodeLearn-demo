package com.mylab.ailearn.core.service.ai.tool;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 解析工具的行为约束：只返回构造时绑定的服务端源码的解析结果。
 */
class SourceFileParseToolTest {

    @Test
    void returnsParseResultOfTheBoundSource() {
        ParseResult result = new SourceFileParseTool(source()).parse();

        assertThat(result.filename()).isEqualTo("main.cpp");
        assertThat(result.language()).isEqualTo(ProgrammingLanguage.CPP);
        assertThat(result.chapter()).isEqualTo(CourseChapter.COMPREHENSIVE);
    }

    @Test
    void rejectsMissingSource() {
        assertThatThrownBy(() -> new SourceFileParseTool(null))
                .isInstanceOf(NullPointerException.class);
    }

    private SourceFile source() {
        return new SourceFile(null, 1L, "main.cpp", ProgrammingLanguage.CPP, CourseChapter.COMPREHENSIVE,
                "int main() {\n    return 0;\n}\n", LocalDateTime.now());
    }
}
