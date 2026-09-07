<template>
  <el-card class="page-card">
    <!-- 工具栏：筛选 / 搜索 / 新建 -->
    <div class="table-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索题目 / 知识点"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.type" placeholder="题目类型" clearable style="width: 150px" @change="handleSearch">
        <el-option label="编程题" value="PROGRAMMING" />
        <el-option label="作业" value="ASSIGNMENT" />
        <el-option label="选择题" value="CHOICE" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" @change="handleSearch">
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建题目 / 作业</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="难度" width="90">
        <template #default="{ row }">
          <el-tag :type="difficultyTag(row.difficulty)" size="small" effect="plain">
            {{ difficultyLabel(row.difficulty) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="knowledgePoint" label="知识点" width="130" show-overflow-tooltip />
      <el-table-column prop="submitCount" label="提交数" width="80" align="center" />
      <el-table-column label="正确率" width="90" align="center">
        <template #default="{ row }">
          <span>{{ row.accuracy }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            inline-prompt
            active-text="上架"
            inactive-text="下架"
            @change="(val: string | number | boolean) => onToggleStatus(row, !!val)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="140" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
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

    <!-- 新建 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目 / 作业' : '新建题目 / 作业'" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="PROGRAMMING">编程题</el-radio>
            <el-radio value="ASSIGNMENT">作业</el-radio>
            <el-radio value="CHOICE">选择题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" style="width: 100%">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点" prop="knowledgePoint">
          <el-input v-model="form.knowledgePoint" placeholder="如：数组 / 哈希表" />
        </el-form-item>
        <el-form-item label="题目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入题目描述 / 作业要求"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  createQuestion,
  deleteQuestion,
  getQuestionList,
  updateQuestion,
  updateQuestionStatus
} from "@/api/teacher";
const loading = ref(false);
const saving = ref(false);
const list = ref([]);
const total = ref(0);
const query = reactive({ page: 1, size: 10, keyword: "", type: "", status: "" });
const dialogVisible = ref(false);
const editingId = ref(null);
const formRef = ref();
const form = reactive({
  title: "",
  type: "PROGRAMMING",
  difficulty: "EASY",
  knowledgePoint: "",
  description: ""
});
const formRules = {
  title: [{ required: true, message: "\u8BF7\u8F93\u5165\u6807\u9898", trigger: "blur" }],
  type: [{ required: true, message: "\u8BF7\u9009\u62E9\u7C7B\u578B", trigger: "change" }],
  difficulty: [{ required: true, message: "\u8BF7\u9009\u62E9\u96BE\u5EA6", trigger: "change" }],
  knowledgePoint: [{ required: true, message: "\u8BF7\u8F93\u5165\u77E5\u8BC6\u70B9", trigger: "blur" }],
  description: [{ required: true, message: "\u8BF7\u8F93\u5165\u9898\u76EE\u63CF\u8FF0", trigger: "blur" }]
};
async function load() {
  loading.value = true;
  try {
    const res = await getQuestionList({ ...query });
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
  query.type = "";
  query.status = "";
  query.page = 1;
  load();
}
function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    title: "",
    type: "PROGRAMMING",
    difficulty: "EASY",
    knowledgePoint: "",
    description: ""
  });
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}
function openEdit(row) {
  editingId.value = row.id;
  Object.assign(form, {
    title: row.title,
    type: row.type,
    difficulty: row.difficulty,
    knowledgePoint: row.knowledgePoint,
    description: row.description
  });
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}
async function onSave() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    if (editingId.value) {
      await updateQuestion(editingId.value, { ...form });
      ElMessage.success("\u4FEE\u6539\u6210\u529F");
    } else {
      await createQuestion({ ...form });
      ElMessage.success("\u521B\u5EFA\u6210\u529F");
    }
    dialogVisible.value = false;
    load();
  } finally {
    saving.value = false;
  }
}
async function onToggleStatus(row, val) {
  const status = val ? 1 : 0;
  try {
    await updateQuestionStatus(row.id, status);
    row.status = status;
    ElMessage.success(status === 1 ? "\u5DF2\u4E0A\u67B6" : "\u5DF2\u4E0B\u67B6");
  } catch {
    load();
  }
}
function onDelete(row) {
  ElMessageBox.confirm(`\u786E\u5B9A\u5220\u9664\u9898\u76EE\u300C${row.title}\u300D\u5417\uFF1F\u5220\u9664\u540E\u4E0D\u53EF\u6062\u590D\u3002`, "\u5220\u9664\u786E\u8BA4", {
    type: "warning"
  }).then(async () => {
    await deleteQuestion(row.id);
    ElMessage.success("\u5220\u9664\u6210\u529F");
    load();
  }).catch(() => {
  });
}
const typeMap = {
  PROGRAMMING: { label: "\u7F16\u7A0B\u9898", tag: "primary" },
  ASSIGNMENT: { label: "\u4F5C\u4E1A", tag: "success" },
  CHOICE: { label: "\u9009\u62E9\u9898", tag: "warning" }
};
const difficultyMap = {
  EASY: { label: "\u7B80\u5355", tag: "success" },
  MEDIUM: { label: "\u4E2D\u7B49", tag: "warning" },
  HARD: { label: "\u56F0\u96BE", tag: "danger" }
};
function typeLabel(t) {
  return typeMap[t]?.label ?? t;
}
function typeTag(t) {
  return typeMap[t]?.tag ?? "info";
}
function difficultyLabel(t) {
  return difficultyMap[t]?.label ?? t;
}
function difficultyTag(t) {
  return difficultyMap[t]?.tag ?? "info";
}
onMounted(load);

</script>