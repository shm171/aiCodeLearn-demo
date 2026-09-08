<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <el-icon :size="40" color="#67c23a"><Platform /></el-icon>
        <h1>EduCode 智能学习平台</h1>
        <p>管理端 · 用户与权限管理</p>
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
        <el-button type="success" class="login-btn" size="large" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>

      <el-alert type="info" :closable="false" class="demo-tip">
        <template #title>
          演示账号：admin@ailearn.com / 123456
          <el-link type="primary" :underline="false" @click="fillDemo">一键填入</el-link>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { Message, Lock } from '@element-plus/icons-vue'
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const formRef = ref();
const loading = ref(false);
const form = reactive({ email: "", password: "" });
const rules = {
  email: [
    { required: true, message: "\u8BF7\u8F93\u5165\u90AE\u7BB1", trigger: "blur" },
    { type: "email", message: "\u90AE\u7BB1\u683C\u5F0F\u4E0D\u6B63\u786E", trigger: ["blur", "change"] }
  ],
  password: [{ required: true, message: "\u8BF7\u8F93\u5165\u5BC6\u7801", trigger: "blur" }]
};
async function onSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await userStore.login({ ...form });
    ElMessage.success("\u767B\u5F55\u6210\u529F");
    const redirect = route.query.redirect || "/admin/dashboard";
    router.push(redirect);
  } catch {
  } finally {
    loading.value = false;
  }
}
function fillDemo() {
  form.email = "admin@ailearn.com";
  form.password = "123456";
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