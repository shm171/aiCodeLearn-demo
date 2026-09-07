import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
    meta: { public: true, title: "\u767B\u5F55 \xB7 AI Learn \u6559\u5E08\u7AEF" }
  },
  {
    path: "/",
    redirect: () => {
      const store = useUserStore();
      return store.token ? "/teacher/dashboard" : "/login";
    }
  },
  {
    path: "/teacher",
    component: () => import("@/layouts/TeacherLayout.vue"),
    meta: { roles: ["TEACHER"] },
    children: [
      {
        path: "dashboard",
        name: "TeacherDashboard",
        component: () => import("@/views/teacher/Dashboard.vue"),
        meta: { roles: ["TEACHER"], title: "\u6570\u636E\u770B\u677F \xB7 \u6559\u5E08\u7AEF" }
      },
      {
        path: "questions",
        name: "TeacherQuestions",
        component: () => import("@/views/teacher/QuestionList.vue"),
        meta: { roles: ["TEACHER"], title: "\u9898\u76EE\u4E0E\u4F5C\u4E1A\u7BA1\u7406 \xB7 \u6559\u5E08\u7AEF" }
      },
      {
        path: "review",
        name: "TeacherReview",
        component: () => import("@/views/teacher/ReviewList.vue"),
        meta: { roles: ["TEACHER"], title: "\u5B66\u751F\u63D0\u4EA4\u5BA1\u9605 \xB7 \u6559\u5E08\u7AEF" }
      },
      {
        path: "review/:id",
        name: "TeacherReviewDetail",
        component: () => import("@/views/teacher/ReviewDetail.vue"),
        meta: { roles: ["TEACHER"], title: "\u5BA1\u9605\u8BE6\u60C5 \xB7 \u6559\u5E08\u7AEF" }
      },
      {
        path: "wrong-questions",
        name: "TeacherWrongQuestions",
        component: () => import("@/views/teacher/WrongQuestions.vue"),
        meta: { roles: ["TEACHER"], title: "\u9519\u9898\u7BA1\u7406 \xB7 \u6559\u5E08\u7AEF" }
      },
      {
        path: "students",
        name: "TeacherStudents",
        component: () => import("@/views/teacher/Students.vue"),
        meta: { roles: ["TEACHER"], title: "\u5B66\u751F\u5B66\u4E60\u62A5\u544A \xB7 \u6559\u5E08\u7AEF" }
      },
      {
        path: "students/:id/report",
        name: "TeacherStudentReport",
        component: () => import("@/views/teacher/StudentReport.vue"),
        meta: { roles: ["TEACHER"], title: "\u5B66\u4E60\u62A5\u544A\u8BE6\u60C5 \xB7 \u6559\u5E08\u7AEF" }
      },
      {
        path: "profile",
        name: "TeacherProfile",
        component: () => import("@/views/teacher/Profile.vue"),
        meta: { roles: ["TEACHER"], title: "\u4E2A\u4EBA\u4E2D\u5FC3 \xB7 \u6559\u5E08\u7AEF" }
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
    if (store.role === "TEACHER") return "/teacher/dashboard";
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  return true;
});
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} \xB7 AI Learn` : "AI Learn \xB7 \u6559\u5E08\u7AEF";
});
var stdin_default = router;
export {
  stdin_default as default
};
