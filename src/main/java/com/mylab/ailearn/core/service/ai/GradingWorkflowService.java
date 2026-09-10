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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 批改智能体工作流（Spring AI Alibaba Graph）。
 *
 * <p>把「解析 → 静态检查 → (有错则) LLM 深度批改 → 汇总」编排为一个
 * {@link StateGraph}，演示多节点智能体工作流的 Service 层实现。条件边会在静态检查
 * 无违规时跳过 LLM，直接进入汇总节点。</p>
 */
@Service
@RequiredArgsConstructor
public class GradingWorkflowService {

    public static final String KEY_FILENAME = "filename";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_OWNER = "ownerUserId";
    public static final String KEY_SOURCE_FILE = "sourceFile";
    public static final String KEY_STATIC_REPORT = "staticReport";
    public static final String KEY_LLM_REVIEW = "llmReview";

    private static final String NODE_PARSE = "parse";
    private static final String NODE_STATIC_CHECK = "static_check";
    private static final String NODE_LLM_GRADE = "llm_grade";
    private static final String NODE_FINALIZE = "finalize";

    private final ChapterMatchService chapterMatchService;
    private final StaticCheckService staticCheckService;
    private final ObjectProvider<LlmGradingClient> llmGradingClientProvider;

    private volatile CompiledGraph compiledGraph;

    /** 工作流汇总产物：原始源码 + 规则检查结论 + 可选的 LLM 深度批改结论。 */
    public record WorkflowOutcome(
            SourceFile sourceFile,
            StaticCheckReport staticReport,
            LlmReview llmReview) {
    }

    /**
     * 以一段源码驱动整条批改工作流。
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
        LlmReview review = client == null
                ? new LlmReview(List.of(), "LLM 深度批改客户端尚未接入，已跳过。")
                : client.review(sourceFile);

        return Map.<String, Object>of(KEY_LLM_REVIEW, review);
    }

    private Map<String, Object> finalize(OverAllState state) {
        return Map.<String, Object>of("finalized", Boolean.TRUE);
    }
}
