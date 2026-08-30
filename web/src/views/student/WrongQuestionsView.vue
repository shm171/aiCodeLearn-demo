<!-- 错题本页面 - 高级黑灰玻璃感 -->
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
      <el-select v-model="knowledgeFilter" placeholder="知识点筛选" clearable class="knowledge-select">
        <el-option v-for="k in knowledgeList" :key="k" :label="k" :value="k" />
      </el-select>
    </div>

    <!-- 错题列表 -->
    <div class="questions-list">
      <div class="question-card" v-for="q in filteredQuestions" :key="q.id">
        <div class="question-header">
          <div class="question-title">
            <span class="q-id">#{{ q.id }}</span>
            <span class="q-name">{{ q.title }}</span>
            <span class="lang-tag">{{ q.language }}</span>
            <span class="knowledge-tag">{{ q.knowledge }}</span>
          </div>
          <span class="status-badge" :class="q.mastered ? 'mastered' : 'unmastered'">
            {{ q.mastered ? '已掌握' : '未掌握' }}
          </span>
        </div>
        <div class="question-body">
          <div class="error-info">
            <div class="error-label">错误原因</div>
            <p class="error-text">{{ q.errorReason }}</p>
          </div>
          <div class="code-preview">
            <div class="code-header">
              <span class="code-filename">{{ q.fileName }}</span>
              <span class="code-line">第 {{ q.errorLine }} 行</span>
            </div>
            <pre class="code-content"><code>{{ q.codeSnippet }}</code></pre>
          </div>
        </div>
        <div class="question-actions">
          <button class="action-btn" @click="viewDetail(q)">查看详情</button>
          <button class="action-btn accent" @click="generateSimilar(q)" :loading="q.generating">
            <el-icon :size="14"><MagicStick /></el-icon>
            AI 生成类似题
          </button>
          <button class="action-btn" @click="toggleMastered(q)" v-if="!q.mastered">标记已掌握</button>
          <button class="action-btn" @click="toggleMastered(q)" v-else>取消掌握</button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-if="filteredQuestions.length === 0">
      <el-icon :size="40" color="#3f3f46"><Notebook /></el-icon>
      <p>暂无错题，继续加油！</p>
    </div>

    <!-- AI 生成类似题弹窗 -->
    <el-dialog v-model="similarDialogVisible" title="AI 生成类似练习题" width="560px">
      <div class="similar-content" v-if="currentQuestion">
        <div class="similar-question">
          <div class="similar-label">AI 根据错题「{{ currentQuestion.title }}」生成</div>
          <div class="similar-body" v-if="!similarLoading">
            <h4>{{ similarQuestion.title }}</h4>
            <p class="similar-desc">{{ similarQuestion.description }}</p>
            <div class="similar-hint">考察知识点：{{ currentQuestion.knowledge }}</div>
            <div class="similar-code">
              <div class="code-header">
                <span class="code-filename">template.{{ currentQuestion.language === 'C++' ? 'cpp' : currentQuestion.language.toLowerCase() }}</span>
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
        <button class="btn-ghost" @click="similarDialogVisible = false">关闭</button>
        <button class="btn-primary" @click="goToSubmit" v-if="!similarLoading">去提交练习</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Notebook, Loading } from '@element-plus/icons-vue'

const router = useRouter()

const filter = ref('all')
const knowledgeFilter = ref('')
const similarDialogVisible = ref(false)
const similarLoading = ref(false)
const currentQuestion = ref(null)

const wrongQuestions = ref([
  {
    id: 1, title: '数组遍历', language: 'C++', knowledge: '数组',
    fileName: 'array.cpp', errorLine: 6,
    errorReason: '数组下标越界，长度为5的数组最大下标为4，访问了arr[5]',
    codeSnippet: `int arr[5] = {1, 2, 3, 4, 5};
for (int i = 0; i <= 5; i++) {
    cout << arr[i] << endl;  // 第6行：i=5时越界
}`,
    mastered: false, generating: false,
  },
  {
    id: 2, title: '冒泡排序', language: 'C', knowledge: '排序算法',
    fileName: 'bubble.c', errorLine: 8,
    errorReason: '内层循环条件错误，应该是 j < n - i - 1，写成了 j < n - i',
    codeSnippet: `for (int i = 0; i < n - 1; i++) {
    for (int j = 0; j < n - i; j++) {  // 第8行：条件错误
        if (arr[j] > arr[j+1]) {
            swap(&arr[j], &arr[j+1]);
        }
    }
}`,
    mastered: false, generating: false,
  },
  {
    id: 3, title: '斐波那契数列', language: 'Java', knowledge: '递归',
    fileName: 'Fibonacci.java', errorLine: 5,
    errorReason: '递归没有终止条件，导致栈溢出',
    codeSnippet: `public static int fib(int n) {
    return fib(n-1) + fib(n-2);  // 第5行：缺少终止条件
}`,
    mastered: true, generating: false,
  },
])

const similarQuestion = ref({ title: '', description: '', template: '' })

const knowledgeList = computed(() => [...new Set(wrongQuestions.value.map(q => q.knowledge))])
const masteredCount = computed(() => wrongQuestions.value.filter(q => q.mastered).length)

const filteredQuestions = computed(() => {
  let list = wrongQuestions.value
  if (filter.value === 'unmastered') list = list.filter(q => !q.mastered)
  else if (filter.value === 'mastered') list = list.filter(q => q.mastered)
  if (knowledgeFilter.value) list = list.filter(q => q.knowledge === knowledgeFilter.value)
  return list
})

function viewDetail(q) {
  ElMessage.info(`查看错题 #${q.id} 详情功能开发中`)
}

function toggleMastered(q) {
  q.mastered = !q.mastered
  ElMessage.success(q.mastered ? '已标记为掌握' : '已取消掌握标记')
}

function generateSimilar(q) {
  currentQuestion.value = q
  similarDialogVisible.value = true
  similarLoading.value = true
  q.generating = true
  setTimeout(() => {
    similarLoading.value = false
    q.generating = false
    similarQuestion.value = {
      title: `练习：${q.knowledge}应用`,
      description: `根据你在「${q.title}」中犯的错误，AI 为你生成了一道类似题目，请完成以下练习：`,
      template: q.language === 'Java'
        ? `public class Practice {\n    public static void main(String[] args) {\n        // 请在这里完成练习\n        \n    }\n}`
        : `#include <stdio.h>\n\nint main() {\n    // 请在这里完成练习\n    \n    return 0;\n}`,
    }
  }, 1500)
}

function goToSubmit() {
  similarDialogVisible.value = false
  router.push('/submit')
}
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
.mini-num { display: block; font-size: 22px; font-weight: 700; color: #a5b4fc; }
.mini-label { font-size: 11px; color: #71717a; }

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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
.knowledge-select { width: 160px; }

.questions-list { display: flex; flex-direction: column; gap: 12px; }
.question-card {
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 20px;
  transition: all 0.2s;
}
.question-card:hover {
  border-color: rgba(255, 255, 255, 0.12);
}
.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.question-title { display: flex; align-items: center; gap: 10px; }
.q-id { font-size: 12px; color: #52525b; font-family: monospace; }
.q-name { font-size: 15px; font-weight: 600; color: #f4f4f5; }
.lang-tag {
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 5px;
  font-size: 11px;
  color: #d4d4d8;
}
.knowledge-tag {
  padding: 2px 8px;
  background: rgba(165, 180, 252, 0.08);
  color: #a5b4fc;
  border-radius: 5px;
  font-size: 11px;
}
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
  background: rgba(165, 180, 252, 0.04);
  border: 1px solid rgba(165, 180, 252, 0.1);
  border-radius: 10px;
}
.error-label {
  font-size: 12px;
  font-weight: 600;
  color: #a5b4fc;
  margin-bottom: 6px;
}
.error-text { font-size: 12px; color: #d4d4d8; line-height: 1.7; margin: 0; }
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
.action-btn:hover {
  background: rgba(255, 255, 255, 0.09);
  color: #f4f4f5;
}
.action-btn.accent {
  background: rgba(165, 180, 252, 0.08);
  border-color: rgba(165, 180, 252, 0.15);
  color: #a5b4fc;
}
.action-btn.accent:hover {
  background: rgba(165, 180, 252, 0.12);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #52525b;
}
.empty-state p { margin-top: 10px; font-size: 13px; }

.similar-content { padding: 4px 0; }
.similar-label {
  font-size: 13px;
  font-weight: 600;
  color: #a5b4fc;
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
}
.similar-loading {
  text-align: center;
  padding: 36px 20px;
  color: #71717a;
}
.loading-icon {
  animation: spin 1s linear infinite;
  color: #a5b4fc;
}
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.similar-loading p { margin-top: 10px; font-size: 13px; }

.btn-ghost {
  padding: 8px 18px;
  background: transparent;
  color: #a1a1aa;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 9px;
  font-size: 13px;
  cursor: pointer;
}
.btn-primary {
  padding: 8px 18px;
  background: #e4e4e7;
  color: #0c0c0e;
  border: none;
  border-radius: 9px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
</style>
