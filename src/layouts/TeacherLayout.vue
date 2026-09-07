<template>
  <el-container class="teacher-layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
      <div class="logo" @click="$router.push('/teacher/dashboard')">
        <el-icon :size="24" color="#409eff"><Platform /></el-icon>
        <span v-show="!collapsed" class="logo-text">AI Learn 教师端</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        router
        background-color="#001529"
        text-color="rgba(255,255,255,0.68)"
        active-text-color="#ffffff"
        class="layout-menu"
      >
        <el-menu-item index="/teacher/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        <el-menu-item index="/teacher/questions">
          <el-icon><Collection /></el-icon>
          <template #title>题目与作业管理</template>
        </el-menu-item>
        <el-menu-item index="/teacher/review">
          <el-icon><EditPen /></el-icon>
          <template #title>学生提交审阅</template>
        </el-menu-item>
        <el-menu-item index="/teacher/wrong-questions">
          <el-icon><Warning /></el-icon>
          <template #title>错题管理</template>
        </el-menu-item>
        <el-menu-item index="/teacher/students">
          <el-icon><User /></el-icon>
          <template #title>学生学习报告</template>
        </el-menu-item>
        <el-menu-item index="/teacher/profile">
          <el-icon><Setting /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="layout-body">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Expand v-if="collapsed" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/teacher/dashboard' }">教师端</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title?.replace(' · 教师端', '') || '当前页面' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag v-if="userStore.role" size="small" effect="plain" type="warning">{{ roleLabel }}</el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-info">
              <el-avatar :size="30" class="avatar">
                {{ (userStore.profile?.username || '师')[0] }}
              </el-avatar>
              <span class="username">{{ userStore.profile?.username || '教师' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/user";
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const collapsed = ref(false);
const activeMenu = computed(() => {
  if (route.path.startsWith("/teacher/review")) return "/teacher/review";
  if (route.path.startsWith("/teacher/students")) return "/teacher/students";
  return route.path;
});
const roleLabel = computed(() => {
  const map = { TEACHER: "\u6559\u5E08", ADMIN: "\u7BA1\u7406\u5458", STUDENT: "\u5B66\u751F" };
  return map[userStore.role] || userStore.role;
});
function onCommand(command) {
  if (command === "profile") {
    router.push("/teacher/profile");
  } else if (command === "logout") {
    ElMessageBox.confirm("\u786E\u5B9A\u9000\u51FA\u767B\u5F55\u5417\uFF1F", "\u63D0\u793A", { type: "warning" }).then(() => {
      userStore.logout();
      router.push("/login");
    }).catch(() => {
    });
  }
}

</script>

<style scoped lang="scss">
.teacher-layout {
  height: 100vh;
}

.layout-aside {
  background-color: #001529;
  transition: width 0.2s;
  overflow-x: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #fff;
    cursor: pointer;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  .layout-menu {
    border-right: none;
  }
}

.layout-body {
  min-width: 0;
}

.layout-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 10;

  .header-left {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    color: #606266;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      color: #303133;
      outline: none;

      .avatar {
        background: #409eff;
        color: #fff;
      }
    }
  }
}

.layout-main {
  padding: 16px;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>