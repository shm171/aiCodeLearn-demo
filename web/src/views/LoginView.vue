<!-- 登录页：目前是模拟登录，等后端接口对接后换成真实请求 -->
<template>
  <div class="login-page">
    <div class="overlay"></div>

    <el-card class="login-card">
      <h2 class="title">登录</h2>

      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>

      <p class="tip">提示：目前是模拟登录，输入任意用户名和密码即可</p>
    </el-card>

    <div class="slogan">
      <p class="slogan-title">EduCode 智能学习平台</p>
      <p class="slogan-sub">让每一次学习，都更高效</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
// 对接真实接口时取消注释：
// import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const formRef = ref(null)
const loading = ref(false)

async function handleLogin() {
  // 1. 校验表单
  await formRef.value.validate()

  // 2. 模拟登录（对接真实接口时替换下面这段）
  loading.value = true
  setTimeout(() => {
    userStore.setLoginInfo('模拟token-' + Date.now(), form.username)
    loading.value = false
    ElMessage.success('登录成功，欢迎回来！')
    router.push('/')
  }, 1000)

  // ===== 真实接口写法（对接时替换上面setTimeout）=====
  // loading.value = true
  // const data = await request.post('/login', form)
  // userStore.setLoginInfo(data.token, data.username)
  // loading.value = false
  // ElMessage.success('登录成功，欢迎回来！')
  // router.push('/')
}
</script>

<style scoped>
.login-page {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  padding-left: 8%;
  background: url('../assets/login-bg.jpg') no-repeat center center;
  background-size: cover;
}

/* 渐变遮罩：左白右透明，保证登录卡清晰 */
.overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0.92) 0%,
    rgba(255, 255, 255, 0.55) 35%,
    rgba(255, 255, 255, 0) 100%
  );
  pointer-events: none;
}

/* 登录卡片：毛玻璃效果 */
.login-card {
  position: relative;
  width: 420px;
  padding: 24px 28px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.25);
}

.title {
  text-align: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
}

.tip {
  margin-top: 16px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

/* 右下角品牌标语 */
.slogan {
  position: absolute;
  right: 40px;
  bottom: 40px;
  text-align: right;
}

.slogan-title {
  font-size: 26px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  margin-bottom: 8px;
}

.slogan-sub {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.4);
}

/* 手机端适配 */
@media (max-width: 768px) {
  .login-page {
    padding-left: 0;
    justify-content: center;
  }
  .login-card {
    width: 90%;
  }
  .slogan {
    right: 16px;
    bottom: 16px;
  }
  .slogan-title {
    font-size: 18px;
  }
}
</style>
