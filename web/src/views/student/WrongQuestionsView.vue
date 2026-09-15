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
            <el-tag v-if="q.language" type="info" size="small" effect="plain" class="lang-tag">
              {{ q.language }}
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
    <el-dialog v-model="detailDialogVisible" title="错题详情" width="720px" top="6vh" :z-index="3000" class="detail-dialog">
      <div v-if="currentQuestion" class="detail-content">
        <div class="detail-header">
          <el-tag :type="severityTagType(currentQuestion.severity)" size="small" effect="dark">
            {{ severityText(currentQuestion.severity) }}
          </el-tag>
          <span class="detail-category">{{ currentQuestion.category }}</span>
          <el-tag v-if="currentQuestion.language" type="info" size="small" effect="plain">
            {{ currentQuestion.language }}
          </el-tag>
          <span class="detail-time">{{ formatTime(currentQuestion.createdAt) }}</span>
        </div>

        <div class="detail-section">
          <div class="detail-label">📝 问题描述</div>
          <p class="detail-text">{{ currentQuestion.message || '暂无描述' }}</p>
        </div>

        <div v-if="currentQuestion.suggestion" class="detail-section">
          <div class="detail-label">💡 修改建议</div>
          <p class="detail-text suggestion">{{ currentQuestion.suggestion }}</p>
        </div>

        <div class="detail-section">
          <div class="detail-label">📄 完整源代码</div>
          <div class="detail-code full-code">
            <pre><code>{{ currentQuestion.fullCode || currentQuestion.codeSnippet || '// 暂无代码' }}</code></pre>
          </div>
        </div>

        <div v-if="currentQuestion.detailedExplanation" class="detail-section">
          <div class="detail-label">🔍 详细解释</div>
          <div class="detail-text detail-paragraph">
            <p v-for="(para, idx) in currentQuestion.detailedExplanation.split('\n\n')" :key="idx">{{ para }}</p>
          </div>
        </div>

        <div v-if="currentQuestion.knowledgePoint" class="detail-section">
          <div class="detail-label">📚 相关知识点</div>
          <div class="detail-text detail-paragraph knowledge-box">
            <p v-for="(para, idx) in currentQuestion.knowledgePoint.split('\n\n')" :key="idx">{{ para }}</p>
          </div>
        </div>

        <div v-if="currentQuestion.example" class="detail-section">
          <div class="detail-label">✏️ 正确写法示例</div>
          <div class="detail-code example-code">
            <pre><code>{{ currentQuestion.example }}</code></pre>
          </div>
        </div>
      </div>
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
              <div class="similar-label">AI 根据错题「{{ currentQuestion.category || '代码问题' }}」生成（{{ currentQuestion.language || '通用' }}）</div>
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
                <span class="editor-filename">practice.{{ currentQuestion.language === 'C++' ? 'cpp' : 'java' }}</span>
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
                  <span class="result-feedback">{{ similarGradingResult.overallFeedback }}</span>
                </div>
                <div class="result-issues">
                  <div class="issue-item" v-for="(issue, idx) in similarGradingResult.issues" :key="idx">
                    <el-tag :type="issue.severity === 'ERROR' ? 'danger' : issue.severity === 'WARNING' ? 'warning' : 'info'" size="small">
                      {{ issue.severity === 'ERROR' ? '严重' : issue.severity === 'WARNING' ? '警告' : '建议' }}
                    </el-tag>
                    <div class="issue-content">
                      <div class="issue-category">{{ issue.category }} <span v-if="issue.lineNumber">（第{{ issue.lineNumber }}行）</span></div>
                      <div class="issue-message">{{ issue.message }}</div>
                      <div class="issue-suggestion" v-if="issue.suggestion">💡 {{ issue.suggestion }}</div>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Notebook, Loading, Refresh, View } from '@element-plus/icons-vue'
import { getErrorListApi } from '../../api/review'
import { uploadSubmissionApi, gradeSubmissionApi } from '../../api/submission'

// ========== CodeMirror 导入 ==========
import { EditorState } from '@codemirror/state'
import { EditorView, basicSetup } from 'codemirror'
import { java } from '@codemirror/lang-java'
import { cpp } from '@codemirror/lang-cpp'
import { oneDark } from '@codemirror/theme-one-dark'

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
const similarQuestions = ref([])
const currentSimilarIndex = ref(0)

// 类似题编辑器相关
const similarEditorRef = ref(null)
let similarEditorView = null
const similarSubmitting = ref(false)
const similarGradingResult = ref(null)

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

// 临时模拟错题数据（等后端接口完善后删除）
const mockWrongQuestions = [
  {
    errorId: 1001,
    category: '数组越界',
    severity: 'ERROR',
    language: 'C++',
    message: '数组长度为5，下标范围是0-4，循环条件i<=5会访问arr[5]，导致运行时越界异常',
    suggestion: '将循环条件改为 i < 5，即 for (int i = 0; i < 5; i++)',
    codeSnippet: 'for (int i = 0; i <= 5; i++) {\n    cout << arr[i] << endl;\n}',
    lineNumber: 6,
    createdAt: '2026-09-10T14:30:00',
    fullCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int arr[5] = {1, 2, 3, 4, 5};\n    for (int i = 0; i <= 5; i++) {\n        cout << arr[i] << endl;\n    }\n    return 0;\n}',
    detailedExplanation: '数组越界是C/C++中最常见的运行时错误之一。在这个程序中，你定义了一个长度为5的数组arr，它的合法下标是0、1、2、3、4。但是你的for循环条件写的是i <= 5，当i等于5时，程序会尝试访问arr[5]，而这个位置并不属于这个数组，属于未定义行为。\n\n在实际运行中，这可能导致三种结果：1）程序崩溃（段错误）；2）读取到内存中的随机值；3）修改了其他变量的值，导致程序逻辑异常。C/C++不会自动检查数组下标是否越界，这需要程序员自己保证。',
    knowledgePoint: '数组下标从0开始：C/C++中数组的第一个元素下标是0，最后一个元素下标是长度-1。例如长度为n的数组，合法下标范围是0到n-1。\n\n循环条件判断：遍历数组时，循环条件应该是i < 数组长度，而不是i <= 数组长度。\n\n越界的危害：数组越界可能导致程序崩溃、数据损坏、安全漏洞（缓冲区溢出攻击）。',
    example: '正确写法：\nint arr[5] = {1, 2, 3, 4, 5};\nfor (int i = 0; i < 5; i++) {  // 注意是 < 不是 <=\n    cout << arr[i] << endl;\n}\n\n另一种安全写法（C++11及以上）：\nfor (int x : arr) {  // 范围for循环，自动遍历每个元素\n    cout << x << endl;\n}',
  },
  {
    errorId: 1002,
    category: '变量未定义',
    severity: 'ERROR',
    language: 'C++',
    message: '变量sum未声明就直接使用，会导致编译失败',
    suggestion: '在使用前声明变量：int sum = 0;',
    codeSnippet: 'int a = 10, b = 20;\nsum = a + b;\ncout << sum << endl;',
    lineNumber: 3,
    createdAt: '2026-09-10T10:15:00',
    fullCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int a = 10, b = 20;\n    sum = a + b;\n    cout << sum << endl;\n    return 0;\n}',
    detailedExplanation: '在C/C++中，所有变量在使用之前必须先声明。声明变量的作用是告诉编译器这个变量的名字、类型，编译器会为它分配内存空间。你在第3行直接使用了sum变量，但前面没有声明过它，所以编译器会报错"sum was not declared in this scope"。\n\n这个错误属于编译时错误，程序根本无法运行，必须修复后才能编译通过。常见的原因包括：忘记声明变量、变量名拼写错误、变量作用域不对（在函数内声明的变量在函数外使用）。',
    knowledgePoint: '变量声明：C++中声明变量的格式是 类型 变量名; 例如 int sum; 可以在声明时同时初始化：int sum = 0;\n\n变量作用域：变量只在它声明的代码块（大括号{}）内有效。在函数内声明的变量叫局部变量，只能在函数内使用。\n\n初始化的重要性：声明变量时最好同时初始化，否则变量的值是不确定的（内存中的随机值）。',
    example: '正确写法：\nint main() {\n    int a = 10, b = 20;\n    int sum = a + b;  // 先声明再使用\n    cout << sum << endl;\n    return 0;\n}\n\n如果需要在多个函数中使用，可以声明为全局变量：\nint sum;  // 全局变量，所有函数都能访问\nint main() {\n    sum = 10 + 20;\n    return 0;\n}',
  },
  {
    errorId: 1003,
    category: '缺少分号',
    severity: 'WARNING',
    language: 'C++',
    message: 'cout输出语句末尾缺少分号，C++语句必须以分号结尾',
    suggestion: '在 cout << "Hello" << endl 末尾添加分号 ;',
    codeSnippet: 'cout << "Hello EduCode" << endl\nreturn 0;',
    lineNumber: 5,
    createdAt: '2026-09-09T16:45:00',
    fullCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << "Hello EduCode" << endl\n    return 0;\n}',
    detailedExplanation: '在C++中，分号是语句的结束标志。每一条可执行语句都必须以分号结尾，告诉编译器这条语句到这里结束了。你在第5行的cout语句后面没有写分号，编译器会把第5行和第6行（return 0;）当成一条语句来解析，导致语法错误。\n\n这个错误虽然简单，但是新手最常犯的错误之一。缺少分号会导致编译失败，而且编译器的报错信息可能不太直观（可能会指向缺少分号的下一行），需要你自己往上找。',
    knowledgePoint: '分号的作用：C++中分号表示一条语句的结束。一条语句可以写在多行，只要最后有分号就行；多条语句也可以写在一行，用分号分隔。\n\n不需要分号的地方：预处理指令（#include、#define）、函数定义的大括号后面、类定义的大括号后面、if/for/while的条件后面（如果后面直接跟大括号）。\n\n常见遗漏位置：cout/cin语句、赋值语句、函数调用语句、return语句。',
    example: '正确写法：\nint main() {\n    cout << "Hello EduCode" << endl;  // 末尾有分号\n    return 0;  // 末尾有分号\n}\n\n一条语句写多行（合法）：\ncout << "Hello" \n     << " World" \n     << endl;  // 最后才有分号\n\n多条语句写一行（合法但不推荐）：\nint a = 1; int b = 2; cout << a + b << endl;',
  },
  {
    errorId: 1004,
    category: '空指针异常',
    severity: 'ERROR',
    language: 'Java',
    message: '对象引用为null时调用方法，会抛出NullPointerException',
    suggestion: '调用方法前先判断对象是否为null，或确保对象已正确初始化',
    codeSnippet: 'String str = null;\nif (str.length() > 0) {\n    System.out.println(str);\n}',
    lineNumber: 2,
    createdAt: '2026-09-09T11:20:00',
    fullCode: 'public class NullPointerExample {\n    public static void main(String[] args) {\n        String str = null;\n        if (str.length() > 0) {\n            System.out.println(str);\n        }\n    }\n}',
    detailedExplanation: '空指针异常（NullPointerException，简称NPE）是Java中最常见的运行时异常之一。当一个引用变量的值是null（表示它不指向任何对象），而你尝试通过它调用方法或访问属性时，就会抛出这个异常。\n\n在这个例子中，你把str赋值为null，然后在第2行调用str.length()。因为str没有指向任何String对象，所以无法调用length()方法，JVM会抛出NullPointerException，程序崩溃。\n\n这个错误在实际开发中非常常见，特别是在处理方法返回值、从集合中取元素、接收外部传入的参数时。Java 8及以上版本可以使用Optional类来更优雅地处理可能为null的情况。',
    knowledgePoint: 'null的含义：null表示引用变量不指向任何对象。它不是一个对象，所以不能调用任何方法。\n\n引用类型 vs 基本类型：基本类型（int、double、boolean等）不能为null，它们有默认值（0、0.0、false）；引用类型（String、数组、自定义类等）可以为null。\n\n防御式编程：在不确定对象是否为null时，调用方法前先判断 if (obj != null)。\n\nJava 8+ Optional：使用Optional可以更优雅地处理可能为null的值，避免显式的null判断。',
    example: '正确写法1 - 先判断null：\nString str = getStringFromSomewhere();\nif (str != null && str.length() > 0) {\n    System.out.println(str);\n}\n\n正确写法2 - 使用Optional（Java 8+）：\nOptional<String> strOpt = Optional.ofNullable(getStringFromSomewhere());\nstrOpt.filter(s -> s.length() > 0)\n      .ifPresent(System.out::println);\n\n正确写法3 - 提供默认值：\nString str = getStringFromSomewhere();\nString safeStr = (str != null) ? str : "";\nif (safeStr.length() > 0) {\n    System.out.println(safeStr);\n}',
  },
  {
    errorId: 1005,
    category: '类型不匹配',
    severity: 'WARNING',
    language: 'Java',
    message: '将double类型的值赋给int变量，会丢失小数部分精度',
    suggestion: '如果需要保留小数，变量类型改为double；如果只要整数，建议显式强制转换 (int)',
    codeSnippet: 'double price = 19.99;\nint intPrice = price;\nSystem.out.println(intPrice);',
    lineNumber: 2,
    createdAt: '2026-09-08T15:30:00',
    fullCode: 'public class TypeMismatchExample {\n    public static void main(String[] args) {\n        double price = 19.99;\n        int intPrice = price;\n        System.out.println(intPrice);\n    }\n}',
    detailedExplanation: '在Java中，将一个取值范围更大、精度更高的类型（double）赋值给取值范围更小、精度更低的类型（int）时，会发生隐式类型转换，小数部分会被直接截断丢弃。在这个例子中，19.99赋值给int变量后，会变成19，丢失了0.99的小数部分。\n\n需要注意的是，这种隐式转换在Java中其实是编译错误的（可能损失精度），需要显式强制转换。但在C/C++中，这种转换是允许的，只会给出警告。不管是哪种语言，这种转换都可能导致数据丢失，需要谨慎使用。\n\n如果你确实需要取整，应该使用Math.round()（四舍五入）、Math.floor()（向下取整）、Math.ceil()（向上取整）等方法，而不是直接强制转换。',
    knowledgePoint: '基本数据类型：Java中有8种基本类型，按精度从低到高：byte < short < int < long < float < double，还有char和boolean。\n\n自动类型转换（隐式）：从低精度到高精度可以自动转换，不需要强制转换，例如 int -> double。\n\n强制类型转换（显式）：从高精度到低精度需要强制转换，格式是 (目标类型)值，例如 (int)3.14。强制转换可能丢失精度或溢出。\n\n取整方法：Math.round()四舍五入、Math.floor()向下取整、Math.ceil()向上取整，这些方法返回的是long或double类型。',
    example: '正确写法1 - 保留小数，用double：\ndouble price = 19.99;\ndouble doublePrice = price;  // 类型相同，直接赋值\nSystem.out.println(doublePrice);  // 输出19.99\n\n正确写法2 - 四舍五入取整：\ndouble price = 19.99;\nint intPrice = (int) Math.round(price);  // 四舍五入后强制转换\nSystem.out.println(intPrice);  // 输出20\n\n正确写法3 - 显式强制转换（截断小数）：\ndouble price = 19.99;\nint intPrice = (int) price;  // 显式强制转换，截断小数\nSystem.out.println(intPrice);  // 输出19\n\n正确写法4 - 向上取整（比如算需要多少个盒子）：\ndouble items = 25.0;\ndouble capacity = 10.0;\nint boxes = (int) Math.ceil(items / capacity);  // 向上取整\nSystem.out.println(boxes);  // 输出3',
  },
  {
    errorId: 1006,
    category: '逻辑错误',
    severity: 'WARNING',
    language: 'Java',
    message: '判断条件使用了赋值运算符=而不是比较运算符==，会导致条件永远为真',
    suggestion: '将 if (flag = true) 改为 if (flag == true)，或者直接写 if (flag)',
    codeSnippet: 'boolean flag = false;\nif (flag = true) {\n    System.out.println("always true");\n}',
    lineNumber: 2,
    createdAt: '2026-09-08T09:10:00',
    fullCode: 'public class LogicErrorExample {\n    public static void main(String[] args) {\n        boolean flag = false;\n        if (flag = true) {\n            System.out.println("always true");\n        }\n    }\n}',
    detailedExplanation: '这是一个非常经典的逻辑错误。在if条件中，你写的是flag = true，这是一个赋值语句，作用是把true赋值给flag，然后整个表达式的值就是true。所以不管flag原来是什么值，这个if条件永远为真，里面的代码一定会执行。\n\n你本来想写的应该是flag == true，这是一个比较表达式，判断flag是否等于true，结果可能是true也可能是false。\n\n在Java中，因为if条件必须是boolean类型，所以如果flag是boolean类型，flag = true是合法的（因为赋值表达式的值是boolean），但这是逻辑错误。如果flag是int类型，if (flag = 5)在Java中是编译错误（因为int不能转成boolean），但在C/C++中是合法的，而且也是常见的bug。',
    knowledgePoint: '赋值运算符=：作用是把右边的值赋给左边的变量，整个表达式的值就是赋的值。例如 a = 5 的结果是5。\n\n比较运算符==：作用是判断左右两边是否相等，结果是boolean类型（true或false）。例如 a == 5 的结果是true或false。\n\nboolean变量的判断：如果变量本身就是boolean类型，不需要写 == true 或 == false，直接写 if (flag) 或 if (!flag) 更简洁，也能避免把==写成=的错误。\n\n常量放前面的习惯：有些程序员习惯把常量放前面，例如 if (true == flag)，这样如果不小心写成 if (true = flag) 会编译错误（因为不能给常量赋值），从而提前发现错误。',
    example: '正确写法1 - 直接判断boolean变量（推荐）：\nboolean flag = false;\nif (flag) {  // 直接判断，不需要==true\n    System.out.println("flag is true");\n}\n\n正确写法2 - 使用==比较：\nboolean flag = false;\nif (flag == true) {  // 注意是两个等号\n    System.out.println("flag is true");\n}\n\n正确写法3 - 判断false：\nboolean flag = false;\nif (!flag) {  // !表示取反，flag为false时!flag为true\n    System.out.println("flag is false");\n}\n\n正确写法4 - 数字比较（注意不要写成=）：\nint score = 85;\nif (score >= 60) {  // >=是大于等于\n    System.out.println("及格");\n}\nif (score != 100) {  // !=是不等于\n    System.out.println("不是满分");\n}',
  },
  {
    errorId: 1007,
    category: '死循环',
    severity: 'ERROR',
    language: 'Java',
    message: 'while循环内没有更新循环变量i，导致条件永远为真，程序陷入死循环',
    suggestion: '在循环体内添加 i++ 或 i = i + 1，确保循环能正常退出',
    codeSnippet: 'int i = 0;\nwhile (i < 10) {\n    System.out.println(i);\n    // 缺少 i++\n}',
    lineNumber: 2,
    createdAt: '2026-09-07T20:00:00',
    fullCode: 'public class InfiniteLoopExample {\n    public static void main(String[] args) {\n        int i = 0;\n        while (i < 10) {\n            System.out.println(i);\n            // 缺少 i++\n        }\n    }\n}',
    detailedExplanation: '死循环是指循环条件永远为真，循环永远不会结束的情况。在这个例子中，你在while循环外面把i初始化为0，循环条件是i < 10。但是在循环体内，你只打印了i的值，没有修改i的值，所以i永远是0，条件i < 10永远为真，循环会一直执行下去，不停地打印0，直到你手动终止程序（或者内存耗尽）。\n\n死循环是编程中常见的错误，可能导致程序卡死、CPU占用100%、系统无响应等问题。在实际开发中，写循环时一定要注意：1）循环变量是否正确初始化；2）循环条件是否正确；3）循环体内是否更新了循环变量；4）是否有其他退出循环的方式（break、return等）。\n\n有些死循环是故意的，比如服务器程序需要一直运行等待请求，这时候用while(true)是正常的，但循环体内一定要有break或return的退出路径。',
    knowledgePoint: '循环三要素：1）初始化：循环开始前设置初始状态；2）条件：判断是否继续循环；3）更新：每次循环后修改状态，使条件最终能变为假。\n\nwhile循环：先判断条件，条件为真才执行循环体。如果一开始条件就为假，循环体一次都不会执行。\n\ndo-while循环：先执行一次循环体，再判断条件。循环体至少执行一次。\n\nfor循环：把初始化、条件、更新写在一起，更紧凑，适合已知循环次数的情况。\n\nbreak和continue：break立即跳出整个循环；continue跳过本次循环，直接进入下一次循环判断。',
    example: '正确写法1 - while循环中更新变量：\nint i = 0;\nwhile (i < 10) {\n    System.out.println(i);\n    i++;  // 更新循环变量\n}\n\n正确写法2 - 用for循环（更推荐，三要素在一起）：\nfor (int i = 0; i < 10; i++) {  // 初始化;条件;更新\n    System.out.println(i);\n}\n\n正确写法3 - 用break退出循环：\nint i = 0;\nwhile (true) {  // 条件永远为真\n    System.out.println(i);\n    i++;\n    if (i >= 10) {\n        break;  // 满足条件时跳出循环\n    }\n}\n\n正确写法4 - 遍历数组（用增强for循环，不会死循环）：\nint[] arr = {1, 2, 3, 4, 5};\nfor (int x : arr) {  // 自动遍历每个元素，不需要管理下标\n    System.out.println(x);\n}',
  },
  {
    errorId: 1008,
    category: '内存泄漏',
    severity: 'INFO',
    language: 'C++',
    message: 'new分配的内存没有用delete释放，会导致内存泄漏',
    suggestion: '在不再使用指针时，用 delete ptr; 释放内存，并将指针置为nullptr',
    codeSnippet: 'int* ptr = new int[100];\nfor (int i = 0; i < 100; i++) {\n    ptr[i] = i;\n}\n// 缺少 delete[] ptr;',
    lineNumber: 1,
    createdAt: '2026-09-07T14:25:00',
    fullCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int* ptr = new int[100];\n    for (int i = 0; i < 100; i++) {\n        ptr[i] = i;\n    }\n    // 缺少 delete[] ptr;\n    return 0;\n}',
    detailedExplanation: '内存泄漏是指程序动态分配了内存（用new或malloc），但在使用完后没有释放（用delete或free），导致这块内存一直被占用，无法被其他程序使用。在这个例子中，你用new分配了100个int的数组（大约400字节），使用完后没有用delete[]释放，虽然程序结束后操作系统会回收所有内存，但如果这是在一个长时间运行的程序（比如服务器）中，并且反复执行这段代码，内存泄漏会越来越严重，最终导致内存耗尽，程序崩溃。\n\nC++不像Java有自动垃圾回收机制，需要程序员手动管理内存。这是C++的灵活性所在，也是容易出错的地方。现代C++推荐使用智能指针（std::unique_ptr、std::shared_ptr）来自动管理内存，避免手动new/delete导致的内存泄漏。\n\n需要注意的是，用new[]分配的数组必须用delete[]释放，用new分配的单个对象用delete释放，不能混用。',
    knowledgePoint: '动态内存分配：C++中用new在堆上分配内存，用delete释放。new返回的是指针，需要手动管理。\n\n栈 vs 堆：栈上的变量（局部变量）在函数结束时自动释放；堆上的变量（new出来的）需要手动释放，否则一直存在。\n\nnew/delete配对：new对应delete，new[]对应delete[]，不能混用。\n\n内存泄漏的危害：长时间运行的程序中，内存泄漏会导致可用内存越来越少，最终程序崩溃。\n\n智能指针：C++11引入了std::unique_ptr和std::shared_ptr，它们会自动释放内存，不需要手动delete，是现代C++推荐的内存管理方式。',
    example: '正确写法1 - 手动释放（传统方式）：\nint* ptr = new int[100];\nfor (int i = 0; i < 100; i++) {\n    ptr[i] = i;\n}\n// 使用完后释放\ndelete[] ptr;  // 注意是delete[]，因为是数组\nptr = nullptr;  // 置空，避免悬空指针\n\n正确写法2 - 使用智能指针（推荐，C++11+）：\n#include <memory>\nstd::unique_ptr<int[]> ptr(new int[100]);  // unique_ptr自动管理\nfor (int i = 0; i < 100; i++) {\n    ptr[i] = i;\n}\n// 不需要手动delete，ptr离开作用域时自动释放\n\n正确写法3 - 使用vector（更推荐，不需要手动管理内存）：\n#include <vector>\nstd::vector<int> arr(100);  // vector自动管理内存\nfor (int i = 0; i < 100; i++) {\n    arr[i] = i;\n}\n// 不需要手动释放，vector离开作用域时自动释放\n\n正确写法4 - 单个对象的new/delete：\nint* p = new int(42);  // 分配单个int，初始值42\ncout << *p << endl;\ndelete p;  // 释放单个对象，用delete不是delete[]\np = nullptr;',
  },
]

// 加载错题列表
async function loadErrors() {
  loading.value = true
  try {
    const data = await getErrorListApi()
    // 适配后端返回的数据格式
    const list = Array.isArray(data) ? data : (data.records || data.list || data.content || [])
    if (list.length > 0) {
      wrongQuestions.value = list.map(item => ({
        ...item,
        mastered: masteredIds.value.has(item.errorId),
        generating: false,
      }))
    } else {
      // 后端返回空时使用临时模拟数据
      wrongQuestions.value = mockWrongQuestions.map(item => ({
        ...item,
        mastered: masteredIds.value.has(item.errorId),
        generating: false,
      }))
    }
  } catch (error) {
    console.error('加载错题失败:', error)
    // 接口失败时使用临时模拟数据
    wrongQuestions.value = mockWrongQuestions.map(item => ({
      ...item,
      mastered: masteredIds.value.has(item.errorId),
      generating: false,
    }))
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

// AI生成类似题（根据错题语言和类型生成3道题）
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
    similarQuestions.value = generateSimilarQuestions(q.category, q.language)
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
  const isCpp = currentQuestion.value?.language === 'C++'
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

// 提交并批改
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
    const isCpp = currentQuestion.value?.language === 'C++'
    const filename = `practice.${isCpp ? 'cpp' : 'java'}`
    const blob = new Blob([code], { type: 'text/plain' })
    const file = new File([blob], filename, { type: 'text/plain' })
    
    // 提交
    const uploadRes = await uploadSubmissionApi(file)
    const submissionId = uploadRes?.submissionId || uploadRes?.id || uploadRes?.data?.submissionId
    
    if (!submissionId) {
      ElMessage.error({ message: '提交失败，请重试', zIndex: 9999 })
      return
    }
    
    // 批改
    const gradeRes = await gradeSubmissionApi(submissionId)
    similarGradingResult.value = gradeRes
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

// 根据错误类型和语言生成类似题目
function generateSimilarQuestions(category, language) {
  const isCpp = language === 'C++'
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
