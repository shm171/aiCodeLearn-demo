<!-- 学习报告 ReportView.vue -->
<template>
  <div class="report-page">
    <h1 class="page-title">学习报告</h1>
    <p class="page-sub">你的编程学习数据一览</p>

    <!-- 数据概览 -->
    <el-row :gutter="20" class="section">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ background: stat.bg }">
          <div class="stat-icon">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 饼图区域 -->
    <el-row :gutter="24" class="section">
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-title">编程语言分布</div>
          </template>
          <div ref="langChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-title">错误类型分布</div>
          </template>
          <div ref="errorChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 折线图 + 柱状图 -->
    <el-row :gutter="24" class="section">
      <el-col :span="14">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-title">近7天提交趋势</div>
          </template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-title">知识点掌握度</div>
          </template>
          <div ref="knowledgeChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学习建议 -->
    <el-card class="section tip-card" shadow="never">
      <div class="tip-content">
        <div>
          <h3>学习建议</h3>
          <p>根据你的学习数据，<strong>指针与内存管理</strong>和<strong>递归算法</strong>是你的薄弱环节，建议多加练习。继续加油！</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Document, CircleCheck, Warning, Timer } from '@element-plus/icons-vue'

const langChartRef = ref(null)
const errorChartRef = ref(null)
const trendChartRef = ref(null)
const knowledgeChartRef = ref(null)

let charts = []

const stats = [
  { label: '总提交次数', value: 42, icon: Document, bg: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { label: '平均正确率', value: '76%', icon: CircleCheck, bg: 'linear-gradient(135deg, #11998e, #38ef7d)' },
  { label: '错题数量', value: 18, icon: Warning, bg: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { label: '学习天数', value: 15, icon: Timer, bg: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
]

onMounted(() => {
  // 1. 语言分布饼图
  const c1 = echarts.init(langChartRef.value)
  c1.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    color: ['#6366f1', '#06b6d4'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 25, name: 'Python' },
        { value: 17, name: 'C++' },
      ],
    }],
  })
  charts.push(c1)

  // 2. 错误类型饼图
  const c2 = echarts.init(errorChartRef.value)
  c2.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    color: ['#ef4444', '#f59e0b', '#6366f1', '#06b6d4'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 8, name: '语法错误' },
        { value: 5, name: '逻辑错误' },
        { value: 3, name: '内存泄漏' },
        { value: 2, name: '算法优化' },
      ],
    }],
  })
  charts.push(c2)

  // 3. 趋势折线图
  const c3 = echarts.init(trendChartRef.value)
  c3.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      smooth: true,
      data: [3, 5, 2, 8, 6, 12, 6],
      areaStyle: { color: 'rgba(99, 102, 241, 0.2)' },
      lineStyle: { color: '#6366f1' },
      itemStyle: { color: '#6366f1' },
    }],
  })
  charts.push(c3)

  // 4. 知识点柱状图
  const c4 = echarts.init(knowledgeChartRef.value)
  c4.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value', max: 100 },
    yAxis: {
      type: 'category',
      data: ['递归', '指针', '循环', '函数', '数组'],
    },
    series: [{
      type: 'bar',
      data: [45, 38, 82, 75, 88],
      itemStyle: { color: '#6366f1' },
    }],
  })
  charts.push(c4)

  window.addEventListener('resize', handleResize)
})

function handleResize() {
  charts.forEach(c => c && c.resize())
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c && c.dispose())
})
</script>

<style scoped>
.report-page { max-width: 1200px; margin: 0 auto; }
.page-title {
  font-size: 28px; font-weight: 700; margin: 0 0 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.page-sub { color: #94a3b8; margin: 0 0 24px; font-size: 14px; }
.section { margin-bottom: 24px; }
.stat-card {
  display: flex; align-items: center; gap: 14px; padding: 18px;
  border-radius: 14px; color: #fff; transition: transform 0.3s;
}
.stat-card:hover { transform: translateY(-3px); }
.stat-icon {
  width: 44px; height: 44px; background: rgba(255,255,255,0.2);
  border-radius: 12px; display: flex; align-items: center; justify-content: center;
}
.stat-value { font-size: 22px; font-weight: 700; }
.stat-label { font-size: 12px; opacity: 0.85; margin-top: 2px; }
.chart-card { border-radius: 14px; border: 1px solid #f1f5f9; }
.card-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.chart { width: 100%; height: 300px; }
.tip-card {
  border-radius: 14px; border: 1px solid #fef3c7;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
}
.tip-content { display: flex; align-items: flex-start; gap: 16px; }
.tip-content h3 { margin: 0 0 6px; color: #92400e; font-size: 16px; }
.tip-content p { margin: 0; color: #78350f; font-size: 14px; line-height: 1.6; }
</style>
