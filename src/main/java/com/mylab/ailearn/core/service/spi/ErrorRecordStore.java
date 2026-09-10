package com.mylab.ailearn.core.service.spi;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;

import java.util.List;
import java.util.Optional;

/**
 * 错题归档持久化端口（SPI）。Repository 负责人实现后，错题归档与复习功能即可落库。
 *
 * <p>这是 Service 层对外暴露的最小持久化契约，Service 层不直接依赖任何
 * {@code JpaRepository} 或 JPA 实体。</p>
 *
 * <p>除 {@link #markMastered} 外，其余方法都不做权限判断：调用方传哪个
 * {@code ownerUserId} 就查谁的数据，身份是否可信由更上层负责。</p>
 */
public interface ErrorRecordStore {

    /**
     * 保存一条错题。
     *
     * @param record 待保存的错题；{@code id} 为 null 表示新增
     * @return 保存后的错题，{@code id} 已回填
     */
    ErrorRecord save(ErrorRecord record);

    /**
     * 批量保存一次批改产生的全部错题。
     *
     * @param records 待保存的错题列表
     * @return 保存后的错题列表，{@code id} 已回填
     */
    List<ErrorRecord> saveAll(List<ErrorRecord> records);

    /** 查询某学生的全部错题；无错题时返回空列表。 */
    List<ErrorRecord> findByOwnerUserId(Long ownerUserId);

    /** 查询全量错题，供教师班级看板聚合使用。 */
    List<ErrorRecord> findAll();

    /** 按错题 ID 查询；不存在时返回 {@link Optional#empty()}。 */
    Optional<ErrorRecord> findById(Long id);

    /** 把指定错题的「已掌握」标记更新为 {@code mastered}，返回更新后的记录。 */
    ErrorRecord markMastered(Long errorId, boolean mastered);
}
