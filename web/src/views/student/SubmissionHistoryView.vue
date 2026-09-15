<!-- 提交历史页：查看自己的源码提交记录、批改产生的错题和源码原文 -->
<template>
  <div class="history-page" v-loading="loading" element-loading-text="正在加载提交历史..." element-loading-background="rgba(10, 10, 12, 0.85)">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">提交历史</h1>
        <p class="page-desc">每次提交的源码与批改产生的错题都归档在这里</p>
      </div>
      <div class="header-stats">
        <div class="mini-stat">
          <span class="mini-num">{{ totalSubmissions }}</span>
          <span class="mini-label">总提交</span>
        </div>
        <div class="mini-stat">
          <span class="mini-num">{{ cleanCount }}</span>
          <span class="mini-label">无错题提交</span>
        </div>
        <div class="mini-stat">
          <span class="mini-num">{{ totalErrorCount }}</span>
          <span class="mini-label">累计错题</span>
        </div>
      </div>
    </div>

    <!-- 提交列表 -->
    <el-card class="glass-card" shadow="never">
      <el-table :data="submissions" style="width: 100%" v-loading="loading" :empty-text="loading ? '' : '还没有提交记录，去提交一份作业吧！'">
        <el-table-column prop="id" label="#" width="60" />
        <el-table-column label="文件名" min-width="180">
          <template #default="{ row }">
            <div class="file-cell">
              <el-icon :size="15"><Document /></el-icon>
              <span class="file-name">{{ row.filename }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="语言" width="90">
          <template #default="{ row }">
            <span class="lang-tag">{{ languageText(row.language) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="章节" min-width="150">
          <template #default="{ row }">
            <span class="chapter-tag">{{ row.chapterText || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="错题数" width="90">
          <template #default="{ row }">
            <span class="error-count" :class="row.errorCount > 0 ? 'has-error' : ''">{{ row.errorCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.errorCount > 0" type="warning" size="small" effect="dark">有错题</el-tag>
            <el-tag v-else type="success" size="small" effect="dark">无错题</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.submittedAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-bar" v-if="totalSubmissions > 0">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="totalSubmissions"
          layout="prev, pager, next, total"
          background
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 提交详情弹窗 -->
    <el-dialog v-model="detailVisible" title="提交详情" width="760px" top="6vh" :z-index="3000" class="detail-dialog">
      <div v-if="currentSubmission" class="detail-content" v-loading="detailLoading">
        <!-- 基本信息 -->
        <div class="detail-header">
          <div class="detail-info">
            <span class="detail-filename">{{ currentSubmission.filename }}</span>
            <span class="lang-tag">{{ languageText(currentSubmission.language) }}</span>
            <span class="chapter-tag">{{ currentSubmission.chapterText || '-' }}</span>
          </div>
          <span class="detail-time">{{ formatTime(currentSubmission.submittedAt) }}</span>
        </div>

        <!-- 本次提交的错题 -->
        <div class="detail-section" v-if="currentErrors.length > 0">
          <div class="detail-label">📝 本次批改发现的错题（{{ currentErrors.length }} 个）</div>
          <div class="detail-errors">
            <div class="detail-error-item" v-for="(err, idx) in currentErrors" :key="idx">
              <el-tag :type="categoryMeta(err.category).tagType" size="small" effect="dark">
                {{ categoryMeta(err.category).label }}
              </el-tag>
              <div class="error-body">
                <div class="error-type">
                  {{ err.errorType || '代码问题' }}
                  <span v-if="err.line && err.line.length" class="error-line">第 {{ err.line.join('、') }} 行</span>
                </div>
                <div class="error-suggestion" v-if="err.fixSuggestion">💡 {{ err.fixSuggestion }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="detail-section no-errors-box" v-else>
          <el-icon :size="20" color="#34d399"><CircleCheckFilled /></el-icon>
          <span>本次提交没有发现错题，太棒了！</span>
        </div>

        <!-- 源码原文 -->
        <div class="detail-section">
          <div class="detail-label">📄 源码原文</div>
          <div class="detail-code">
            <pre><code>{{ currentSubmission.content || '// 暂无代码' }}</code></pre>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, CircleCheckFilled } from '@element-plus/icons-vue'
import { getSubmissionListApi, getSubmissionDetailApi } from '../../api/submission'
import { getErrorListApi } from '../../api/review'

// 状态
const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const submissions = ref([])
const currentSubmission = ref(null)
const page = ref(1)
const pageSize = 10
const totalSubmissions = ref(0)

// 全部错题（用于按提交ID统计错题数）
const allErrors = ref([])

// 当前详情弹窗对应提交的错题
const currentErrors = computed(() => {
  const id = currentSubmission.value?.id
  if (!id) return []
  return allErrors.value.filter((e) => e.sourceFileId === id)
})

// 统计：无错题提交数 = 总提交 - 有错题的提交数
const cleanCount = computed(() => {
  const dirtyIds = new Set(allErrors.value.map((e) => e.sourceFileId).filter(Boolean))
  return submissions.value.filter((s) => !dirtyIds.has(s.id)).length
})
const totalErrorCount = computed(() => allErrors.value.length)

// 错误分类 → 中文名/标签颜色
function categoryMeta(category) {
  const map = {
    SYNTAX_ERROR: { label: '语法错误', tagType: 'danger' },
    LOGIC_ERROR: { label: '逻辑错误', tagType: 'warning' },
    FORMAT_ERROR: { label: '格式错误', tagType: 'info' },
  }
  return map[category] || { label: category || '代码问题', tagType: 'info' }
}

// 语言枚举 → 展示名
function languageText(language) {
  const map = { CPP: 'C++', JAVA: 'Java' }
  return map[language] || language || '-'
}

// 章节枚举 → 展示文案
function chapterText(chapter) {
  const map = {
    FUNDAMENTALS: 'CH1 程序基础与开发环境',
    DATA_TYPES: 'CH2 数据类型与表达式',
    CONTROL_FLOW: 'CH3 选择与循环结构',
    ARRAYS: 'CH4 数组与字符串',
    POINTERS: 'CH5 指针与引用',
    FUNCTIONS: 'CH6 函数与递归',
    OOP: 'CH7 面向对象初步',
    FILE_IO: 'CH8 文件与流',
    COMPREHENSIVE: 'CH9 综合练习',
  }
  return map[chapter] || ''
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  try {
    const d = new Date(timeStr)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch {
    return timeStr
  }
}

// 加载提交列表（分页）
async function loadList() {
  loading.value = true
  try {
    const data = await getSubmissionListApi({ page: page.value - 1, size: pageSize })
    const list = Array.isArray(data) ? data : (data?.content || [])
    totalSubmissions.value = data?.totalElements ?? list.length
    submissions.value = list.map((item) => ({
      ...item,
      chapterText: chapterText(item.chapter),
    }))
  } catch (error) {
    submissions.value = []
    if (error.response?.status !== 401 && error.response?.status !== 403) {
      ElMessage.error(error.response?.data?.detail || '加载提交历史失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 加载全部错题（用于统计每次提交的错题数）
async function loadErrors() {
  try {
    const data = await getErrorListApi({ page: 0, size: 500 })
    const list = Array.isArray(data) ? data : (data?.content || [])
    allErrors.value = list
  } catch (error) {
    // 错题统计失败不阻塞列表展示
    allErrors.value = []
  }
}

// 查看详情：拉取该次提交的源码原文
async function viewDetail(row) {
  detailVisible.value = true
  currentSubmission.value = row
  detailLoading.value = true
  try {
    const detail = await getSubmissionDetailApi(row.id)
    currentSubmission.value = {
      ...detail,
      chapterText: chapterText(detail.chapter),
    }
  } catch (error) {
    // 详情拉取失败就用列表里的数据兜底
    if (error.response?.status !== 401 && error.response?.status !== 403) {
      ElMessage.error(error.response?.data?.detail || '加载提交详情失败')
    }
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadList()
  loadErrors()
})
</script>

<style scoped>
.history-page { max-width: 1100px; margin: 0 auto; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}
.page-title { font-size: 26px; font-weight: 700; color: #f4f4f5; margin: 0 0 4px; }
.page-desc { font-size: 13px; color: #a1a1aa; margin: 0; }
.header-stats { display: flex; gap: 28px; }
.mini-stat { text-align: center; }
.mini-num { display: block; font-size: 22px; font-weight: 700; color: #c4b5fd; }
.mini-label { font-size: 11px; color: #71717a; }

/* 玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.025) !important;
  border: 1px solid rgba(255, 255, 255, 0.06) !important;
  border-radius: 14px !important;
  backdrop-filter: blur(10px);
}
.glass-card :deep(.el-card__body) {
  padding: 8px 16px 16px;
}

/* 表格深色适配 */
.glass-card :deep(.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.03);
  --el-table-border-color: rgba(255, 255, 255, 0.06);
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.05);
  --el-table-text-color: #d4d4d8;
  --el-table-header-text-color: #a1a1aa;
  --el-table-empty-text-color: #71717a;
}

/* 文件名 */
.file-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #a1a1aa;
}
.file-name {
  color: #e4e4e7;
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
}

/* 标签 */
.lang-tag {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(96, 165, 250, 0.1);
  border-radius: 5px;
  font-size: 11px;
  color: #60a5fa;
  font-weight: 500;
}
.chapter-tag {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 5px;
  font-size: 11px;
  color: #a1a1aa;
  font-weight: 500;
}
.error-count {
  font-size: 14px;
  font-weight: 600;
  color: #34d399;
}
.error-count.has-error {
  color: #fbbf24;
}
.time-text { font-size: 12px; color: #71717a; }

/* 分页 */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

/* ===== 详情弹窗 ===== */
.detail-content { padding: 4px 0; }
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.detail-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.detail-filename {
  font-size: 15px;
  font-weight: 600;
  color: #e4e4e7;
  font-family: 'JetBrains Mono', Consolas, monospace;
}
.detail-time { font-size: 12px; color: #71717a; }

.detail-section { margin-bottom: 18px; }
.detail-label {
  font-size: 13px;
  font-weight: 600;
  color: #c4b5fd;
  margin-bottom: 10px;
}

/* 错题列表 */
.detail-errors {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 220px;
  overflow-y: auto;
}
.detail-error-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
}
.error-body { flex: 1; min-width: 0; }
.error-type {
  font-size: 13px;
  color: #e4e4e7;
  font-weight: 500;
  margin-bottom: 4px;
}
.error-line { font-size: 11px; color: #71717a; margin-left: 8px; }
.error-suggestion {
  font-size: 12px;
  color: #86efac;
  line-height: 1.6;
  background: rgba(52, 211, 153, 0.06);
  padding: 6px 10px;
  border-radius: 6px;
}
.no-errors-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  background: rgba(52, 211, 153, 0.06);
  border-radius: 10px;
  color: #86efac;
  font-size: 13px;
}

/* 源码 */
.detail-code {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  overflow: hidden;
}
.detail-code pre {
  padding: 14px;
  margin: 0;
  max-height: 320px;
  overflow: auto;
  font-family: 'Consolas', 'JetBrains Mono', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #d4d4d8;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
