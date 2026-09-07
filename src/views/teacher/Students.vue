<template>
  <el-card class="page-card">
    <div class="table-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索学生姓名"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.className" placeholder="班级" clearable style="width: 160px" @change="handleSearch">
        <el-option v-for="c in classNames" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="className" label="班级" width="130" />
      <el-table-column prop="submitCount" label="提交次数" width="90" align="center" />
      <el-table-column label="平均正确率" width="120" align="center">
        <template #default="{ row }">
          <el-progress
            :percentage="row.avgAccuracy"
            :stroke-width="10"
            :color="accuracyColor(row.avgAccuracy)"
            style="max-width: 120px"
          />
        </template>
      </el-table-column>
      <el-table-column label="错题数" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.wrongCount >= 10 ? 'danger' : row.wrongCount >= 5 ? 'warning' : 'info'" size="small">
            {{ row.wrongCount }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="activeDays" label="活跃天数" width="90" align="center" />
      <el-table-column prop="lastActiveAt" label="最近活跃" width="150" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/teacher/students/${row.id}/report`)">
            查看报告
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
import { getStudentList } from "@/api/teacher";
const loading = ref(false);
const list = ref([]);
const total = ref(0);
const query = reactive({ page: 1, size: 10, keyword: "", className: "" });
const classNames = ["\u8BA1\u7B97\u673A 2101", "\u8BA1\u7B97\u673A 2102", "\u8BA1\u7B97\u673A 2103", "\u8F6F\u4EF6 2101"];
async function load() {
  loading.value = true;
  try {
    const res = await getStudentList({ ...query });
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
  query.className = "";
  query.page = 1;
  load();
}
function accuracyColor(v) {
  if (v >= 85) return "#67c23a";
  if (v >= 70) return "#e6a23c";
  return "#f56c6c";
}
onMounted(load);

</script>