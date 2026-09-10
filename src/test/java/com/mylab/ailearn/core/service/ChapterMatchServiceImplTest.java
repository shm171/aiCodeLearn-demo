package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChapterMatchServiceImplTest {

    private final ChapterMatchServiceImpl service = new ChapterMatchServiceImpl();

    @Test
    void parsesCppFilename() {
        ParseResult parsed = service.parse("main.cpp", "#include <iostream>\nint main() { return 0; }");
        assertThat(parsed.language()).isEqualTo(ProgrammingLanguage.CPP);
    }

    @Test
    void parsesJavaFilename() {
        ParseResult parsed = service.parse("Main.java", "public class Main { }");
        assertThat(parsed.language()).isEqualTo(ProgrammingLanguage.JAVA);
    }

    @Test
    void rejectsUnsupportedExtension() {
        assertThatThrownBy(() -> service.parse("notes.txt", "int x;"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void returnsNormalizedFilename() {
        ParseResult parsed = service.parse("  main.cpp  ", "#include <iostream>\nint main() { return 0; }");

        assertThat(parsed.filename()).isEqualTo("main.cpp");
    }

    @Test
    void rejectsFilenameWithWhitespace() {
        badRequest(() -> service.parse("Main Bad.java", "public class Main { }"));
    }

    @Test
    void rejectsFilenameWithPathSeparator() {
        badRequest(() -> service.parse("../../etc/passwd.cpp", "int main() { return 0; }"));
        badRequest(() -> service.parse("sub/main.cpp", "int main() { return 0; }"));
    }

    @Test
    void rejectsOversizedContent() {
        String oversized = "x".repeat(SourceInputValidator.MAX_SOURCE_BYTES + 1);

        badRequest(() -> service.parse("main.cpp", oversized));
    }

    @Test
    void rejectsContentOverLineBudget() {
        String tooManyLines = "x\n".repeat(SourceInputValidator.MAX_SOURCE_LINES);

        badRequest(() -> service.parse("main.cpp", tooManyLines));
    }

    private void badRequest(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(400));
    }
}
