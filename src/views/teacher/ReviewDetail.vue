<template>
  <div v-loading="loading">
    <el-card class="page-card" v-if="detail">
      <template #header>
        <div class="detail-header">
          <div>
            <el-button link @click="$router.back()">
              <el-icon><ArrowLeft /></el-icon>返回
            </el-button>
            <span class="title">{{ detail.questionTitle }}</span>
            <el-tag :type="statusTag(detail.status)" size="small" class="status-tag">
              {{ statusLabel(detail.status) }}
            </el-tag>
          </div>
          <el-descriptions :column="4" size="small" class="sub-meta">
            <el-descriptions-item label="学生">{{ detail.studentName }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ detail.className }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.submittedAt }}</el-descriptions-item>
            <el-descriptions-item label="语言">{{ detail.language }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>

      <el-row :gutter="16">
        <!-- 左列：题目 + 代码 -->
        <el-col :xs="24" :lg="13">
          <el-card shadow="never" class="block-card">
            <template #header><span class="block-title">题目描述</span></template>
            <p class="desc">{{ detail.questionDescription }}</p>
          </el-card>

          <el-card shadow="never" class="block-card">
            <template #header>
              <span class="block-title">学生代码（{{ detail.language }}）</span>
            </template>
            <pre class="code-block mono">{{ detail.code }}</pre>
          </el-card>
        </el-col>

        <!-- 右列：AI 自动批改 -->
        <el-col :xs="24" :lg="11">
          <el-card shadow="never" class="block-card">
            <template #header>
              <span class="block-title">AI 自动批改</span>
              <el-tag type="success" size="small" class="auto-tag">规则校验 + LLM 深度批改</el-tag>
            </template>

            <div class="grade-controls">
              <el-form label-width="140px">
                <el-form-item label="启用 LLM 深度批改">
                  <el-switch v-model="enableLLM" />
                  <div class="form-tip">关闭时只跑规则校验（更快）；开启后追加 LLM 逻辑/算法点评</div>
                </el-form-item>
              </el-form>
              <el-button type="primary" :icon="MagicStick" :loading="grading" @click="onGrade">
                开始批改
              </el-button>
            </div>

            <el-divider />

            <div v-if="result">
              <div class="llm-score">
                <span class="score-num">{{ result.score }}</span>
                <span class="score-max">/ 100</span>
                <span class="score-tip">本次批改得分</span>
              </div>

              <el-alert
                v-if="result.overallFeedback"
                type="info"
                :closable="false"
                class="summary"
              >
                <template #title>{{ result.overallFeedback }}</template>
              </el-alert>

              <div class="issue-title">发现问题（{{ result.issues.length }} 条）</div>
              <el-table :data="result.issues" size="small" border>
                <el-table-column label="级别" width="80">
                  <template #default="{ row }">
                    <el-tag :type="severityTag(row.severity)" size="small">{{ row.severity }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="来源" width="70">
                  <template #default="{ row }">
                    <el-tag :type="row.source === 'LLM' ? 'success' : 'primary'" size="small" effect="plain">
                      {{ row.source }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="category" label="分类" width="100" show-overflow-tooltip />
                <el-table-column label="行号" width="60" align="center">
                  <template #default="{ row }">
                    <span>{{ row.lineNumber ?? '--' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="message" label="说明" min-width="140" show-overflow-tooltip />
                <el-table-column prop="suggestion" label="建议" min-width="140" show-overflow-tooltip />
              </el-table>
            </div>

            <el-empty v-else description="点击「开始批改」查看自动批改结果" :image-size="70" />
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { getReviewDetail, gradeSubmission } from "@/api/teacher";
const route = useRoute();
const id = Number(route.params.id);
const loading = ref(false);
const grading = ref(false);
const detail = ref(null);
const enableLLM = ref(false);
const result = ref(null);
onMounted(async () => {
  loading.value = true;
  try {
    detail.value = await getReviewDetail(id);
  } finally {
    loading.value = false;
  }
});
async function onGrade() {
  grading.value = true;
  try {
    result.value = await gradeSubmission(id, enableLLM.value);
    ElMessage.success("\u6279\u6539\u5B8C\u6210");
  } finally {
    grading.value = false;
  }
}
const statusMap = {
  PENDING: { label: "\u5F85\u6279\u6539", tag: "warning" },
  GRADED: { label: "\u5DF2\u6279\u6539", tag: "success" },
  REJECTED: { label: "\u9700\u590D\u6838", tag: "danger" }
};
function statusLabel(s) {
  return statusMap[s]?.label ?? s;
}
function statusTag(s) {
  return statusMap[s]?.tag ?? "info";
}
const severityMap = {
  ERROR: "danger",
  WARNING: "warning",
  INFO: "info"
};
function severityTag(s) {
  return severityMap[s] ?? "info";
}

</script>

<style scoped lang="scss">
.detail-header {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 10px;
  }

  .sub-meta {
    margin-top: 4px;
  }
}

.block-card {
  margin-bottom: 16px;
}

.block-title {
  font-weight: 600;
}

.auto-tag {
  margin-left: 8px;
}

.desc {
  color: #606266;
  line-height: 1.7;
  margin: 0;
}

.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 14px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  margin: 0;
  max-height: 420px;
  overflow-y: auto;
}

.grade-controls {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.llm-score {
  margin-bottom: 12px;

  .score-num {
    font-size: 40px;
    font-weight: 700;
    color: #409eff;
  }

  .score-max {
    color: #909399;
    font-size: 16px;
  }

  .score-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 13px;
  }
}

.summary {
  margin-bottom: 12px;
}

.issue-title {
  font-weight: 600;
  margin-bottom: 8px;
}
</style>