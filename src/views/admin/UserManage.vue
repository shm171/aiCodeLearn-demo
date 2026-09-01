<template>
  <el-card class="page-card">
    <div class="table-toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索用户名 / 邮箱"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 130px" @change="handleSearch">
        <el-option label="学生" value="STUDENT" />
        <el-option label="教师" value="TEACHER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="success" :icon="Plus" @click="openCreate">新建用户</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="110" />
      <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
      <el-table-column label="角色" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="roleTag(row.role)" size="small">{{ roleName(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="150" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openRole(row)">分配角色</el-button>
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

    <el-dialog v-model="createVisible" title="新建用户" width="480px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password placeholder="8~16 位" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" title="分配角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentRow?.username }}（{{ currentRow?.email }}）</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="roleForm.role" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingRole" @click="onSaveRole">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createUser, deleteUser, getUserList, updateUserRole } from '@/api/admin'
import type { AdminUser, Role, UserQuery } from '@/api/schema'

const loading = ref(false)
const saving = ref(false)
const savingRole = ref(false)
const list = ref<AdminUser[]>([])
const total = ref(0)
const query = reactive<UserQuery>({ page: 1, size: 10, keyword: '', role: '' })

const createVisible = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({ username: '', email: '', password: '', role: 'STUDENT' })
const createRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 16, message: '密码长度 8~16 位', trigger: 'blur' },
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const roleVisible = ref(false)
const currentRow = ref<AdminUser | null>(null)
const roleForm = reactive<{ role: Role }>({ role: 'STUDENT' })

async function load() {
  loading.value = true
  try {
    const res = await getUserList({ ...query })
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
  query.role = ''
  query.page = 1
  load()
}

function openCreate() {
  Object.assign(createForm, { username: '', email: '', password: '', role: 'STUDENT' })
  createVisible.value = true
  createFormRef.value?.clearValidate()
}

async function onCreate() {
  if (!createFormRef.value) return
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createUser({ ...createForm })
    ElMessage.success('创建成功')
    createVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openRole(row: AdminUser) {
  currentRow.value = row
  roleForm.role = row.role
  roleVisible.value = true
}

async function onSaveRole() {
  if (!currentRow.value) return
  savingRole.value = true
  try {
    await updateUserRole(currentRow.value.id, roleForm.role)
    ElMessage.success('角色已更新')
    roleVisible.value = false
    load()
  } finally {
    savingRole.value = false
  }
}

function onDelete(row: AdminUser) {
  ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

const roleNameMap: Record<Role, string> = { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }
function roleName(r: string) {
  return roleNameMap[r as Role] || r
}
function roleTag(r: string) {
  const map: Record<string, 'primary' | 'success' | 'danger' | 'warning' | 'info'> = {
    STUDENT: 'primary',
    TEACHER: 'success',
    ADMIN: 'danger',
  }
  return map[r] || 'info'
}

onMounted(load)
</script>