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
}
