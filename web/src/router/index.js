// ============================================================
// 路由配置文件 router/index.js
// 作用：定义"网址"和"页面"的对应关系（路由表）
// 例如：访问 /login 显示登录页，访问 / 显示首页
// ============================================================

import { createRouter, createWebHistory } from 'vue-router'

// 导入主布局组件（布局 = 页面上公共的部分，比如顶部导航栏）
import MainLayout from '../layout/MainLayout.vue'

// ========== 路由表 ==========
// routes 是一个数组，里面每一个 {} 都是一条路由规则
const routes = [
  // ---------- 登录页 ----------
  // 它是一级路由，直接显示，不套导航栏
  {
    path: '/login', // 网址路径
    name: 'login',  // 路由名字，跳转时可以用这个名字代替路径
    // component：这个网址对应显示哪个页面
    // () => import(...) 这种写法叫"懒加载"：
    // 访问到该页面时才去加载它，让网站第一次打开更快
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录' }, // meta 是附加信息，这里用来存"页面标题"
  },

  // ---------- 主布局（带顶部导航栏） ----------
  // path 为 '/' 表示：所有 "/" 开头的网址都先进这个布局
  {
    path: '/',
    component: MainLayout,
    // children 是"子路由"：
    // 子路由的页面会显示在 MainLayout 内部的 <router-view> 里
    children: [
      // 首页：path 为空字符串表示访问 / 时默认显示这个页面
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页' },
      },
      // 👇 以后要加新页面，就在 children 里照着上面加一条，
      //    例如加"我的课程"页面：
      // {
      //   path: 'courses',
      //   name: 'courses',
      //   component: () => import('../views/CoursesView.vue'),
      //   meta: { title: '我的课程' },
      // },
    ],
  },

  // ---------- 404 页面（兜底） ----------
  // pathMatch(.*)* 是特殊写法，表示"匹配所有没被上面规则匹配到的网址"
  // 这条规则必须放在最后，否则会把所有页面都拦截掉
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

// 创建路由对象
const router = createRouter({
  // history 模式：网址里没有 # 号（比如 /login 而不是 /#/login）
  history: createWebHistory(),
  routes, // 传入上面定义的路由表
})

// ========== 全局前置守卫 ==========
// 每次页面跳转"之前"都会执行这个函数
// 以后可以在这里做"登录检查"（比如：没登录就跳去登录页）
router.beforeEach((to) => {
  // 把浏览器标签页的标题改成当前页面的标题
  document.title = to.meta.title ? `${to.meta.title} - EduCode` : 'EduCode'
})

// 导出路由对象，main.js 里会用到（app.use(router)）
export default router
