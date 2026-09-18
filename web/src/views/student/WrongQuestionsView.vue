<!-- 错题本页面 - 对接真实接口 -->
<template>
  <div class="wrong-questions" v-loading="loading" element-loading-text="正在加载错题本..." element-loading-background="rgba(10, 10, 12, 0.85)">
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
        <el-select v-model="categoryKeyFilter" placeholder="错误分类" clearable class="filter-select">
          <el-option label="🔴 语法错误" value="SYNTAX_ERROR" />
          <el-option label="🟡 逻辑错误" value="LOGIC_ERROR" />
          <el-option label="🟢 格式错误" value="FORMAT_ERROR" />
        </el-select>
        <el-select v-model="typeFilter" placeholder="错误类型" clearable class="filter-select">
          <el-option v-for="t in typeList" :key="t" :label="t" :value="t" />
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
            <span class="q-name">{{ q.errorType || q.category || '代码问题' }}</span>
            <el-tag :type="categoryMeta(q.categoryKey).tagType" size="small" effect="dark">
              {{ q.category }}
            </el-tag>
            <el-tag v-if="q.chapter" type="info" size="small" effect="plain" class="lang-tag">
              {{ q.chapter }}
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
            <p class="error-text">{{ q.errorType || q.category || '暂无详细描述' }}</p>
            <div v-if="q.fixSuggestion" class="suggestion-box">
              <span class="suggestion-label">💡 修改建议：</span>
              <span>{{ q.fixSuggestion }}</span>
            </div>
            <div v-if="q.lineText" class="line-info">出错行号：第 {{ q.lineText }} 行</div>
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
          <button class="action-btn" @click="markMastered(q)" v-if="!q.mastered" :disabled="q.mastering">
            {{ q.mastering ? '标记中...' : '标记已掌握' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredQuestions.length === 0" class="empty-state">
      <el-icon :size="40" color="#3f3f46"><Notebook /></el-icon>
      <p>{{ wrongQuestions.length === 0 ? '暂无错题，继续加油！' : '没有符合筛选条件的错题' }}</p>
    </div>

    <!-- 错题详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="错题详情" width="720px" top="6vh" :z-index="3000" class="detail-dialog">
      <div v-if="currentQuestion" class="detail-content">
        <div class="detail-header">
          <el-tag :type="categoryMeta(currentQuestion.categoryKey).tagType" size="small" effect="dark">
            {{ currentQuestion.category }}
          </el-tag>
          <span class="detail-category">{{ currentQuestion.errorType || '代码问题' }}</span>
          <el-tag v-if="currentQuestion.chapter" type="info" size="small" effect="plain">
            {{ currentQuestion.chapter }}
          </el-tag>
          <span class="detail-time">{{ formatTime(currentQuestion.createdAt) }}</span>
        </div>

        <div class="detail-section">
          <div class="detail-label">📝 问题描述</div>
          <p class="detail-text">{{ currentQuestion.errorType || '暂无描述' }}</p>
        </div>

        <div v-if="currentQuestion.fixSuggestion" class="detail-section">
          <div class="detail-label">💡 修改建议</div>
          <p class="detail-text suggestion">{{ currentQuestion.fixSuggestion }}</p>
        </div>

        <div v-if="currentQuestion.lineText" class="detail-section">
          <div class="detail-label">📍 出错行号</div>
          <p class="detail-text">第 {{ currentQuestion.lineText }} 行</p>
        </div>
      </div>

      <!-- 底部：问AI这道题 -->
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" :icon="MagicStick" @click="askAiAboutError">
          问AI这道题
        </el-button>
      </template>
    </el-dialog>

    <!-- AI 生成类似题弹窗：左右布局，左边题目，右边写代码+提交+批改 -->
    <el-dialog v-model="similarDialogVisible" title="AI 生成类似练习题" width="1000px" top="5vh" :z-index="3000" class="similar-dialog">
      <div class="similar-content" v-if="currentQuestion">
        <div class="similar-loading" v-if="similarLoading">
          <el-icon class="loading-icon" :size="28"><Loading /></el-icon>
          <p>AI 正在生成类似题目...</p>
        </div>
        <div class="similar-body" v-else-if="similarQuestions.length > 0">
          <!-- 翻页指示器 -->
          <div class="similar-pagination">
            <span class="pagination-info">第 {{ currentSimilarIndex + 1 }} / {{ similarQuestions.length }} 题</span>
            <div class="pagination-btns">
              <el-button size="small" :disabled="currentSimilarIndex === 0" @click="prevSimilar">上一题</el-button>
              <el-button size="small" :disabled="currentSimilarIndex === similarQuestions.length - 1" @click="nextSimilar">下一题</el-button>
            </div>
          </div>

          <div class="similar-main">
            <!-- 左边：题目信息 -->
            <div class="similar-left">
              <div class="similar-label">AI 根据错题「{{ currentQuestion.errorType || currentQuestion.category || '代码问题' }}」生成</div>
              <h4 class="similar-title">{{ similarQuestions[currentSimilarIndex].title }}</h4>
              <p class="similar-desc">{{ similarQuestions[currentSimilarIndex].description }}</p>
              <div class="similar-hint">考察知识点：{{ currentQuestion.category }}</div>
              
              <!-- 代码模板参考 -->
              <div class="similar-template">
                <div class="template-header">
                  <span>📋 代码模板参考</span>
                  <el-button size="small" text @click="fillTemplate">填入编辑器</el-button>
                </div>
                <pre class="template-code"><code>{{ similarQuestions[currentSimilarIndex].template }}</code></pre>
              </div>
            </div>

            <!-- 右边：代码编辑器 + 提交 + 批改结果 -->
            <div class="similar-right">
              <div class="editor-header">
                <span class="editor-filename">practice.cpp</span>
                <el-button size="small" text @click="clearEditor">清空</el-button>
              </div>
              <div ref="similarEditorRef" class="similar-code-editor"></div>
              
              <!-- 提交按钮 -->
              <div class="submit-area">
                <el-button type="primary" @click="submitSimilar" :loading="similarSubmitting" class="submit-btn">
                  {{ similarSubmitting ? 'AI 批改中...' : '提交并批改' }}
                </el-button>
              </div>

              <!-- 批改结果 -->
              <div class="grading-result" v-if="similarGradingResult">
                <div class="result-header">
                  <span class="result-score">{{ similarGradingResult.score }}分</span>
                  <span class="result-feedback">{{ similarGradingResult.feedback }}</span>
                </div>
                <div class="result-issues">
                  <div class="issue-item" v-for="(issue, idx) in similarGradingResult.errors" :key="idx">
                    <el-tag :type="categoryMeta(issue.category).tagType" size="small">
                      {{ categoryMeta(issue.category).label }}
                    </el-tag>
                    <div class="issue-content">
                      <div class="issue-category">{{ issue.errorType }} <span v-if="issue.line && issue.line.length">（第{{ issue.line.join('、') }}行）</span></div>
                      <div class="issue-message">{{ issue.message }}</div>
                      <div class="issue-suggestion" v-if="issue.fixSuggestion">💡 {{ issue.fixSuggestion }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeSimilarDialog">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Notebook, Loading, Refresh, View } from '@element-plus/icons-vue'
import { getErrorListApi, getErrorDetailApi, markMasteredApi } from '../../api/review'
import { uploadSubmissionApi, getSubmissionDetailApi } from '../../api/submission'

// ========== CodeMirror 导入 ==========
import { EditorState } from '@codemirror/state'
import { EditorView, basicSetup } from 'codemirror'
import { java } from '@codemirror/lang-java'
import { cpp } from '@codemirror/lang-cpp'
import { oneDark } from '@codemirror/theme-one-dark'

// 状态
const loading = ref(false)
const filter = ref('all')
const categoryKeyFilter = ref('')
const typeFilter = ref('')
const wrongQuestions = ref([])
const detailDialogVisible = ref(false)
const similarDialogVisible = ref(false)
const similarLoading = ref(false)
const currentQuestion = ref(null)
const similarQuestions = ref([])
const currentSimilarIndex = ref(0)

// 类似题编辑器相关
const similarEditorRef = ref(null)
let similarEditorView = null
const similarSubmitting = ref(false)
const similarGradingResult = ref(null)

// 计算属性
const typeList = computed(() => [...new Set(wrongQuestions.value.map(q => q.errorType).filter(Boolean))])
const masteredCount = computed(() => wrongQuestions.value.filter(q => q.mastered).length)

const filteredQuestions = computed(() => {
  let list = wrongQuestions.value
  if (filter.value === 'unmastered') list = list.filter(q => !q.mastered)
  else if (filter.value === 'mastered') list = list.filter(q => q.mastered)
  if (categoryKeyFilter.value) list = list.filter(q => q.categoryKey === categoryKeyFilter.value)
  if (typeFilter.value) list = list.filter(q => q.errorType === typeFilter.value)
  return list
})

// 错误分类 → 中文名/严重程度/标签颜色 映射（后端没有severity字段，按category分档）
function categoryMeta(category) {
  const map = {
    SYNTAX_ERROR: { label: '语法错误', severity: 'ERROR', tagType: 'danger' },
    LOGIC_ERROR: { label: '逻辑错误', severity: 'WARNING', tagType: 'warning' },
    FORMAT_ERROR: { label: '格式错误', severity: 'INFO', tagType: 'info' },
  }
  return map[category] || { label: category || '代码问题', severity: 'INFO', tagType: 'info' }
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

// 后端错题记录 → 页面展示模型
function normalizeError(item) {
  return {
    errorId: item.id,
    sourceFileId: item.sourceFileId,
    categoryKey: item.category,
    category: categoryMeta(item.category).label,
    chapter: chapterText(item.chapter),
    errorType: item.errorType,
    fixSuggestion: item.fixSuggestion,
    lineText: Array.isArray(item.line) && item.line.length ? item.line.join('、') : '',
    createdAt: item.createdAt,
    mastered: !!item.mastered,
    generating: false,
    mastering: false,
  }
}

// 加载错题列表：从后端真实接口拉取
async function loadErrors() {
  loading.value = true
  try {
    const data = await getErrorListApi({ page: 0, size: 100 })
    // Spring 分页结构：数据在 content 里
    const list = Array.isArray(data) ? data : (data?.content || [])
    wrongQuestions.value = list.map(normalizeError)
  } catch (error) {
    console.error('加载错题失败:', error)
    wrongQuestions.value = []
    if (error.response?.status !== 401) {
      ElMessage.error(error.response?.data?.detail || '加载错题失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 查看详情：调详情接口拿最新数据，失败时用列表数据兜底
async function viewDetail(q) {
  try {
    const detail = await getErrorDetailApi(q.errorId)
    currentQuestion.value = normalizeError(detail)
  } catch (error) {
    currentQuestion.value = q
  }
  detailDialogVisible.value = true
}

// 问AI这道题：带错误类型/修复建议/源码唤起AI助手自动发送
async function askAiAboutError() {
  const q = currentQuestion.value
  if (!q) return

  // 错题记录本身不含源码，按 sourceFileId 拉提交详情拿代码；失败就只带错题信息
  let code = ''
  if (q.sourceFileId) {
    try {
      const detail = await getSubmissionDetailApi(q.sourceFileId)
      code = detail?.content || ''
    } catch {
      code = ''
    }
  }

  const parts = [`我想问一道错题：${q.errorType || q.category || '代码问题'}`]
  if (q.fixSuggestion) parts.push(`修复建议：${q.fixSuggestion}`)
  if (code) {
    // 后端消息上限2000字，代码过长时截断
    const limit = 1200
    const codePart = code.length > limit
      ? code.slice(0, limit) + '\n// ……（代码过长，已截断）'
      : code
    parts.push(`相关代码：\n\`\`\`\n${codePart}\n\`\`\``)
  }

  detailDialogVisible.value = false
  window.dispatchEvent(new CustomEvent('open-ai-assistant', { detail: { message: parts.join('\n') } }))
}

// 标记已掌握：必须调后端接口，成功后更新本地状态
async function markMastered(q) {
  q.mastering = true
  try {
    const updated = await markMasteredApi(q.errorId)
    q.mastered = updated.mastered !== false
    ElMessage.success('已标记为掌握')
  } catch (error) {
    if (error.response?.status !== 401) {
      ElMessage.error(error.response?.data?.detail || '操作失败，请稍后重试')
    }
  } finally {
    q.mastering = false
  }
}

// AI生成类似题（前端模拟，后端暂无此接口）：根据错题类型生成3道题
function generateSimilar(q) {
  currentQuestion.value = q
  similarDialogVisible.value = true
  similarLoading.value = true
  currentSimilarIndex.value = 0
  similarGradingResult.value = null
  q.generating = true
  setTimeout(() => {
    similarLoading.value = false
    q.generating = false
    similarQuestions.value = generateSimilarQuestions(q.errorType || q.category)
    nextTick(() => {
      initSimilarEditor()
    })
  }, 1500)
}

// ========== 类似题编辑器相关 ==========

// 初始化编辑器
function initSimilarEditor() {
  if (!similarEditorRef.value) return
  if (similarEditorView) {
    similarEditorView.destroy()
    similarEditorView = null
  }
  // 错题记录里没有语言字段，默认按 C++ 高亮
  const isCpp = true
  const langExtension = isCpp ? cpp() : java()
  
  const state = EditorState.create({
    doc: '',
    extensions: [
      basicSetup,
      langExtension,
      oneDark,
      EditorView.theme({
        '&': { height: '280px' },
        '.cm-scroller': { overflow: 'auto' },
      }),
    ],
  })
  similarEditorView = new EditorView({
    state,
    parent: similarEditorRef.value,
  })
}

// 销毁编辑器
function destroySimilarEditor() {
  if (similarEditorView) {
    similarEditorView.destroy()
    similarEditorView = null
  }
}

// 填入模板
function fillTemplate() {
  if (!similarEditorView) return
  const template = similarQuestions.value[currentSimilarIndex.value]?.template || ''
  similarEditorView.dispatch({
    changes: { from: 0, to: similarEditorView.state.doc.length, insert: template },
  })
}

// 清空编辑器
function clearEditor() {
  if (!similarEditorView) return
  similarEditorView.dispatch({
    changes: { from: 0, to: similarEditorView.state.doc.length, insert: '' },
  })
  similarGradingResult.value = null
}

// 提交并批改（一步完成：上传即批改，响应里直接带 gradingResult）
async function submitSimilar() {
  if (!similarEditorView) return
  const code = similarEditorView.state.doc.toString()
  if (!code.trim()) {
    ElMessage.warning({ message: '请先写代码再提交', zIndex: 9999 })
    return
  }

  similarSubmitting.value = true
  similarGradingResult.value = null

  try {
    const isCpp = true
    const filename = `practice.${isCpp ? 'cpp' : 'java'}`
    const blob = new Blob([code], { type: 'text/plain' })
    const file = new File([blob], filename, { type: 'text/plain' })

    // 提交（一步完成上传+批改）
    const uploadRes = await uploadSubmissionApi(file)
    similarGradingResult.value = uploadRes?.gradingResult || null
    if (!similarGradingResult.value) {
      ElMessage.error({ message: '未获取到批改结果，请重试', zIndex: 9999 })
      return
    }
    ElMessage.success({ message: '批改完成', zIndex: 9999 })
  } catch (error) {
    console.error('提交批改失败:', error)
    ElMessage.error({ message: '提交批改失败，请重试', zIndex: 9999 })
  } finally {
    similarSubmitting.value = false
  }
}

// 关闭弹窗
function closeSimilarDialog() {
  similarDialogVisible.value = false
  destroySimilarEditor()
  similarGradingResult.value = null
}

// 根据错误类型生成类似题目（前端模拟，后端暂无此接口）
function generateSimilarQuestions(category) {
  const isCpp = true
  const ext = isCpp ? '.cpp' : '.java'
  
  // 通用题目模板
  const templates = {
    '数组越界': [
      {
        title: `练习1：遍历数组并求和`,
        description: `请编写一个程序，定义一个长度为10的整型数组，初始化为1到10，然后遍历数组计算所有元素的和并输出。注意：循环条件要正确，不要越界！`,
        template: isCpp 
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int arr[10] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};\n    int sum = 0;\n    // 请在这里遍历数组求和\n    // 注意循环条件：i < 10，不是 i <= 10\n    \n    cout << "sum = " << sum << endl;\n    return 0;\n}`
          : `public class ArraySum {\n    public static void main(String[] args) {\n        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};\n        int sum = 0;\n        // 请在这里遍历数组求和\n        // 注意循环条件：i < arr.length，不是 i <= arr.length\n        \n        System.out.println("sum = " + sum);\n    }\n}`
      },
      {
        title: `练习2：查找数组中的最大值`,
        description: `请编写一个程序，定义一个长度为8的整型数组，初始化为任意值，然后遍历数组找出最大值并输出。提示：用一个变量保存当前最大值，遍历过程中不断更新。`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int arr[8] = {12, 45, 7, 89, 23, 56, 34, 78};\n    int max = arr[0];\n    // 请在这里遍历数组找最大值\n    // 从i=1开始，因为max已经初始化为arr[0]\n    \n    cout << "max = " << max << endl;\n    return 0;\n}`
          : `public class ArrayMax {\n    public static void main(String[] args) {\n        int[] arr = {12, 45, 7, 89, 23, 56, 34, 78};\n        int max = arr[0];\n        // 请在这里遍历数组找最大值\n        // 从i=1开始，因为max已经初始化为arr[0]\n        \n        System.out.println("max = " + max);\n    }\n}`
      },
      {
        title: `练习3：数组元素逆序输出`,
        description: `请编写一个程序，定义一个长度为6的字符数组，初始化为'a'到'f'，然后从后往前逆序输出每个元素。注意：逆序遍历时下标从length-1开始，到0结束。`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    char arr[6] = {'a', 'b', 'c', 'd', 'e', 'f'};\n    // 请在这里逆序输出数组元素\n    // 从i=5开始，到i>=0结束，每次i--\n    \n    return 0;\n}`
          : `public class ArrayReverse {\n    public static void main(String[] args) {\n        char[] arr = {'a', 'b', 'c', 'd', 'e', 'f'};\n        // 请在这里逆序输出数组元素\n        // 从i=arr.length-1开始，到i>=0结束，每次i--\n        \n    }\n}`
      }
    ],
    '变量未定义': [
      {
        title: `练习1：计算两个数的平均值`,
        description: `请编写一个程序，定义两个整型变量a和b，分别赋值为10和20，然后计算它们的平均值并输出。注意：所有变量在使用前必须先声明！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int a = 10, b = 20;\n    // 请在这里声明avg变量并计算平均值\n    // 平均值 = (a + b) / 2.0\n    \n    cout << "avg = " << avg << endl;\n    return 0;\n}`
          : `public class Average {\n    public static void main(String[] args) {\n        int a = 10, b = 20;\n        // 请在这里声明avg变量并计算平均值\n        // 平均值 = (a + b) / 2.0\n        \n        System.out.println("avg = " + avg);\n    }\n}`
      },
      {
        title: `练习2：计算圆的面积`,
        description: `请编写一个程序，定义半径r为5.0，圆周率PI为3.14159，然后计算圆的面积并输出。公式：面积 = PI * r * r。注意：需要声明面积变量！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    double r = 5.0;\n    const double PI = 3.14159;\n    // 请在这里声明area变量并计算面积\n    \n    cout << "area = " << area << endl;\n    return 0;\n}`
          : `public class CircleArea {\n    public static void main(String[] args) {\n        double r = 5.0;\n        final double PI = 3.14159;\n        // 请在这里声明area变量并计算面积\n        \n        System.out.println("area = " + area);\n    }\n}`
      },
      {
        title: `练习3：字符串拼接`,
        description: `请编写一个程序，定义两个字符串firstName和lastName，分别赋值为"Zhang"和"San"，然后拼接成全名并输出。注意：需要声明结果变量！`,
        template: isCpp
          ? `#include <iostream>\n#include <string>\nusing namespace std;\n\nint main() {\n    string firstName = "Zhang";\n    string lastName = "San";\n    // 请在这里声明fullName变量并拼接\n    // 拼接：firstName + " " + lastName\n    \n    cout << "fullName = " << fullName << endl;\n    return 0;\n}`
          : `public class FullName {\n    public static void main(String[] args) {\n        String firstName = "Zhang";\n        String lastName = "San";\n        // 请在这里声明fullName变量并拼接\n        // 拼接：firstName + " " + lastName\n        \n        System.out.println("fullName = " + fullName);\n    }\n}`
      }
    ],
    '缺少分号': [
      {
        title: `练习1：输出个人信息`,
        description: `请编写一个程序，输出你的姓名、年龄、专业。每行一个输出语句。注意：每条语句末尾都要有分号！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << "姓名：张三" << endl\n    cout << "年龄：20" << endl\n    cout << "专业：计算机" << endl\n    return 0;\n}`
          : `public class PersonalInfo {\n    public static void main(String[] args) {\n        System.out.println("姓名：张三")\n        System.out.println("年龄：20")\n        System.out.println("专业：计算机")\n    }\n}`
      },
      {
        title: `练习2：变量赋值与输出`,
        description: `请编写一个程序，定义三个变量a、b、c，分别赋值为1、2、3，然后输出它们的和。注意：赋值语句和输出语句都要有分号！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int a = 1\n    int b = 2\n    int c = 3\n    cout << "a+b+c = " << a + b + c << endl\n    return 0;\n}`
          : `public class SumABC {\n    public static void main(String[] args) {\n        int a = 1\n        int b = 2\n        int c = 3\n        System.out.println("a+b+c = " + (a + b + c))\n    }\n}`
      },
      {
        title: `练习3：计算阶乘`,
        description: `请编写一个程序，计算5的阶乘（5! = 1*2*3*4*5 = 120）。用for循环实现。注意：循环体内的每条语句都要有分号！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int n = 5\n    int result = 1\n    for (int i = 1; i <= n; i++) {\n        result = result * i\n    }\n    cout << n << "! = " << result << endl\n    return 0;\n}`
          : `public class Factorial {\n    public static void main(String[] args) {\n        int n = 5\n        int result = 1\n        for (int i = 1; i <= n; i++) {\n            result = result * i\n        }\n        System.out.println(n + "! = " + result)\n    }\n}`
      }
    ],
    '空指针异常': [
      {
        title: `练习1：安全地获取字符串长度`,
        description: `请编写一个方法，接收一个字符串参数，返回字符串的长度。如果字符串为null，返回0。注意：调用方法前先判断是否为null！`,
        template: isCpp
          ? `#include <iostream>\n#include <string>\nusing namespace std;\n\nint getLength(const string* str) {\n    // 请在这里判断str是否为nullptr\n    // 如果为nullptr，返回0\n    // 否则返回str->length()\n    \n}\n\nint main() {\n    string s = "hello";\n    cout << "length = " << getLength(&s) << endl;\n    cout << "length = " << getLength(nullptr) << endl;\n    return 0;\n}`
          : `public class SafeLength {\n    public static int getLength(String str) {\n        // 请在这里判断str是否为null\n        // 如果为null，返回0\n        // 否则返回str.length()\n        \n    }\n    \n    public static void main(String[] args) {\n        System.out.println("length = " + getLength("hello"));\n        System.out.println("length = " + getLength(null));\n    }\n}`
      },
      {
        title: `练习2：安全地调用对象方法`,
        description: `请编写一个程序，定义一个Person对象引用，可能为null。调用对象的getName()方法前先判断是否为null，如果为null输出"未知"。`,
        template: isCpp
          ? `#include <iostream>\n#include <string>\nusing namespace std;\n\nclass Person {\npublic:\n    string getName() { return "张三"; }\n};\n\nint main() {\n    Person* p = nullptr;\n    // 请在这里安全地调用p->getName()\n    // 如果p为nullptr，输出"未知"\n    // 否则输出p->getName()\n    \n    return 0;\n}`
          : `class Person {\n    public String getName() { return "张三"; }\n}\n\npublic class SafeCall {\n    public static void main(String[] args) {\n        Person p = null;\n        // 请在这里安全地调用p.getName()\n        // 如果p为null，输出"未知"\n        // 否则输出p.getName()\n        \n    }\n}`
      },
      {
        title: `练习3：使用Optional处理可能为null的值（Java）/ 智能指针（C++）`,
        description: `请编写一个程序，演示如何安全地处理可能为null的值。Java用Optional，C++用智能指针或判断nullptr。`,
        template: isCpp
          ? `#include <iostream>\n#include <memory>\n#include <string>\nusing namespace std;\n\nint main() {\n    // 使用智能指针，自动管理内存\n    unique_ptr<string> p1 = make_unique<string>("hello");\n    unique_ptr<string> p2 = nullptr;\n    \n    // 请在这里安全地使用p1和p2\n    // 判断p是否为空：if (p)\n    \n    return 0;\n}`
          : `import java.util.Optional;\n\npublic class OptionalDemo {\n    public static void main(String[] args) {\n        Optional<String> opt1 = Optional.of("hello");\n        Optional<String> opt2 = Optional.empty();\n        \n        // 请在这里安全地使用opt1和opt2\n        // 使用opt.isPresent()判断是否有值\n        // 使用opt.orElse("默认值")获取值或默认值\n        \n    }\n}`
      }
    ],
    '类型不匹配': [
      {
        title: `练习1：正确的类型转换`,
        description: `请编写一个程序，将double类型的温度（摄氏度）转换为int类型的整数温度。要求四舍五入，而不是直接截断。注意：需要显式强制转换！`,
        template: isCpp
          ? `#include <iostream>\n#include <cmath>\nusing namespace std;\n\nint main() {\n    double celsius = 26.7;\n    // 请在这里将celsius四舍五入转换为int\n    // 使用round()函数四舍五入，然后强制转换为int\n    int result = \n    \n    cout << "温度约为：" << result << "度" << endl;\n    return 0;\n}`
          : `public class Temperature {\n    public static void main(String[] args) {\n        double celsius = 26.7;\n        // 请在这里将celsius四舍五入转换为int\n        // 使用Math.round()函数四舍五入，然后强制转换为int\n        int result = \n        \n        System.out.println("温度约为：" + result + "度");\n    }\n}`
      },
      {
        title: `练习2：整数除法 vs 浮点数除法`,
        description: `请编写一个程序，计算7除以2的结果。注意：整数除法会截断小数部分，如果要得到3.5，需要将其中一个数转为double。`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int a = 7, b = 2;\n    // 请在这里计算a除以b的结果\n    // 整数除法：a / b 结果是3\n    // 浮点数除法：(double)a / b 结果是3.5\n    double result = \n    \n    cout << "7 / 2 = " << result << endl;\n    return 0;\n}`
          : `public class Division {\n    public static void main(String[] args) {\n        int a = 7, b = 2;\n        // 请在这里计算a除以b的结果\n        // 整数除法：a / b 结果是3\n        // 浮点数除法：(double)a / b 结果是3.5\n        double result = \n        \n        System.out.println("7 / 2 = " + result);\n    }\n}`
      },
      {
        title: `练习3：计算平均分`,
        description: `请编写一个程序，定义三个学生的成绩（int类型），计算平均分（double类型）。注意：总和是int，除以人数时要转为double才能得到小数。`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int score1 = 85, score2 = 90, score3 = 78;\n    int sum = score1 + score2 + score3;\n    // 请在这里计算平均分\n    // 注意：sum是int，除以3要转为double\n    double average = \n    \n    cout << "平均分：" << average << endl;\n    return 0;\n}`
          : `public class AverageScore {\n    public static void main(String[] args) {\n        int score1 = 85, score2 = 90, score3 = 78;\n        int sum = score1 + score2 + score3;\n        // 请在这里计算平均分\n        // 注意：sum是int，除以3要转为double\n        double average = \n        \n        System.out.println("平均分：" + average);\n    }\n}`
      }
    ],
    '逻辑错误': [
      {
        title: `练习1：判断是否及格`,
        description: `请编写一个程序，定义成绩score为75，判断是否及格（>=60）。注意：判断相等用==，判断大于等于用>=，不要写成赋值=！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int score = 75;\n    // 请在这里判断是否及格\n    // 注意：用 >= ，不要用 = \n    if () {\n        cout << "及格" << endl;\n    } else {\n        cout << "不及格" << endl;\n    }\n    return 0;\n}`
          : `public class PassCheck {\n    public static void main(String[] args) {\n        int score = 75;\n        // 请在这里判断是否及格\n        // 注意：用 >= ，不要用 = \n        if () {\n            System.out.println("及格");\n        } else {\n            System.out.println("不及格");\n        }\n    }\n}`
      },
      {
        title: `练习2：判断闰年`,
        description: `请编写一个程序，判断2024年是否是闰年。闰年规则：能被4整除但不能被100整除，或者能被400整除。注意：逻辑运算符&&和||不要写错！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int year = 2024;\n    // 请在这里判断是否是闰年\n    // 规则：(year % 4 == 0 && year % 100 != 0) || year % 400 == 0\n    bool isLeap = \n    \n    if (isLeap) {\n        cout << year << "年是闰年" << endl;\n    } else {\n        cout << year << "年不是闰年" << endl;\n    }\n    return 0;\n}`
          : `public class LeapYear {\n    public static void main(String[] args) {\n        int year = 2024;\n        // 请在这里判断是否是闰年\n        // 规则：(year % 4 == 0 && year % 100 != 0) || year % 400 == 0\n        boolean isLeap = \n        \n        if (isLeap) {\n            System.out.println(year + "年是闰年");\n        } else {\n            System.out.println(year + "年不是闰年");\n        }\n    }\n}`
      },
      {
        title: `练习3：找出三个数中的最大值`,
        description: `请编写一个程序，定义三个数a、b、c，找出最大值。注意：比较用==，不要用=赋值！可以用嵌套if或者Math.max()。`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int a = 10, b = 25, c = 15;\n    int max = a;\n    // 请在这里找出最大值\n    // 注意：比较用 > ，不要用 = \n    if () max = b;\n    if () max = c;\n    \n    cout << "最大值：" << max << endl;\n    return 0;\n}`
          : `public class MaxThree {\n    public static void main(String[] args) {\n        int a = 10, b = 25, c = 15;\n        int max = a;\n        // 请在这里找出最大值\n        // 注意：比较用 > ，不要用 = \n        if () max = b;\n        if () max = c;\n        \n        System.out.println("最大值：" + max);\n    }\n}`
      }
    ],
    '死循环': [
      {
        title: `练习1：用while循环计算1到100的和`,
        description: `请编写一个程序，用while循环计算1到100的和。注意：循环体内一定要更新循环变量i，否则会死循环！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int i = 1;\n    int sum = 0;\n    while (i <= 100) {\n        sum += i;\n        // 请在这里更新循环变量i\n        // 别忘了 i++ ！\n        \n    }\n    cout << "1到100的和：" << sum << endl;\n    return 0;\n}`
          : `public class Sum100 {\n    public static void main(String[] args) {\n        int i = 1;\n        int sum = 0;\n        while (i <= 100) {\n            sum += i;\n            // 请在这里更新循环变量i\n            // 别忘了 i++ ！\n            \n        }\n        System.out.println("1到100的和：" + sum);\n    }\n}`
      },
      {
        title: `练习2：用do-while循环输出倒计时`,
        description: `请编写一个程序，用do-while循环从10倒计时到0。注意：循环体内要更新循环变量，否则会死循环！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int count = 10;\n    do {\n        cout << count << " ";
        // 请在这里更新count\n        // 别忘了 count-- ！\n        \n    } while (count >= 0);\n    cout << endl;\n    return 0;\n}`
          : `public class Countdown {\n    public static void main(String[] args) {\n        int count = 10;\n        do {\n            System.out.print(count + " ");
            // 请在这里更新count\n            // 别忘了 count-- ！\n            \n        } while (count >= 0);\n        System.out.println();\n    }\n}`
      },
      {
        title: `练习3：用for循环遍历数组`,
        description: `请编写一个程序，用for循环遍历数组并输出每个元素。注意：for循环的三个部分（初始化、条件、更新）都要写对！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int arr[] = {10, 20, 30, 40, 50};\n    int n = sizeof(arr) / sizeof(arr[0]);\n    // 请在这里用for循环遍历数组\n    // 注意三个部分：int i = 0; i < n; i++\n    for () {\n        cout << arr[i] << " ";
    }\n    cout << endl;\n    return 0;\n}`
          : `public class ArrayTraverse {\n    public static void main(String[] args) {\n        int[] arr = {10, 20, 30, 40, 50};\n        // 请在这里用for循环遍历数组\n        // 注意三个部分：int i = 0; i < arr.length; i++\n        for () {\n            System.out.print(arr[i] + " ");
        }\n        System.out.println();\n    }\n}`
      }
    ],
    '内存泄漏': [
      {
        title: `练习1：正确释放动态分配的数组`,
        description: `请编写一个程序，用new动态分配一个长度为10的int数组，赋值后输出，然后正确释放内存。注意：数组用delete[]释放，不是delete！`,
        template: isCpp
          ? `#include <iostream>\nusing namespace std;\n\nint main() {\n    int* arr = new int[10];\n    for (int i = 0; i < 10; i++) {\n        arr[i] = i * 2;\n    }\n    for (int i = 0; i < 10; i++) {\n        cout << arr[i] << " ";
    }\n    cout << endl;\n    // 请在这里释放数组内存\n    // 注意：用 delete[] arr; 不是 delete arr;\n    \n    arr = nullptr;  // 置空，避免悬空指针\n    return 0;\n}`
          : `// Java有自动垃圾回收，不需要手动释放内存\n// 这个练习演示Java中如何让对象被垃圾回收\npublic class GCDemo {\n    public static void main(String[] args) {\n        int[] arr = new int[10];\n        for (int i = 0; i < 10; i++) {\n            arr[i] = i * 2;\n        }\n        for (int i = 0; i < 10; i++) {\n            System.out.print(arr[i] + " ");\n        }\n        System.out.println();\n        // Java中不需要手动释放\n        // 让引用指向null，对象就可以被垃圾回收\n        arr = null;\n        System.gc();  // 建议JVM进行垃圾回收（不保证立即执行）\n    }\n}`
      },
      {
        title: `练习2：使用智能指针自动管理内存（C++）`,
        description: `请编写一个程序，使用std::unique_ptr管理动态分配的对象，不需要手动delete。智能指针离开作用域时会自动释放内存。`,
        template: isCpp
          ? `#include <iostream>\n#include <memory>\nusing namespace std;\n\nclass MyClass {\npublic:\n    MyClass() { cout << "构造函数" << endl; }\n    ~MyClass() { cout << "析构函数" << endl; }\n    void hello() { cout << "Hello!" << endl; }\n};\n\nint main() {\n    // 使用unique_ptr，不需要手动delete\n    unique_ptr<MyClass> ptr(new MyClass());\n    ptr->hello();\n    // 请在这里再创建一个unique_ptr管理int数组\n    // unique_ptr<int[]> arr(new int[5]);\n    \n    // 离开作用域时，ptr和arr会自动释放内存\n    return 0;\n}`
          : `// Java中所有对象都是自动管理的\n// 这个练习演示Java中对象的创建和使用\npublic class SmartPointerDemo {\n    static class MyClass {\n        MyClass() { System.out.println("构造函数"); }\n        void hello() { System.out.println("Hello!"); }\n    }\n    \n    public static void main(String[] args) {\n        MyClass obj = new MyClass();\n        obj.hello();\n        // Java不需要手动释放\n        // 当没有引用指向对象时，会被垃圾回收\n        obj = null;\n    }\n}`
      },
      {
        title: `练习3：使用vector代替动态数组`,
        description: `请编写一个程序，使用std::vector代替new[]动态数组。vector自动管理内存，不需要手动释放，而且可以动态增长。`,
        template: isCpp
          ? `#include <iostream>\n#include <vector>\nusing namespace std;\n\nint main() {\n    // 使用vector，不需要手动new和delete\n    vector<int> arr;\n    // 请在这里向vector中添加10个元素（用push_back）\n    // 然后遍历输出所有元素\n    \n    \n    // vector离开作用域时自动释放内存\n    return 0;\n}`
          : `import java.util.ArrayList;\n\npublic class VectorDemo {\n    public static void main(String[] args) {\n        // 使用ArrayList，自动管理内存\n        ArrayList<Integer> arr = new ArrayList<>();\n        // 请在这里向ArrayList中添加10个元素（用add）\n        // 然后遍历输出所有元素\n        \n        \n        // Java自动垃圾回收，不需要手动释放\n    }\n}`
      }
    ]
  }
  
  // 返回对应类型的题目，如果没有匹配的类型，返回通用题目
  return templates[category] || [
    {
      title: `练习1：${category}基础练习`,
      description: `根据你在「${category}」中犯的错误，请完成以下练习，注意避免同样的问题。`,
      template: isCpp ? `// 请在这里完成练习\n// 考察点：${category}` : `// 请在这里完成练习\n// 考察点：${category}`
    },
    {
      title: `练习2：${category}进阶练习`,
      description: `进一步练习${category}相关的知识点，确保完全掌握。`,
      template: isCpp ? `// 请在这里完成练习\n// 考察点：${category}` : `// 请在这里完成练习\n// 考察点：${category}`
    },
    {
      title: `练习3：${category}综合练习`,
      description: `综合运用所学知识，完成这个练习。`,
      template: isCpp ? `// 请在这里完成练习\n// 考察点：${category}` : `// 请在这里完成练习\n// 考察点：${category}`
    }
  ]
}

// 翻页：上一题
function prevSimilar() {
  if (currentSimilarIndex.value > 0) {
    currentSimilarIndex.value--
    similarGradingResult.value = null
    nextTick(() => {
      initSimilarEditor()
    })
  }
}

// 翻页：下一题
function nextSimilar() {
  if (currentSimilarIndex.value < similarQuestions.value.length - 1) {
    currentSimilarIndex.value++
    similarGradingResult.value = null
    nextTick(() => {
      initSimilarEditor()
    })
  }
}

// 辅助函数
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
  grid-template-columns: 1fr;
  gap: 12px;
  margin-bottom: 14px;
}
.line-info {
  margin-top: 8px;
  font-size: 11px;
  color: #c4b5fd;
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

/* 详情弹窗样式 */
.detail-dialog .el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
}

/* 语言标签 */
.lang-tag {
  margin-left: 4px;
}

/* 翻页指示器 */
.similar-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.pagination-info {
  font-size: 13px;
  color: #a1a1aa;
}
.pagination-btns {
  display: flex;
  gap: 8px;
}

/* 详情弹窗段落 */
.detail-paragraph p {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.8;
  color: #a1a1aa;
}
.detail-paragraph p:last-child {
  margin-bottom: 0;
}

/* 知识点框 */
.knowledge-box {
  background: rgba(196, 181, 253, 0.04);
  border-left: 3px solid #c4b5fd;
  padding: 12px 16px;
  border-radius: 0 8px 8px 0;
}
.knowledge-box p {
  color: #c4b5fd;
}

/* 完整代码和示例代码 */
.full-code pre,
.example-code pre {
  max-height: 400px;
  overflow-y: auto;
}
.full-code code,
.example-code code {
  font-size: 12px;
  line-height: 1.7;
}

/* 详情弹窗代码块 */
.detail-code {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  overflow: hidden;
}
.detail-code pre {
  padding: 14px;
  margin: 0;
  font-family: 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #a1a1aa;
  white-space: pre-wrap;
  overflow-x: auto;
}

/* ========== AI类似题弹窗：左右布局 ========== */
.similar-dialog :deep(.el-dialog__body) {
  padding: 16px 20px;
}
.similar-loading {
  text-align: center;
  padding: 60px 20px;
  color: #71717a;
}
.similar-loading p {
  margin-top: 10px;
  font-size: 13px;
}

/* 左右主布局 */
.similar-main {
  display: flex;
  gap: 20px;
  margin-top: 12px;
}
.similar-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.similar-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 左边题目信息 */
.similar-label {
  font-size: 12px;
  color: #c4b5fd;
  font-weight: 500;
}
.similar-title {
  font-size: 16px;
  color: #f4f4f5;
  margin: 0;
  font-weight: 600;
}
.similar-desc {
  font-size: 13px;
  color: #a1a1aa;
  line-height: 1.7;
  margin: 0;
}
.similar-hint {
  font-size: 12px;
  color: #71717a;
  padding: 6px 10px;
  background: rgba(196, 181, 253, 0.06);
  border-radius: 6px;
  display: inline-block;
  align-self: flex-start;
}

/* 代码模板参考 */
.similar-template {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  overflow: hidden;
}
.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 12px;
  color: #a1a1aa;
}
.template-code {
  padding: 12px;
  margin: 0;
  font-family: 'Consolas', monospace;
  font-size: 11px;
  line-height: 1.6;
  color: #71717a;
  white-space: pre-wrap;
  overflow-x: auto;
  max-height: 200px;
  overflow-y: auto;
}

/* 右边编辑器 */
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-bottom: none;
  border-radius: 10px 10px 0 0;
}
.editor-filename {
  font-size: 12px;
  color: #a1a1aa;
  font-family: 'Consolas', monospace;
}
.similar-code-editor {
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-top: none;
  border-radius: 0 0 10px 10px;
  overflow: hidden;
}

/* 提交按钮 */
.submit-area {
  display: flex;
  justify-content: flex-end;
}
.submit-btn {
  min-width: 140px;
}

/* 批改结果 */
.grading-result {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  padding: 14px;
  max-height: 300px;
  overflow-y: auto;
}
.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.result-score {
  font-size: 24px;
  font-weight: 700;
  color: #c4b5fd;
}
.result-feedback {
  font-size: 12px;
  color: #a1a1aa;
  line-height: 1.5;
  flex: 1;
}
.result-issues {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.issue-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.issue-content {
  flex: 1;
  min-width: 0;
}
.issue-category {
  font-size: 13px;
  color: #e4e4e7;
  font-weight: 500;
  margin-bottom: 4px;
}
.issue-message {
  font-size: 12px;
  color: #a1a1aa;
  line-height: 1.6;
  margin-bottom: 4px;
}
.issue-suggestion {
  font-size: 12px;
  color: #86efac;
  line-height: 1.6;
  background: rgba(52, 211, 153, 0.06);
  padding: 6px 10px;
  border-radius: 6px;
}
</style>
