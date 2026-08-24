<!--
  登录页 LoginView.vue
  作用：用户输入用户名和密码登录
  目前是"模拟登录"（前端本地模拟，不发真实请求），
  等后端登录接口做好后，按注释里写的方式换成真实请求即可
-->
<template>
  <div class="login-page">
    <!-- 登录表单卡片 -->
    <el-card class="login-card">
      <h2 class="title">登录</h2>

      <!-- el-form：Element Plus 的表单组件
           :model 绑定表单数据（form）
           :rules 绑定校验规则（rules）
           ref    表单的引用，后面用 formRef 来触发校验 -->
      <el-form :model="form" :rules="rules" ref="formRef">
        <!-- 用户名输入框
             v-model 双向绑定：输入框里的内容会实时存到 form.username -->
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>

        <!-- 密码输入框
             type="password" 隐藏输入内容
             show-password 加一个"小眼睛"按钮，可切换显示/隐藏 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 登录按钮
             loading 为 true 时按钮会显示转圈并禁止点击
             @click 点击时执行 handleLogin 函数 -->
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

      <!-- 提示文字 -->
      <p class="tip">提示：目前是模拟登录，输入任意用户名和密码即可</p>
    </el-card>
  </div>
</template>

<script setup>
// 导入 Vue 的基础 API
import { ref, reactive } from 'vue'
// 导入路由和 Element Plus 的消息提示
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 导入用户 store（保存登录信息）
import { useUserStore } from '../stores/user'
// 等后端接口做好后，取消下面这行的注释，用它发真实请求
// import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()

// ---------- 表单数据 ----------
// reactive：把普通对象变成"响应式"数据（数据变了，页面自动更新）
const form = reactive({
  username: '', // 用户名
  password: '', // 密码
})

// ---------- 表单校验规则 ----------
// 每个字段对应一条数组：
// required 表示必填，message 是没填时提示的文字，trigger 是"什么时候校验"（blur = 输入框失去焦点时）
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// formRef：表单组件的引用（ref 的初始值是 null，模板渲染后自动填上）
// 用它来调用表单的校验方法：formRef.value.validate()
const formRef = ref(null)

// loading：是否正在登录中（true 时按钮转圈）
const loading = ref(false)

// ---------- 点击"登录"按钮时执行 ----------
async function handleLogin() {
  // 1. 先校验表单是否填写完整
  //    validate() 校验通过就正常继续，不通过会抛出错误、停在这里
  await formRef.value.validate()

  // 2. 模拟登录（用 setTimeout 假装请求了 1 秒）
  loading.value = true
  setTimeout(() => {
    // 把登录信息保存到 Pinia store（任何页面都能通过 useUserStore() 拿到）
    userStore.setLoginInfo('模拟token-' + Date.now(), form.username)
    loading.value = false
    ElMessage.success('登录成功，欢迎回来！') // 弹出成功提示
    router.push('/') // 跳转到首页
  }, 1000)

  // ===== 等后端接口做好后，把上面的 setTimeout 换成真实请求 =====
  // loading.value = true
  // const data = await request.post('/login', form)     // 调用后端登录接口
  // userStore.setLoginInfo(data.token, data.username)   // 保存后端返回的 token 和用户名
  // loading.value = false
  // ElMessage.success('登录成功，欢迎回来！')
  // router.push('/')
}
</script>

<style scoped>
/* 让登录卡片在屏幕正中间 */
.login-page {
  height: 100%;
  display: flex;
  align-items: center;    /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  /* 渐变背景：从左上角的蓝色渐变到右下角的紫色 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 登录卡片：固定宽度，上下左右留一点内边距 */
.login-card {
  width: 400px;
  padding: 10px 20px;
}

/* 标题居中 */
.title {
  text-align: center;
  margin-bottom: 24px;
}

/* 登录按钮占满整行 */
.login-btn {
  width: 100%;
}

/* 底部提示文字：灰色、小号、居中 */
.tip {
  margin-top: 16px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}
</style>
