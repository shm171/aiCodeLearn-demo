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
        <!-- 左列：题目 + 代码 + 静态检查 -->
        <el-col :xs="24" :lg="14">
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

          <el-card shadow="never" class="block-card">
            <template #header>
              <span class="block-title">静态代码检查</span>
              <el-tag v-if="detail.staticIssues.length === 0" type="success" size="small">
                未发现问题
              </el-tag>
            </template>
            <el-empty v-if="detail.staticIssues.length === 0" description="静态检查全部通过" :image-size="60" />
            <el-table v-else :data="detail.staticIssues" size="small" border>
              <el-table-column label="级别" width="80">
                <template #default="{ row }">
                  <el-tag :type="severityTag(row.severity)" size="small">{{ row.severity }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="rule" label="规则" width="150" show-overflow-tooltip />
              <el-table-column label="位置" width="110">
                <template #default="{ row }">
                  <span class="mono">{{ row.file }}:{{ row.line }}:{{ row.column }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="说明" min-width="180" show-overflow-tooltip />
            </el-table>
          </el-card>
        </el-col>

        <!-- 右列：LLM 批改 + 人工复核 -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="block-card" v-if="detail.llmGrading">
            <template #header>
              <span class="block-title">LLM 深度批改</span>
            </template>
            <div class="llm-score">
              <span class="score-num">{{ detail.llmGrading.totalScore }}</span>
              <span class="score-max">/ {{ detail.llmGrading.maxScore }}</span>
            </div>
            <el-alert type="info" :closable="false" class="summary">
              <template #title>{{ detail.llmGrading.summary }}</template>
            </el-alert>
            <div v-for="item in detail.llmGrading.items" :key="item.dimension" class="grading-item">
              <div class="grading-head">
                <span>{{ item.dimension }}</span>
                <span>{{ item.score }} / {{ item.maxScore }}</span>
              </div>
              <el-progress
                :percentage="Math.round((item.score / item.maxScore) * 100)"
                :stroke-width="8"
                :color="progressColor(item.score / item.maxScore)"
              />
              <div class="grading-comment">{{ item.comment }}</div>
            </div>
          </el-card>

          <el-card shadow="never" class="block-card">
            <template #header><span class="block-title">人工复核</span></template>
            <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="70px">
              <el-form-item label="评分" prop="score">
                <el-input-number v-model="reviewForm.score" :min="0" :max="100" />
              </el-form-item>
              <el-form-item label="结论" prop="status">
                <el-radio-group v-model="reviewForm.status">
                  <el-radio value="GRADED">通过</el-radio>
                  <el-radio value="REJECTED">打回重做</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="评语" prop="comment">
                <el-input
                  v-model="reviewForm.comment"
                  type="textarea"
                  :rows="4"
                  placeholder="填写给学生的评语（可选）"
                />
              </el-form-item>
              <el-button type="primary" :loading="submitting" @click="onSubmitReview">
                提交复核结果
              </el-button>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getReviewDetail, submitReview } from '@/api/teacher'
import type { ReviewDetail, ReviewPayload } from '@/api/schema'

const route = useRoute()
const id = Number(route.params.id)

const loading = ref(false)
const submitting = ref(false)
const detail = ref<ReviewDetail | null>(null)
const reviewFormRef = ref<FormInstance>()

const reviewForm = reactive<ReviewPayload>({
  score: 85,
  status: 'GRADED',
  comment: '',
})

const reviewRules: FormRules = {
  score: [{ required: true, message: '请输入评分', trigger: 'blur' }],
  status: [{ required: true, message: '请选择结论', trigger: 'change' }],
}

onMounted(async () => {
  loading.value = true
  try {
    detail.value = await getReviewDetail(id)
    if (detail.value.manualScore !== null) reviewForm.score = detail.value.manualScore
    if (detail.value.manualComment) reviewForm.comment = detail.value.manualComment
    reviewForm.status = detail.value.status === 'REJECTED' ? 'REJECTED' : 'GRADED'
  } finally {
    loading.value = false
  }
})

async function onSubmitReview() {
  if (!reviewFormRef.value) return
  const valid = await reviewFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const res = await submitReview(id, { ...reviewForm })
    detail.value = res
    ElMessage.success('复核结果已提交')
  } finally {
    submitting.value = false
  }
}

const statusMap: Record<string, { label: string; tag: 'primary' | 'success' | 'danger' | 'warning' | 'info' }> = {
  PENDING: { label: '待批改', tag: 'warning' },
  GRADED: { label: '已批改', tag: 'success' },
  REJECTED: { label: '需复核', tag: 'danger' },
}
function statusLabel(s: string) {
  return statusMap[s]?.label ?? s
}
function statusTag(s: string) {
  return statusMap[s]?.tag ?? 'info'
}

const severityMap: Record<string, 'danger' | 'warning' | 'info'> = {
  ERROR: 'danger',
  WARNING: 'warning',
  INFO: 'info',
}
function severityTag(s: string) {
  return severityMap[s] ?? 'info'
}

function progressColor(ratio: number) {
  if (ratio >= 0.8) return '#67c23a'
  if (ratio >= 0.6) return '#e6a23c'
  return '#f56c6c'
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
}

.summary {
  margin-bottom: 14px;
}

.grading-item {
  margin-bottom: 14px;

  .grading-head {
    display: flex;
    justify-content: space-between;
    color: #303133;
    font-size: 13px;
    margin-bottom: 4px;
  }

  .grading-comment {
    color: #909399;
    font-size: 12px;
    margin-top: 4px;
  }
}
</style>