<!-- 提交作业页：上传文件/在线写代码 + AI批改结果展示 -->
<template>
  <div class="submit-page">
    <el-card class="submit-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon :size="22"><Upload /></el-icon>
          <span>提交作业</span>
          <el-tag size="small" type="info" class="lang-tag">支持 Java / C++</el-tag>
        </div>
      </template>

      <!-- 提交方式切换 -->
      <el-radio-group v-model="submitMode" class="mode-switch" size="large">
        <el-radio-button value="upload">
          <el-icon><UploadFilled /></el-icon>
          <span style="margin-left:4px">上传文件</span>
        </el-radio-button>
        <el-radio-button value="editor">
          <el-icon><Edit /></el-icon>
          <span style="margin-left:4px">在线写代码</span>
        </el-radio-button>
      </el-radio-group>

      <!-- 语言选择 -->
      <div class="lang-select-row">
        <span class="lang-label">编程语言：</span>
        <el-select v-model="selectedLang" size="default" style="width:160px">
          <el-option label="Java" value="JAVA" />
          <el-option label="C++" value="CPP" />
        </el-select>
      </div>

      <!-- ===== 方式一：上传文件 ===== -->
      <div v-if="submitMode === 'upload'" class="upload-section">
        <el-upload
          ref="uploadRef"
          class="upload-area"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".cpp,.cc,.cxx,.java"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          :file-list="fileList"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="upload-tip">支持 .java、.cpp、.cc、.cxx 格式，单文件不超过 10MB</div>
          </template>
        </el-upload>

        <div v-if="selectedFile" class="file-info">
          <el-icon><Document /></el-icon>
          <span class="file-name">{{ selectedFile.name }}</span>
          <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
          <el-button link type="danger" @click="clearFile">移除</el-button>
        </div>
      </div>

      <!-- ===== 方式二：在线写代码 ===== -->
      <div v-if="submitMode === 'editor'" class="editor-section">
        <div class="editor-header">
          <span class="editor-filename">{{ editorFilename }}</span>
          <el-button link size="small" @click="clearEditor">清空代码</el-button>
        </div>
        <div ref="editorRef" class="code-editor"></div>
        <div class="editor-tip">提示：直接在上面写代码，支持语法高亮和行号显示</div>
      </div>

      <!-- 提交按钮 -->
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        {{ submitting ? (grading ? 'AI 批改中...' : '上传中...') : '提交作业' }}
      </el-button>

      <!-- ===== 批改结果展示 ===== -->
      <div v-if="gradingResult" class="result-section">
        <el-divider content-position="left">
          <span class="divider-text">批改结果</span>
        </el-divider>

        <!-- 得分概览 -->
        <div class="score-overview">
          <div class="score-circle" :class="scoreLevel">
            <span class="score-num">{{ gradingResult.score }}</span>
            <span class="score-unit">分</span>
          </div>
          <div class="score-stats">
            <div class="stat-item error">
              <span class="stat-icon">🔴</span>
              <span class="stat-label">严重错误</span>
              <span class="stat-count">{{ errorCount }}</span>
            </div>
            <div class="stat-item warning">
              <span class="stat-icon">🟡</span>
              <span class="stat-label">警告</span>
              <span class="stat-count">{{ warningCount }}</span>
            </div>
            <div class="stat-item info">
              <span class="stat-icon">🟢</span>
              <span class="stat-label">建议</span>
              <span class="stat-count">{{ infoCount }}</span>
            </div>
          </div>
        </div>

        <!-- AI整体评语 -->
        <div class="feedback-box">
          <div class="feedback-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI 整体评语</span>
          </div>
          <p class="feedback-text">{{ gradingResult.overallFeedback || '本次代码整体表现良好，继续加油！' }}</p>
        </div>

        <!-- 错误列表 -->
        <div v-if="gradingResult.issues && gradingResult.issues.length > 0" class="issues-section">
          <h4 class="issues-title">问题列表（{{ gradingResult.issues.length }} 个）</h4>
          <div
            v-for="(issue, index) in gradingResult.issues"
            :key="index"
            class="issue-card"
            :class="'severity-' + (issue.severity || '').toLowerCase()"
          >
            <div class="issue-header">
              <el-tag :type="severityTagType(issue.severity)" size="small" effect="dark">
                {{ severityText(issue.severity) }}
              </el-tag>
              <span class="issue-category">{{ issue.category || '代码问题' }}</span>
              <span v-if="issue.lineNumber" class="issue-line">第 {{ issue.lineNumber }} 行</span>
            </div>
            <p class="issue-message">{{ issue.message }}</p>
            <div v-if="issue.suggestion" class="issue-suggestion">
              <span class="suggestion-label">💡 修改建议：</span>
              <span>{{ issue.suggestion }}</span>
            </div>
          </div>
        </div>

        <div v-else class="no-issues">
          <el-icon :size="40" color="#34d399"><CircleCheckFilled /></el-icon>
          <p>太棒了！没有发现任何问题</p>
        </div>

        <!-- 操作按钮 -->
        <div class="result-actions">
          <el-button type="primary" @click="resetForm">继续提交</el-button>
          <el-button @click="$router.push('/wrong-questions')">查看错题本</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Upload, UploadFilled, Document, Edit,
  ChatDotRound, CircleCheckFilled
} from '@element-plus/icons-vue'
import { uploadSubmissionApi, gradeSubmissionApi } from '../../api/submission'

// ========== CodeMirror 导入 ==========
import { EditorState } from '@codemirror/state'
import { EditorView, basicSetup } from 'codemirror'
import { java } from '@codemirror/lang-java'
import { cpp } from '@codemirror/lang-cpp'
import { oneDark } from '@codemirror/theme-one-dark'

// ========== 状态 ==========
const submitMode = ref('upload') // upload | editor
const selectedLang = ref('JAVA')
const uploadRef = ref(null)
const fileList = ref([])
const selectedFile = ref(null)
const submitting = ref(false)
const grading = ref(false)
const gradingResult = ref(null)

// 编辑器相关
const editorRef = ref(null)
let editorView = null

// 编辑器文件名
const editorFilename = computed(() => {
  return selectedLang.value === 'JAVA' ? 'Main.java' : 'main.cpp'
})

// 是否可以提交
const canSubmit = computed(() => {
  if (submitMode.value === 'upload') {
    return !!selectedFile.value
  }
  return editorView && editorView.state.doc.toString().trim().length > 0
})

// 统计各严重程度数量
const errorCount = computed(() => gradingResult.value?.issues?.filter(i => i.severity === 'ERROR').length || 0)
const warningCount = computed(() => gradingResult.value?.issues?.filter(i => i.severity === 'WARNING').length || 0)
const infoCount = computed(() => gradingResult.value?.issues?.filter(i => i.severity === 'INFO').length || 0)

// 得分等级样式
const scoreLevel = computed(() => {
  const s = gradingResult.value?.score ?? 100
  if (s >= 90) return 'score-high'
  if (s >= 70) return 'score-mid'
  return 'score-low'
})

// ========== CodeMirror 初始化 ==========
function initEditor() {
  if (!editorRef.value) return

  const langExt = selectedLang.value === 'JAVA' ? java() : cpp()

  const state = EditorState.create({
    doc: getDefaultCode(),
    extensions: [
      basicSetup,
      langExt,
      oneDark,
      EditorView.theme({
        '&': { height: '320px', fontSize: '14px' },
        '.cm-scroller': { overflow: 'auto' },
      }),
    ],
  })

  editorView = new EditorView({
    state,
    parent: editorRef.value,
  })
}

function getDefaultCode() {
  if (selectedLang.value === 'JAVA') {
    return `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, EduCode!");
    }
}
`
  }
  return `#include <iostream>
using namespace std;

int main() {
    cout << "Hello, EduCode!" << endl;
    return 0;
}
`
}

// 切换语言时重新初始化编辑器
watch(selectedLang, () => {
  if (submitMode.value === 'editor' && editorView) {
    editorView.destroy()
    initEditor()
  }
})

onMounted(() => {
  // 初始不创建编辑器，等切换到在线写代码时再创建
})

onBeforeUnmount(() => {
  if (editorView) {
    editorView.destroy()
    editorView = null
  }
})

// 切换到在线写代码时初始化编辑器
watch(submitMode, (newMode) => {
  if (newMode === 'editor' && !editorView) {
    setTimeout(() => initEditor(), 50)
  }
})

// ========== 文件上传 ==========
function handleFileChange(uploadFile) {
  selectedFile.value = uploadFile.raw
  gradingResult.value = null
  // 根据文件扩展名自动选择语言
  const name = uploadFile.name.toLowerCase()
  if (name.endsWith('.java')) selectedLang.value = 'JAVA'
  else if (name.endsWith('.cpp') || name.endsWith('.cc') || name.endsWith('.cxx')) selectedLang.value = 'CPP'
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先移除当前文件')
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function clearFile() {
  selectedFile.value = null
  fileList.value = []
  gradingResult.value = null
}

function clearEditor() {
  if (editorView) {
    editorView.dispatch({
      changes: { from: 0, to: editorView.state.doc.length, insert: '' },
    })
  }
}

// ========== 提交作业 ==========
async function handleSubmit() {
  submitting.value = true
  gradingResult.value = null

  try {
    let fileToUpload = selectedFile.value

    // 如果是在线写代码，把代码内容转成Blob文件
    if (submitMode.value === 'editor') {
      const code = editorView.state.doc.toString()
      const blob = new Blob([code], { type: 'text/plain' })
      fileToUpload = new File([blob], editorFilename.value, { type: 'text/plain' })
    }

    if (!fileToUpload) {
      ElMessage.warning('请先选择文件或写代码')
      submitting.value = false
      return
    }

    // 第一步：上传文件
    const uploadRes = await uploadSubmissionApi(fileToUpload)
    const submissionId = uploadRes.submissionId || uploadRes.id

    if (!submissionId) {
      throw new Error('上传成功但未获取到作业编号')
    }

    // 第二步：触发AI批改
    grading.value = true
    // 模拟批改耗时，让用户看到"批改中"状态
    await new Promise(resolve => setTimeout(resolve, 1500))

    const gradeRes = await gradeSubmissionApi(submissionId)
    gradingResult.value = gradeRes

    ElMessage.success('批改完成！')
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data?.detail || error.message || '提交失败，请稍后重试'
    ElMessage.error(msg)
    gradingResult.value = null
  } finally {
    submitting.value = false
    grading.value = false
  }
}

// ========== 辅助函数 ==========
function severityText(severity) {
  const map = { ERROR: '严重', WARNING: '警告', INFO: '建议' }
  return map[severity] || '未知'
}

function severityTagType(severity) {
  const map = { ERROR: 'danger', WARNING: 'warning', INFO: 'info' }
  return map[severity] || 'info'
}

function resetForm() {
  clearFile()
  gradingResult.value = null
  if (editorView) {
    editorView.dispatch({
      changes: { from: 0, to: editorView.state.doc.length, insert: getDefaultCode() },
    })
  }
}
</script>

<style scoped>
.submit-page {
  max-width: 820px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
}

.lang-tag {
  margin-left: auto;
}

/* 提交方式切换 */
.mode-switch {
  margin-bottom: 20px;
  width: 100%;
}
.mode-switch :deep(.el-radio-button__inner) {
  width: 50%;
  text-align: center;
}

/* 语言选择 */
.lang-select-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 8px;
}
.lang-label {
  font-size: 14px;
  color: var(--text-secondary, #d4d4d8);
  font-weight: 500;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 20px;
}
.upload-icon {
  font-size: 48px;
  color: #c4b5fd;
}
.upload-text {
  font-size: 14px;
  color: #a1a1aa;
  margin-top: 8px;
}
.upload-text em {
  color: #c4b5fd;
  font-style: normal;
}
.upload-tip {
  font-size: 12px;
  color: #71717a;
  margin-top: 4px;
}

/* 已选文件信息 */
.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  margin-top: 16px;
}
.file-name {
  flex: 1;
  font-size: 14px;
  color: #e4e4e7;
}
.file-size {
  font-size: 12px;
  color: #71717a;
}

/* 在线编辑器 */
.editor-section {
  margin-bottom: 20px;
}
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-bottom: none;
  border-radius: 10px 10px 0 0;
}
.editor-filename {
  font-size: 13px;
  color: #a1a1aa;
  font-family: 'JetBrains Mono', Consolas, monospace;
}
.code-editor {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0 0 10px 10px;
  overflow: hidden;
}
.editor-tip {
  font-size: 12px;
  color: #71717a;
  margin-top: 8px;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 10px;
}

/* ===== 批改结果 ===== */
.result-section {
  margin-top: 28px;
}
.divider-text {
  font-size: 16px;
  font-weight: 700;
  color: #f4f4f5;
}

/* 得分概览 */
.score-overview {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  margin-bottom: 20px;
}
.score-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.score-high {
  background: linear-gradient(135deg, rgba(52, 211, 153, 0.2), rgba(16, 185, 129, 0.1));
  border: 2px solid rgba(52, 211, 153, 0.5);
}
.score-mid {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.2), rgba(245, 158, 11, 0.1));
  border: 2px solid rgba(251, 191, 36, 0.5);
}
.score-low {
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.2), rgba(239, 68, 68, 0.1));
  border: 2px solid rgba(248, 113, 113, 0.5);
}
.score-num {
  font-size: 36px;
  font-weight: 800;
  color: #f4f4f5;
  line-height: 1;
}
.score-unit {
  font-size: 14px;
  color: #a1a1aa;
  margin-top: 2px;
}

.score-stats {
  display: flex;
  gap: 32px;
  flex: 1;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stat-icon {
  font-size: 24px;
}
.stat-label {
  font-size: 13px;
  color: #a1a1aa;
}
.stat-count {
  font-size: 28px;
  font-weight: 700;
  color: #f4f4f5;
}

/* AI评语 */
.feedback-box {
  padding: 18px 20px;
  background: rgba(196, 181, 253, 0.06);
  border: 1px solid rgba(196, 181, 253, 0.15);
  border-radius: 12px;
  margin-bottom: 20px;
}
.feedback-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #c4b5fd;
  margin-bottom: 10px;
}
.feedback-text {
  font-size: 14px;
  color: #d4d4d8;
  line-height: 1.7;
  margin: 0;
}

/* 问题列表 */
.issues-title {
  font-size: 15px;
  font-weight: 600;
  color: #e4e4e7;
  margin: 0 0 14px 0;
}
.issue-card {
  padding: 16px 18px;
  border-radius: 12px;
  margin-bottom: 12px;
  border-left: 3px solid;
}
.severity-error {
  background: rgba(248, 113, 113, 0.06);
  border-color: #f87171;
}
.severity-warning {
  background: rgba(251, 191, 36, 0.06);
  border-color: #fbbf24;
}
.severity-info {
  background: rgba(52, 211, 153, 0.06);
  border-color: #34d399;
}
.issue-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.issue-category {
  font-size: 14px;
  font-weight: 600;
  color: #e4e4e7;
}
.issue-line {
  font-size: 12px;
  color: #71717a;
  margin-left: auto;
}
.issue-message {
  font-size: 14px;
  color: #d4d4d8;
  line-height: 1.6;
  margin: 0 0 8px 0;
}
.issue-suggestion {
  font-size: 13px;
  color: #a1a1aa;
  line-height: 1.6;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}
.suggestion-label {
  color: #c4b5fd;
  font-weight: 600;
}

/* 没有问题 */
.no-issues {
  text-align: center;
  padding: 40px 20px;
  color: #a1a1aa;
}
.no-issues p {
  margin-top: 12px;
  font-size: 15px;
}

/* 结果操作按钮 */
.result-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  justify-content: center;
}
</style>
