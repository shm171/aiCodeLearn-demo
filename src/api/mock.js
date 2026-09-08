function delay(data, ms = 300) {
  return new Promise((resolve) => setTimeout(() => resolve(structuredClone(data)), ms));
}
const MOCK_ADMIN = {
  id: 1,
  userId: 1,
  username: "\u7BA1\u7406\u5458",
  email: "admin@ailearn.com",
  role: "ADMIN"
};
const DEMO_ACCOUNTS = {
  "admin@ailearn.com": { password: "123456", profile: MOCK_ADMIN },
  "teacher@ailearn.com": {
    password: "123456",
    profile: { id: 2, userId: 2, username: "\u738B\u8001\u5E08", email: "teacher@ailearn.com", role: "TEACHER" }
  },
  "student@ailearn.com": {
    password: "123456",
    profile: { id: 3, userId: 3, username: "\u674E\u60F3", email: "student@ailearn.com", role: "STUDENT" }
  }
};
let currentEmail = "";
function mockLogin(data) {
  const account = DEMO_ACCOUNTS[data.email];
  if (!account || account.password !== data.password) {
    return Promise.reject(new Error("\u90AE\u7BB1\u6216\u5BC6\u7801\u9519\u8BEF"));
  }
  currentEmail = data.email;
  return delay({ token: `mock-jwt-token-${Date.now()}`, tokenType: "Bearer", expiresIn: 86400 });
}
function mockGetProfile() {
  const account = DEMO_ACCOUNTS[currentEmail] || DEMO_ACCOUNTS["admin@ailearn.com"];
  return delay(account.profile);
}
function mockUpdateProfile(data) {
  const account = DEMO_ACCOUNTS[currentEmail] || DEMO_ACCOUNTS["admin@ailearn.com"];
  Object.assign(account.profile, data);
  return delay(account.profile);
}
function mockRegister(data) {
  return delay({ id: 100, email: data.email });
}
const users = [
  { id: 1, email: "admin@ailearn.com", username: "\u7BA1\u7406\u5458", role: "ADMIN", status: 1, createdAt: "2026-07-01 09:00" },
  { id: 2, email: "teacher@ailearn.com", username: "\u738B\u8001\u5E08", role: "TEACHER", status: 1, createdAt: "2026-07-02 10:20" },
  { id: 3, email: "teacher2@ailearn.com", username: "\u674E\u8001\u5E08", role: "TEACHER", status: 1, createdAt: "2026-07-05 14:10" },
  { id: 4, email: "student@ailearn.com", username: "\u674E\u60F3", role: "STUDENT", status: 1, createdAt: "2026-07-08 08:30" },
  { id: 5, email: "student2@ailearn.com", username: "\u5F20\u96E8\u6850", role: "STUDENT", status: 1, createdAt: "2026-07-09 11:45" },
  { id: 6, email: "student3@ailearn.com", username: "\u9648\u4E00\u9E23", role: "STUDENT", status: 0, createdAt: "2026-07-12 16:00" },
  { id: 7, email: "teacher3@ailearn.com", username: "\u8D75\u8001\u5E08", role: "TEACHER", status: 1, createdAt: "2026-07-15 09:25" },
  { id: 8, email: "student4@ailearn.com", username: "\u5218\u601D\u8FDC", role: "STUDENT", status: 1, createdAt: "2026-07-18 13:50" }
];
function mockGetUserList(query) {
  let list = [...users];
  if (query.keyword) {
    const kw = query.keyword.toLowerCase();
    list = list.filter(
      (u) => u.username.toLowerCase().includes(kw) || u.email.toLowerCase().includes(kw)
    );
  }
  if (query.role) list = list.filter((u) => u.role === query.role);
  const total = list.length;
  const start = (query.page - 1) * query.size;
  return delay({ list: list.slice(start, start + query.size), total, page: query.page, size: query.size });
}
function mockCreateUser(data) {
  const item = {
    id: Math.max(0, ...users.map((u) => u.id)) + 1,
    email: data.email,
    username: data.username,
    role: data.role,
    status: 1,
    createdAt: (/* @__PURE__ */ new Date()).toISOString().slice(0, 16).replace("T", " ")
  };
  users.unshift(item);
  return delay(item);
}
function mockUpdateUserRole(id, role) {
  const u = users.find((x) => x.id === id);
  if (!u) throw new Error("\u7528\u6237\u4E0D\u5B58\u5728");
  u.role = role;
  return delay(u);
}
function mockDeleteUser(id) {
  const idx = users.findIndex((u) => u.id === id);
  if (idx >= 0) users.splice(idx, 1);
  return delay(void 0);
}
function mockGetAdminStats() {
  return delay({
    totalUsers: users.length,
    teacherCount: users.filter((u) => u.role === "TEACHER").length,
    studentCount: users.filter((u) => u.role === "STUDENT").length,
    adminCount: users.filter((u) => u.role === "ADMIN").length,
    recentUsers: users.slice(0, 5),
    roleDistribution: [
      { name: "\u5B66\u751F", value: users.filter((u) => u.role === "STUDENT").length },
      { name: "\u6559\u5E08", value: users.filter((u) => u.role === "TEACHER").length },
      { name: "\u7BA1\u7406\u5458", value: users.filter((u) => u.role === "ADMIN").length }
    ]
  });
}
function mockGetRoles() {
  return delay([
    { role: "ADMIN", name: "\u7BA1\u7406\u5458", description: "\u5E73\u53F0\u7EA7\u7BA1\u7406\uFF1A\u7528\u6237\u3001\u89D2\u8272\u4E0E\u914D\u7F6E\u7BA1\u7406", userCount: users.filter((u) => u.role === "ADMIN").length, permissions: ["\u7528\u6237\u7BA1\u7406", "\u89D2\u8272\u7BA1\u7406", "\u57FA\u7840\u914D\u7F6E", "\u6570\u636E\u770B\u677F"] },
    { role: "TEACHER", name: "\u6559\u5E08", description: "\u6559\u5B66\u7BA1\u7406\uFF1A\u770B\u677F\u3001\u5BA1\u9605\u3001\u9519\u9898\u3001\u5B66\u4E60\u62A5\u544A", userCount: users.filter((u) => u.role === "TEACHER").length, permissions: ["\u6559\u5E08\u770B\u677F", "\u9898\u76EE/\u4F5C\u4E1A\u7BA1\u7406", "\u63D0\u4EA4\u5BA1\u9605", "\u9519\u9898\u7BA1\u7406", "\u5B66\u751F\u62A5\u544A"] },
    { role: "STUDENT", name: "\u5B66\u751F", description: "\u5B66\u4E60\u7AEF\uFF1A\u4E0A\u4F20\u3001\u4F5C\u7B54\u3001\u9519\u9898\u672C\u3001\u5B66\u4E60\u62A5\u544A", userCount: users.filter((u) => u.role === "STUDENT").length, permissions: ["\u6587\u4EF6\u4E0A\u4F20", "\u9898\u76EE\u4F5C\u7B54", "\u9759\u6001\u68C0\u67E5", "LLM \u6279\u6539", "\u9519\u9898\u672C", "\u5B66\u4E60\u62A5\u544A"] }
  ]);
}
let config = {
  platformName: "EduCode \u667A\u80FD\u5B66\u4E60\u5E73\u53F0",
  allowRegister: true,
  defaultRole: "STUDENT",
  classes: ["\u8BA1\u7B97\u673A 2101", "\u8BA1\u7B97\u673A 2102", "\u8BA1\u7B97\u673A 2103", "\u8F6F\u4EF6 2101"]
};
function mockGetConfig() {
  return delay(config);
}
function mockUpdateConfig(data) {
  config = { ...data };
  return delay(config);
}
export {
  delay,
  mockCreateUser,
  mockDeleteUser,
  mockGetAdminStats,
  mockGetConfig,
  mockGetProfile,
  mockGetRoles,
  mockGetUserList,
  mockLogin,
  mockRegister,
  mockUpdateConfig,
  mockUpdateProfile,
  mockUpdateUserRole
};
