<template>
  <div v-loading="loading">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="26"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-meta">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 + 错题分布 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">近两周提交量与平均正确率趋势</span>
          </template>
          <LineChart
            :labels="trendLabels"
            :series="trendSeries"
            height="340px"
            y-name="数量 / 正确率(%)"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">错题知识点分布</span>
          </template>
          <PieChart :data="wrongDistribution" height="340px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 班级活跃度 + 最近提交 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">各班级提交量与活跃学生数</span>
          </template>
          <BarChart :labels="classLabels" :series="classSeries" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">最近提交</span>
              <el-button link type="primary" @click="$router.push('/teacher/review')">
                全部审阅
              </el-button>
            </div>
          </template>
          <el-table :data="recentSubmissions" size="small">
            <el-table-column prop="studentName" label="学生" width="90" />
            <el-table-column prop="questionTitle" label="题目" min-width="140" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submittedAt" label="提交时间" width="140" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" @click="$router.push(`/teacher/review/${row.id}`)">
                  审阅
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { DataLine, DocumentChecked, TrendCharts, User, Warning } from '@element-plus/icons-vue'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import { getDashboardStats } from '@/api/dashboard'
import type { DashboardStats } from '@/api/schema'

const loading = ref(false)
const stats = ref<DashboardStats | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
  } finally {
    loading.value = false
  }
})

const statCards = computed(() => {
  const s = stats.value
  return [
    { label: '学生总数', value: s?.studentCount ?? 0, icon: User, bg: '#ecf5ff', color: '#409eff' },
    { label: '今日提交', value: s?.todaySubmit ?? 0, icon: DocumentChecked, bg: '#f0f9eb', color: '#67c23a' },
    { label: '待批改', value: s?.pendingReview ?? 0, icon: Warning, bg: '#fdf6ec', color: '#e6a23c' },
    { label: '平均正确率', value: s ? `${s.avgAccuracy}%` : '--', icon: TrendCharts, bg: '#fef0f0', color: '#f56c6c' },
  ]
})

const trendLabels = computed(() => stats.value?.submitTrend.map((t) => t.date) ?? [])
const trendSeries = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { name: '提交量', data: s.submitTrend.map((t) => t.submitCount) },
    { name: '正确率(%)', data: s.submitTrend.map((t) => t.accuracy) },
  ]
})

const wrongDistribution = computed(() =>
  (stats.value?.wrongDistribution ?? []).map((w) => ({ name: w.knowledgePoint, value: w.wrongCount }))
)

const classLabels = computed(() => stats.value?.classActivity.map((c) => c.className) ?? [])
const classSeries = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { name: '提交量', data: s.classActivity.map((c) => c.submitCount) },
    { name: '活跃学生', data: s.classActivity.map((c) => c.activeStudents) },
  ]
})

const recentSubmissions = computed(() => stats.value?.recentSubmissions ?? [])

const statusMap: Record<string, { label: string; tag: 'primary' | 'success' | 'danger' | 'warning' | 'info' }> = {
  PENDING: { label: '待批改', tag: 'warning' },
  GRADED: { label: '已批改', tag: 'success' },
  REJECTED: { label: '需复核', tag: 'danger' },
}
function statusLabel(s: string) {
  return statusMap[s]?.label ?? s
}
function statusTag(s: string) {
  return statusMap[s]?.tag ?? 'info'
}
</script>

<style scoped lang="scss">
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>