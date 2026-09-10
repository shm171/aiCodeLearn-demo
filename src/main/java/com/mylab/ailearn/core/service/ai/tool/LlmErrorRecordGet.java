package com.mylab.ailearn.core.service.ai.tool;


import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 错题查询工具，暴露给 AI 助手调用。
 *
 * <p>按用户 ID 从数据库调取该用户的批改记录（错题），供助手回答「我最近错在哪、薄弱点是什么」
 * 之类的问题。持久化通过 {@link ErrorRecordStore} 端口完成；该端口未实现时抛出明确错误
 * 而非返回空数据，避免把「没接数据库」误当成「这个学生没有错题」。</p>
 *
 * <p><b>信任边界（重要）</b>：{@code ownerUserId} 来自<b>模型生成的工具调用参数</b>，
 * 不是服务端身份。也就是说模型（或诱导模型的源码内容）可以要求查询任意用户的数据，
 * 本类<b>不会</b>校验调用者是否有权读这个 ID。真正修复需要在工具侧改用服务端可信身份
 * 覆盖该参数（当前尚未实现）。</p>
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
     * @param ownerUserId 用户 ID，<b>由模型给出</b>，不可信（见类注释）
     * @return 该用户的错题列表（无错题时返回空列表，不会为 null）
     * @throws IllegalStateException 持久化端口尚未实现时抛出
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
