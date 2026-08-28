<!-- 登录/注册页：左侧品牌介绍区 + 右侧表单区 -->
<template>
  <div class="login-page">
    <!-- ===== 左侧：品牌介绍区 ===== -->
    <div class="brand-panel">
      <!-- 顶部：logo + 平台名 -->
      <div class="brand-top">
        <img src="../assets/logo.png" alt="logo" class="logo" />
        <span class="brand-name">EduCode</span>
      </div>

      <!-- 主标语 + 功能亮点 -->
      <div class="brand-main">
        <h1 class="brand-title">让每一次学习<br />都更高效</h1>
        <p class="brand-desc">上传代码，AI 智能批改，帮你发现编程错误，提升编程能力。</p>

        <div class="brand-features">
          <div class="feature-item">
            <el-icon class="feature-icon"><Upload /></el-icon>
            <div>
              <div class="feature-title">AI 智能批改</div>
              <div class="feature-desc">提交代码，秒级反馈</div>
            </div>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><Notebook /></el-icon>
            <div>
              <div class="feature-title">我的错题本</div>
              <div class="feature-desc">历史错误，针对性复习</div>
            </div>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><TrendCharts /></el-icon>
            <div>
              <div class="feature-title">学习报告</div>
              <div class="feature-desc">数据可视化，掌握薄弱点</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 代码窗口：展示核心功能（AI 批改代码），带语法高亮 -->
      <div class="code-window">
        <div class="code-bar">
          <span class="code-dot dot-red"></span>
          <span class="code-dot dot-yellow"></span>
          <span class="code-dot dot-green"></span>
          <span class="code-filename">homework.py</span>
        </div>
        <pre class="code-content"><span class="code-comment"># 提交作业：Python 基础练习</span>
<span class="code-keyword">def</span> <span class="code-func">fibonacci</span>(n):
    <span class="code-keyword">if</span> n &lt;= 1:
        <span class="code-keyword">return</span> n
    <span class="code-keyword">return</span> fibonacci(n - 1) + fibonacci(n - 2)<span class="code-cursor">|</span></pre>
        <!-- AI 批改结果标签 -->
        <div class="ai-badge">
          <el-icon><CircleCheck /></el-icon>
          <span>AI 批改通过 · 95 分</span>
        </div>
      </div>
    </div>

    <!-- ===== 右侧：登录/注册表单区 ===== -->
    <div class="form-panel">
      <el-card class="login-card">
        <el-tabs v-model="activeTab" stretch class="login-tabs">
          <!-- 登录Tab -->
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
              <el-form-item prop="email">
                <el-input v-model="loginForm.email" placeholder="请输入邮箱" size="large" prefix-icon="Message" autocomplete="off" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password prefix-icon="Lock" autocomplete="current-password" />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loginLoading" @click="handleLogin">登 录</el-button>
            </el-form>
          </el-tab-pane>

          <!-- 注册Tab -->
          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
              <el-form-item prop="email">
                <el-input v-model="registerForm.email" placeholder="请输入邮箱" size="large" prefix-icon="Message" autocomplete="off" />
              </el-form-item>
              <el-form-item prop="username">
                <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" prefix-icon="User" autocomplete="off" name="new-username" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（8-16位）" size="large" show-password prefix-icon="Lock" autocomplete="new-password" />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password prefix-icon="Lock" autocomplete="new-password" />
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
      <p class="copyright">© 2026 EduCode 智能学习平台</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { loginApi, registerApi } from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')

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
/* 品牌主色：靛蓝。改这里整页的Element Plus组件（按钮/输入框/下拉框）都会跟着变 */
.login-page {
  --el-color-primary: #4f46e5;
  --el-color-primary-light-3: #7c74f0;
  --el-color-primary-light-5: #a5a0f5;
  --el-color-primary-light-7: #cdcafa;
  --el-color-primary-light-8: #e0defb;
  --el-color-primary-light-9: #eeedfd;
  --el-color-primary-dark-2: #4338ca;
}

.login-page {
  height: 100%;
  display: flex;
  padding: 20px; /* 四周留白，让左右两栏整体像一张大卡片 */
  background: #eef2f7; /* 页面底色，比卡片深一点，衬托出卡片轮廓 */
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  flex: 1.1;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 44px 52px;
  background-color: #1e1b4b;
  /* 细网格纹理：像编程用的稿纸 */
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px);
  background-size: 40px 40px;
  border-radius: 20px 0 0 20px; /* 左边两个角圆角 */
  overflow: hidden;
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  height: 40px;
}

.brand-name {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-title {
  color: #fff;
  font-size: 42px;
  font-weight: 700;
  line-height: 1.3;
  letter-spacing: 0.03em;
  margin-bottom: 16px;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.65);
  font-size: 15px;
  line-height: 1.7;
  max-width: 420px;
  margin-bottom: 36px;
}

/* 功能亮点列表 */
.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 22px;
}

.feature-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  color: #a5b4fc;
  font-size: 20px;
}

.feature-title {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.feature-desc {
  color: rgba(255, 255, 255, 0.55);
  font-size: 13px;
  margin-top: 2px;
}

/* 代码窗口 */
.code-window {
  position: relative;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
}

.code-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.code-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dot-red { background: #f87171; }
.dot-yellow { background: #fbbf24; }
.dot-green { background: #34d399; }

.code-filename {
  margin-left: 8px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.code-content {
  margin: 0;
  padding: 14px 16px;
  font-family: 'JetBrains Mono', Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.8;
  color: #e2e8f0;
  overflow-x: auto;
}

/* 语法高亮配色 */
.code-comment { color: #94a3b8; }
.code-keyword { color: #c4b5fd; }
.code-func { color: #93c5fd; }

/* 闪烁光标 */
.code-cursor {
  display: inline-block;
  color: #93c5fd;
  animation: blink 1s step-end infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}

/* AI 批改结果标签：浮在代码窗口右下角 */
.ai-badge {
  position: absolute;
  right: 14px;
  bottom: -12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #fff;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
}

/* ===== 右侧表单区 ===== */
.form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border-radius: 0 20px 20px 0; /* 右边两个角圆角，和左栏拼成完整的大卡片 */
}

/* 白色卡片，干净利落 */
.login-card {
  width: 420px;
  padding: 32px 32px 24px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.06);
  background: #fff;
}

/* Tab 文字稍大一点 */
.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 10px;
  transition: all 0.25s;
}
.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.35);
}

/* 角色下拉框 */
.role-select {
  width: 100%;
}

.copyright {
  margin-top: 20px;
  color: #94a3b8;
  font-size: 12px;
}

/* 屏幕较窄时隐藏左侧品牌区，只留表单 */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }
  .form-panel {
    border-radius: 20px; /* 左栏隐藏后，右侧变成完整的圆角卡片 */
  }
}

/* 手机端 */
@media (max-width: 768px) {
  .login-page {
    padding: 12px; /* 小屏幕少留一点边 */
  }
  .login-card {
    width: 100%;
  }
}
</style>
