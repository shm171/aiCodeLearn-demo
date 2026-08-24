// ============================================================
// 用户相关的接口请求（API 层）
// 作用：把"调用哪个后端接口"集中写在这里，
//      页面里只需要调用这些函数，不用关心请求细节。
//
// 以后每对接一个后端接口，就在这里加一个函数，
// 比如：注册、修改资料、查询用户等。
// ============================================================

import request from '../utils/request'

// 登录接口
// 对应后端：POST /login
// 参数：email 邮箱、password 密码
// 成功后返回：{ token: "jwt字符串" }
export function loginApi(email, password) {
  return request.post('/login', { email, password })
}
