<!-- 主布局：顶部导航栏 + 主体区域，子页面显示在 <router-view> -->
<template>
  <el-container class="layout">
    <el-header class="header">
      <!-- 左侧：logo和网站名 -->
      <div class="header-left">
        <img src="../assets/logo.png" alt="logo" class="logo" />
        <span class="site-name">EduCode 智能学习平台</span>
      </div>

      <!-- 中间：导航菜单，router模式点击自动跳转 -->
      <el-menu mode="horizontal" router :default-active="route.path" class="nav-menu">
        <el-menu-item index="/">首页</el-menu-item>
      </el-menu>

      <!-- 右侧：根据登录状态显示不同内容 -->
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <span class="username">你好，{{ userStore.email }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

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
  color: #409eff;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.main {
  padding: 20px;
}
</style>
