<!-- 学习报告 ReportView.vue - 高级黑灰玻璃感 -->
<template>
  <div class="report-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">学习报告</h1>
        <p class="page-sub">AI 智能分析你的编程学习数据</p>
      </div>
      <el-select v-model="period" class="period-select">
        <el-option label="近7天" value="7" />
        <el-option label="近30天" value="30" />
        <el-option label="全部" value="all" />
      </el-select>
    </div>

    <!-- 数据概览 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-title">编程语言分布</div>
        <div ref="langChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">错误类型分布</div>
        <div ref="errorChartRef" class="chart"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-title">近7天提交趋势</div>
        <div ref="trendChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">知识点掌握度</div>
        <div ref="knowledgeChartRef" class="chart"></div>
      </div>
    </div>

    <!-- AI 智能推荐 -->
    <div class="ai-recommend-section">
      <div class="section-header">
        <div>
          <div class="ai-badge">AI 智能推荐</div>
          <h2 class="section-title">为你定制的学习建议</h2>
        </div>
        <button class="refresh-btn" @click="refreshRecommend" :loading="refreshing">
          <el-icon :size="14"><Refresh /></el-icon>
          重新分析
        </button>
      </div>

      <div class="recommend-grid">
        <!-- 薄弱知识点 -->
        <div class="recommend-card">
          <h3>薄弱知识点</h3>
          <p class="recommend-desc">根据错题数据分析，需要重点突破</p>
          <div class="weak-list">
            <div class="weak-item" v-for="item in weakPoints" :key="item.name">
              <span class="weak-name">{{ item.name }}</span>
              <div class="weak-bar">
                <div class="weak-progress" :style="{ width: item.mastery + '%' }"></div>
              </div>
              <span class="weak-percent">{{ item.mastery }}%</span>
            </div>
          </div>
        </div>

        <!-- 推荐复习 -->
        <div class="recommend-card">
          <h3>推荐复习错题</h3>
          <p class="recommend-desc">这些错题你还未掌握，建议优先复习</p>
          <div class="review-list">
            <div class="review-item" v-for="item in reviewQuestions" :key="item.id" @click="goToWrongQuestions">
              <span class="review-title">{{ item.title }}</span>
              <span class="review-tag">{{ item.knowledge }}</span>
            </div>
          </div>
        </div>

        <!-- 学习建议 -->
        <div class="recommend-card">
          <h3>学习方向建议</h3>
          <p class="recommend-desc">AI 根据你的学习轨迹给出的提升建议</p>
          <ul class="suggestion-list">
            <li v-for="(s, i) in suggestions" :key="i">{{ s }}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const period = ref('7')
const refreshing = ref(false)

const langChartRef = ref(null)
const errorChartRef = ref(null)
const trendChartRef = ref(null)
const knowledgeChartRef = ref(null)

let charts = []

const stats = [
  { label: '总提交次数', value: 42 },
  { label: '平均正确率', value: '76%' },
  { label: '错题数量', value: 18 },
  { label: '学习天数', value: 15 },
]

const weakPoints = [
  { name: '指针与内存', mastery: 38 },
  { name: '递归算法', mastery: 45 },
  { name: '排序算法', mastery: 62 },
]

const reviewQuestions = [
  { id: 1, title: '数组遍历越界问题', knowledge: '数组' },
  { id: 2, title: '冒泡排序条件错误', knowledge: '排序' },
  { id: 3, title: '递归缺少终止条件', knowledge: '递归' },
]

const suggestions = [
  '每天坚持提交1-2道编程题，保持代码手感',
  '重点复习指针与内存管理相关知识点',
  '建议完成错题本中未掌握的题目后再做新题',
  '尝试用不同语言实现同一算法，加深理解',
]

function refreshRecommend() {
  refreshing.value = true
  setTimeout(() => {
    refreshing.value = false
    ElMessage.success('AI 已重新分析你的学习数据')
  }, 1000)
}

function goToWrongQuestions() {
  router.push('/wrong-questions')
}

onMounted(() => {
  const textColor = '#71717a'
  const splitColor = 'rgba(255,255,255,0.04)'

  // 1. 语言分布饼图
  const c1 = echarts.init(langChartRef.value)
  c1.setOption({
    tooltip: { trigger: 'item', backgroundColor: 'rgba(18,18,22,0.97)', borderColor: 'rgba(255,255,255,0.08)', textStyle: { color: '#d4d4d8' } },
    legend: { bottom: 0, textStyle: { color: textColor, fontSize: 11 } },
    color: ['#a5b4fc', '#71717a', '#3f3f46'],
    series: [{
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '45%'],
      itemStyle: { borderColor: '#0c0c0e', borderWidth: 2 },
      label: { show: false },
      data: [
        { value: 25, name: 'C++' },
        { value: 17, name: 'C语言' },
        { value: 8, name: 'Java' },
      ],
    }],
  })
  charts.push(c1)

  // 2. 错误类型饼图
  const c2 = echarts.init(errorChartRef.value)
  c2.setOption({
    tooltip: { trigger: 'item', backgroundColor: 'rgba(18,18,22,0.97)', borderColor: 'rgba(255,255,255,0.08)', textStyle: { color: '#d4d4d8' } },
    legend: { bottom: 0, textStyle: { color: textColor, fontSize: 11 } },
    color: ['#a5b4fc', '#71717a', '#52525b', '#3f3f46'],
    series: [{
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '45%'],
      itemStyle: { borderColor: '#0c0c0e', borderWidth: 2 },
      label: { show: false },
      data: [
        { value: 8, name: '语法错误' },
        { value: 5, name: '逻辑错误' },
        { value: 3, name: '内存问题' },
        { value: 2, name: '算法优化' },
      ],
    }],
  })
  charts.push(c2)

  // 3. 趋势折线图
  const c3 = echarts.init(trendChartRef.value)
  c3.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(18,18,22,0.97)', borderColor: 'rgba(255,255,255,0.08)', textStyle: { color: '#d4d4d8' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: splitColor } },
      axisLabel: { color: textColor, fontSize: 11 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: textColor, fontSize: 11 },
      splitLine: { lineStyle: { color: splitColor } },
    },
    series: [{
      type: 'line',
      smooth: true,
      data: [3, 5, 2, 8, 6, 12, 6],
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(165,180,252,0.15)' },
          { offset: 1, color: 'rgba(165,180,252,0)' },
        ]),
      },
      lineStyle: { color: '#a5b4fc', width: 2 },
      itemStyle: { color: '#a5b4fc', borderColor: '#0c0c0e', borderWidth: 2 },
    }],
  })
  charts.push(c3)

  // 4. 知识点柱状图
  const c4 = echarts.init(knowledgeChartRef.value)
  c4.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(18,18,22,0.97)', borderColor: 'rgba(255,255,255,0.08)', textStyle: { color: '#d4d4d8' } },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', max: 100, axisLine: { show: false }, axisLabel: { color: textColor, fontSize: 11 }, splitLine: { lineStyle: { color: splitColor } } },
    yAxis: {
      type: 'category',
      data: ['递归', '指针', '循环', '函数', '数组'],
      axisLine: { lineStyle: { color: splitColor } },
      axisLabel: { color: textColor, fontSize: 11 },
      axisTick: { show: false },
    },
    series: [{
      type: 'bar',
      data: [
        { value: 45, itemStyle: { color: '#a5b4fc' } },
        { value: 38, itemStyle: { color: '#a5b4fc' } },
        { value: 82, itemStyle: { color: '#52525b' } },
        { value: 75, itemStyle: { color: '#52525b' } },
        { value: 88, itemStyle: { color: '#52525b' } },
      ],
      barWidth: 14,
      itemStyle: { borderRadius: [0, 7, 7, 0] },
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { font-size: 26px; font-weight: 700; color: #f4f4f5; margin: 0 0 4px; }
.page-sub { color: #a1a1aa; margin: 0; font-size: 13px; }
.period-select { width: 130px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  padding: 18px;
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  transition: all 0.2s;
}
.stat-card:hover { border-color: rgba(255, 255, 255, 0.12); }
.stat-value { font-size: 24px; font-weight: 700; color: #f4f4f5; }
.stat-label { font-size: 12px; color: #a1a1aa; margin-top: 2px; }

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}
.chart-card {
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 18px;
}
.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #f4f4f5;
  margin-bottom: 12px;
}
.chart { width: 100%; height: 260px; }

.ai-recommend-section { margin-top: 28px; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
}
.ai-badge {
  display: inline-block;
  padding: 3px 10px;
  background: rgba(165, 180, 252, 0.08);
  border: 1px solid rgba(165, 180, 252, 0.15);
  border-radius: 20px;
  font-size: 11px;
  color: #a5b4fc;
  margin-bottom: 6px;
}
.section-title { font-size: 20px; font-weight: 600; color: #f4f4f5; margin: 0; }
.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 9px;
  color: #d4d4d8;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.refresh-btn:hover { background: rgba(255, 255, 255, 0.1); color: #f4f4f5; }

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.recommend-card {
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 20px;
  transition: all 0.2s;
}
.recommend-card:hover { border-color: rgba(255, 255, 255, 0.12); }
.recommend-card h3 {
  font-size: 15px;
  color: #f4f4f5;
  margin: 0 0 4px;
  font-weight: 600;
}
.recommend-desc {
  font-size: 12px;
  color: #a1a1aa;
  margin: 0 0 14px;
  line-height: 1.6;
}

.weak-list { display: flex; flex-direction: column; gap: 10px; }
.weak-item { display: flex; align-items: center; gap: 8px; }
.weak-name { font-size: 12px; color: #d4d4d8; width: 64px; flex-shrink: 0; }
.weak-bar {
  flex: 1;
  height: 5px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 3px;
  overflow: hidden;
}
.weak-progress {
  height: 100%;
  background: #c4b5fd;
  border-radius: 3px;
  transition: width 0.5s;
}
.weak-percent { font-size: 11px; color: #a1a1aa; width: 32px; text-align: right; }

.review-list { display: flex; flex-direction: column; gap: 6px; }
.review-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.review-item:hover { background: rgba(255, 255, 255, 0.05); }
.review-title { font-size: 12px; color: #d4d4d8; }
.review-tag {
  padding: 1px 7px;
  background: rgba(196, 181, 253, 0.12);
  color: #c4b5fd;
  border-radius: 4px;
  font-size: 10px;
}

.suggestion-list {
  margin: 0;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.suggestion-list li {
  font-size: 12px;
  color: #d4d4d8;
  line-height: 1.6;
}
</style>
