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
      <el-select v-model="query.knowledgePoint" placeholder="知识点" clearable style="width: 180px" @change="handleSearch">
        <el-option v-for="kp in knowledgePoints" :key="kp" :label="kp" :value="kp" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="success" :icon="Download" :loading="exporting" @click="onExport">
        导出错题统计
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="questionTitle" label="题目" min-width="180" show-overflow-tooltip />
      <el-table-column prop="knowledgePoint" label="知识点" width="140" show-overflow-tooltip />
      <el-table-column prop="errorType" label="错误类型" width="110" />
      <el-table-column label="错误次数" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.errorCount >= 4 ? 'danger' : row.errorCount >= 2 ? 'warning' : 'info'" size="small">
            {{ row.errorCount }} 次
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastOccurredAt" label="最近出错" width="120" />
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
import { ElMessage } from "element-plus";
import { exportWrongQuestions, getWrongQuestions } from "@/api/teacher";
const loading = ref(false);
const exporting = ref(false);
const list = ref([]);
const total = ref(0);
const query = reactive({ page: 1, size: 10, keyword: "", knowledgePoint: "" });
const knowledgePoints = ["\u6570\u7EC4 / \u54C8\u5E0C\u8868", "\u94FE\u8868", "\u6808", "\u6811 / BFS", "\u52A8\u6001\u89C4\u5212", "\u6ED1\u52A8\u7A97\u53E3", "\u6392\u5E8F", "\u57FA\u7840\u8BED\u6CD5"];
async function load() {
  loading.value = true;
  try {
    const res = await getWrongQuestions({ ...query });
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
  query.knowledgePoint = "";
  query.page = 1;
  load();
}
async function onExport() {
  exporting.value = true;
  try {
    const blob = await exportWrongQuestions();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `\u9519\u9898\u7EDF\u8BA1_${(/* @__PURE__ */ new Date()).toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    ElMessage.success("\u5BFC\u51FA\u6210\u529F");
  } finally {
    exporting.value = false;
  }
}
onMounted(load);

</script>