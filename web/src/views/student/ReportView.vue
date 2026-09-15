<!-- 学习报告 ReportView.vue - 对接真实接口 -->
<template>
  <div class="report-page" v-loading="loading" element-loading-text="正在生成学习报告..." element-loading-background="rgba(10, 10, 12, 0.85)">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">学习报告</h1>
        <p class="page-sub">AI 智能分析你的编程学习数据</p>
      </div>
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
        <div class="chart-title">错误分类分布</div>
        <div ref="langChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">月度学习曲线</div>
        <div ref="errorChartRef" class="chart"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-title">知识点掌握度</div>
        <div ref="trendChartRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">薄弱知识点排行</div>
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
import { getReportApi, getWeakTopicsApi } from '../../api/review'

const router = useRouter()
const refreshing = ref(false)
const loading = ref(false)

const langChartRef = ref(null)
const errorChartRef = ref(null)
const trendChartRef = ref(null)
const knowledgeChartRef = ref(null)

let charts = []

// 响应式数据
const stats = ref([
  { label: '总提交次数', value: 0 },
  { label: '平均正确率', value: '0%' },
  { label: '错题数量', value: 0 },
  { label: '待复习错题', value: 0 },
])

const weakPoints = ref([])
const reviewQuestions = ref([])
const suggestions = ref([])

// 错误分类枚举 → 中文名
function categoryLabel(category) {
  const map = { FORMAT_ERROR: '格式错误', SYNTAX_ERROR: '语法错误', LOGIC_ERROR: '逻辑错误' }
  return map[category] || category || '未分类'
}

// 加载所有数据
async function loadAllData() {
  loading.value = true
  try {
    // 并行加载学习报告和薄弱知识点
    const [reportRes, weakRes] = await Promise.all([
      getReportApi().catch(() => null),
      getWeakTopicsApi().catch(() => null),
    ])

    if (!reportRes) throw new Error('report unavailable')

    // 数据概览：accuracyRate 是 0.0~1.0 的小数，显示百分比要乘以100
    const unmasteredCount = (reportRes.practiceList || []).filter((e) => !e.mastered).length
    stats.value = [
      { label: '总提交次数', value: reportRes.totalSubmissions ?? 0 },
      { label: '平均正确率', value: `${Math.round((reportRes.accuracyRate ?? 0) * 100)}%` },
      { label: '错题数量', value: reportRes.totalErrors ?? 0 },
      { label: '待复习错题', value: unmasteredCount },
    ]

    // 薄弱知识点：mastery 是 0.0~1.0 小数，乘以100转百分比
    const weakList =
      reportRes.topWeakPoints && reportRes.topWeakPoints.length > 0
        ? reportRes.topWeakPoints
        : Array.isArray(weakRes)
          ? weakRes
          : []
    weakPoints.value = weakList.map((item) => ({
      name: item.errorType || categoryLabel(item.category),
      mastery: Math.round((item.mastery ?? 0) * 100),
      errorCount: item.count || 0,
    }))

    // 推荐复习错题：取前3个未掌握的
    reviewQuestions.value = (reportRes.practiceList || [])
      .filter((e) => !e.mastered)
      .slice(0, 3)
      .map((e, i) => ({
        id: e.id ?? i,
        title: e.errorType || categoryLabel(e.category) || `错题 ${i + 1}`,
        knowledge: categoryLabel(e.category),
      }))

    // 学习建议：后端一句话诊断 + 根据薄弱点补充
    suggestions.value = buildSuggestions(reportRes)

    // 更新图表
    updateCharts(reportRes)
  } catch (error) {
    console.error('加载学习报告失败:', error)
    stats.value = stats.value.map((s) => ({ ...s, value: s.label === '平均正确率' ? '0%' : 0 }))
    weakPoints.value = []
    reviewQuestions.value = []
    suggestions.value = ['暂无学习数据，先去提交一份作业吧！']
    updateCharts(null)
  } finally {
    loading.value = false
  }
}

// 根据报告数据生成学习建议
function buildSuggestions(report) {
  const tips = []
  if (report.summary) tips.push(report.summary)
  const weak = weakPoints.value
  if (weak.length > 0) {
    tips.push(`重点复习「${weak[0].name}」相关知识点，这是你最薄弱的环节`)
  }
  if (report.totalErrors > 10) {
    tips.push('错题数量较多，建议每天复习3-5道错题，不要只做新题')
  } else if (report.totalSubmissions > 0) {
    tips.push('每天坚持提交1-2道编程题，保持代码手感')
  }
  if (report.totalSubmissions > 0 && report.accuracyRate < 0.7) {
    tips.push('提交代码前先自己检查一遍，减少低级错误')
  }
  return tips.slice(0, 4)
}

// 刷新推荐
async function refreshRecommend() {
  refreshing.value = true
  try {
    await loadAllData()
    ElMessage.success('AI 已重新分析你的学习数据')
  } finally {
    refreshing.value = false
  }
}

function goToWrongQuestions() {
  router.push('/wrong-questions')
}

// ========== 图表相关 ==========
const textColor = '#71717a'
const splitColor = 'rgba(255,255,255,0.04)'
const accentColor = '#c4b5fd'
// 高区分度配色：淡紫、天蓝、翠绿、金黄、橙红
const chartColors = ['#c4b5fd', '#60a5fa', '#34d399', '#fbbf24', '#f87171', '#a78bfa']

// 通用深色 tooltip 配置
const darkTooltip = {
  backgroundColor: 'rgba(18,18,22,0.97)',
  borderColor: 'rgba(255,255,255,0.08)',
  textStyle: { color: '#d4d4d8' },
}

// 空数据的占位提示
function emptyChartOption() {
  return {
    title: {
      text: '暂无数据',
      left: 'center',
      top: 'middle',
      textStyle: { color: '#52525b', fontSize: 14, fontWeight: 'normal' },
    },
  }
}

function initCharts() {
  // 销毁旧图表
  charts.forEach((c) => c && c.dispose())
  charts = []

  // 1. 错误分类分布饼图
  const c1 = echarts.init(langChartRef.value)
  charts.push(c1)

  // 2. 月度学习曲线折线图
  const c2 = echarts.init(errorChartRef.value)
  charts.push(c2)

  // 3. 知识点掌握度柱状图
  const c3 = echarts.init(trendChartRef.value)
  charts.push(c3)

  // 4. 薄弱知识点排行柱状图
  const c4 = echarts.init(knowledgeChartRef.value)
  charts.push(c4)
}

function updateCharts(report) {
  if (charts.length === 0) initCharts()

  const weak = weakPoints.value
  const distribution = report?.distribution?.slices || []
  const curvePoints = report?.studentCurve?.points || []

  // 1. 错误分类分布（饼图）
  if (distribution.length > 0) {
    charts[0].setOption({
      tooltip: { trigger: 'item', ...darkTooltip },
      legend: { bottom: 0, textStyle: { color: textColor, fontSize: 11 } },
      color: chartColors,
      series: [{
        type: 'pie', radius: ['45%', '68%'], center: ['50%', '45%'],
        itemStyle: { borderColor: '#0c0c0e', borderWidth: 2 },
        label: { show: false },
        data: distribution.map((s) => ({ name: s.label, value: s.value })),
      }],
    })
  } else {
    charts[0].setOption(emptyChartOption())
  }

  // 2. 月度学习曲线（折线图：提交数 + 错题数两条线）
  if (curvePoints.length > 0) {
    const labels = curvePoints.map((p) => `${p.year}/${String(p.month).padStart(2, '0')}`)
    const seriesData = (key) => curvePoints.map((p) => p[key] || 0)
    charts[1].setOption({
      tooltip: { trigger: 'axis', ...darkTooltip },
      legend: { bottom: 0, textStyle: { color: textColor, fontSize: 11 } },
      grid: { left: '3%', right: '4%', bottom: '16%', top: '8%', containLabel: true },
      xAxis: {
        type: 'category', data: labels,
        axisLine: { lineStyle: { color: splitColor } },
        axisLabel: { color: textColor, fontSize: 11 },
        axisTick: { show: false },
      },
      yAxis: {
        type: 'value', axisLine: { show: false },
        axisLabel: { color: textColor, fontSize: 11 },
        splitLine: { lineStyle: { color: splitColor } },
      },
      series: [{
        name: '提交数', type: 'line', smooth: true, data: seriesData('submissionCount'),
        symbol: 'circle', symbolSize: 6,
        lineStyle: { color: accentColor, width: 2 },
        itemStyle: { color: accentColor, borderColor: '#0c0c0e', borderWidth: 2 },
      }, {
        name: '错题数', type: 'line', smooth: true, data: seriesData('errorCount'),
        symbol: 'circle', symbolSize: 6,
        lineStyle: { color: '#60a5fa', width: 2 },
        itemStyle: { color: '#60a5fa', borderColor: '#0c0c0e', borderWidth: 2 },
      }],
    })
  } else {
    charts[1].setOption(emptyChartOption())
  }

  // 3. 知识点掌握度（横向柱状图，mastery 已转成 0~100）
  const knowledgeData = weak.map((w) => ({ name: w.name, value: w.mastery }))
  if (knowledgeData.length > 0) {
    charts[2].setOption({
      tooltip: { trigger: 'axis', ...darkTooltip, formatter: (params) => `${params[0].name}：掌握度 ${params[0].value}%` },
      grid: { left: '3%', right: '10%', bottom: '3%', top: '5%', containLabel: true },
      xAxis: { type: 'value', max: 100, axisLine: { show: false }, axisLabel: { color: textColor, fontSize: 11 }, splitLine: { lineStyle: { color: splitColor } } },
      yAxis: {
        type: 'category', data: knowledgeData.map((k) => k.name),
        axisLine: { lineStyle: { color: splitColor } },
        axisLabel: { color: textColor, fontSize: 11 },
        axisTick: { show: false },
      },
      series: [{
        type: 'bar',
        data: knowledgeData.map((k) => ({
          value: k.value,
          itemStyle: {
            color: k.value < 50 ? '#f87171' : k.value < 75 ? '#fbbf24' : '#34d399',
            borderRadius: [0, 7, 7, 0],
          },
        })),
        barWidth: 16,
      }],
    })
  } else {
    charts[2].setOption(emptyChartOption())
  }

  // 4. 薄弱知识点排行（按错题条数横向柱状图）
  const rankData = weak.map((w) => ({ name: w.name, value: w.errorCount }))
  if (rankData.length > 0) {
    charts[3].setOption({
      tooltip: { trigger: 'axis', ...darkTooltip },
      grid: { left: '3%', right: '10%', bottom: '3%', top: '5%', containLabel: true },
      xAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: textColor, fontSize: 11 }, splitLine: { lineStyle: { color: splitColor } } },
      yAxis: {
        type: 'category', data: rankData.map((k) => k.name),
        axisLine: { lineStyle: { color: splitColor } },
        axisLabel: { color: textColor, fontSize: 11 },
        axisTick: { show: false },
      },
      series: [{
        type: 'bar',
        data: rankData.map((k) => ({ value: k.value, itemStyle: { color: '#c4b5fd', borderRadius: [0, 7, 7, 0] } })),
        barWidth: 16,
        label: { show: true, position: 'right', color: textColor, fontSize: 11 },
      }],
    })
  } else {
    charts[3].setOption(emptyChartOption())
  }
}

function handleResize() {
  charts.forEach((c) => c && c.resize())
}

onMounted(() => {
  initCharts()
  loadAllData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach((c) => c && c.dispose())
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
  background: rgba(196, 181, 253, 0.08);
  border: 1px solid rgba(196, 181, 253, 0.15);
  border-radius: 20px;
  font-size: 11px;
  color: #c4b5fd;
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
  gap: 16px;
}
.recommend-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 28px;
  transition: all 0.2s;
}
.recommend-card:hover { border-color: rgba(255, 255, 255, 0.15); transform: translateY(-2px); }
.recommend-card h3 {
  font-size: 18px;
  color: #f4f4f5;
  margin: 0 0 6px;
  font-weight: 700;
}
.recommend-desc {
  font-size: 13px;
  color: #a1a1aa;
  margin: 0 0 20px;
  line-height: 1.6;
}

.weak-list { display: flex; flex-direction: column; gap: 14px; }
.weak-item { display: flex; align-items: center; gap: 10px; }
.weak-name { font-size: 14px; color: #d4d4d8; width: 80px; flex-shrink: 0; font-weight: 500; }
.weak-bar {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  overflow: hidden;
}
.weak-progress {
  height: 100%;
  background: linear-gradient(90deg, #c4b5fd, #a78bfa);
  border-radius: 4px;
  transition: width 0.5s;
}
.weak-percent { font-size: 13px; color: #c4b5fd; width: 40px; text-align: right; font-weight: 600; }

.review-list { display: flex; flex-direction: column; gap: 10px; }
.review-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
}
.review-item:hover { background: rgba(255, 255, 255, 0.07); }
.review-title { font-size: 14px; color: #d4d4d8; font-weight: 500; }
.review-tag {
  padding: 3px 10px;
  background: rgba(196, 181, 253, 0.12);
  color: #c4b5fd;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.suggestion-list {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.suggestion-list li {
  font-size: 14px;
  color: #d4d4d8;
  line-height: 1.7;
}
</style>
