<!-- 首页 HomeView.vue -->
<template>
  <div class="home">
    <!-- ===== 1. 欢迎横幅 ===== -->
    <el-card class="banner" shadow="never">
      <div class="banner-content">
        <div class="banner-text">
          <h1>👋 欢迎回来，{{ userStore.email || '同学' }}</h1>
          <p>上传代码，AI 智能批改，帮你发现编程错误，提升编程能力。</p>
          <el-button type="primary" size="large" @click="goToSubmit">
            <el-icon><Upload /></el-icon>
            立即提交作业
          </el-button>
        </div>
        <div class="banner-icon">
          <el-icon :size="120"><Cpu /></el-icon>
        </div>
      </div>
    </el-card>

    <!-- ===== 2. 数据概览 ===== -->
    <el-row :gutter="20" class="section">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 3. 快捷功能入口 ===== -->
    <h2 class="section-title">快捷功能</h2>
    <el-row :gutter="20" class="section">
      <el-col :span="6" v-for="item in features" :key="item.title">
        <el-card class="feature-card" shadow="hover" @click="handleFeatureClick(item)">
          <div class="feature-icon" :style="{ color: item.color }">
            <el-icon :size="40"><component :is="item.icon" /></el-icon>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 4. 最近提交记录 ===== -->
    <h2 class="section-title">最近提交</h2>
    <el-card class="section" shadow="never">
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

    <!-- ===== 5. 平台功能介绍 ===== -->
    <h2 class="section-title">平台特色</h2>
    <el-row :gutter="20" class="section">
      <el-col :span="8" v-for="feat in platformFeatures" :key="feat.title">
        <el-card class="platform-card" shadow="hover">
          <el-icon :size="48" :color="feat.color"><component :is="feat.icon" /></el-icon>
          <h3>{{ feat.title }}</h3>
          <p>{{ feat.desc }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import {
  Upload, Cpu, Document, Notebook, TrendCharts,
  Files, CircleCheck, Warning, Timer,
  MagicStick, Collection, DataLine
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 数据概览（Mock数据，等后端接口好了替换）
const stats = [
  { label: '提交次数', value: 12, icon: Document, color: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { label: '平均正确率', value: '78%', icon: CircleCheck, color: 'linear-gradient(135deg, #11998e, #38ef7d)' },
  { label: '错题数量', value: 5, icon: Warning, color: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { label: '学习天数', value: 8, icon: Timer, color: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
]

// 快捷功能入口
const features = [
  { title: '提交作业', desc: '上传代码，AI智能批改', icon: Upload, color: '#667eea', path: '/submit' },
  { title: '我的错题本', desc: '历史错误，针对性复习', icon: Notebook, color: '#f5576c', path: '/wrong-questions' },
  { title: '学习报告', desc: '数据可视化，掌握薄弱点', icon: TrendCharts, color: '#11998e', path: '/report' },
  { title: '历史提交', desc: '查看所有提交记录', icon: Files, color: '#4facfe', path: '/history' },
]

// 最近提交记录（Mock数据）
const recentSubmissions = [
  { id: 12, title: 'Python基础：斐波那契数列', language: 'Python', score: 95, status: '已批改', time: '2026-08-24 14:30' },
  { id: 11, title: 'C语言：冒泡排序', language: 'C', score: 72, status: '已批改', time: '2026-08-23 16:20' },
  { id: 10, title: 'Python基础：列表操作', language: 'Python', score: 88, status: '已批改', time: '2026-08-22 10:15' },
  { id: 9, title: 'C语言：指针练习', language: 'C', score: 55, status: '已批改', time: '2026-08-21 19:45' },
  { id: 8, title: 'Python基础：函数定义', language: 'Python', score: 0, status: '批改中', time: '2026-08-24 15:00' },
]

// 平台特色功能
const platformFeatures = [
  { title: 'AI智能批改', desc: '多智能体协作，规则校验+深度分析，秒级反馈', icon: MagicStick, color: '#667eea' },
  { title: '错题本归档', desc: '自动收集错误代码，分类整理，针对性复习', icon: Collection, color: '#f5576c' },
  { title: '学习数据分析', desc: '可视化报告，掌握薄弱知识点，精准提升', icon: DataLine, color: '#11998e' },
]

// 点击功能卡片
function handleFeatureClick(item) {
  if (item.path === '/submit') {
    router.push(item.path)
  } else if (item.path) {
    ElMessage.info(`${item.title}功能开发中，敬请期待`)
  }
}

// 跳转到提交作业页
function goToSubmit() {
  router.push('/submit')
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎横幅 */
.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  margin-bottom: 24px;
  color: #fff;
}
.banner :deep(.el-card__body) {
  padding: 32px;
}
.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.banner-text h1 {
  font-size: 28px;
  margin: 0 0 12px;
}
.banner-text p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 20px;
}
.banner-icon {
  opacity: 0.3;
}

/* 区块标题 */
.section-title {
  font-size: 20px;
  margin: 24px 0 16px;
  color: #303133;
}
.section {
  margin-bottom: 24px;
}

/* 数据概览卡片 */
.stat-card {
  border-radius: 12px;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 快捷功能卡片 */
.feature-card {
  text-align: center;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}
.feature-card:hover {
  transform: translateY(-4px);
}
.feature-card :deep(.el-card__body) {
  padding: 28px 20px;
}
.feature-icon {
  margin-bottom: 12px;
}
.feature-card h3 {
  font-size: 18px;
  margin: 0 0 8px;
  color: #303133;
}
.feature-card p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 分数颜色 */
.score-high { color: #67c23a; font-weight: bold; }
.score-mid { color: #e6a23c; font-weight: bold; }
.score-low { color: #f56c6c; font-weight: bold; }

/* 平台特色卡片 */
.platform-card {
  text-align: center;
  border-radius: 12px;
}
.platform-card :deep(.el-card__body) {
  padding: 32px 20px;
}
.platform-card h3 {
  font-size: 18px;
  margin: 16px 0 8px;
  color: #303133;
}
.platform-card p {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.6;
}
</style>
