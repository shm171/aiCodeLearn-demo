<!-- 主布局：顶部导航栏 + 主体区域 -->
<template>
  <el-container class="layout">
    <el-header class="header">
      <!-- 左侧：logo和网站名 -->
      <div class="header-left" @click="router.push('/')">
        <div class="logo-wrap">
          <el-icon :size="24" color="#fff"><Cpu /></el-icon>
        </div>
        <span class="site-name">EduCode</span>
        <span class="site-sub">智能学习平台</span>
      </div>

      <!-- 中间：导航菜单 -->
      <el-menu mode="horizontal" router :default-active="route.path" class="nav-menu">
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/submit" v-if="userStore.isLoggedIn">
          <el-icon><Upload /></el-icon>
          <span>提交作业</span>
        </el-menu-item>
      </el-menu>

      <!-- 右侧：用户区域 -->
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.email ? userStore.email.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <span class="user-email">{{ userStore.email }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" class="login-btn" @click="router.push('/login')">
            登录 / 注册
          </el-button>
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
import { ElMessage } from 'element-plus'
import { Cpu, HomeFilled, Upload, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'

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
}

.header {
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0 32px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-wrap {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.site-name {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.site-sub {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 2px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
}

.nav-menu :deep(.el-menu-item) {
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background 0.2s;
}
.user-info:hover {
  background: rgba(0, 0, 0, 0.04);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.user-email {
  font-size: 14px;
  color: #475569;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-btn {
  border-radius: 20px;
  padding: 8px 24px;
}

.main {
  padding: 24px 32px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}
</style>
