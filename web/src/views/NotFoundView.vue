<!--
  404 页面 NotFoundView.vue
  作用：访问了不存在的网址时显示（比如 /abcdefg）
-->
<template>
  <div class="not-found">
    <!-- el-result：Element Plus 的结果页组件，专门用来展示"结果"（成功/失败/404 等） -->
    <el-result icon="warning" title="404" sub-title="抱歉，你访问的页面不存在">
      <!-- #extra 是 el-result 的插槽，用来放"额外操作"（这里是按钮） -->
      <template #extra>
        <!-- 点击按钮回到首页 -->
        <el-button type="primary" @click="$router.push('/')">回到首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
// 404 页面不需要逻辑
// 注意：模板里可以直接用 $router 跳转（$router 是路由对象，
// 在模板里是全局可用的），在 <script setup> 里则要用 useRouter()
</script>

<style scoped>
/* 让结果页在屏幕里垂直居中显示 */
.not-found {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
