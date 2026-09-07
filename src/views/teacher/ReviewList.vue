<template>
  <el-card class="page-card">
    <div class="table-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索学生 / 题目"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.status" placeholder="审阅状态" clearable style="width: 140px" @change="handleSearch">
        <el-option label="待批改" value="PENDING" />
        <el-option label="已批改" value="GRADED" />
        <el-option label="需复核" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="questionTitle" label="题目 / 作业" min-width="180" show-overflow-tooltip />
      <el-table-column prop="submittedAt" label="提交时间" width="150" />
      <el-table-column label="静态检查问题" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.staticIssueCount > 0 ? 'danger' : 'success'" size="small" effect="plain">
            {{ row.staticIssueCount }} 个
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="LLM 评分" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.llmScore !== null" :class="{ 'score-low': row.llmScore < 60 }">
            {{ row.llmScore }}
          </span>
          <span v-else class="muted">--</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/teacher/review/${row.id}`)">
            去审阅
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @change="load"
      />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { getSubmissionList } from "@/api/teacher";
const loading = ref(false);
const list = ref([]);
const total = ref(0);
const query = reactive({ page: 1, size: 10, keyword: "", status: "" });
async function load() {
  loading.value = true;
  try {
    const res = await getSubmissionList({ ...query });
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
}
function handleSearch() {
  query.page = 1;
  load();
}
function handleReset() {
  query.keyword = "";
  query.status = "";
  query.page = 1;
  load();
}
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
onMounted(load);

</script>

<style scoped lang="scss">
.score-low {
  color: #f56c6c;
  font-weight: 600;
}
.muted {
  color: #c0c4cc;
}
</style>