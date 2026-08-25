<!-- 登录页：粒子背景 + 毛玻璃卡片 + 登录/注册Tab -->
<template>
  <div class="login-page">
    <!-- 粒子背景画布 -->
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
    <!-- 渐变遮罩，让粒子和背景融合 -->
    <div class="overlay"></div>

    <!-- 右侧装饰：发光能量球 -->
    <div class="decoration">
      <div class="energy-ball"></div>
      <div class="energy-ring ring1"></div>
      <div class="energy-ring ring2"></div>
    </div>

    <el-card class="login-card">
      <el-tabs v-model="activeTab" stretch class="login-tabs">
        <!-- 登录Tab -->
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
            <el-form-item prop="email">
              <el-input v-model="loginForm.email" placeholder="请输入邮箱" size="large" prefix-icon="Message" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password prefix-icon="Lock" />
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loginLoading" @click="handleLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <!-- 注册Tab -->
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
            <el-form-item prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" size="large" prefix-icon="Message" />
            </el-form-item>
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（8-16位）" size="large" show-password prefix-icon="Lock" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password prefix-icon="Lock" />
            </el-form-item>
            <el-form-item prop="role">
              <el-select v-model="registerForm.role" size="large" class="role-select">
                <el-option label="学员" value="STUDENT" />
                <el-option label="老师" value="TEACHER" />
              </el-select>
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="registerLoading" @click="handleRegister">注 册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <div class="slogan">
      <p class="slogan-title">EduCode 智能学习平台</p>
      <p class="slogan-sub">让每一次学习，都更高效</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { loginApi, registerApi } from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')

// ========== 粒子背景 ==========
const canvasRef = ref(null)
let animationId = null
let particles = []
const PARTICLE_COUNT = 70 // 粒子数量，手机端会减半

// 初始化粒子
function initParticles(canvas) {
  const count = window.innerWidth < 768 ? PARTICLE_COUNT / 2 : PARTICLE_COUNT
  particles = []
  for (let i = 0; i < count; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 0.5, // 水平速度
      vy: (Math.random() - 0.5) * 0.5, // 垂直速度
      radius: Math.random() * 2 + 1,   // 粒子大小
    })
  }
}

// 绘制粒子和连线
function drawParticles(canvas, ctx) {
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // 绘制粒子
  particles.forEach((p) => {
    p.x += p.vx
    p.y += p.vy
    // 碰到边界反弹
    if (p.x < 0 || p.x > canvas.width) p.vx *= -1
    if (p.y < 0 || p.y > canvas.height) p.vy *= -1

    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)'
    ctx.fill()
  })

  // 绘制连线（距离近的粒子之间连线）
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < 120) {
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = `rgba(100, 180, 255, ${1 - dist / 120})`
        ctx.lineWidth = 0.5
        ctx.stroke()
      }
    }
  }
}

// 动画循环
function animate(canvas, ctx) {
  drawParticles(canvas, ctx)
  animationId = requestAnimationFrame(() => animate(canvas, ctx))
}

onMounted(() => {
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  // 设置画布大小为窗口大小
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  initParticles(canvas)
  animate(canvas, ctx)

  // 窗口大小改变时重新设置画布
  const handleResize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    initParticles(canvas)
  }
  window.addEventListener('resize', handleResize)

  // 页面不可见时暂停动画，节省资源
  const handleVisibility = () => {
    if (document.hidden) {
      cancelAnimationFrame(animationId)
    } else {
      animate(canvas, ctx)
    }
  }
  document.addEventListener('visibilitychange', handleVisibility)

  // 清理函数
  onUnmounted(() => {
    cancelAnimationFrame(animationId)
    window.removeEventListener('resize', handleResize)
    document.removeEventListener('visibilitychange', handleVisibility)
  })
})

// ========== 登录 ==========
const loginForm = reactive({ email: '', password: '' })
const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
const loginFormRef = ref(null)
const loginLoading = ref(false)

async function handleLogin() {
  await loginFormRef.value.validate()
  loginLoading.value = true
  try {
    const data = await loginApi(loginForm.email, loginForm.password)
    userStore.setLoginInfo(data.token, loginForm.email)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    if (error.response?.status === 401) {
      ElMessage.error('邮箱或密码错误')
    } else {
      ElMessage.error(error.response?.data?.message || error.response?.data?.detail || '登录失败，请稍后重试')
    }
  } finally {
    loginLoading.value = false
  }
}

// ========== 注册 ==========
const registerForm = reactive({ email: '', username: '', password: '', confirmPassword: '', role: 'STUDENT' })

function checkConfirmPassword(rule, value, callback) {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 50, message: '用户名最长50个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 16, message: '密码长度8-16位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: checkConfirmPassword, trigger: 'blur' },
  ],
}
const registerFormRef = ref(null)
const registerLoading = ref(false)

async function handleRegister() {
  await registerFormRef.value.validate()
  registerLoading.value = true
  try {
    await registerApi(registerForm.email, registerForm.password, registerForm.username, registerForm.role)
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.email = registerForm.email
    registerFormRef.value.resetFields()
  } catch (error) {
    const detail = error.response?.data?.detail || ''
    if (error.response?.status === 409) {
      ElMessage.error(detail.includes('Email') ? '该邮箱已被注册' : '该用户名已被注册')
    } else {
      ElMessage.error(detail || '注册失败，请稍后重试')
    }
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  padding-left: 8%;
  overflow: hidden;
  /* 深蓝紫渐变背景 */
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
}

/* 粒子画布铺满整个屏幕 */
.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 渐变遮罩：让粒子和背景融合，左侧稍暗保证卡片清晰 */
.overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, rgba(15, 12, 41, 0.7) 0%, rgba(15, 12, 41, 0.3) 40%, transparent 100%);
  z-index: 2;
  pointer-events: none;
}

/* 右侧装饰：发光能量球 */
.decoration {
  position: absolute;
  right: 10%;
  top: 50%;
  transform: translateY(-50%);
  width: 400px;
  height: 400px;
  z-index: 1;
  pointer-events: none;
}

.energy-ball {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(100, 180, 255, 0.35) 0%, rgba(102, 126, 234, 0.2) 35%, rgba(118, 75, 162, 0.1) 60%, transparent 75%);
  box-shadow: 0 0 50px rgba(100, 180, 255, 0.25), 0 0 100px rgba(102, 126, 234, 0.15);
  animation: pulse 5s ease-in-out infinite;
}

.energy-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  border: 1px solid rgba(100, 180, 255, 0.2);
}

.ring1 {
  width: 300px;
  height: 300px;
  animation: rotate 20s linear infinite;
  border-top-color: rgba(100, 180, 255, 0.4);
  border-right-color: transparent;
}

.ring2 {
  width: 380px;
  height: 380px;
  animation: rotate 30s linear infinite reverse;
  border-bottom-color: rgba(118, 75, 162, 0.35);
  border-left-color: transparent;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.7; }
  50% { transform: translate(-50%, -50%) scale(1.05); opacity: 1; }
}

@keyframes rotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

/* 登录卡片：毛玻璃效果 */
.login-card {
  position: relative;
  z-index: 3;
  width: 420px;
  padding: 24px 28px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

/* Tab样式 */
.login-tabs :deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}
.login-tabs :deep(.el-tabs__item.is-active) {
  color: #fff;
}
.login-tabs :deep(.el-tabs__active-bar) {
  background-color: #64b4ff;
}

/* 输入框：深色背景适配 */
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset;
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(100, 180, 255, 0.5) inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #64b4ff inset, 0 0 8px rgba(100, 180, 255, 0.3);
}
:deep(.el-input__inner) {
  color: #fff;
}
:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}
:deep(.el-input__prefix-inner) {
  color: rgba(255, 255, 255, 0.6);
}

/* 下拉框 */
.role-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 右下角品牌标语 */
.slogan {
  position: absolute;
  right: 40px;
  bottom: 40px;
  text-align: right;
  z-index: 3;
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
