package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.dto.sendback.ErrorRecordDto;
import com.mylab.ailearn.core.dto.sendback.ReviewPackageDto;
import com.mylab.ailearn.core.dto.sendback.WeakTopicDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 错题归档与复习的服务契约。
 *
 *
 * 落地时需先与负责人协调归档表、关联表与 Flyway 版本。
 */
@Service
public class ReviewService {

    /**
     * 分页查询当前学生的错题列表。
     *
     * @param ownerUserId 当前登录学生 ID
     * @param category    错误分类过滤，为 null 则不过滤
     * @param severity    严重程度过滤，为 null 则不过滤
     * @param pageable    分页参数
     */
    public Page<ErrorRecordDto> listMyErrors(Long ownerUserId, String category, String severity, Pageable pageable) {

        return Page.empty();
    }

    /**
     * 查询单条错题详情。资源归属校验（错题是否属于当前学生）由队友在实现内完成。
     */
    public ErrorRecordDto getMyError(Long ownerUserId, Long errorId) {

        return null;
    }

    /** 生成当前学生的专属复习包：刷题清单 + 薄弱知识点 + 图表。 */
    public ReviewPackageDto buildMyReviewPackage(Long ownerUserId) {

        return null;
    }

    /** 查询当前学生的薄弱知识点，按扣分权重降序。 */
    public List<WeakTopicDto> listMyWeakTopics(Long ownerUserId) {

        return List.of();
    }
}
