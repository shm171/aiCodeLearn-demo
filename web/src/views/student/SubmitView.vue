<!-- 提交作业页 SubmitView.vue -->
<template>
  <div class="submit-page">
    <el-card class="submit-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon :size="24"><Upload /></el-icon>
          <span>提交作业</span>
        </div>
      </template>

      <!-- 文件上传区域 -->
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".cpp,.java"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        :file-list="fileList"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="upload-tip">支持 .cpp、.java 格式，单文件不超过 10MB</div>
        </template>
      </el-upload>

      <!-- 已选文件信息 -->
      <div v-if="selectedFile" class="file-info">
        <el-icon><Document /></el-icon>
        <span class="file-name">{{ selectedFile.name }}</span>
        <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
        <el-button link type="danger" @click="clearFile">移除</el-button>
      </div>

      <!-- 提交按钮 -->
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="uploading"
        :disabled="!selectedFile"
        @click="handleSubmit"
      >
        {{ uploading ? '上传中...' : '提交作业' }}
      </el-button>

      <!-- 上传结果 -->
      <el-result
        v-if="submitResult"
        :icon="submitResult.success ? 'success' : 'error'"
        :title="submitResult.success ? '提交成功' : '提交失败'"
        :sub-title="submitResult.message"
        class="result-area"
      >
        <template #extra v-if="submitResult.success">
          <el-button type="primary" @click="resetForm">继续提交</el-button>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, UploadFilled, Document } from '@element-plus/icons-vue'
import { uploadSubmissionApi } from '../../api/submission'

const uploadRef = ref(null)
const fileList = ref([])
const selectedFile = ref(null)
const uploading = ref(false)
const submitResult = ref(null)

// 文件选择变化
function handleFileChange(uploadFile) {
  selectedFile.value = uploadFile.raw
  submitResult.value = null
}

// 超过文件数量限制
function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先移除当前文件')
}

// 格式化文件大小
function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 清除已选文件
function clearFile() {
  selectedFile.value = null
  fileList.value = []
  submitResult.value = null
}

// 提交作业
async function handleSubmit() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  try {
    const res = await uploadSubmissionApi(selectedFile.value)
    submitResult.value = {
      success: true,
      message: `提交成功！作业编号：${res.submissionId || res.id || '已生成'}，AI 正在批改中`,
    }
    ElMessage.success('提交成功')
  } catch (error) {
    submitResult.value = {
      success: false,
      message: error.response?.data?.message || error.response?.data?.detail || '提交失败，请稍后重试',
    }
  } finally {
    uploading.value = false
  }
}

// 重置表单，继续提交
function resetForm() {
  clearFile()
  submitResult.value = null
}
</script>

<style scoped>
.submit-page {
  max-width: 700px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
}

.upload-area {
  margin-bottom: 20px;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.submit-btn {
  width: 100%;
}

.result-area {
  margin-top: 20px;
}
</style>
