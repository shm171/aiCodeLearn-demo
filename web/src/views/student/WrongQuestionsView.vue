<!-- 错题本页面 - 对接真实接口 -->
<template>
  <div class="wrong-questions">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">我的错题本</h1>
        <p class="page-desc">AI 自动归档的错误代码，针对性复习巩固</p>
      </div>
      <div class="header-stats">
        <div class="mini-stat">
          <span class="mini-num">{{ wrongQuestions.length }}</span>
          <span class="mini-label">总错题</span>
        </div>
        <div class="mini-stat">
          <span class="mini-num">{{ masteredCount }}</span>
          <span class="mini-label">已掌握</span>
        </div>
        <div class="mini-stat">
          <span class="mini-num">{{ wrongQuestions.length - masteredCount }}</span>
          <span class="mini-label">未掌握</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <button class="filter-tab" :class="{ active: filter === 'all' }" @click="filter = 'all'">全部</button>
        <button class="filter-tab" :class="{ active: filter === 'unmastered' }" @click="filter = 'unmastered'">未掌握</button>
        <button class="filter-tab" :class="{ active: filter === 'mastered' }" @click="filter = 'mastered'">已掌握</button>
      </div>
      <div class="filter-right">
        <el-select v-model="severityFilter" placeholder="严重程度" clearable class="filter-select">
          <el-option label="🔴 严重" value="ERROR" />
          <el-option label="🟡 警告" value="WARNING" />
          <el-option label="🟢 建议" value="INFO" />
        </el-select>
        <el-select v-model="categoryFilter" placeholder="错误类型" clearable class="filter-select">
          <el-option v-for="c in categoryList" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button :icon="Refresh" @click="loadErrors" :loading="loading">刷新</el-button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="loading-icon" :size="32"><Loading /></el-icon>
      <p>正在加载错题数据...</p>
    </div>

    <!-- 错题列表 -->
    <div v-else class="questions-list">
      <div class="question-card" v-for="q in filteredQuestions" :key="q.errorId">
        <div class="question-header">
          <div class="question-title">
            <span class="q-id">#{{ q.errorId }}</span>
            <span class="q-name">{{ q.category || '代码问题' }}</span>
            <el-tag :type="severityTagType(q.severity)" size="small" effect="dark">
              {{ severityText(q.severity) }}
            </el-tag>
          </div>
          <div class="header-right">
            <span class="q-time">{{ formatTime(q.createdAt) }}</span>
            <span class="status-badge" :class="q.mastered ? 'mastered' : 'unmastered'">
              {{ q.mastered ? '已掌握' : '未掌握' }}
            </span>
          </div>
        </div>

        <div class="question-body">
          <div class="error-info">
            <div class="error-label">问题描述</div>
            <p class="error-text">{{ q.message || q.suggestion || '暂无详细描述' }}</p>
            <div v-if="q.suggestion" class="suggestion-box">
              <span class="suggestion-label">💡 修改建议：</span>
              <span>{{ q.suggestion }}</span>
            </div>
          </div>
          <div class="code-preview">
            <div class="code-header">
              <span class="code-filename">代码片段</span>
              <span v-if="q.lineNumber" class="code-line">第 {{ q.lineNumber }} 行</span>
            </div>
            <pre class="code-content"><code>{{ q.codeSnippet || '// 暂无代码片段' }}</code></pre>
          </div>
        </div>

        <div class="question-actions">
          <button class="action-btn" @click="viewDetail(q)">
            <el-icon :size="13"><View /></el-icon>
            查看详情
          </button>
          <button class="action-btn accent" @click="generateSimilar(q)" :loading="q.generating">
            <el-icon :size="13"><MagicStick /></el-icon>
            AI 生成类似题
          </button>
          <button class="action-btn" @click="toggleMastered(q)" v-if="!q.mastered">标记已掌握</button>
          <button class="action-btn" @click="toggleMastered(q)" v-else>取消掌握</button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredQuestions.length === 0" class="empty-state">
      <el-icon :size="40" color="#3f3f46"><Notebook /></el-icon>
      <p>{{ wrongQuestions.length === 0 ? '暂无错题，继续加油！' : '没有符合筛选条件的错题' }}</p>
    </div>

    <!-- 错题详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="错题详情" width="640px">
      <div v-if="currentQuestion" class="detail-content">
        <div class="detail-header">
          <el-tag :type="severityTagType(currentQuestion.severity)" size="small" effect="dark">
            {{ severityText(currentQuestion.severity) }}
          </el-tag>
          <span class="detail-category">{{ currentQuestion.category }}</span>
          <span class="detail-time">{{ formatTime(currentQuestion.createdAt) }}</span>
        </div>

        <div class="detail-section">
          <div class="detail-label">问题描述</div>
          <p class="detail-text">{{ currentQuestion.message || '暂无描述' }}</p>
        </div>

        <div v-if="currentQuestion.suggestion" class="detail-section">
          <div class="detail-label">修改建议</div>
          <p class="detail-text suggestion">{{ currentQuestion.suggestion }}</p>
        </div>

        <div class="detail-section">
          <div class="detail-label">代码片段</div>
          <div class="detail-code">
            <pre><code>{{ currentQuestion.codeSnippet || '// 暂无代码' }}</code></pre>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- AI 生成类似题弹窗 -->
    <el-dialog v-model="similarDialogVisible" title="AI 生成类似练习题" width="560px">
      <div class="similar-content" v-if="currentQuestion">
        <div class="similar-question">
          <div class="similar-label">AI 根据错题「{{ currentQuestion.category || '代码问题' }}」生成</div>
          <div class="similar-body" v-if="!similarLoading">
            <h4>{{ similarQuestion.title }}</h4>
            <p class="similar-desc">{{ similarQuestion.description }}</p>
            <div class="similar-hint">考察知识点：{{ currentQuestion.category }}</div>
            <div class="similar-code">
              <div class="code-header">
                <span class="code-filename">template.java</span>
              </div>
              <pre><code>{{ similarQuestion.template }}</code></pre>
            </div>
          </div>
          <div class="similar-loading" v-else>
            <el-icon class="loading-icon" :size="28"><Loading /></el-icon>
            <p>AI 正在生成类似题目...</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="similarDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToSubmit" v-if="!similarLoading">去提交练习</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Notebook, Loading, Refresh, View } from '@element-plus/icons-vue'
import { getErrorListApi } from '../../api/review'

const router = useRouter()

// 状态
const loading = ref(false)
const filter = ref('all')
const severityFilter = ref('')
const categoryFilter = ref('')
const wrongQuestions = ref([])
const detailDialogVisible = ref(false)
const similarDialogVisible = ref(false)
const similarLoading = ref(false)
const currentQuestion = ref(null)
const similarQuestion = ref({ title: '', description: '', template: '' })

// 从localStorage读取已掌握的错题ID
const masteredIds = ref(new Set(JSON.parse(localStorage.getItem('masteredErrorIds') || '[]')))

// 计算属性
const categoryList = computed(() => [...new Set(wrongQuestions.value.map(q => q.category).filter(Boolean))])
const masteredCount = computed(() => wrongQuestions.value.filter(q => q.mastered).length)

const filteredQuestions = computed(() => {
  let list = wrongQuestions.value
  if (filter.value === 'unmastered') list = list.filter(q => !q.mastered)
  else if (filter.value === 'mastered') list = list.filter(q => q.mastered)
  if (severityFilter.value) list = list.filter(q => q.severity === severityFilter.value)
  if (categoryFilter.value) list = list.filter(q => q.category === categoryFilter.value)
  return list
})

// 加载错题列表
async function loadErrors() {
  loading.value = true
  try {
    const data = await getErrorListApi()
    // 适配后端返回的数据格式
    const list = Array.isArray(data) ? data : (data.records || data.list || data.content || [])
    wrongQuestions.value = list.map(item => ({
      ...item,
      mastered: masteredIds.value.has(item.errorId),
      generating: false,
    }))
  } catch (error) {
    console.error('加载错题失败:', error)
    // 接口失败时使用空列表，不显示Mock数据
    wrongQuestions.value = []
  } finally {
    loading.value = false
  }
}

// 查看详情
function viewDetail(q) {
  currentQuestion.value = q
  detailDialogVisible.value = true
}

// 标记已掌握/取消掌握
function toggleMastered(q) {
  q.mastered = !q.mastered
  if (q.mastered) {
    masteredIds.value.add(q.errorId)
    ElMessage.success('已标记为掌握')
  } else {
    masteredIds.value.delete(q.errorId)
    ElMessage.info('已取消掌握标记')
  }
  localStorage.setItem('masteredErrorIds', JSON.stringify([...masteredIds.value]))
}

// AI生成类似题
function generateSimilar(q) {
  currentQuestion.value = q
  similarDialogVisible.value = true
  similarLoading.value = true
  q.generating = true
  setTimeout(() => {
    similarLoading.value = false
    q.generating = false
    similarQuestion.value = {
      title: `练习：${q.category || '代码'}应用`,
      description: `根据你在「${q.category || '代码问题'}」中犯的错误，AI 为你生成了一道类似题目，请完成以下练习：`,
      template: `public class Practice {\n    public static void main(String[] args) {\n        // 请在这里完成练习\n        // 考察点：${q.category}\n        \n    }\n}`,
    }
  }, 1500)
}

function goToSubmit() {
  similarDialogVisible.value = false
  router.push('/submit')
}

// 辅助函数
function severityText(severity) {
  const map = { ERROR: '严重', WARNING: '警告', INFO: '建议' }
  return map[severity] || '未知'
}

function severityTagType(severity) {
  const map = { ERROR: 'danger', WARNING: 'warning', INFO: 'info' }
  return map[severity] || 'info'
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  try {
    const d = new Date(timeStr)
    return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch {
    return timeStr
  }
}

onMounted(() => {
  loadErrors()
})
</script>

<style scoped>
.wrong-questions { max-width: 960px; margin: 0 auto; }

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

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}
.filter-tabs {
  display: flex;
  gap: 2px;
  padding: 3px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 9px;
}
.filter-tab {
  padding: 7px 16px;
  background: transparent;
  border: none;
  border-radius: 7px;
  color: #71717a;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.filter-tab:hover { color: #d4d4d8; }
.filter-tab.active { background: rgba(255, 255, 255, 0.07); color: #e4e4e7; }
.filter-right { display: flex; gap: 8px; align-items: center; }
.filter-select { width: 130px; }

/* 加载中 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #71717a;
}
.loading-icon {
  animation: spin 1s linear infinite;
  color: #c4b5fd;
}
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.loading-state p { margin-top: 10px; font-size: 13px; }

/* 错题卡片 */
.questions-list { display: flex; flex-direction: column; gap: 12px; }
.question-card {
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 20px;
  transition: all 0.2s;
}
.question-card:hover { border-color: rgba(255, 255, 255, 0.12); }

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.question-title { display: flex; align-items: center; gap: 10px; }
.q-id { font-size: 12px; color: #52525b; font-family: monospace; }
.q-name { font-size: 15px; font-weight: 600; color: #f4f4f5; }
.header-right { display: flex; align-items: center; gap: 12px; }
.q-time { font-size: 12px; color: #71717a; }

.status-badge {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}
.status-badge.mastered { background: rgba(134, 239, 172, 0.08); color: #86efac; }
.status-badge.unmastered { background: rgba(252, 211, 77, 0.08); color: #fcd34d; }

.question-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 14px;
}
.error-info {
  padding: 12px;
  background: rgba(196, 181, 253, 0.04);
  border: 1px solid rgba(196, 181, 253, 0.1);
  border-radius: 10px;
}
.error-label {
  font-size: 12px;
  font-weight: 600;
  color: #c4b5fd;
  margin-bottom: 6px;
}
.error-text { font-size: 12px; color: #d4d4d8; line-height: 1.7; margin: 0 0 8px; }
.suggestion-box {
  font-size: 11px;
  color: #a1a1aa;
  line-height: 1.6;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 6px;
}
.suggestion-label { color: #c4b5fd; font-weight: 600; }

.code-preview {
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  overflow: hidden;
}
.code-header {
  display: flex;
  justify-content: space-between;
  padding: 7px 12px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.code-filename { font-size: 11px; color: #a1a1aa; font-family: monospace; }
.code-line { font-size: 11px; color: #c4b5fd; }
.code-content {
  padding: 10px 12px;
  margin: 0;
  font-family: 'Consolas', monospace;
  font-size: 11px;
  line-height: 1.7;
  color: #d4d4d8;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.question-actions { display: flex; gap: 8px; }
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: #d4d4d8;
  transition: all 0.15s;
}
.action-btn:hover { background: rgba(255, 255, 255, 0.09); color: #f4f4f5; }
.action-btn.accent {
  background: rgba(196, 181, 253, 0.08);
  border-color: rgba(196, 181, 253, 0.15);
  color: #c4b5fd;
}
.action-btn.accent:hover { background: rgba(196, 181, 253, 0.12); }

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #52525b;
}
.empty-state p { margin-top: 10px; font-size: 13px; }

/* 详情弹窗 */
.detail-content { padding: 4px 0; }
.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}
.detail-category { font-size: 15px; font-weight: 600; color: #e4e4e7; }
.detail-time { font-size: 12px; color: #71717a; margin-left: auto; }
.detail-section { margin-bottom: 16px; }
.detail-label {
  font-size: 13px;
  font-weight: 600;
  color: #c4b5fd;
  margin-bottom: 8px;
}
.detail-text {
  font-size: 14px;
  color: #d4d4d8;
  line-height: 1.7;
  margin: 0;
}
.detail-text.suggestion {
  padding: 12px 14px;
  background: rgba(196, 181, 253, 0.06);
  border-radius: 8px;
  border-left: 3px solid #c4b5fd;
}
.detail-code {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  overflow: hidden;
}
.detail-code pre {
  padding: 14px;
  margin: 0;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.7;
  color: #d4d4d8;
  white-space: pre-wrap;
  word-break: break-all;
}

/* AI生成类似题 */
.similar-content { padding: 4px 0; }
.similar-label {
  font-size: 13px;
  font-weight: 600;
  color: #c4b5fd;
  margin-bottom: 14px;
}
.similar-body h4 { font-size: 17px; color: #e4e4e7; margin: 0 0 8px; }
.similar-desc { font-size: 13px; color: #a1a1aa; line-height: 1.7; margin: 0 0 12px; }
.similar-hint { font-size: 12px; color: #71717a; margin-bottom: 12px; }
.similar-code {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  overflow: hidden;
}
.similar-code pre {
  padding: 12px;
  margin: 0;
  font-family: 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #a1a1aa;
  white-space: pre-wrap;
}
.similar-loading {
  text-align: center;
  padding: 36px 20px;
  color: #71717a;
}
.similar-loading p { margin-top: 10px; font-size: 13px; }
</style>
