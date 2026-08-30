<!-- 主布局：顶部导航栏 + 主体区域 -->
<template>
  <div class="layout">
    <!-- 背景装饰：暗紫色渐变光晕 -->
    <div class="bg-decoration">
      <div class="bg-gradient"></div>
      <div class="bg-glow glow-1"></div>
      <div class="bg-glow glow-2"></div>
    </div>

    <!-- 顶部导航栏：白透明圆角长方形 -->
    <header class="header">
      <div class="nav-bar">
        <!-- 左侧：logo和网站名 -->
        <div class="header-left" @click="router.push('/')">
          <div class="logo-wrap">
            <el-icon :size="20" color="#0a0a0c"><Cpu /></el-icon>
          </div>
          <span class="site-name">EduCode</span>
        </div>

        <!-- 中间：导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
            首页
          </router-link>
          <router-link to="/submit" class="nav-item" :class="{ active: route.path === '/submit' }" v-if="userStore.isLoggedIn">
            提交作业
          </router-link>
          <router-link to="/wrong-questions" class="nav-item" :class="{ active: route.path === '/wrong-questions' }" v-if="userStore.isLoggedIn">
            错题本
          </router-link>
          <router-link to="/report" class="nav-item" :class="{ active: route.path === '/report' }" v-if="userStore.isLoggedIn">
            学习报告
          </router-link>
        </nav>

        <!-- 右侧：用户区域 -->
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="28" class="user-avatar">
                  {{ userStore.email ? userStore.email.charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
                <span class="user-email">{{ userStore.email }}</span>
                <el-icon :size="14"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <button class="login-btn" @click="router.push('/login')">
              登录 / 注册
            </button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Cpu, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰：暗紫色渐变光晕 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}
.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 60%;
  background: radial-gradient(ellipse at 50% 0%, rgba(88, 28, 135, 0.25) 0%, rgba(30, 27, 75, 0.15) 40%, transparent 70%);
}
.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.08;
}
.glow-1 {
  width: 500px;
  height: 500px;
  background: #7c3aed;
  top: -150px;
  right: 10%;
}
.glow-2 {
  width: 400px;
  height: 400px;
  background: #4c1d95;
  top: 10%;
  left: -100px;
}

/* 导航栏 */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 14px 32px;
}
.nav-bar {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.logo-wrap {
  width: 32px;
  height: 32px;
  background: #f4f4f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.site-name {
  font-size: 16px;
  font-weight: 600;
  color: #f4f4f5;
  letter-spacing: 0.3px;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 2px;
  flex: 1;
  justify-content: center;
}
.nav-item {
  padding: 7px 16px;
  border-radius: 8px;
  color: #a1a1aa;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.15s;
}
.nav-item:hover {
  color: #e4e4e7;
  background: rgba(255, 255, 255, 0.05);
}
.nav-item.active {
  color: #f4f4f5;
  background: rgba(255, 255, 255, 0.08);
}

/* 右侧用户区域 */
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background 0.15s;
}
.user-info:hover {
  background: rgba(255, 255, 255, 0.06);
}
.user-avatar {
  background: rgba(196, 181, 253, 0.15);
  color: #c4b5fd;
  font-weight: 600;
  font-size: 12px;
}
.user-email {
  font-size: 13px;
  color: #d4d4d8;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.login-btn {
  padding: 7px 18px;
  border-radius: 8px;
  background: #f4f4f5;
  color: #0a0a0c;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.login-btn:hover {
  background: #fff;
}

/* 主体 */
.main {
  position: relative;
  z-index: 1;
  padding: 20px 32px 48px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
</style>
