package com.mylab.ailearn.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ErrorSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** error_record 表的数据访问接口。 */
public interface ErrorRecordJpaRepository extends JpaRepository<ErrorRecordEntity, Long> {

    List<ErrorRecordEntity> findByOwnerUserIdOrderByCreatedAtDescIdDesc(Long ownerUserId);

    Page<ErrorRecordEntity> findByOwnerUserIdOrderByCreatedAtDescIdDesc(Long ownerUserId, Pageable pageable);

    Page<ErrorRecordEntity> findByOwnerUserIdAndCategoryOrderByCreatedAtDescIdDesc(
            Long ownerUserId, ErrorCategory category, Pageable pageable);

    Page<ErrorRecordEntity> findByOwnerUserIdAndSeverityOrderByCreatedAtDescIdDesc(
            Long ownerUserId, ErrorSeverity severity, Pageable pageable);

    Page<ErrorRecordEntity> findByOwnerUserIdAndCategoryAndSeverityOrderByCreatedAtDescIdDesc(
            Long ownerUserId, ErrorCategory category, ErrorSeverity severity, Pageable pageable);

}
