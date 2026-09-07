<template>
  <div v-loading="loading">
    <el-card class="page-card" v-if="report">
      <template #header>
        <div class="report-header">
          <div>
            <el-button link @click="$router.push('/teacher/students')">
              <el-icon><ArrowLeft /></el-icon>返回学生列表
            </el-button>
            <span class="title">{{ report.name }}</span>
            <el-tag size="small" type="info">{{ report.className }}</el-tag>
          </div>
          <div class="report-meta">
            <el-tag type="success" effect="plain">学习报告</el-tag>
          </div>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :xs="24" :lg="14">
          <el-card shadow="never" class="block-card">
            <template #header><span class="block-title">近两周提交量与正确率</span></template>
            <LineChart :labels="trendLabels" :series="trendSeries" height="320px" y-name="数量 / 正确率(%)" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="block-card">
            <template #header><span class="block-title">知识点掌握度</span></template>
            <RadarChart :indicators="radarIndicators" :series="radarSeries" height="320px" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="block-card">
            <template #header><span class="block-title">错题知识点分布</span></template>
            <PieChart :data="wrongDistribution" height="300px" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="14">
          <el-card shadow="never" class="block-card">
            <template #header><span class="block-title">近期提交记录</span></template>
            <el-table :data="report.recentSubmissions" size="small">
              <el-table-column prop="questionTitle" label="题目" min-width="160" show-overflow-tooltip />
              <el-table-column prop="submittedAt" label="提交时间" width="150" />
              <el-table-column label="LLM 评分" width="90" align="center">
                <template #default="{ row }">
                  <span v-if="row.llmScore !== null">{{ row.llmScore }}</span>
                  <span v-else class="muted">--</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="report.recentSubmissions.length === 0" description="暂无提交记录" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { getStudentReport } from "@/api/teacher";
const route = useRoute();
const id = Number(route.params.id);
const loading = ref(false);
const report = ref(null);
onMounted(async () => {
  loading.value = true;
  try {
    report.value = await getStudentReport(id);
  } finally {
    loading.value = false;
  }
});
const trendLabels = computed(() => report.value?.trend.map((t) => t.date) ?? []);
const trendSeries = computed(() => {
  const r = report.value;
  if (!r) return [];
  return [
    { name: "\u63D0\u4EA4\u91CF", data: r.trend.map((t) => t.submitCount) },
    { name: "\u6B63\u786E\u7387(%)", data: r.trend.map((t) => t.accuracy) }
  ];
});
const radarIndicators = computed(() => (report.value?.knowledge ?? []).map((k) => ({ name: k.name, max: 100 })));
const radarSeries = computed(() => {
  const r = report.value;
  if (!r) return [];
  return [{ name: r.name, value: r.knowledge.map((k) => k.value) }];
});
const wrongDistribution = computed(
  () => (report.value?.wrongDistribution ?? []).map((w) => ({ name: w.knowledgePoint, value: w.wrongCount }))
);
const statusMap = {
  PENDING: { label: "\u5F85\u6279\u6539", tag: "warning" },
  GRADED: { label: "\u5DF2\u6279\u6539", tag: "success" },
  REJECTED: { label: "\u9700\u590D\u6838", tag: "danger" }
};
function statusLabel(s) {
  return statusMap[s]?.label ?? s;
}
function statusTag(s) {
  return statusMap[s]?.tag ?? "info";
}

</script>

<style scoped lang="scss">
.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 10px;
  }
}

.block-card {
  margin-bottom: 16px;
}

.block-title {
  font-weight: 600;
}

.muted {
  color: #c0c4cc;
}
</style>