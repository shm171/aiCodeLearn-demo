package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ErrorSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 错题归档：查询某学生已归档的全部错题，并支持把错题标记为「已掌握」。
 *
 * <p>供学生复习与业务统计读取使用。</p>
 *
 * <p><b>权限说明</b>：{@link #listByOwner} 按传入的 {@code ownerUserId} 直接取数，
 * 不校验该 ID 与当前登录用户是否一致；{@link #markMastered} 会校验错题确实属于该
 * {@code ownerUserId}（不属于时按 404 处理）。因此调用方必须传入已认证用户的 ID。</p>
 */
public interface ErrorArchiveService {

    /**
     * 查询某学生的全部错题。
     *
     * @param ownerUserId 学生用户 ID
     * @return 该学生的错题列表（无错题时返回空列表，不会为 null）
     * @throws org.springframework.web.server.ResponseStatusException ownerUserId 无效时抛 400
     */
    List<ErrorRecord> listByOwner(Long ownerUserId);

    /** 在数据库中分页并按可选分类、严重程度过滤错题。 */
    Page<ErrorRecord> listByOwner(
            Long ownerUserId,
            ErrorCategory category,
            ErrorSeverity severity,
            Pageable pageable);

    /**
     * 把某学生的一条错题标记为「已掌握」。
     *
     * @param ownerUserId 学生用户 ID
     * @param errorId     错题记录 ID
     * @return 更新后的错题记录
     * @throws org.springframework.web.server.ResponseStatusException 错题不存在或不属于该学生（404）、参数无效（400）时抛出
     */
    ErrorRecord markMastered(Long ownerUserId, Long errorId);
}
