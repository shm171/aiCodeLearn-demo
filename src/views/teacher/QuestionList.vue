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

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createQuestion,
  deleteQuestion,
  getQuestionList,
  updateQuestion,
  updateQuestionStatus,
} from '@/api/teacher'
import type { Question, QuestionPayload, QuestionQuery } from '@/api/schema'

const loading = ref(false)
const saving = ref(false)
const list = ref<Question[]>([])
const total = ref(0)
const query = reactive<QuestionQuery>({ page: 1, size: 10, keyword: '', type: '', status: '' })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<QuestionPayload>({
  title: '',
  type: 'PROGRAMMING',
  difficulty: 'EASY',
  knowledgePoint: '',
  description: '',
})

const formRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  knowledgePoint: [{ required: true, message: '请输入知识点', trigger: 'blur' }],
  description: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const res = await getQuestionList({ ...query })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  load()
}

function handleReset() {
  query.keyword = ''
  query.type = ''
  query.status = ''
  query.page = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    title: '',
    type: 'PROGRAMMING',
    difficulty: 'EASY',
    knowledgePoint: '',
    description: '',
  })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEdit(row: Question) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    type: row.type,
    difficulty: row.difficulty,
    knowledgePoint: row.knowledgePoint,
    description: row.description,
  })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function onSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateQuestion(editingId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createQuestion({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onToggleStatus(row: Question, val: boolean) {
  const status: 0 | 1 = val ? 1 : 0
  try {
    await updateQuestionStatus(row.id, status)
    row.status = status
    ElMessage.success(status === 1 ? '已上架' : '已下架')
  } catch {
    // 失败时由请求层提示，switch 回显由重新加载修正
    load()
  }
}

function onDelete(row: Question) {
  ElMessageBox.confirm(`确定删除题目「${row.title}」吗？删除后不可恢复。`, '删除确认', {
    type: 'warning',
  })
    .then(async () => {
      await deleteQuestion(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

const typeMap: Record<string, { label: string; tag: 'primary' | 'success' | 'warning' | 'info' }> = {
  PROGRAMMING: { label: '编程题', tag: 'primary' },
  ASSIGNMENT: { label: '作业', tag: 'success' },
  CHOICE: { label: '选择题', tag: 'warning' },
}
const difficultyMap: Record<string, { label: string; tag: 'success' | 'warning' | 'danger' }> = {
  EASY: { label: '简单', tag: 'success' },
  MEDIUM: { label: '中等', tag: 'warning' },
  HARD: { label: '困难', tag: 'danger' },
}
function typeLabel(t: string) {
  return typeMap[t]?.label ?? t
}
function typeTag(t: string) {
  return typeMap[t]?.tag ?? 'info'
}
function difficultyLabel(t: string) {
  return difficultyMap[t]?.label ?? t
}
function difficultyTag(t: string) {
  return difficultyMap[t]?.tag ?? 'info'
}

onMounted(load)
</script>