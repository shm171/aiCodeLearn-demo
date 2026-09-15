package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.service.spi.SourceFileStore;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * 输入错误与基础设施故障必须分开返回（S08 验收）：
 * 前者是调用方能修的 400，后者不是输入问题，不能被当成 400 混在一起。
 */
class FileUploadServiceImplTest {

    private final ChapterMatchServiceImpl chapterMatchService = new ChapterMatchServiceImpl();

    @Test
    void invalidInputIsReportedAsBadRequest() {
        FileUploadServiceImpl service = serviceWithoutStore();

        assertThatThrownBy(() -> service.receive(1L, "Main Bad.java", "public class Main { }"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void missingOwnerIsReportedAsBadRequest() {
        FileUploadServiceImpl service = serviceWithoutStore();

        assertThatThrownBy(() -> service.receive(null, "main.cpp", "int main() { return 0; }"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(400));
    }

    private FileUploadServiceImpl serviceWithoutStore() {
        return new FileUploadServiceImpl(mock(SourceFileStore.class), chapterMatchService);
    }
}
