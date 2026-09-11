package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;
import com.mylab.ailearn.core.model.specialmodel.WeakPoint;
import com.mylab.ailearn.core.service.AiLearnOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 错题归档与复习入口。
 *
 * <p>门面提供的是「全量错题列表 + 学生看板」两个原子能力；
 * 分页、按分类过滤、单条查询都在本层内存中完成适配。</p>
 */
@RestController
@RequestMapping("/core/review")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 错题归档与复习",
        description = "学生错题列表、错题详情、标记已掌握、专属刷题清单、薄弱知识点报告与学习图表")
public class ReviewController {

    private final CurrentUserResolver currentUserResolver;

    private final AiLearnOrchestrator orchestrator;

    @GetMapping("/errors")
    @Operation(summary = "分页查询我的错题列表",
            description = "需要 JWT。支持按错误分类过滤（FORMAT_ERROR / SYNTAX_ERROR / LOGIC_ERROR）；"
                    + "默认每页 20 条，按归档时间倒序。只返回当前登录学生自己的错题。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<Page<ErrorRecord>> listMyErrors(
            @Parameter(description = "错误分类过滤，例如 SYNTAX_ERROR；不传则不过滤")
            @RequestParam(required = false) String category,
            @Parameter(description = "严重程度过滤（预留参数）：当前版本归档记录未包含严重程度字段，该参数暂不生效")
            @RequestParam(required = false) String severity,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long ownerUserId = currentUserResolver.currentUserId();

        var stream = orchestrator.listErrorArchive(ownerUserId).stream();
        if (category != null && !category.isBlank()) {
            ErrorCategory parsed = parseCategory(category);
            stream = stream.filter(record -> record.category() == parsed);
        }
        // severity 参数当前版本无法生效（ErrorRecord 无该字段），仅做兼容保留。

        List<ErrorRecord> filtered = stream
                .sorted(Comparator.comparing(ErrorRecord::createdAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        int start = (int) Math.min(pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return ResponseEntity.ok(new PageImpl<>(filtered.subList(start, end), pageable, filtered.size()));
    }

    @GetMapping("/errors/{errorId}")
    @Operation(summary = "查询单条错题详情",
            description = "需要 JWT。错题不存在或不属于当前学生时返回 404。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<ErrorRecord> getMyError(@PathVariable long errorId) {
        Long ownerUserId = currentUserResolver.currentUserId();
        ErrorRecord record = orchestrator.listErrorArchive(ownerUserId).stream()
                .filter(error -> error.id() != null && error.id() == errorId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "error record not found"));
        return ResponseEntity.ok(record);
    }

    @PostMapping("/errors/{errorId}/mastered")
    @Operation(summary = "把一条错题标记为「已掌握」",
            description = "需要 JWT。标记后该错题不再作为薄弱点重点推荐。错题不存在或不属于当前学生时返回 404。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<ErrorRecord> markMyErrorMastered(@PathVariable long errorId) {
        Long ownerUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(orchestrator.markErrorMastered(ownerUserId, errorId));
    }

    @GetMapping("/package")
    @Operation(summary = "生成我的专属复习包",
            description = "需要 JWT。一次性返回错题分布饼图、薄弱知识点排行、刷题清单、月度学习曲线、"
                    + "总错题数、总提交数、正确率与一句话诊断，供复习页直接渲染。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<StudentDashboard> getMyReviewPackage() {
        Long ownerUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(orchestrator.studentDashboard(ownerUserId));
    }

    @GetMapping("/weak-topics")
    @Operation(summary = "查询我的薄弱知识点",
            description = "需要 JWT。按扣分权重降序返回（最多 10 条），用于薄弱知识点报告独立加载。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<List<WeakPoint>> getMyWeakTopics() {
        Long ownerUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(orchestrator.studentDashboard(ownerUserId).topWeakPoints());
    }

    private ErrorCategory parseCategory(String category) {
        try {
            return ErrorCategory.valueOf(category.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "unknown category: " + category + ", expected FORMAT_ERROR / SYNTAX_ERROR / LOGIC_ERROR");
        }
    }
}
