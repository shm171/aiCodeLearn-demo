<template>
  <div class="not-found">
    <h1>404</h1>
    <p>抱歉，您访问的页面不存在或没有访问权限。</p>
    <el-button type="primary" @click="$router.push('/teacher/dashboard')">返回数据看板</el-button>
  </div>
</template>

<style scoped lang="scss">
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #f0f2f5;

  h1 {
    font-size: 80px;
    margin: 0;
    color: #409eff;
  }

  p {
    color: #909399;
  }
}
</style>