package com.mylab.ailearn.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** error_record 表的数据访问接口。 */
public interface ErrorRecordJpaRepository extends JpaRepository<ErrorRecordEntity, Long> {

    List<ErrorRecordEntity> findByOwnerUserId(Long ownerUserId);
}
