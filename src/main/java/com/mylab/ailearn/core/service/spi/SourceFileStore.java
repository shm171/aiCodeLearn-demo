package com.mylab.ailearn.core.service.spi;


import com.mylab.ailearn.core.model.commonmodel.SourceFile;

import java.util.List;
import java.util.Optional;

/**
 * 源码文件持久化端口（SPI）。
 *
 * <p>这是 Service 层对外暴露的最小持久化契约，供 Repository 负责人实现：
 * 可以基于 Spring Data JPA 编写 {@code @Repository} 实现类适配本接口，
 * Service 层不直接依赖任何 {@code JpaRepository} 或 JPA 实体。</p>
 *
 * <p>所有方法都不做权限判断：调用方传哪个 {@code ownerUserId} 就查谁的数据，
 * 身份是否可信由更上层负责。</p>
 */
public interface SourceFileStore {

    /**
     * 保存一份源码提交。
     *
     * @param file 待保存的源码；{@code id} 为 null 表示新增
     * @return 保存后的源码，{@code id} 已回填
     */
    SourceFile save(SourceFile file);

    /** 按提交 ID 查询；不存在时返回 {@link Optional#empty()}。 */
    Optional<SourceFile> findById(Long id);

    /** 查询某学生的全部提交；无提交时返回空列表。 */
    List<SourceFile> findByOwnerUserId(Long ownerUserId);

    /** 查询全量源码提交，供教师班级看板统计总提交数。 */
    List<SourceFile> findAll();
}
