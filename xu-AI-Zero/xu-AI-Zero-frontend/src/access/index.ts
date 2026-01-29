import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import ACCESS_ENUM from '@/access/accessEnum.ts'
import checkAccess from '@/access/checkAccess.ts'

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  //如果之前没登录过，自动登录
  if (!loginUser || !loginUser.userRole) {
    //加await是为了等用户登录成功之后，再执行后续的代码
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
  }
  console.log('登录用户信息', loginUser)
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN
  //要跳转的页面必须要登录
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    //如果没登录，跳转到登录页面
    if (!loginUser || !loginUser.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }

    //如果已经登录了，但是权限不足，那么跳转到无权限页面
    if (!checkAccess(loginUser, needAccess)) {
      next('/noAuth')
      return
    }
  }
  next()
})
