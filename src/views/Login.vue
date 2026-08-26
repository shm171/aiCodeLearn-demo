<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <el-icon :size="40" color="#409eff"><Platform /></el-icon>
        <h1>AI Learn 智能学习平台</h1>
        <p>教师端 · 数据看板与教学管理</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @keyup.enter="onSubmit"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入登录邮箱" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button
          type="primary"
          class="login-btn"
          size="large"
          :loading="loading"
          @click="onSubmit"
        >
          登 录
        </el-button>
      </el-form>

      <el-alert type="info" :closable="false" class="demo-tip">
        <template #title>
          演示账号：teacher@ailearn.com / 123456
          <el-link type="primary" :underline="false" @click="fillDemo">一键填入</el-link>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ email: '', password: '' })

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/teacher/dashboard'
    router.push(redirect)
  } catch {
    // 错误提示由请求层统一处理
  } finally {
    loading.value = false
  }
}

function fillDemo() {
  form.email = 'teacher@ailearn.com'
  form.password = '123456'
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 45%, #2c5364 100%);
}

.login-card {
  width: 400px;
  padding: 40px 36px 28px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.login-brand {
  text-align: center;
  margin-bottom: 28px;

  h1 {
    font-size: 20px;
    margin: 12px 0 6px;
    color: #303133;
  }

  p {
    margin: 0;
    color: #909399;
    font-size: 13px;
  }
}

.login-btn {
  width: 100%;
  margin-top: 4px;
}

.demo-tip {
  margin-top: 18px;
}
</style>