package com.mylab.ailearn.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** source_file 表的数据访问接口。 */
public interface SourceFileJpaRepository extends JpaRepository<SourceFileEntity, Long> {

    List<SourceFileEntity> findByOwnerUserIdOrderBySubmittedAtDescIdDesc(Long ownerUserId);

    Page<SourceFileEntity> findByOwnerUserIdOrderBySubmittedAtDescIdDesc(Long ownerUserId, Pageable pageable);

}
