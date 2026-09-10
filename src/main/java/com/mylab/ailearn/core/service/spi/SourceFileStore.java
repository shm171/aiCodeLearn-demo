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
 */
public interface SourceFileStore {

    SourceFile save(SourceFile file);

    Optional<SourceFile> findById(Long id);

    List<SourceFile> findByOwnerUserId(Long ownerUserId);

    /** 查询全量源码提交，供教师班级看板统计总提交数。 */
    List<SourceFile> findAll();
}