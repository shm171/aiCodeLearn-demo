<!-- 首页 HomeView.vue - 高级黑灰玻璃感 -->
<template>
  <div class="home">
    <!-- ===== 1. Hero 区域 ===== -->
    <section class="hero">
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          AI 编程学习智能体
        </div>
        <h1 class="hero-title">
          让每一行代码<br />
          <span class="accent-text">都有智能反馈</span>
        </h1>
        <p class="hero-desc">
          上传代码，AI 智能体自动批改、归档错题、生成学习报告，<br />
          形成完整的编程学习闭环
        </p>
        <div class="hero-btns">
          <button class="btn-primary" @click="goToSubmit" v-if="userStore.isLoggedIn">
            立即提交作业
          </button>
          <button class="btn-primary" @click="router.push('/login')" v-else>
            开始使用
          </button>
        </div>
      </div>
      <div class="hero-visual">
        <div class="code-compare">
          <!-- 左边：你的代码（有错误） -->
          <div class="code-pane">
            <div class="code-pane-header error-header">
              <el-icon :size="14"><Warning /></el-icon>
              <span>你的代码</span>
            </div>
            <div class="code-pane-body">
              <div class="code-line"><span class="ln">1</span><span class="kw">#include</span> <span class="str">&lt;iostream&gt;</span></div>
              <div class="code-line"><span class="ln">2</span><span class="kw">using namespace</span> std;</div>
              <div class="code-line"><span class="ln">3</span>&nbsp;</div>
              <div class="code-line"><span class="ln">4</span><span class="kw">int</span> <span class="fn">main</span>() {</div>
              <div class="code-line"><span class="ln">5</span>&nbsp;&nbsp;<span class="kw">int</span> arr[<span class="num">5</span>];</div>
              <div class="code-line error-line"><span class="ln">6</span>&nbsp;&nbsp;arr[<span class="num">5</span>] = <span class="num">10</span>;</div>
              <div class="code-line"><span class="ln">7</span>&nbsp;&nbsp;<span class="kw">return</span> <span class="num">0</span>;</div>
              <div class="code-line"><span class="ln">8</span>}</div>
            </div>
          </div>

          <!-- 中间：AI批改动画 -->
          <div class="ai-bridge">
            <div class="ai-icon-wrap">
              <el-icon :size="28" class="ai-icon"><MagicStick /></el-icon>
            </div>
            <div class="ai-arrow">
              <el-icon :size="20"><ArrowRight /></el-icon>
            </div>
            <span class="ai-bridge-text">AI 智能批改</span>
          </div>

          <!-- 右边：AI批改后（正确代码） -->
          <div class="code-pane">
            <div class="code-pane-header fixed-header">
              <el-icon :size="14"><CircleCheck /></el-icon>
              <span>AI 批改后</span>
            </div>
            <div class="code-pane-body">
              <div class="code-line"><span class="ln">1</span><span class="kw">#include</span> <span class="str">&lt;iostream&gt;</span></div>
              <div class="code-line"><span class="ln">2</span><span class="kw">using namespace</span> std;</div>
              <div class="code-line"><span class="ln">3</span>&nbsp;</div>
              <div class="code-line"><span class="ln">4</span><span class="kw">int</span> <span class="fn">main</span>() {</div>
              <div class="code-line"><span class="ln">5</span>&nbsp;&nbsp;<span class="kw">int</span> arr[<span class="num">5</span>];</div>
              <div class="code-line fixed-line"><span class="ln">6</span>&nbsp;&nbsp;arr[<span class="num">4</span>] = <span class="num">10</span>;</div>
              <div class="code-line"><span class="ln">7</span>&nbsp;&nbsp;<span class="kw">return</span> <span class="num">0</span>;</div>
              <div class="code-line"><span class="ln">8</span>}</div>
            </div>
          </div>
        </div>

        <!-- AI批改建议 -->
        <div class="ai-suggestion">
          <div class="ai-dot"></div>
          <div class="ai-text">
            <span class="ai-label">AI 检测</span>
            第6行数组越界：长度为5的数组下标范围是 0-4，arr[5] 越界。已自动修正为 arr[4]
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 2. 数据概览（登录后显示） ===== -->
    <section class="stats-section" v-if="userStore.isLoggedIn">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bg, color: stat.color }">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-meta">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <!-- ===== 3. 最近错题 ===== -->
    <section class="section" v-if="userStore.isLoggedIn">
      <div class="section-header">
        <div>
          <h2 class="section-title">最近错题</h2>
          <p class="section-sub">来自最近作业批改的错题归档</p>
        </div>
        <button class="btn-link" @click="router.push('/submissions')">查看全部</button>
      </div>
      <div class="glass-card">
        <el-table :data="recentSubmissions" style="width: 100%">
          <el-table-column prop="id" label="#" width="60" />
          <el-table-column prop="title" label="错题类型" />
          <el-table-column prop="language" label="分类" width="110">
            <template #default="{ row }">
              <span class="lang-tag">{{ row.language }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="出错行号" width="100">
            <template #default="{ row }">
              <span class="score">{{ row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span class="status-tag" :class="row.status === '已掌握' ? 'done' : 'pending'">{{ row.status }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="归档时间" width="160" />
        </el-table>
      </div>
    </section>

    <!-- ===== AI 助手弹窗 ===== -->
    <el-dialog v-model="aiAssistantVisible" title="AI 学习助手" width="480px" class="ai-dialog">
      <div class="ai-chat">
        <div class="ai-message ai-bot">
          <div class="ai-avatar">AI</div>
          <div class="ai-bubble">你好！我是你的编程学习助手，有什么问题可以问我，比如"数组越界怎么解决"、"怎么提高代码效率"等。</div>
        </div>
        <div class="ai-message ai-bot" v-for="(msg, i) in aiMessages" :key="i">
          <div class="ai-avatar">AI</div>
          <div class="ai-bubble">{{ msg }}</div>
        </div>
      </div>
      <div class="ai-input-area">
        <el-input v-model="aiInput" placeholder="输入你的问题..." @keyup.enter="sendAIMessage" />
        <button class="btn-send" @click="sendAIMessage">
          <el-icon :size="16"><Promotion /></el-icon>
        </button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getReportApi, getErrorListApi } from '../api/review'
import { ArrowRight, Promotion, DocumentChecked, TrendCharts, Warning, Notebook, MagicStick, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const aiAssistantVisible = ref(false)
const aiInput = ref('')
const aiMessages = ref([])

// 数据概览：登录后从学习报告接口拉取真实数据
const stats = ref([
  { label: '提交次数', value: 0, icon: DocumentChecked, bg: 'rgba(96, 165, 250, 0.15)', color: '#60a5fa' },
  { label: '平均正确率', value: '0%', icon: TrendCharts, bg: 'rgba(52, 211, 153, 0.15)', color: '#34d399' },
  { label: '错题数量', value: 0, icon: Warning, bg: 'rgba(251, 191, 36, 0.15)', color: '#fbbf24' },
  { label: '待复习错题', value: 0, icon: Notebook, bg: 'rgba(196, 181, 253, 0.15)', color: '#c4b5fd' },
])

// 最近错题：从错题列表接口取前几条
const recentSubmissions = ref([])

// 错误分类枚举 → 中文名
function categoryLabel(category) {
  const map = { FORMAT_ERROR: '格式错误', SYNTAX_ERROR: '语法错误', LOGIC_ERROR: '逻辑错误' }
  return map[category] || category || '未分类'
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

// 登录后加载首页真实数据
async function loadHomeData() {
  if (!userStore.isLoggedIn) return
  try {
    const [report, errorRes] = await Promise.all([
      getReportApi().catch(() => null),
      getErrorListApi({ page: 0, size: 5 }).catch(() => null),
    ])

    if (report) {
      // accuracyRate 是 0.0~1.0 的小数，显示百分比要乘以100
      const unmasteredCount = (report.practiceList || []).filter((e) => !e.mastered).length
      stats.value = [
        { label: '提交次数', value: report.totalSubmissions ?? 0, icon: DocumentChecked, bg: 'rgba(96, 165, 250, 0.15)', color: '#60a5fa' },
        { label: '平均正确率', value: `${Math.round((report.accuracyRate ?? 0) * 100)}%`, icon: TrendCharts, bg: 'rgba(52, 211, 153, 0.15)', color: '#34d399' },
        { label: '错题数量', value: report.totalErrors ?? 0, icon: Warning, bg: 'rgba(251, 191, 36, 0.15)', color: '#fbbf24' },
        { label: '待复习错题', value: unmasteredCount, icon: Notebook, bg: 'rgba(196, 181, 253, 0.15)', color: '#c4b5fd' },
      ]
    }

    // 最近错题：取错题列表前5条
    const list = Array.isArray(errorRes) ? errorRes : (errorRes?.content || [])
    recentSubmissions.value = list.map((e) => ({
      id: e.id ?? '',
      title: e.errorType || categoryLabel(e.category) || '代码问题',
      language: categoryLabel(e.category),
      score: Array.isArray(e.line) && e.line.length ? `第 ${e.line.join('、')} 行` : '-',
      status: e.mastered ? '已掌握' : '未掌握',
      time: formatTime(e.createdAt),
    }))
  } catch (error) {
    // 首页数据加载失败不阻塞页面展示，静默处理
    console.error('加载首页数据失败:', error)
  }
}

const platformFeatures = [
  { title: 'AI 双层批改', desc: '静态语法检查 + 大模型深度分析，全方位检测代码问题' },
  { title: '智能错题归档', desc: '自动识别错误知识点，分类整理，形成个人错题库' },
  { title: '个性化学习报告', desc: '基于学习数据生成可视化报告，精准定位薄弱环节' },
]

onMounted(() => {
  loadHomeData()
})

function handleFeatureClick(path) {
  if (path === '/submit' || path === '/report' || path === '/wrong-questions') {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    router.push(path)
  }
}

function goToSubmit() {
  router.push('/submit')
}

function scrollToSection(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}

function openAIAssistant() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后使用AI助手')
    router.push('/login')
    return
  }
  aiAssistantVisible.value = true
}

function sendAIMessage() {
  if (!aiInput.value.trim()) return
  const question = aiInput.value
  aiInput.value = ''
  setTimeout(() => {
    aiMessages.value.push(`关于"${question}"，建议你：1. 先理解基本概念；2. 多写代码练习；3. 遇到错误时仔细阅读报错信息。具体问题可以在提交作业后查看AI批改详情。`)
  }, 500)
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}
/* 背景渐变光晕 */
.home::before {
  content: '';
  position: fixed;
  top: -20%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}
.home::after {
  content: '';
  position: fixed;
  bottom: -10%;
  left: -5%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}
.home > * {
  position: relative;
  z-index: 1;
}

/* Hero 区域 */
.hero {
  display: flex;
  align-items: center;
  gap: 56px;
  padding: 56px 0 64px;
  min-height: 400px;
}
.hero-content {
  flex: 1;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  background: rgba(165, 180, 252, 0.08);
  border: 1px solid rgba(165, 180, 252, 0.15);
  border-radius: 20px;
  font-size: 12px;
  color: #a5b4fc;
  margin-bottom: 20px;
}
.badge-dot {
  width: 5px;
  height: 5px;
  background: #a5b4fc;
  border-radius: 50%;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
.hero-title {
  font-size: 40px;
  font-weight: 700;
  line-height: 1.25;
  color: #f4f4f5;
  margin: 0 0 16px;
  letter-spacing: -0.5px;
}
.accent-text {
  color: #c4b5fd;
}
.hero-desc {
  font-size: 15px;
  color: #a1a1aa;
  line-height: 1.8;
  margin: 0 0 28px;
}
.hero-btns {
  display: flex;
  gap: 10px;
}
.btn-primary {
  padding: 11px 24px;
  background: #e4e4e7;
  color: #0c0c0e;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-primary:hover {
  background: #fff;
}
.btn-ghost {
  padding: 11px 24px;
  background: transparent;
  color: #a1a1aa;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-ghost:hover {
  border-color: rgba(255, 255, 255, 0.2);
  color: #d4d4d8;
}
.btn-link {
  background: none;
  border: none;
  color: #a5b4fc;
  font-size: 13px;
  cursor: pointer;
  padding: 0;
}
.btn-link:hover {
  text-decoration: underline;
}

/* AI批改代码对比 */
.hero-visual {
  flex: 1;
}
.code-compare {
  display: flex;
  align-items: stretch;
  gap: 0;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}
.code-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.code-pane-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
.error-header {
  background: rgba(248, 113, 113, 0.08);
  color: #f87171;
}
.fixed-header {
  background: rgba(52, 211, 153, 0.08);
  color: #34d399;
}
.code-pane-body {
  padding: 14px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 11px;
  line-height: 1.9;
  flex: 1;
}
.code-line {
  display: flex;
  gap: 12px;
  color: #a1a1aa;
}
.error-line {
  background: rgba(248, 113, 113, 0.08);
  margin: 0 -14px;
  padding: 0 14px;
  border-left: 2px solid #f87171;
  animation: errorPulse 2s ease-in-out infinite;
}
.fixed-line {
  background: rgba(52, 211, 153, 0.08);
  margin: 0 -14px;
  padding: 0 14px;
  border-left: 2px solid #34d399;
}
@keyframes errorPulse {
  0%, 100% { background: rgba(248, 113, 113, 0.08); }
  50% { background: rgba(248, 113, 113, 0.15); }
}
.ln {
  color: #3f3f46;
  min-width: 16px;
  text-align: right;
  user-select: none;
}
.kw { color: #c4b5fd; }
.fn { color: #7dd3fc; }
.num { color: #fcd34d; }
.str { color: #86efac; }

/* AI批改桥 */
.ai-bridge {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 12px;
  background: rgba(196, 181, 253, 0.03);
  border-left: 1px solid rgba(255, 255, 255, 0.05);
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  min-width: 70px;
}
.ai-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(196, 181, 253, 0.2), rgba(139, 92, 246, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  animation: aiBreath 2.5s ease-in-out infinite;
}
.ai-icon {
  color: #c4b5fd;
}
@keyframes aiBreath {
  0%, 100% { box-shadow: 0 0 0 0 rgba(196, 181, 253, 0.3); transform: scale(1); }
  50% { box-shadow: 0 0 20px 5px rgba(196, 181, 253, 0.15); transform: scale(1.05); }
}
.ai-arrow {
  color: #c4b5fd;
  animation: arrowMove 1.5s ease-in-out infinite;
}
@keyframes arrowMove {
  0%, 100% { transform: translateX(0); opacity: 0.6; }
  50% { transform: translateX(4px); opacity: 1; }
}
.ai-bridge-text {
  font-size: 10px;
  color: #a1a1aa;
  text-align: center;
  line-height: 1.4;
}

.ai-suggestion {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(196, 181, 253, 0.04);
  border-top: 1px solid rgba(196, 181, 253, 0.1);
  margin-top: 12px;
  border-radius: 10px;
}
.ai-dot {
  width: 6px;
  height: 6px;
  background: #c4b5fd;
  border-radius: 50%;
  margin-top: 7px;
  flex-shrink: 0;
  animation: pulse 2s infinite;
}
.ai-text {
  font-size: 12px;
  color: #a1a1aa;
  line-height: 1.7;
}
.ai-label {
  color: #a5b4fc;
  font-weight: 600;
  margin-right: 6px;
}

/* 数据概览 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 56px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.07);
  border-radius: 14px;
  transition: all 0.25s ease;
}
.stat-card:hover {
  transform: translateY(-3px);
  border-color: rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-meta {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #f4f4f5;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #a1a1aa;
  margin-top: 2px;
}

/* 区块 */
.section {
  margin-bottom: 56px;
}
.section-header {
  margin-bottom: 24px;
}
.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #f4f4f5;
  margin: 0 0 4px;
  letter-spacing: -0.3px;
}
.section-sub {
  font-size: 13px;
  color: #a1a1aa;
  margin: 0;
}

/* 功能卡片 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.feature-card {
  position: relative;
  padding: 24px 20px;
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.feature-card:hover {
  border-color: rgba(165, 180, 252, 0.25);
  background: rgba(255, 255, 255, 0.04);
  transform: translateY(-2px);
}
.feature-num {
  font-size: 12px;
  font-weight: 600;
  color: #71717a;
  margin-bottom: 16px;
  font-family: monospace;
}
.feature-card h3 {
  font-size: 15px;
  color: #f4f4f5;
  margin: 0 0 8px;
  font-weight: 600;
}
.feature-card p {
  font-size: 12px;
  color: #a1a1aa;
  line-height: 1.7;
  margin: 0;
}
.feature-arrow {
  position: absolute;
  top: 20px;
  right: 20px;
  color: #3f3f46;
  transition: all 0.2s;
}
.feature-card:hover .feature-arrow {
  color: #a5b4fc;
  transform: translateX(3px);
}

/* 玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 4px;
  overflow: hidden;
}

/* 标签 */
.lang-tag {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 5px;
  font-size: 11px;
  color: #a1a1aa;
  font-weight: 500;
}
.score {
  font-weight: 600;
  color: #a1a1aa;
}
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 500;
}
.status-tag.done {
  background: rgba(134, 239, 172, 0.08);
  color: #86efac;
}
.status-tag.pending {
  background: rgba(252, 211, 77, 0.08);
  color: #fcd34d;
}

/* 平台特色 */
.platform-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.platform-card {
  padding: 28px 24px;
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  transition: all 0.2s;
}
.platform-card:hover {
  border-color: rgba(255, 255, 255, 0.12);
}
.platform-line {
  width: 24px;
  height: 2px;
  background: #a5b4fc;
  margin-bottom: 16px;
  border-radius: 1px;
}
.platform-card h3 {
  font-size: 16px;
  color: #f4f4f5;
  margin: 0 0 8px;
  font-weight: 600;
}
.platform-card p {
  font-size: 13px;
  color: #a1a1aa;
  line-height: 1.7;
  margin: 0;
}

/* AI 助手弹窗 */
.ai-chat {
  max-height: 280px;
  overflow-y: auto;
  padding: 8px 0;
}
.ai-message {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}
.ai-avatar {
  width: 28px;
  height: 28px;
  background: rgba(165, 180, 252, 0.12);
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a5b4fc;
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
}
.ai-bubble {
  background: rgba(255, 255, 255, 0.04);
  padding: 10px 14px;
  border-radius: 0 10px 10px 10px;
  font-size: 13px;
  color: #a1a1aa;
  line-height: 1.6;
  max-width: 85%;
}
.ai-input-area {
  display: flex;
  gap: 8px;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}
.btn-send {
  width: 40px;
  height: 40px;
  background: #e4e4e7;
  color: #0c0c0e;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s;
}
.btn-send:hover {
  background: #fff;
}
</style>
