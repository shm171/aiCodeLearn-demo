<template>
  <div v-loading="loading">
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="26"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-meta">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="card-title">角色分布</span></template>
          <div v-for="item in roleDistribution" :key="item.name" class="role-row">
            <span class="role-name">{{ item.name }}</span>
            <el-progress
              :percentage="rolePercent(item.value)"
              :stroke-width="12"
              :color="roleColor(item.name)"
            />
            <span class="role-value">{{ item.value }} 人</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">最近注册用户</span>
              <el-button link type="success" @click="$router.push('/admin/users')">全部用户</el-button>
            </div>
          </template>
          <el-table :data="recentUsers" size="small">
            <el-table-column prop="username" label="用户名" width="100" />
            <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
            <el-table-column label="角色" width="90">
              <template #default="{ row }">
                <el-tag :type="roleTag(row.role)" size="small">{{ roleName(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="140" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { DocumentChecked, TrendCharts, User, Warning } from '@element-plus/icons-vue'
import { getAdminStats } from '@/api/admin'
import type { AdminStats, Role } from '@/api/schema'

const loading = ref(false)
const stats = ref<AdminStats | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getAdminStats()
  } finally {
    loading.value = false
  }
})

const statCards = computed(() => {
  const s = stats.value
  return [
    { label: '用户总数', value: s?.totalUsers ?? 0, icon: User, bg: '#ecf5ff', color: '#409eff' },
    { label: '教师数', value: s?.teacherCount ?? 0, icon: DocumentChecked, bg: '#f0f9eb', color: '#67c23a' },
    { label: '学生数', value: s?.studentCount ?? 0, icon: TrendCharts, bg: '#fdf6ec', color: '#e6a23c' },
    { label: '管理员数', value: s?.adminCount ?? 0, icon: Warning, bg: '#fef0f0', color: '#f56c6c' },
  ]
})

const roleDistribution = computed(() => stats.value?.roleDistribution ?? [])
const recentUsers = computed(() => stats.value?.recentUsers ?? [])

function rolePercent(v: number) {
  const total = stats.value?.totalUsers ?? 1
  return total ? Math.round((v / total) * 100) : 0
}
function roleColor(name: string) {
  const map: Record<string, string> = { 学生: '#409eff', 教师: '#67c23a', 管理员: '#f56c6c' }
  return map[name] || '#909399'
}
const roleNameMap: Record<Role, string> = { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }
function roleName(r: string) {
  return roleNameMap[r as Role] || r
}
function roleTag(r: string) {
  const map: Record<string, 'primary' | 'success' | 'danger' | 'warning' | 'info'> = {
    STUDENT: 'primary',
    TEACHER: 'success',
    ADMIN: 'danger',
  }
  return map[r] || 'info'
}
</script>

<style scoped lang="scss">
.stat-row { margin-bottom: 16px; }
.stat-card { margin-bottom: 16px; }
.stat-item { display: flex; align-items: center; gap: 14px; }
.stat-icon {
  width: 52px; height: 52px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-value { font-size: 24px; font-weight: 600; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; }
.chart-card { margin-bottom: 16px; }
.card-title { font-weight: 600; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.role-row {
  display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
  .role-name { width: 48px; color: #303133; }
  .role-value { width: 56px; text-align: right; color: #909399; font-size: 13px; }
}
</style>