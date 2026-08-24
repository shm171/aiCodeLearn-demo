<!--
  首页 HomeView.vue
  作用：登录后看到的第一个页面，展示欢迎信息和功能入口
-->
<template>
  <div class="home">
    <!-- 欢迎卡片：el-card 是 Element Plus 的卡片组件 -->
    <el-card class="welcome-card">
      <h2>👋 欢迎来到 EduCode 智能学习平台</h2>
      <p>上传代码，AI 智能批改，帮你发现编程错误，提升编程能力。</p>
    </el-card>

    <!-- 功能占位卡片：
         el-row 是"行"，el-col 是"列"（一行分成 24 份，:span="8" 表示占 8 份 = 三分之一宽）
         v-for 循环渲染 features 数组里的每一项 -->
    <el-row :gutter="20">
      <el-col :span="8" v-for="item in features" :key="item.title">
        <el-card class="feature-card">
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// 页面的数据写在这里

// 三个功能入口，对应学员端核心功能
const features = [
  { title: '📝 提交作业', desc: '上传代码，获取 AI 智能批改' },
  { title: '📚 我的错题本', desc: '查看历史错误，针对性复习' },
  { title: '📊 学习报告', desc: '查看批改统计和薄弱知识点' },
]
</script>

<style scoped>
/* 欢迎卡片和下面的卡片之间留一点距离 */
.welcome-card {
  margin-bottom: 20px;
}

/* 功能卡片：文字居中 */
.feature-card {
  text-align: center;
}

.feature-card h3 {
  margin-bottom: 10px;
}

.feature-card p {
  color: #909399; /* Element Plus 的次要文字灰色 */
  font-size: 14px;
}
</style>
