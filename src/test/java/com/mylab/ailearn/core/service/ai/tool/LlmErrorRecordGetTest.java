package com.mylab.ailearn.core.service.ai.tool;

import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LlmErrorRecordGetTest {

    @SuppressWarnings("unchecked")
    @Test
    void modelToolIsBoundToTrustedOwnerId() {
        ObjectProvider<ErrorRecordStore> provider = mock(ObjectProvider.class);
        ErrorRecordStore store = mock(ErrorRecordStore.class);
        when(provider.getIfAvailable()).thenReturn(store);
        when(store.findByOwnerUserId(7L)).thenReturn(List.of());

        var tool = new LlmErrorRecordGet(provider).forOwner(7L);

        assertThat(tool.getMyErrorRecords()).isEmpty();
        verify(store).findByOwnerUserId(7L);
    }
}
