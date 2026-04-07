<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div id="homePage">
    <!-- 全屏加载遮罩 -->
    <div v-if="creating" class="loading-overlay">
      <div class="loading-content">
        <a-spin size="large" />
        <p class="loading-text">正在为您初始化应用，请稍候...</p>
        <p class="loading-subtext">AI 正在为您生成代码，这可能需要一点时间</p>
      </div>
    </div>
    <!-- 雪花背景 -->
    <div class="snow-container">
      <span class="snow snow-1"></span>
      <span class="snow snow-2"></span>
      <span class="snow snow-3"></span>
      <span class="snow snow-4"></span>
      <span class="snow snow-5"></span>
      <span class="snow snow-6"></span>
      <span class="snow snow-7"></span>
      <span class="snow snow-8"></span>
      <span class="snow snow-9"></span>
      <span class="snow snow-10"></span>
      <span class="snow snow-11"></span>
      <span class="snow snow-12"></span>
      <span class="snow snow-13"></span>
      <span class="snow snow-14"></span>
      <span class="snow snow-15"></span>
      <span class="snow snow-16"></span>
      <span class="snow snow-17"></span>
      <span class="snow snow-18"></span>
      <span class="snow snow-19"></span>
      <span class="snow snow-20"></span>
      <span class="snow snow-21"></span>
      <span class="snow snow-22"></span>
      <span class="snow snow-23"></span>
      <span class="snow snow-24"></span>
      <span class="snow snow-25"></span>
      <span class="snow snow-26"></span>
      <span class="snow snow-27"></span>
      <span class="snow snow-28"></span>
      <span class="snow snow-29"></span>
      <span class="snow snow-30"></span>
      <span class="snow snow-31"></span>
      <span class="snow snow-32"></span>
      <span class="snow snow-33"></span>
      <span class="snow snow-34"></span>
      <span class="snow snow-35"></span>
      <span class="snow snow-36"></span>
      <span class="snow snow-37"></span>
      <span class="snow snow-38"></span>
      <span class="snow snow-39"></span>
      <span class="snow snow-40"></span>
      <span class="snow snow-41"></span>
      <span class="snow snow-42"></span>
      <span class="snow snow-43"></span>
      <span class="snow snow-44"></span>
      <span class="snow snow-45"></span>
      <span class="snow snow-46"></span>
      <span class="snow snow-47"></span>
      <span class="snow snow-48"></span>
      <span class="snow snow-49"></span>
      <span class="snow snow-50"></span>
    </div>

    <div class="container">
      <!-- 英雄区域 -->
      <div class="hero-section">
        <div class="hero-badge">
          <span class="badge-icon">🚀</span>
          <span>AI 驱动的新一代开发方式</span>
        </div>

        <h1 class="hero-title">
          <span class="title-line">让创意瞬间</span>
          <span class="title-gradient">变为现实</span>
        </h1>

        <p class="hero-description">
          用自然语言描述你的想法，AI 帮你生成完整的应用代码。
          <br />无需编程基础，人人都是开发者。
        </p>

        <!-- 输入区域 -->
        <div class="input-section">
          <div class="input-wrapper">
            <a-textarea
              v-model:value="userPrompt"
              placeholder="描述你想创建的应用... 例如：一个现代化的个人博客网站"
              :rows="4"
              :maxlength="1000"
              class="prompt-input"
              @keydown.enter.exact.prevent="createApp"
            />
            <div class="input-actions">
              <a-button type="primary" size="large" @click="createApp" :loading="creating" class="submit-btn">
                <template #icon>
                  <span>✨</span>
                </template>
                {{ creating ? '生成中...' : '开始创建' }}
              </a-button>
            </div>
          </div>
          <p class="input-hint">按 Enter 发送，支持中英文描述</p>
        </div>

        <!-- 快捷标签 -->
        <div class="quick-tags">
          <span class="tag-label">试试这些：</span>
          <a-button
            v-for="(item, index) in [
              { label: '个人博客', prompt: '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能' },
              { label: '企业官网', prompt: '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面' },
              { label: '在线商城', prompt: '构建一个功能完整的在线商城，包含商品展示、购物车、订单管理、支付结算等功能' },
              { label: '作品集', prompt: '制作一个精美的作品展示网站，适合设计师、摄影师展示作品，采用瀑布流布局' },
            ]"
            :key="index"
            type="text"
            size="small"
            class="quick-tag"
            @click="setPrompt(item.prompt)"
          >
            {{ item.label }}
          </a-button>
        </div>
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">📦</span>
            我的作品
          </h2>
          <span class="section-count">{{ myAppsPage.total }} 个项目</span>
        </div>
        <div class="app-grid" v-if="myApps.length > 0">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="empty-state" v-else>
          <div class="empty-icon">💡</div>
          <p>还没有作品？在上方输入描述，开始创作吧！</p>
        </div>
        <div class="pagination-wrapper" v-if="myAppsPage.total > myAppsPage.pageSize">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section section-featured">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">⭐</span>
            精选案例
          </h2>
          <span class="section-count">来自社区的优秀作品</span>
        </div>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper" v-if="featuredAppsPage.total > featuredAppsPage.pageSize">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(200, 220, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(230, 210, 255, 0.12) 0%, transparent 50%),
    linear-gradient(180deg, #e8f4fc 0%, #f0f4ff 30%, #f8faff 60%, #fff5f8 100%);
  position: relative;
  overflow-x: hidden;
}

/* 全屏加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #e8f4fc 0%, #f0f4ff 30%, #f8faff 60%, #fff5f8 100%);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.loading-content {
  text-align: center;
  padding: 48px 32px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.2), 0 8px 24px rgba(0, 0, 0, 0.08);
  max-width: 480px;
  animation: scaleIn 0.4s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.loading-text {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 24px 0 8px;
}

.loading-subtext {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* ========== 雪花飞舞效果 ========== */
.snow-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
}

.snow {
  position: absolute;
  top: -10px;
  background: radial-gradient(circle, #ffffff 0%, #e8f0ff 60%, #d0e0ff 100%);
  border-radius: 50%;
  opacity: 0;
  animation: snowFall linear infinite;
  box-shadow:
    0 0 8px rgba(180, 200, 255, 0.9),
    0 0 16px rgba(160, 190, 255, 0.6),
    0 0 28px rgba(140, 170, 255, 0.3);
}

.snow-1 { left: 2%; width: 7px; height: 7px; animation-duration: 9s; animation-delay: 0s; }
.snow-2 { left: 5%; width: 4px; height: 4px; animation-duration: 12s; animation-delay: -2s; }
.snow-3 { left: 9%; width: 9px; height: 9px; animation-duration: 10s; animation-delay: -1s; }
.snow-4 { left: 14%; width: 5px; height: 5px; animation-duration: 14s; animation-delay: -4s; }
.snow-5 { left: 19%; width: 10px; height: 10px; animation-duration: 8s; animation-delay: -3s; }
.snow-6 { left: 24%; width: 4px; height: 4px; animation-duration: 15s; animation-delay: -7s; }
.snow-7 { left: 29%; width: 7px; height: 7px; animation-duration: 11s; animation-delay: -5s; }
.snow-8 { left: 34%; width: 5px; height: 5px; animation-duration: 13s; animation-delay: -9s; }
.snow-9 { left: 39%; width: 8px; height: 8px; animation-duration: 9s; animation-delay: -2s; }
.snow-10 { left: 44%; width: 4px; height: 4px; animation-duration: 15s; animation-delay: -11s; }
.snow-11 { left: 49%; width: 7px; height: 7px; animation-duration: 10s; animation-delay: -6s; }
.snow-12 { left: 54%; width: 5px; height: 5px; animation-duration: 12s; animation-delay: -8s; }
.snow-13 { left: 59%; width: 10px; height: 10px; animation-duration: 8s; animation-delay: -1s; }
.snow-14 { left: 64%; width: 4px; height: 4px; animation-duration: 14s; animation-delay: -13s; }
.snow-15 { left: 69%; width: 7px; height: 7px; animation-duration: 11s; animation-delay: -5s; }
.snow-16 { left: 74%; width: 5px; height: 5px; animation-duration: 13s; animation-delay: -10s; }
.snow-17 { left: 79%; width: 8px; height: 8px; animation-duration: 9s; animation-delay: -3s; }
.snow-18 { left: 84%; width: 4px; height: 4px; animation-duration: 17s; animation-delay: -14s; }
.snow-19 { left: 89%; width: 7px; height: 7px; animation-duration: 10s; animation-delay: -7s; }
.snow-20 { left: 94%; width: 5px; height: 5px; animation-duration: 12s; animation-delay: -4s; }
.snow-21 { left: 3%; width: 5px; height: 5px; animation-duration: 18s; animation-delay: -16s; }
.snow-22 { left: 8%; width: 8px; height: 8px; animation-duration: 12s; animation-delay: -8s; }
.snow-23 { left: 13%; width: 6px; height: 6px; animation-duration: 15s; animation-delay: -12s; }
.snow-24 { left: 18%; width: 9px; height: 9px; animation-duration: 10s; animation-delay: -5s; }
.snow-25 { left: 23%; width: 4px; height: 4px; animation-duration: 14s; animation-delay: -9s; }
.snow-26 { left: 28%; width: 7px; height: 7px; animation-duration: 12s; animation-delay: -3s; }
.snow-27 { left: 33%; width: 5px; height: 5px; animation-duration: 16s; animation-delay: -15s; }
.snow-28 { left: 38%; width: 10px; height: 10px; animation-duration: 9s; animation-delay: -2s; }
.snow-29 { left: 43%; width: 4px; height: 4px; animation-duration: 14s; animation-delay: -11s; }
.snow-30 { left: 48%; width: 7px; height: 7px; animation-duration: 11s; animation-delay: -6s; }
.snow-31 { left: 53%; width: 5px; height: 5px; animation-duration: 17s; animation-delay: -17s; }
.snow-32 { left: 58%; width: 8px; height: 8px; animation-duration: 10s; animation-delay: -4s; }
.snow-33 { left: 63%; width: 4px; height: 4px; animation-duration: 14s; animation-delay: -10s; }
.snow-34 { left: 68%; width: 7px; height: 7px; animation-duration: 12s; animation-delay: -7s; }
.snow-35 { left: 73%; width: 5px; height: 5px; animation-duration: 15s; animation-delay: -13s; }
.snow-36 { left: 78%; width: 10px; height: 10px; animation-duration: 8s; animation-delay: -1s; }
.snow-37 { left: 83%; width: 4px; height: 4px; animation-duration: 19s; animation-delay: -18s; }
.snow-38 { left: 88%; width: 7px; height: 7px; animation-duration: 12s; animation-delay: -8s; }
.snow-39 { left: 93%; width: 5px; height: 5px; animation-duration: 14s; animation-delay: -12s; }
.snow-40 { left: 97%; width: 8px; height: 8px; animation-duration: 10s; animation-delay: -5s; }
.snow-41 { left: 4%; width: 5px; height: 5px; animation-duration: 20s; animation-delay: -20s; }
.snow-42 { left: 11%; width: 7px; height: 7px; animation-duration: 14s; animation-delay: -14s; }
.snow-43 { left: 22%; width: 6px; height: 6px; animation-duration: 16s; animation-delay: -16s; }
.snow-44 { left: 37%; width: 9px; height: 9px; animation-duration: 10s; animation-delay: -9s; }
.snow-45 { left: 47%; width: 4px; height: 4px; animation-duration: 17s; animation-delay: -17s; }
.snow-46 { left: 57%; width: 7px; height: 7px; animation-duration: 12s; animation-delay: -11s; }
.snow-47 { left: 67%; width: 5px; height: 5px; animation-duration: 14s; animation-delay: -14s; }
.snow-48 { left: 77%; width: 10px; height: 10px; animation-duration: 9s; animation-delay: -3s; }
.snow-49 { left: 87%; width: 5px; height: 5px; animation-duration: 18s; animation-delay: -19s; }
.snow-50 { left: 92%; width: 7px; height: 7px; animation-duration: 12s; animation-delay: -10s; }

@keyframes snowFall {
  0% {
    opacity: 0;
    transform: translateY(-10px) rotate(0deg) translateX(0);
  }
  8% {
    opacity: 1;
  }
  50% {
    opacity: 0.92;
  }
  90% {
    opacity: 0.85;
  }
  100% {
    opacity: 0;
    transform: translateY(110vh) rotate(720deg) translateX(var(--sway, 30px));
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 24px 100px;
  position: relative;
  z-index: 2;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: 80px 0 70px;
  position: relative;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  border: 1px solid rgba(102, 126, 234, 0.15);
  border-radius: 50px;
  font-size: 14px;
  font-weight: 500;
  color: #667eea;
  margin-bottom: 32px;
  animation: fadeInUp 0.8s ease-out;
}

.badge-icon {
  font-size: 16px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-title {
  font-size: 64px;
  font-weight: 800;
  line-height: 1.15;
  margin: 0 0 28px;
  letter-spacing: -2px;
  animation: fadeInUp 0.8s ease-out 0.1s both;
}

.title-line {
  display: block;
  color: #1e293b;
}

.title-gradient {
  display: block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 40%, #f093fb 70%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% auto;
  animation: gradientShift 5s ease infinite;
}

@keyframes gradientShift {
  0%,
  100% {
    background-position: 0% center;
  }
  50% {
    background-position: 100% center;
  }
}

.hero-description {
  font-size: 18px;
  line-height: 1.7;
  color: #64748b;
  margin: 0 auto 44px;
  max-width: 650px;
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

/* 输入区域 */
.input-section {
  max-width: 750px;
  margin: 0 auto 32px;
  animation: fadeInUp 0.8s ease-out 0.3s both;
}

.input-wrapper {
  position: relative;
  background: #ffffff;
  border-radius: 20px;
  box-shadow:
    0 4px 24px rgba(102, 126, 234, 0.1),
    0 1px 3px rgba(0, 0, 0, 0.04);
  border: 2px solid #eef2ff;
  transition: all 0.3s ease;

  &:focus-within {
    transform: translateY(-3px);
    box-shadow:
      0 12px 40px rgba(102, 126, 234, 0.15),
      0 4px 12px rgba(0, 0, 0, 0.06);
    border-color: #667eea;
  }
}

.prompt-input {
  border: none !important;
  font-size: 16px !important;
  padding: 20px 80px 20px 24px !important;
  border-radius: 18px !important;
  background: transparent !important;
  resize: none;
  box-shadow: none !important;
  color: #1e293b !important;

  &:focus {
    box-shadow: none !important;
  }

  &::placeholder {
    color: #94a3b8;
  }
}

.input-actions {
  position: absolute;
  bottom: 16px;
  right: 16px;
}

.submit-btn {
  border-radius: 14px !important;
  font-weight: 600 !important;
  font-size: 16px !important;
  padding: 10px 28px !important;
  height: auto !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.25) !important;
  transition: all 0.3s ease !important;

  &:hover {
    transform: translateY(-2px) scale(1.02) !important;
    box-shadow: 0 6px 24px rgba(102, 126, 234, 0.35) !important;
  }
}

.input-hint {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  margin: 12px 0 0;
}

/* 快捷标签 */
.quick-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.tag-label {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.quick-tag {
  border-radius: 20px !important;
  padding: 6px 16px !important;
  height: auto !important;
  font-size: 13px !important;
  background: #f8faff !important;
  border: 1px solid #e2e8f0 !important;
  color: #667eea !important;
  font-weight: 500 !important;
  transition: all 0.25s ease !important;

  &:hover {
    background: #eef2ff !important;
    border-color: #667eea !important;
    color: #5a67d8 !important;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  }
}

/* 区域样式 */
.section {
  margin-bottom: 70px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 28px;
}

.section-count {
  font-size: 14px;
  color: #94a3b8;
  font-weight: 500;
}

/* 网格布局 */
.app-grid,
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
  margin-bottom: 28px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #64748b;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 40px;
  }

  .hero-description {
    font-size: 16px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .quick-tags {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
