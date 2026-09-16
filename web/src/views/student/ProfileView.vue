<!-- 个人中心 ProfileView.vue -->
<template>
  <div class="profile-page" v-loading="saving" element-loading-text="正在保存个人信息..." element-loading-background="rgba(10, 10, 12, 0.85)">
    <!-- 顶部个人信息卡片 -->
    <el-card class="profile-header" shadow="never">
      <div class="header-content">
        <el-avatar :size="80" class="big-avatar">
          {{ userStore.email ? userStore.email.charAt(0).toUpperCase() : 'U' }}
        </el-avatar>
        <div class="user-detail">
          <h2>{{ userStore.username || '未设置昵称' }}</h2>
          <p class="user-email">{{ userStore.email }}</p>
          <el-tag type="primary" effect="light" size="small">
            <el-icon><User /></el-icon>{{ roleText }}
          </el-tag>
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
              <el-input v-model="form.username" placeholder="请输入昵称" maxlength="20" show-word-limit class="nickname-input" />
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
            <span class="data-value">{{ learningStats.totalSubmissions }} 次</span>
          </div>
          <div class="data-item">
            <span>错题数量</span>
            <span class="data-value">{{ learningStats.totalErrors }} 道</span>
          </div>
          <div class="data-item">
            <span>平均正确率</span>
            <span class="data-value success">{{ learningStats.accuracyRate }}%</span>
          </div>
          <div class="data-item">
            <span>待复习错题</span>
            <span class="data-value">{{ learningStats.unmasteredCount }} 道</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { getUserProfileApi, updateUserProfileApi } from '../../api/user'
import { getReportApi } from '../../api/review'
import { User, Setting, Lock, Key, Message, DataAnalysis } from '@element-plus/icons-vue'

const userStore = useUserStore()
const saving = ref(false)
const loadingProfile = ref(false)
const form = ref({
  username: userStore.username || '',
})

// 角色中文名
const roleText = computed(() => {
  const map = { STUDENT: '学员', ADMIN: '管理员' }
  return map[userStore.role] || '学员'
})

// 学习数据（来自学习报告接口）
const learningStats = ref({
  totalSubmissions: 0,
  totalErrors: 0,
  accuracyRate: 0,
  unmasteredCount: 0,
})

// 页面加载：拉取真实用户档案
async function loadProfile() {
  if (!userStore.userId) {
    ElMessage.warning('未获取到用户ID，请重新注册登录')
    return
  }
  loadingProfile.value = true
  try {
    const profile = await getUserProfileApi(userStore.userId)
    userStore.setProfile({
      username: profile.username,
      role: profile.role,
    })
    form.value.username = profile.username || ''
  } catch (error) {
    // 401 由 request.js 统一处理跳登录，其他错误提示即可
    if (error.response?.status !== 401) {
      ElMessage.error('加载个人信息失败，请稍后重试')
    }
  } finally {
    loadingProfile.value = false
  }
}

// 加载学习数据
async function loadLearningStats() {
  try {
    const report = await getReportApi()
    // accuracyRate 是 0.0~1.0 的小数，显示百分比要乘以100
    learningStats.value = {
      totalSubmissions: report.totalSubmissions ?? 0,
      totalErrors: report.totalErrors ?? 0,
      accuracyRate: Math.round((report.accuracyRate ?? 0) * 100),
      unmasteredCount: (report.practiceList || []).filter((e) => !e.mastered).length,
    }
  } catch (error) {
    // 学习数据加载失败不影响页面主体，静默处理
  }
}

// 保存昵称：调用真实接口
async function handleSave() {
  if (!form.value.username.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  if (!userStore.userId) {
    ElMessage.warning('未获取到用户ID，请重新注册登录')
    return
  }
  saving.value = true
  try {
    const profile = await updateUserProfileApi(userStore.userId, form.value.username.trim())
    userStore.setProfile({
      username: profile.username,
      role: profile.role,
    })
    form.value.username = profile.username || ''
    ElMessage.success('保存成功')
  } catch (error) {
    if (error.response?.status !== 401) {
      ElMessage.error(error.response?.data?.detail || '保存失败，请稍后重试')
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadLearningStats()
})
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
}

.profile-header {
  background: rgba(255, 255, 255, 0.03) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 18px !important;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}
.profile-header :deep(.el-card__body) {
  padding: 28px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.big-avatar {
  background: linear-gradient(135deg, #c4b5fd, #a78bfa) !important;
  color: #0a0a0c !important;
  font-size: 32px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-detail {
  flex: 1;
}
.user-detail h2 {
  font-size: 22px;
  font-weight: 700;
  color: #f4f4f5;
  margin: 0 0 6px;
}
.user-email {
  font-size: 14px;
  color: #a1a1aa;
  margin: 0 0 10px;
}

.info-card {
  background: rgba(255, 255, 255, 0.03) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 18px !important;
  backdrop-filter: blur(10px);
}
.info-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.06) !important;
  padding: 18px 24px;
}
.info-card :deep(.el-card__body) {
  padding: 24px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #f4f4f5;
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
  font-weight: 600;
  color: #f4f4f5;
}

.security-desc {
  font-size: 12px;
  color: #a1a1aa;
  margin-top: 2px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 14px;
  color: #a1a1aa;
}
.data-item:last-child {
  border-bottom: none;
}
.data-value {
  font-weight: 600;
  color: #e4e4e7;
}
.data-value.success {
  color: #34d399;
}

/* 昵称输入框字数统计：平时隐藏，聚焦时显示，去掉白色背景 */
.nickname-input :deep(.el-input__count) {
  display: none;
  background: transparent;
  color: #71717a;
  font-size: 12px;
}
.nickname-input:focus-within :deep(.el-input__count) {
  display: flex;
}
</style>
