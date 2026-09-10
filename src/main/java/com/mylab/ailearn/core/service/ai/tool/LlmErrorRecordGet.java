package com.mylab.ailearn.core.service.ai.tool;


import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 错题查询 {@link Tool}。
 *
 * <p>暴露给 AI 助手调用：按用户 ID 从数据库调取该用户的批改记录（错题），
 * 供助手回答「我最近错在哪、薄弱点是什么」之类的问题。持久化通过
 * {@link ErrorRecordStore} 端口完成；该端口未实现时抛出明确错误而非空指针。</p>
 */
@Service
public class LlmErrorRecordGet {

    private final ObjectProvider<ErrorRecordStore> errorRecordStoreProvider;

    public LlmErrorRecordGet(ObjectProvider<ErrorRecordStore> errorRecordStoreProvider) {
        this.errorRecordStoreProvider = errorRecordStoreProvider;
    }

    /**
     * 按用户 ID 查询其全部批改记录（错题）。
     *
     * @param ownerUserId 用户 ID
     * @return 该用户的错题列表（无错题时返回空列表，不会为 null）
     */
    @Tool(name = "getErrorRecords", description = "根据用户ID从数据库调取该用户的批改记录（错题），返回其全部错题列表")
    public List<ErrorRecord> getErrorRecords(@ToolParam(required = true, description = "用户ID") Long ownerUserId) {
        ErrorRecordStore errorRecordStore = errorRecordStoreProvider.getIfAvailable();
        if (errorRecordStore == null) {
            throw new IllegalStateException("ErrorRecordStore 尚未由 Repository 负责人实现");
        }
        List<ErrorRecord> records = errorRecordStore.findByOwnerUserId(ownerUserId);
        return records == null ? List.of() : records;
    }
}
