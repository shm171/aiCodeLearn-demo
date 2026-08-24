<!--
  根组件 App.vue
  作用：整个应用最外层的组件，所有页面都显示在它里面。
  它只做一件事：根据当前网址，显示对应的页面。
-->
<template>
  <!--
    <router-view> 是路由的"出口"（占位符）
    当前访问什么网址，这里就显示哪个页面的内容。
    例如：
    - 访问 /login → 显示登录页（LoginView.vue）
    - 访问 /      → 显示主布局（MainLayout.vue），里面再套首页
  -->
  <router-view />
</template>

<script setup>
// 根组件不需要写任何逻辑，具体页面都写在 views 文件夹里
</script>
