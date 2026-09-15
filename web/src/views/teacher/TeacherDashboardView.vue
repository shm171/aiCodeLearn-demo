<template>
  <div class="teacher-dashboard" v-loading="loading" element-loading-background="rgba(10, 10, 12, 0.75)">
    <div class="page-heading">
      <div>
        <h1>班级整体错题数据</h1>
        <p>汇总学生错题，帮助教师快速定位教学薄弱点。</p>
      </div>
      <div class="heading-actions">
        <el-tag type="warning" effect="plain">教师数据看板</el-tag>
      </div>
    </div>

    <el-card class="summary-card" shadow="never">
      <div class="summary-content">
        <div class="summary-icon">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div>
          <div class="summary-label">班级总错题数</div>
          <div class="summary-value">{{ stats.totalErrors }}</div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="card-title">易错知识点排行</span>
          </template>
          <BarChart :labels="topicLabels" :series="topicSeries" height="340px" y-name="错题数" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="card-title">错题分类分布</span>
          </template>
          <PieChart :data="categoryDistribution" height="340px" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="profileVisible" width="900px" top="7vh" append-to-body class="teacher-profile-dialog" :close-on-click-modal="false">
      <template #header><span class="dialog-title">个人中心</span></template>

      <div class="profile-page" v-loading="savingProfile">
        <el-card class="profile-header" shadow="never">
          <div class="header-content">
            <el-avatar :size="80" class="big-avatar">{{ avatarText }}</el-avatar>
            <div class="user-detail">
              <h2>{{ profileForm.username || '未设置昵称' }}</h2>
              <p class="user-email">{{ profileForm.email }}</p>
              <el-tag type="warning" effect="light" size="small">
                <el-icon><User /></el-icon>教师
              </el-tag>
            </div>
          </div>
        </el-card>

        <el-row :gutter="24">
          <el-col :xs="24" :lg="14">
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="card-title"><el-icon><Setting /></el-icon><span>基本信息</span></div>
              </template>
              <el-form :model="profileForm" label-width="80px" class="info-form">
                <el-form-item label="邮箱"><el-input v-model="profileForm.email" disabled /></el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="profileForm.username" maxlength="50" show-word-limit />
                </el-form-item>
                <el-form-item label="角色"><el-tag type="warning">教师</el-tag></el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <el-col :xs="24" :lg="10">
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="card-title"><el-icon><Lock /></el-icon><span>账号安全</span></div>
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
                    <div class="security-desc">{{ profileForm.email }}</div>
                  </div>
                </div>
                <el-tag type="success" size="small">已绑定</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { Key, Lock, Message, Setting, User, Warning } from '@element-plus/icons-vue'
import BarChart from '../../components/charts/BarChart.vue'
import PieChart from '../../components/charts/PieChart.vue'
import { getTeacherDashboardApi } from '../../api/teacher'
import { updateUserProfileApi } from '../../api/user'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const stats = ref({ totalErrors: 0, classTopicRanking: [], classCategoryDistribution: [] })
const profileVisible = ref(false)
const savingProfile = ref(false)
const profileForm = reactive({ username: '', email: '' })

const avatarText = computed(() => (profileForm.username || '师').charAt(0).toUpperCase())
const topicLabels = computed(() => stats.value.classTopicRanking.map((item) => item.topic))
const topicSeries = computed(() => [{ name: '错题数', data: stats.value.classTopicRanking.map((item) => item.errorCount) }])
const categoryDistribution = computed(() => stats.value.classCategoryDistribution)

async function loadDashboard() {
  loading.value = true
  try {
    const data = await getTeacherDashboardApi()
    stats.value = {
      totalErrors: data.totalErrors ?? 0,
      classTopicRanking: (data.topWeakPoints ?? []).map((item, index) => ({ rank: index + 1, topic: item.errorType, errorCount: item.count })),
      classCategoryDistribution: (data.distribution?.slices ?? []).map((item) => ({ name: item.label, value: item.value })),
    }
  } catch (error) {
    if (error.response?.status !== 401) ElMessage.error(error.response?.data?.message || '教师看板加载失败')
  } finally {
    loading.value = false
  }
}

async function openProfile() {
  try {
    if (!userStore.userId) await userStore.fetchUserInfo()
    profileForm.username = userStore.username || ''
    profileForm.email = userStore.email || ''
    profileVisible.value = true
  } catch {
    ElMessage.error('无法加载教师档案')
  }
}

async function saveProfile() {
  const username = profileForm.username.trim()
  if (!username) return ElMessage.warning('昵称不能为空')
  if (!userStore.userId) return ElMessage.error('未获取到教师用户 ID')

  savingProfile.value = true
  try {
    const profile = await updateUserProfileApi(userStore.userId, username)
    userStore.setProfile({ username: profile.username, role: profile.role })
    profileForm.username = profile.username
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    savingProfile.value = false
  }
}

onMounted(loadDashboard)

watch(
  () => route.query.profile,
  async (value) => {
    if (value !== '1') return
    await openProfile()
    router.replace({ path: '/teacher/dashboard' })
  },
  { immediate: true }
)
</script>

<style scoped>
.teacher-dashboard { max-width: 1200px; margin: 0 auto; padding: 0; }
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 24px; }
.heading-actions { display: flex; align-items: center; gap: 12px; }
.page-heading h1 { margin: 0 0 8px; color: #f4f4f5; font-size: 28px; font-weight: 700; }
.page-heading p { margin: 0; color: #a1a1aa; font-size: 14px; }
.summary-card, .chart-card { background: rgba(255, 255, 255, 0.03) !important; border: 1px solid rgba(255, 255, 255, 0.08) !important; border-radius: 18px !important; backdrop-filter: blur(12px); }
.summary-card { margin-bottom: 20px; }
.summary-card :deep(.el-card__body) { padding: 24px; }
.summary-content { display: flex; align-items: center; gap: 18px; }
.summary-icon { width: 58px; height: 58px; border-radius: 16px; display: flex; align-items: center; justify-content: center; background: rgba(251, 113, 133, 0.12); color: #fb7185; }
.summary-label { color: #a1a1aa; font-size: 14px; }
.summary-value { color: #f4f4f5; font-size: 34px; font-weight: 700; line-height: 1.2; }
.chart-card :deep(.el-card__header) { border-bottom: 1px solid rgba(255, 255, 255, 0.06); }
.chart-card :deep(.el-card__body) { padding: 18px 20px 20px; }
.card-title { display: flex; align-items: center; gap: 8px; color: #e4e4e7; font-weight: 600; }
.chart-row { margin-bottom: 4px; }
.profile-page { padding-bottom: 4px; }
.profile-header, .info-card { background: rgba(255, 255, 255, 0.03) !important; border: 1px solid rgba(255, 255, 255, 0.08) !important; border-radius: 18px !important; backdrop-filter: blur(10px); }
.profile-header { margin-bottom: 24px; }
.profile-header :deep(.el-card__body) { padding: 28px; }
.header-content { display: flex; align-items: center; gap: 24px; }
.big-avatar { background: linear-gradient(135deg, #c4b5fd, #a78bfa) !important; color: #0a0a0c !important; font-size: 32px; font-weight: 700; flex-shrink: 0; }
.user-detail h2 { margin: 0 0 6px; color: #f4f4f5; font-size: 22px; font-weight: 700; }
.user-email { margin: 0 0 10px; color: #a1a1aa; font-size: 14px; }
.info-card :deep(.el-card__header) { border-bottom: 1px solid rgba(255, 255, 255, 0.06) !important; padding: 18px 24px; }
.info-card :deep(.el-card__body) { padding: 24px; }
.info-form { padding: 8px 0; }
.security-item { display: flex; align-items: center; justify-content: space-between; padding: 8px 0; }
.security-info { display: flex; align-items: center; gap: 12px; }
.security-title { color: #f4f4f5; font-size: 14px; font-weight: 600; }
.security-desc { margin-top: 2px; color: #a1a1aa; font-size: 12px; }
:global(.teacher-profile-dialog) { background: #111114 !important; border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 18px; }
:global(.teacher-profile-dialog .el-dialog__header) { border-bottom: 1px solid rgba(255, 255, 255, 0.06); }
:global(.teacher-profile-dialog .el-dialog__title), :global(.teacher-profile-dialog .dialog-title) { color: #f4f4f5; font-weight: 700; }
:global(.teacher-profile-dialog .el-dialog__body) { padding-top: 24px; }
@media (max-width: 768px) { .page-heading { flex-direction: column; } .heading-actions { width: 100%; justify-content: space-between; } }
</style>