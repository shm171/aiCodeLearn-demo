import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
    meta: { public: true, title: "\u767B\u5F55 \xB7 EduCode \u7BA1\u7406\u7AEF" }
  },
  {
    path: "/",
    redirect: () => {
      const store = useUserStore();
      return store.token ? "/admin/dashboard" : "/login";
    }
  },
  {
    path: "/admin",
    component: () => import("@/layouts/AdminLayout.vue"),
    meta: { roles: ["ADMIN"] },
    children: [
      {
        path: "dashboard",
        name: "AdminDashboard",
        component: () => import("@/views/admin/AdminDashboard.vue"),
        meta: { roles: ["ADMIN"], title: "\u6570\u636E\u6982\u89C8 \xB7 \u7BA1\u7406\u7AEF" }
      },
      {
        path: "users",
        name: "AdminUsers",
        component: () => import("@/views/admin/UserManage.vue"),
        meta: { roles: ["ADMIN"], title: "\u7528\u6237\u7BA1\u7406 \xB7 \u7BA1\u7406\u7AEF" }
      },
      {
        path: "roles",
        name: "AdminRoles",
        component: () => import("@/views/admin/RoleManage.vue"),
        meta: { roles: ["ADMIN"], title: "\u89D2\u8272\u4E0E\u6743\u9650 \xB7 \u7BA1\u7406\u7AEF" }
      },
      {
        path: "config",
        name: "AdminConfig",
        component: () => import("@/views/admin/ConfigManage.vue"),
        meta: { roles: ["ADMIN"], title: "\u57FA\u7840\u914D\u7F6E \xB7 \u7BA1\u7406\u7AEF" }
      },
      {
        path: "profile",
        name: "AdminProfile",
        component: () => import("@/views/admin/Profile.vue"),
        meta: { roles: ["ADMIN"], title: "\u4E2A\u4EBA\u4E2D\u5FC3 \xB7 \u7BA1\u7406\u7AEF" }
      }
    ]
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue"),
    meta: { title: "\u9875\u9762\u4E0D\u5B58\u5728" }
  }
];
const router = createRouter({
  history: createWebHistory(),
  routes
});
router.beforeEach(async (to) => {
  const store = useUserStore();
  if (to.meta.public) return true;
  if (!store.token) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  if (!store.profile) {
    try {
      await store.fetchProfile();
    } catch {
      store.logout();
      return { path: "/login", query: { redirect: to.fullPath } };
    }
  }
  const roles = to.meta.roles;
  if (roles && roles.length && !roles.includes(store.role)) {
    if (store.role === "ADMIN") return "/admin/dashboard";
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  return true;
});
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} \xB7 EduCode` : "EduCode \xB7 \u7BA1\u7406\u7AEF";
});
var stdin_default = router;
export {
  stdin_default as default
};
