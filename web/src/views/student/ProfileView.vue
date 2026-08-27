<!-- 个人中心 ProfileView.vue -->
<template>
  <div class="profile-page">
    <!-- 顶部个人信息卡片 -->
    <el-card class="profile-header" shadow="never">
      <div class="header-content">
        <el-avatar :size="80" class="big-avatar">
          {{ userStore.email ? userStore.email.charAt(0).toUpperCase() : 'U' }}
        </el-avatar>
        <div class="user-detail">
          <h2>{{ username || '未设置昵称' }}</h2>
          <p class="user-email">{{ userStore.email }}</p>
          <el-tag type="primary" effect="light" size="small">
            <el-icon><User /></el-icon>学员
          </el-tag>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <div class="stat-num">12</div>
            <div class="stat-label">提交次数</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">78%</div>
            <div class="stat-label">正确率</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">8</div>
            <div class="stat-label">学习天数</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="24">
      <!-- 左侧：基本信息 -->
      <el-col :span="14">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon><Setting /></el-icon>
              <span>基本信息</span>
            </div>
          </template>

          <el-form :model="form" label-width="80px" class="info-form">
            <el-form-item label="邮箱">
              <el-input :value="userStore.email" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.username" placeholder="请输入昵称" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag type="primary">学员</el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：账号安全 -->
      <el-col :span="10">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon><Lock /></el-icon>
              <span>账号安全</span>
            </div>
          </template>

          <div class="security-item">
            <div class="security-info">
              <el-icon :size="20" color="#6366f1"><Key /></el-icon>
              <div>
                <div class="security-title">登录密码</div>
                <div class="security-desc">定期修改密码更安全</div>
              </div>
            </div>
            <el-button link type="primary" @click="ElMessage.info('修改密码功能开发中')">修改</el-button>
          </div>

          <el-divider />

          <div class="security-item">
            <div class="security-info">
              <el-icon :size="20" color="#10b981"><Message /></el-icon>
              <div>
                <div class="security-title">绑定邮箱</div>
                <div class="security-desc">{{ userStore.email }}</div>
              </div>
            </div>
            <el-tag type="success" size="small">已绑定</el-tag>
          </div>
        </el-card>

        <!-- 学习数据 -->
        <el-card class="info-card" shadow="never" style="margin-top: 24px">
          <template #header>
            <div class="card-title">
              <el-icon><DataAnalysis /></el-icon>
              <span>学习数据</span>
            </div>
          </template>
          <div class="data-item">
            <span>总提交次数</span>
            <span class="data-value">12 次</span>
          </div>
          <div class="data-item">
            <span>通过次数</span>
            <span class="data-value">9 次</span>
          </div>
          <div class="data-item">
            <span>平均正确率</span>
            <span class="data-value success">78%</span>
          </div>
          <div class="data-item">
            <span>累计学习天数</span>
            <span class="data-value">8 天</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { User, Setting, Lock, Key, Message, DataAnalysis } from '@element-plus/icons-vue'

const userStore = useUserStore()
const saving = ref(false)
const username = ref(localStorage.getItem('username') || '')
const form = ref({
  username: localStorage.getItem('username') || '',
})

function handleSave() {
  if (!form.value.username.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  // 暂时存在本地，等后端接口好了再对接
  setTimeout(() => {
    localStorage.setItem('username', form.value.username)
    username.value = form.value.username
    saving.value = false
    ElMessage.success('保存成功')
  }, 500)
}
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}
.profile-header :deep(.el-card__body) {
  padding: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
  color: #fff;
}

.big-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-detail h2 {
  font-size: 24px;
  margin: 0 0 8px;
}

.user-email {
  font-size: 14px;
  opacity: 0.85;
  margin: 0 0 8px;
}

.header-stats {
  margin-left: auto;
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  opacity: 0.8;
  margin-top: 4px;
}

.info-card {
  border-radius: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.info-form {
  padding: 8px 0;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.security-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.security-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  color: #64748b;
}
.data-item:last-child {
  border-bottom: none;
}

.data-value {
  font-weight: 600;
  color: #1e293b;
}
.data-value.success {
  color: #10b981;
}
</style>
