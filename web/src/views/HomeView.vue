<!-- 首页 HomeView.vue -->
<template>
  <div class="home">
    <!-- ===== 1. 欢迎横幅 ===== -->
    <div class="banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <div class="banner-text">
          <div class="banner-tag">
            <el-icon><MagicStick /></el-icon>
            AI 智能代码批改
          </div>
          <h1>让编程学习更高效</h1>
          <p>上传代码，AI 多智能体协作批改，秒级反馈错误与改进建议</p>
          <div class="banner-btns">
            <el-button type="primary" size="large" @click="goToSubmit" v-if="userStore.isLoggedIn">
              <el-icon><Upload /></el-icon>立即提交作业
            </el-button>
            <el-button type="primary" size="large" @click="router.push('/login')" v-else>
              开始使用
            </el-button>
            <el-button size="large" @click="scrollToFeatures">了解更多</el-button>
          </div>
        </div>
        <div class="banner-visual">
          <div class="code-window">
            <div class="code-header">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
              <span class="code-title">main.py</span>
            </div>
            <div class="code-body">
              <div class="code-line"><span class="line-num">1</span><span class="kw">def</span> <span class="fn">fibonacci</span>(n):</div>
              <div class="code-line"><span class="line-num">2</span>  <span class="kw">if</span> n <= <span class="num">1</span>:</div>
              <div class="code-line"><span class="line-num">3</span>    <span class="kw">return</span> n</div>
              <div class="code-line"><span class="line-num">4</span>  <span class="kw">return</span> fibonacci(n-<span class="num">1</span>) + fibonacci(n-<span class="num">2</span>)</div>
              <div class="code-line highlight"><span class="line-num">5</span><span class="comment"># AI: 建议添加记忆化优化</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 2. 数据概览 ===== -->
    <div class="section" v-if="userStore.isLoggedIn">
      <el-row :gutter="20">
        <el-col :span="6" v-for="stat in stats" :key="stat.label">
          <div class="stat-card" :style="{ background: stat.bg }">
            <div class="stat-icon">
              <el-icon :size="28"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- ===== 3. 快捷功能入口 ===== -->
    <div class="section" id="features">
      <h2 class="section-title">核心功能</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in features" :key="item.title">
          <div class="feature-card" @click="handleFeatureClick(item)">
            <div class="feature-icon" :style="{ background: item.bg }">
              <el-icon :size="32"><component :is="item.icon" /></el-icon>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
            <div class="feature-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- ===== 4. 最近提交记录 ===== -->
    <div class="section" v-if="userStore.isLoggedIn">
      <div class="section-header">
        <h2 class="section-title">最近提交</h2>
        <el-button link type="primary" @click="ElMessage.info('历史记录功能开发中')">查看全部</el-button>
      </div>
      <el-card class="table-card" shadow="never">
        <el-table :data="recentSubmissions" stripe>
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="title" label="作业题目" />
          <el-table-column prop="language" label="语言" width="100">
            <template #default="{ row }">
              <el-tag :type="row.language === 'Python' ? 'success' : 'primary'" size="small">{{ row.language }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="100">
            <template #default="{ row }">
              <span :class="row.score >= 80 ? 'score-high' : row.score >= 60 ? 'score-mid' : 'score-low'">
                {{ row.score }}分
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '已批改' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="提交时间" width="180" />
        </el-table>
      </el-card>
    </div>

    <!-- ===== 5. 平台特色 ===== -->
    <div class="section">
      <h2 class="section-title">为什么选择 EduCode</h2>
      <el-row :gutter="24">
        <el-col :span="8" v-for="feat in platformFeatures" :key="feat.title">
          <div class="platform-card">
            <div class="platform-icon" :style="{ color: feat.color }">
              <el-icon :size="40"><component :is="feat.icon" /></el-icon>
            </div>
            <h3>{{ feat.title }}</h3>
            <p>{{ feat.desc }}</p>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import {
  Upload, MagicStick, Document, Notebook, TrendCharts,
  Files, CircleCheck, Warning, Timer, ArrowRight,
  Collection, DataLine
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const stats = [
  { label: '提交次数', value: 12, icon: Document, bg: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { label: '平均正确率', value: '78%', icon: CircleCheck, bg: 'linear-gradient(135deg, #11998e, #38ef7d)' },
  { label: '错题数量', value: 5, icon: Warning, bg: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { label: '学习天数', value: 8, icon: Timer, bg: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
]

const features = [
  { title: '提交作业', desc: '上传代码，AI智能批改', icon: Upload, bg: 'linear-gradient(135deg, #667eea, #764ba2)', path: '/submit' },
  { title: '我的错题本', desc: '历史错误，针对性复习', icon: Notebook, bg: 'linear-gradient(135deg, #f093fb, #f5576c)', path: '/wrong-questions' },
  { title: '学习报告', desc: '数据可视化，掌握薄弱点', icon: TrendCharts, bg: 'linear-gradient(135deg, #11998e, #38ef7d)', path: '/report' },
  { title: '历史提交', desc: '查看所有提交记录', icon: Files, bg: 'linear-gradient(135deg, #4facfe, #00f2fe)', path: '/history' },
]

const recentSubmissions = [
  { id: 12, title: 'Python基础：斐波那契数列', language: 'Python', score: 95, status: '已批改', time: '2026-08-24 14:30' },
  { id: 11, title: 'C语言：冒泡排序', language: 'C', score: 72, status: '已批改', time: '2026-08-23 16:20' },
  { id: 10, title: 'Python基础：列表操作', language: 'Python', score: 88, status: '已批改', time: '2026-08-22 10:15' },
  { id: 9, title: 'C语言：指针练习', language: 'C', score: 55, status: '已批改', time: '2026-08-21 19:45' },
  { id: 8, title: 'Python基础：函数定义', language: 'Python', score: 0, status: '批改中', time: '2026-08-24 15:00' },
]

const platformFeatures = [
  { title: 'AI智能批改', desc: '多智能体协作，规则校验+深度分析，秒级反馈', icon: MagicStick, color: '#667eea' },
  { title: '错题本归档', desc: '自动收集错误代码，分类整理，针对性复习', icon: Collection, color: '#f5576c' },
  { title: '学习数据分析', desc: '可视化报告，掌握薄弱知识点，精准提升', icon: DataLine, color: '#11998e' },
]

function handleFeatureClick(item) {
  if (item.path === '/submit') {
    router.push(item.path)
  } else if (item.path) {
    ElMessage.info(`${item.title}功能开发中，敬请期待`)
  }
}

function goToSubmit() {
  router.push('/submit')
}

function scrollToFeatures() {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎横幅 */
.banner {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 32px;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4c1d95 100%);
}
.banner-bg {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.3) 0%, transparent 70%);
  border-radius: 50%;
}
.banner-content {
  position: relative;
  display: flex;
  align-items: center;
  padding: 48px;
  gap: 40px;
}
.banner-text {
  flex: 1;
  color: #fff;
  z-index: 1;
}
.banner-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.1);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  margin-bottom: 16px;
}
.banner-text h1 {
  font-size: 36px;
  margin: 0 0 12px;
  font-weight: 700;
}
.banner-text p {
  font-size: 16px;
  opacity: 0.8;
  margin: 0 0 24px;
  line-height: 1.6;
}
.banner-btns {
  display: flex;
  gap: 12px;
}
.banner-btns .el-button {
  border-radius: 24px;
  padding: 10px 24px;
}
.banner-btns .el-button:last-child {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: #fff;
}

/* 代码窗口 */
.banner-visual {
  flex: 1;
  z-index: 1;
}
.code-window {
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.code-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}
.dot.red { background: #ff5f57; }
.dot.yellow { background: #febc2e; }
.dot.green { background: #28c840; }
.code-title {
  margin-left: 12px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}
.code-body {
  padding: 16px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.8;
}
.code-line {
  display: flex;
  gap: 12px;
  color: #e2e8f0;
}
.line-num {
  color: rgba(255, 255, 255, 0.3);
  min-width: 20px;
  text-align: right;
}
.kw { color: #c084fc; }
.fn { color: #60a5fa; }
.num { color: #fbbf24; }
.comment { color: #34d399; font-style: italic; }
.code-line.highlight {
  background: rgba(52, 211, 153, 0.1);
  margin: 0 -16px;
  padding: 0 16px;
  border-left: 3px solid #34d399;
}

/* 区块 */
.section {
  margin-bottom: 32px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px;
}

/* 数据卡片 */
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  color: #fff;
  transition: transform 0.3s;
}
.stat-card:hover {
  transform: translateY(-4px);
}
.stat-icon {
  width: 52px;
  height: 52px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
}
.stat-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 2px;
}

/* 功能卡片 */
.feature-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  border: 1px solid #f1f5f9;
}
.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}
.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 16px;
}
.feature-card h3 {
  font-size: 17px;
  margin: 0 0 8px;
  color: #1e293b;
}
.feature-card p {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
  line-height: 1.6;
}
.feature-arrow {
  position: absolute;
  top: 24px;
  right: 24px;
  color: #cbd5e1;
  transition: all 0.3s;
}
.feature-card:hover .feature-arrow {
  color: #6366f1;
  transform: translateX(4px);
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  border: 1px solid #f1f5f9;
}

/* 分数颜色 */
.score-high { color: #10b981; font-weight: 600; }
.score-mid { color: #f59e0b; font-weight: 600; }
.score-low { color: #ef4444; font-weight: 600; }

/* 平台特色 */
.platform-card {
  text-align: center;
  padding: 36px 24px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #f1f5f9;
  transition: all 0.3s;
}
.platform-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
}
.platform-icon {
  margin-bottom: 16px;
}
.platform-card h3 {
  font-size: 18px;
  margin: 0 0 10px;
  color: #1e293b;
}
.platform-card p {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
  line-height: 1.7;
}
</style>
