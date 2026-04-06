<template>
  <a-layout-header class="header">
    <a-row :wrap="false" class="header-row">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="280px">
        <RouterLink to="/">
          <div class="header-left">
            <div class="logo-wrapper">
              <img class="logo" src="@/assets/logo.png" alt="Logo" />
              <div class="logo-glow"></div>
            </div>
            <div class="title-wrapper">
              <h1 class="site-title">CodeCraft<span class="title-accent">AI</span></h1>
              <p class="site-subtitle">智能代码生成平台</p>
            </div>
          </div>
        </RouterLink>
      </a-col>

      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
          class="nav-menu"
        />
      </a-col>

      <!-- 右侧：用户操作区域 -->
      <a-col flex="200px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space class="user-info">
                <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="36" class="user-avatar">
                  {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
                </a-avatar>
                <span class="user-name">{{ loginUserStore.loginUser.userName ?? '创作者' }}</span>
              </a-space>
              <template #overlay>
                <a-menu class="dropdown-menu">
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>

          </div>
          <div v-else class="auth-buttons">
            <a-button type="primary" href="/user/login" class="login-btn">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'

import { useLoginUserStore } from '@/stores/loginUser.ts'
import { LogoutOutlined, HomeOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const router = useRouter()

// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '首页',
    title: '首页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://iamxurulin.github.io', target: '_blank' }, 'Coder_Studio'),
    title: 'iamxurulin',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败,' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  padding: 0 32px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 64px;
  line-height: 64px;
}

.header :deep(.ant-row) {
  height: 64px !important;
}

.header :deep(.ant-col) {
  height: 64px !important;
}

.header-row {
  max-width: 1400px;
  margin: 0 auto;
  height: 64px;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;
  height: 64px;
  line-height: 1;
}

.header-left:hover {
  transform: scale(1.02);
}

.logo-wrapper {
  position: relative;
  width: 38px;
  height: 38px;
  flex-shrink: 0;
}

.logo {
  height: 38px;
  width: 38px;
  position: relative;
  z-index: 2;
  border-radius: 8px;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  filter: blur(12px);
  opacity: 0.6;
  animation: glowPulse 3s ease-in-out infinite;
}

@keyframes glowPulse {
  0%,
  100% {
    opacity: 0.4;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.8;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0px;
  justify-content: center;
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.3px;
  line-height: 1.3;
}

.title-accent {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.site-subtitle {
  margin: 0;
  font-size: 10px;
  color: #94a3b8;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  line-height: 1.4;
  padding-left: 8px;
}

.nav-menu {
  border-bottom: none !important;
  background: transparent;
  line-height: 64px;
}

.nav-menu :deep(.ant-menu-item) {
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s ease;

  &:hover {
    color: #667eea;
  }

  &.ant-menu-item-selected {
    color: #667eea;
    border-bottom: 2px solid #667eea !important;
  }
}

.user-login-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 64px;
}

.user-info {
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 10px;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(102, 126, 234, 0.08);
  }
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-name {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.auth-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.login-btn {
  border-radius: 10px;
  font-weight: 600;
  padding: 8px 24px;
  height: auto;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  }
}

.dropdown-menu {
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}
</style>
