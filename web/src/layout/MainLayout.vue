<!--
  主布局组件 MainLayout.vue
  作用：整个应用的"外壳"，带顶部导航栏。
  所有 "/" 开头的子路由页面（首页、以后的"我的课程"等）
  都会显示在这个布局的 <router-view> 里。
-->
<template>
  <!-- el-container：Element Plus 的布局容器（上下结构） -->
  <el-container class="layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <!-- 左侧：logo 和网站名字 -->
      <div class="header-left">
        <img src="../assets/logo.png" alt="logo" class="logo" />
        <span class="site-name">EduCode 智能学习平台</span>
      </div>

      <!-- 中间：导航菜单 -->
      <!-- router 模式：点击菜单项会自动跳转到对应网址 -->
      <!-- default-active：根据当前网址，自动高亮对应的菜单项 -->
      <el-menu mode="horizontal" router :default-active="route.path" class="nav-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <!-- 👇 以后加了新页面，在这里再加一个 el-menu-item 就行 -->
      </el-menu>

      <!-- 右侧：用户信息 / 登录按钮 -->
      <div class="header-right">
        <!-- v-if 的两种写法对比：
             template 包裹 + v-if / v-else，用来根据"是否登录"显示不同内容 -->
        <template v-if="userStore.isLoggedIn">
          <!-- 已登录：显示用户名和退出按钮 -->
          <span class="username">你好，{{ userStore.username }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </template>
        <template v-else>
          <!-- 未登录：显示登录按钮，点击跳转到登录页 -->
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </template>
      </div>
    </el-header>

    <!-- 主体区域：子路由的页面会显示在这里 -->
    <el-main class="main">
      <!-- 嵌套路由的"出口"：首页等子页面显示的位置 -->
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
// 导入路由和用户状态
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

// useRoute()：拿到"当前路由"的信息（比如当前网址 path）
// useRouter()：拿到路由对象，用它来跳转页面（router.push）
const route = useRoute()
const router = useRouter()

// useUserStore()：拿到用户数据（store 里的 token、username 等）
// 注意：在任何页面调用 useUserStore() 拿到的都是"同一份"数据
const userStore = useUserStore()

// 退出登录：清空用户信息，然后跳回登录页
function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* scoped 表示这里的样式只对这个组件生效，不会影响其他页面 */

/* 布局占满整个屏幕高度 */
.layout {
  height: 100%;
}

/* 顶部导航栏：白色背景 + 底部细边框 */
.header {
  display: flex;
  align-items: center;
  gap: 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

/* 左侧：logo 和网站名字 */
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo {
  height: 32px;
}

.site-name {
  font-size: 18px;
  font-weight: bold;
  color: #409eff; /* Element Plus 的主题蓝色 */
}

/* 导航菜单：flex: 1 表示占满剩余空间，把右侧挤到最右边 */
.nav-menu {
  flex: 1;
  border-bottom: none; /* 去掉菜单自带的底部边框 */
}

/* 主体区域和边缘留一点距离 */
.main {
  padding: 20px;
}
</style>
