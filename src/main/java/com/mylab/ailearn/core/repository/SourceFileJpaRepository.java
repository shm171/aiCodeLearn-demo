package com.mylab.ailearn.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** source_file 表的数据访问接口。 */
public interface SourceFileJpaRepository extends JpaRepository<SourceFileEntity, Long> {

    List<SourceFileEntity> findByOwnerUserId(Long ownerUserId);
}
