package com.mylab.ailearn.core.service.ai.tool;


import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import org.springframework.ai.tool.annotation.Tool;
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
 * <p><b>信任边界</b>：用户 ID 只能由服务端通过 {@link #forOwner(Long)} 绑定。
 * 暴露给模型的方法没有用户 ID 参数，因此提示词注入无法令工具读取其他用户的数据。</p>
 */
@Service
public class LlmErrorRecordGet {

    private final ObjectProvider<ErrorRecordStore> errorRecordStoreProvider;

    public LlmErrorRecordGet(ObjectProvider<ErrorRecordStore> errorRecordStoreProvider) {
        this.errorRecordStoreProvider = errorRecordStoreProvider;
    }

    /** 为一次已认证请求创建绑定当前用户的模型工具。 */
    public BoundErrorRecordTool forOwner(Long ownerUserId) {
        if (ownerUserId == null || ownerUserId <= 0) {
            throw new IllegalArgumentException("ownerUserId 无效");
        }
        ErrorRecordStore errorRecordStore = errorRecordStoreProvider.getIfAvailable();
        if (errorRecordStore == null) {
            throw new IllegalStateException("ErrorRecordStore 尚未由 Repository 负责人实现");
        }
        return new BoundErrorRecordTool(ownerUserId, errorRecordStore);
    }

    /** 仅暴露无参数查询，归属 ID 在创建实例时已经固定。 */
    public static final class BoundErrorRecordTool {
        private final Long ownerUserId;
        private final ErrorRecordStore errorRecordStore;

        private BoundErrorRecordTool(Long ownerUserId, ErrorRecordStore errorRecordStore) {
            this.ownerUserId = ownerUserId;
            this.errorRecordStore = errorRecordStore;
        }

        @Tool(name = "getMyErrorRecords", description = "读取当前登录用户自己的批改记录（错题）")
        public List<ErrorRecord> getMyErrorRecords() {
            List<ErrorRecord> records = errorRecordStore.findByOwnerUserId(ownerUserId);
            return records == null ? List.of() : records;
        }
    }
}
