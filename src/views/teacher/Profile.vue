<template>
  <el-card class="page-card profile-card" v-loading="loading">
    <template #header><span class="card-title">个人中心</span></template>

    <div class="profile-body">
      <div class="avatar-col">
        <el-avatar :size="88" class="avatar">{{ (form.username || '师')[0] }}</el-avatar>
        <div class="role-tag">
          <el-tag type="warning" effect="plain">{{ roleLabel }}</el-tag>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="profile-form"
        style="max-width: 520px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input :model-value="form.email" disabled />
          <div class="form-tip">邮箱为登录账号，暂不支持修改</div>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="个人简介" prop="bio">
          <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="介绍一下自己（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  username: '',
  phone: '',
  bio: '',
  email: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

const roleLabel = computed(() => {
  const map: Record<string, string> = { TEACHER: '教师', ADMIN: '管理员', STUDENT: '学生' }
  return map[userStore.role] || userStore.role
})

onMounted(async () => {
  loading.value = true
  try {
    if (!userStore.profile) await userStore.fetchProfile()
    const p = userStore.profile
    if (p) {
      form.username = p.username
      form.phone = p.phone || ''
      form.bio = p.bio || ''
      form.email = p.email
    }
  } finally {
    loading.value = false
  }
})

async function onSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await userStore.updateProfile({ username: form.username, phone: form.phone, bio: form.bio })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.card-title {
  font-weight: 600;
}

.profile-body {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.avatar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding-top: 8px;

  .avatar {
    background: #409eff;
    color: #fff;
    font-size: 32px;
  }
}

.profile-form {
  flex: 1;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>