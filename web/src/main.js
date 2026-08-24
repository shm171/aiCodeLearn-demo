// ============================================================
// 项目入口文件 main.js
// 作用：这是整个应用的"启动文件"，浏览器打开网页后最先执行它。
// 它负责创建 Vue 应用，并把我们需要的"插件"都安装上去。
// ============================================================

// 1. 导入 Vue 的核心函数 createApp（用来创建应用）
import { createApp } from 'vue'

// 2. 导入我们要使用的各种插件
import { createPinia } from 'pinia'        // Pinia：状态管理（保存全局共享的数据，比如登录状态）
import ElementPlus from 'element-plus'     // Element Plus：UI 组件库（按钮、表单、表格等现成组件）
import 'element-plus/dist/index.css'       // Element Plus 自带的样式文件，不导入的话组件会没有样式
import * as ElementPlusIconsVue from '@element-plus/icons-vue' // Element Plus 的图标库

// 3. 导入我们自己写的文件
import App from './App.vue'      // 根组件：整个页面最外层的壳
import router from './router'    // 路由配置：管理"网址 ↔ 页面"的对应关系（自动找 router/index.js）
import './style.css'             // 全局样式：对整个页面生效的基础样式

// 4. 创建 Vue 应用（把根组件 App 作为参数传进去）
const app = createApp(App)

// 5. 安装插件（app.use() 就是"安装插件"的意思，安装后所有页面都能使用这些功能）
app.use(createPinia())   // 安装 Pinia 状态管理
app.use(router)          // 安装路由
app.use(ElementPlus)     // 安装 Element Plus 组件库

// 6. 注册 Element Plus 的全部图标
//    注册后，页面上可以直接用 <User />、<HomeFilled /> 这样的标签显示图标
for (const [iconName, iconComponent] of Object.entries(ElementPlusIconsVue)) {
  app.component(iconName, iconComponent)
}

// 7. 把应用挂载到 index.html 里的 <div id="app"></div> 上
//    从这一行开始，页面上才会真正显示出内容
app.mount('#app')
