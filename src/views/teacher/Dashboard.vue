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

    <!-- 易错知识点排行 + 错题分类分布 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">班级易错知识点排行</span>
          </template>
          <BarChart :labels="topicLabels" :series="topicSeries" height="340px" y-name="错题数" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">错题分类分布</span>
          </template>
          <PieChart :data="categoryDistribution" height="340px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生错题统计 + 待关注学生 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">学生错题统计（按错题数降序，便于定位后进生）</span>
          </template>
          <el-table :data="studentStats" size="small" border stripe>
            <el-table-column prop="ownerUserId" label="学生 ID" width="90" />
            <el-table-column label="错题数" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.errorCount >= 12 ? 'danger' : row.errorCount >= 6 ? 'warning' : 'info'" size="small">
                  {{ row.errorCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submissionCount" label="提交数" width="90" align="center" />
            <el-table-column prop="lastActiveAt" label="最近活跃" min-width="150" />
            <el-table-column label="待关注" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.attention ? 'danger' : 'success'" size="small" effect="plain">
                  {{ row.attention ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">待关注学生</span>
          </template>
          <div v-if="attentionStudents.length" class="attention-list">
            <el-tag
              v-for="uid in attentionStudents"
              :key="uid"
              type="danger"
              effect="light"
              class="attention-tag"
            >
              学生 #{{ uid }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无待关注学生" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { DocumentChecked, TrendCharts, User, Warning } from "@element-plus/icons-vue";
import { getDashboardStats } from "@/api/dashboard";
const loading = ref(false);
const stats = ref(null);
onMounted(async () => {
  loading.value = true;
  try {
    stats.value = await getDashboardStats();
  } finally {
    loading.value = false;
  }
});
const statCards = computed(() => {
  const s = stats.value;
  return [
    { label: "\u73ED\u7EA7\u603B\u63D0\u4EA4\u6570", value: s?.totalSubmissions ?? 0, icon: DocumentChecked, bg: "#ecf5ff", color: "#409eff" },
    { label: "\u73ED\u7EA7\u603B\u9519\u9898\u6570", value: s?.totalErrors ?? 0, icon: Warning, bg: "#fef0f0", color: "#f56c6c" },
    { label: "\u5B66\u751F\u4EBA\u6570", value: s?.perStudentStats.length ?? 0, icon: User, bg: "#f0f9eb", color: "#67c23a" },
    { label: "\u5F85\u5173\u6CE8\u5B66\u751F", value: s?.attentionStudentIds.length ?? 0, icon: TrendCharts, bg: "#fdf6ec", color: "#e6a23c" }
  ];
});
const topicLabels = computed(() => stats.value?.classTopicRanking.map((t) => t.topic) ?? []);
const topicSeries = computed(
  () => stats.value ? [{ name: "\u9519\u9898\u6570", data: stats.value.classTopicRanking.map((t) => t.errorCount) }] : []
);
const categoryDistribution = computed(
  () => (stats.value?.classCategoryDistribution ?? []).map((c) => ({ name: c.label, value: c.value }))
);
const studentStats = computed(() => stats.value?.perStudentStats ?? []);
const attentionStudents = computed(() => stats.value?.attentionStudentIds ?? []);

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

.attention-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.attention-tag {
  font-size: 13px;
}
</style>