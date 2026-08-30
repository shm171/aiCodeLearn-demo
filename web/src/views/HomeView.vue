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
          <button class="btn-ghost" @click="scrollToSection('features')">
            了解功能
          </button>
        </div>
      </div>
      <div class="hero-visual">
        <div class="code-card">
          <div class="code-card-header">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="code-filename">solution.cpp</span>
          </div>
          <div class="code-card-body">
            <div class="code-line"><span class="ln">1</span><span class="kw">#include</span> <span class="str">&lt;iostream&gt;</span></div>
            <div class="code-line"><span class="ln">2</span><span class="kw">using namespace</span> std;</div>
            <div class="code-line"><span class="ln">3</span>&nbsp;</div>
            <div class="code-line"><span class="ln">4</span><span class="kw">int</span> <span class="fn">main</span>() {</div>
            <div class="code-line"><span class="ln">5</span>&nbsp;&nbsp;<span class="kw">int</span> arr[<span class="num">5</span>];</div>
            <div class="code-line error"><span class="ln">6</span>&nbsp;&nbsp;arr[<span class="num">5</span>] = <span class="num">10</span>;</div>
            <div class="code-line"><span class="ln">7</span>&nbsp;&nbsp;<span class="kw">return</span> <span class="num">0</span>;</div>
            <div class="code-line"><span class="ln">8</span>}</div>
          </div>
          <div class="ai-suggestion">
            <div class="ai-dot"></div>
            <div class="ai-text">
              <span class="ai-label">AI 检测</span>
              数组长度为5，下标范围 0-4，第6行 arr[5] 越界。建议改为 arr[4] = 10;
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 2. 数据概览（登录后显示） ===== -->
    <section class="stats-section" v-if="userStore.isLoggedIn">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </section>

    <!-- ===== 3. 核心功能区域 ===== -->
    <section class="section" id="features">
      <div class="section-header">
        <h2 class="section-title">核心功能</h2>
        <p class="section-sub">智能体驱动的编程学习全流程</p>
      </div>
      <div class="features-grid">
        <div class="feature-card" @click="handleFeatureClick('/submit')">
          <div class="feature-num">01</div>
          <h3>智能批改</h3>
          <p>上传代码，AI 双层批改，秒级反馈错误与改进建议</p>
          <div class="feature-arrow">
            <el-icon :size="16"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="feature-card" @click="handleFeatureClick('/wrong-questions')">
          <div class="feature-num">02</div>
          <h3>错题本</h3>
          <p>自动归档错误代码，分类整理，针对性复习巩固</p>
          <div class="feature-arrow">
            <el-icon :size="16"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="feature-card" @click="handleFeatureClick('/report')">
          <div class="feature-num">03</div>
          <h3>学习报告</h3>
          <p>数据可视化分析，掌握薄弱知识点，精准提升</p>
          <div class="feature-arrow">
            <el-icon :size="16"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="feature-card" @click="openAIAssistant">
          <div class="feature-num">04</div>
          <h3>AI 学习助手</h3>
          <p>随时提问，智能解答编程疑问，个性化学习建议</p>
          <div class="feature-arrow">
            <el-icon :size="16"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 4. 最近提交 ===== -->
    <section class="section" v-if="userStore.isLoggedIn">
      <div class="section-header">
        <div>
          <h2 class="section-title">最近提交</h2>
          <p class="section-sub">查看你的批改历史</p>
        </div>
        <button class="btn-link" @click="ElMessage.info('历史记录功能开发中')">查看全部</button>
      </div>
      <div class="glass-card">
        <el-table :data="recentSubmissions" style="width: 100%">
          <el-table-column prop="id" label="#" width="60" />
          <el-table-column prop="title" label="作业题目" />
          <el-table-column prop="language" label="语言" width="100">
            <template #default="{ row }">
              <span class="lang-tag">{{ row.language }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="100">
            <template #default="{ row }">
              <span class="score">{{ row.score }}分</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span class="status-tag" :class="row.status === '已批改' ? 'done' : 'pending'">{{ row.status }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="提交时间" width="160" />
        </el-table>
      </div>
    </section>

    <!-- ===== 5. 平台特色 ===== -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">为什么选择 EduCode</h2>
        <p class="section-sub">智能体驱动，让学习更高效</p>
      </div>
      <div class="platform-grid">
        <div class="platform-card" v-for="feat in platformFeatures" :key="feat.title">
          <div class="platform-line"></div>
          <h3>{{ feat.title }}</h3>
          <p>{{ feat.desc }}</p>
        </div>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { ArrowRight, Promotion } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const aiAssistantVisible = ref(false)
const aiInput = ref('')
const aiMessages = ref([])

const stats = [
  { label: '提交次数', value: 12 },
  { label: '平均正确率', value: '78%' },
  { label: '错题数量', value: 5 },
  { label: '学习天数', value: 8 },
]

const recentSubmissions = [
  { id: 12, title: 'C++：斐波那契数列', language: 'C++', score: 95, status: '已批改', time: '2026-08-24 14:30' },
  { id: 11, title: 'C语言：冒泡排序', language: 'C', score: 72, status: '已批改', time: '2026-08-23 16:20' },
  { id: 10, title: 'Java：列表操作', language: 'Java', score: 88, status: '已批改', time: '2026-08-22 10:15' },
  { id: 9, title: 'C语言：指针练习', language: 'C', score: 55, status: '已批改', time: '2026-08-21 19:45' },
]

const platformFeatures = [
  { title: 'AI 双层批改', desc: '静态语法检查 + 大模型深度分析，全方位检测代码问题' },
  { title: '智能错题归档', desc: '自动识别错误知识点，分类整理，形成个人错题库' },
  { title: '个性化学习报告', desc: '基于学习数据生成可视化报告，精准定位薄弱环节' },
]

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

/* 代码卡片 */
.hero-visual {
  flex: 1;
}
.code-card {
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}
.code-card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}
.code-filename {
  margin-left: 10px;
  font-size: 12px;
  color: #52525b;
  font-family: 'Consolas', monospace;
}
.code-card-body {
  padding: 16px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 2;
}
.code-line {
  display: flex;
  gap: 14px;
  color: #a1a1aa;
}
.code-line.error {
  background: rgba(165, 180, 252, 0.06);
  margin: 0 -16px;
  padding: 0 16px;
  border-left: 2px solid #a5b4fc;
}
.ln {
  color: #3f3f46;
  min-width: 18px;
  text-align: right;
  user-select: none;
}
.kw { color: #a5b4fc; }
.fn { color: #7dd3fc; }
.num { color: #fcd34d; }
.str { color: #86efac; }
.ai-suggestion {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(165, 180, 252, 0.04);
  border-top: 1px solid rgba(165, 180, 252, 0.1);
}
.ai-dot {
  width: 6px;
  height: 6px;
  background: #a5b4fc;
  border-radius: 50%;
  margin-top: 7px;
  flex-shrink: 0;
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
  gap: 12px;
  margin-bottom: 56px;
}
.stat-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  transition: all 0.2s;
}
.stat-card:hover {
  border-color: rgba(255, 255, 255, 0.12);
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #f4f4f5;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 12px;
  color: #a1a1aa;
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
