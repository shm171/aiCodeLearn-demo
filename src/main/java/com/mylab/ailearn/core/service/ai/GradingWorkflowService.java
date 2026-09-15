package com.mylab.ailearn.core.service.ai;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.ChapterMatchService;
import com.mylab.ailearn.core.service.StaticCheckService;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 批改智能体工作流（Spring AI Alibaba Graph）。
 *
 * <p>把「解析 → 规则静态检查 → LLM 深度批改 → 汇总」编排为一个 {@link StateGraph}，
 * 演示多节点智能体工作流的 Service 层实现。</p>
 *
 * <p><b>注意两点</b>：① 当前节点之间都是无条件边，静态检查之后<b>总会</b>执行 LLM 深度批改，
 * 「静态检查无违规就跳过 LLM」的条件边尚未实现（{@code AsyncEdgeAction} 已导入但未使用）；
 * ② 本类是独立的工作流演示，不参与 {@code GradingService} 的正式批改流程，
 * 正式流程见 {@code GradingServiceImpl}。</p>
 */
@Service
@RequiredArgsConstructor
public class GradingWorkflowService {

    /** 工作流状态键：输入的文件名、源码内容与提交者用户 ID。 */
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_OWNER = "ownerUserId";
    /** 工作流状态键：解析得到的源码对象。 */
    public static final String KEY_SOURCE_FILE = "sourceFile";
    /** 工作流状态键：规则静态检查报告。 */
    public static final String KEY_STATIC_REPORT = "staticReport";
    /** 工作流状态键：LLM 深度批改结论（已经过校验）。 */
    public static final String KEY_LLM_REVIEW = "llmReview";

    private static final String NODE_PARSE = "parse";
    private static final String NODE_STATIC_CHECK = "static_check";
    private static final String NODE_LLM_GRADE = "llm_grade";
    private static final String NODE_FINALIZE = "finalize";

    private final ChapterMatchService chapterMatchService;
    private final StaticCheckService staticCheckService;
    private final ObjectProvider<LlmGradingClient> llmGradingClientProvider;
    private final LlmReviewValidator llmReviewValidator;

    private volatile CompiledGraph compiledGraph;

    /** 工作流汇总产物：原始源码 + 规则检查结论 + 可选的 LLM 深度批改结论。 */
    public record WorkflowOutcome(
            SourceFile sourceFile,
            StaticCheckReport staticReport,
            LlmReview llmReview) {
    }

    /**
     * 以一段源码驱动整条批改工作流。
     *
     * <p>输入非法（文件名为空、后缀不支持、超出输入预算）时抛 400；
     * 工作流未产出结果时抛 {@code IllegalStateException}。</p>
     */
    public WorkflowOutcome run(String filename, String content, Long ownerUserId) {
        Map<String, Object> input = new HashMap<>();
        input.put(KEY_FILENAME, filename);
        input.put(KEY_CONTENT, content);
        input.put(KEY_OWNER, ownerUserId);

        Optional<OverAllState> state = compiled().invoke(input);
        OverAllState result = state.orElseThrow(() -> new IllegalStateException("批改工作流未产出结果"));

        return new WorkflowOutcome(
                result.value(KEY_SOURCE_FILE, SourceFile.class).orElse(null),
                result.value(KEY_STATIC_REPORT, StaticCheckReport.class).orElse(null),
                result.value(KEY_LLM_REVIEW, LlmReview.class).orElse(null));
    }

    private CompiledGraph compiled() {
        CompiledGraph graph = compiledGraph;
        if (graph == null) {
            synchronized (this) {
                graph = compiledGraph;
                if (graph == null) {
                    graph = buildGraph();
                    compiledGraph = graph;
                }
            }
        }
        return graph;
    }

    private CompiledGraph buildGraph() {
        try {
            StateGraph graph = new StateGraph("ai-grading-workflow", HashMap::new);

            graph.addEdge(StateGraph.START, NODE_PARSE);
            graph.addNode(NODE_PARSE, AsyncNodeAction.node_async(this::parse));
            graph.addNode(NODE_STATIC_CHECK, AsyncNodeAction.node_async(this::staticCheck));
            graph.addNode(NODE_LLM_GRADE, AsyncNodeAction.node_async(this::llmGrade));
            graph.addNode(NODE_FINALIZE, AsyncNodeAction.node_async(this::finalize));

            graph.addEdge(NODE_PARSE, NODE_STATIC_CHECK)
                            .addEdge(NODE_STATIC_CHECK, NODE_LLM_GRADE);
            graph.addEdge(NODE_LLM_GRADE, NODE_FINALIZE);
            graph.addEdge(NODE_FINALIZE, StateGraph.END);

            return graph.compile();
        } catch (GraphStateException e) {
            throw new IllegalStateException("批改工作流构建失败", e);
        }
    }

    private Map<String, Object> parse(OverAllState state) {
        String filename = state.value(KEY_FILENAME, "");
        String content = state.value(KEY_CONTENT, "");
        Long ownerUserId = state.value(KEY_OWNER, (Long) null);

        ParseResult parsed = chapterMatchService.parse(filename, content);
        SourceFile sourceFile = new SourceFile(
                null, ownerUserId, parsed.filename(), parsed.language(), parsed.chapter(), content, LocalDateTime.now());

        return Map.<String, Object>of(KEY_SOURCE_FILE, sourceFile);
    }

    private Map<String, Object> staticCheck(OverAllState state) {
        SourceFile sourceFile = state.value(KEY_SOURCE_FILE, SourceFile.class)
                .orElseThrow(() -> new IllegalStateException("工作流缺少 sourceFile 状态"));
        StaticCheckReport report = staticCheckService.check(sourceFile);
        return Map.<String, Object>of(KEY_STATIC_REPORT, report);
    }

    private Map<String, Object> llmGrade(OverAllState state) {
        SourceFile sourceFile = state.value(KEY_SOURCE_FILE, SourceFile.class)
                .orElseThrow(() -> new IllegalStateException("工作流缺少 sourceFile 状态"));
        StaticCheckReport report = state.value(KEY_STATIC_REPORT, StaticCheckReport.class).orElse(null);

        LlmGradingClient client = llmGradingClientProvider.getIfAvailable();
        // 模型输出不是可信证据：与 GradingServiceImpl 一样先校验再返回
        LlmReview review = client == null
                ? LlmReview.unavailable("LLM 深度批改客户端尚未接入，已跳过。")
                : llmReviewValidator.validate(client.review(sourceFile), sourceFile);

        return Map.<String, Object>of(KEY_LLM_REVIEW, review);
    }

    private Map<String, Object> finalize(OverAllState state) {
        return Map.<String, Object>of("finalized", Boolean.TRUE);
    }
}
