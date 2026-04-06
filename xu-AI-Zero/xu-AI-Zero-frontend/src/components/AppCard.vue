<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <!-- 预览图区域 -->
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <div class="placeholder-icon">🎨</div>
        <span class="placeholder-text">预览图</span>
      </div>

      <!-- 悬浮操作层 -->
      <div class="app-overlay">
        <div class="overlay-content">
          <a-button type="primary" class="action-btn" @click="handleViewChat">
            <span>💬</span> 查看对话
          </a-button>
          <a-button v-if="app.deployKey" class="action-btn action-btn--secondary" @click="handleViewWork">
            <span>🌐</span> 预览作品
          </a-button>
        </div>
      </div>

      <!-- 精选标签 -->
      <div v-if="featured" class="featured-badge">
        <span>⭐ 精选</span>
      </div>
    </div>

    <!-- 信息区域 -->
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="app.user?.userAvatar" :size="44" class="creator-avatar">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-meta">
          <span class="author">{{ app.user?.userName || (featured ? '官方' : '创作者') }}</span>
          <span class="divider">·</span>
          <span class="time">{{ formatTime(app.createTime) }}</span>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '刚刚'

  const now = new Date()
  const date = new Date(time)
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}
</script>

<style scoped>
.app-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;

  &:hover {
    transform: translateY(-10px);
    box-shadow:
      0 20px 50px rgba(102, 126, 234, 0.15),
      0 8px 25px rgba(0, 0, 0, 0.08);

    .app-preview img {
      transform: scale(1.08);
    }

    .app-overlay {
      opacity: 1;
    }
  }

  &--featured {
    border-color: rgba(102, 126, 234, 0.2);
    box-shadow:
      0 4px 24px rgba(102, 126, 234, 0.12),
      0 1px 6px rgba(0, 0, 0, 0.04);

    &:hover {
      box-shadow:
        0 24px 60px rgba(102, 126, 234, 0.2),
        0 10px 30px rgba(0, 0, 0, 0.1);
    }
  }
}

/* 预览图 */
.app-preview {
  height: 200px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.app-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #c4b5fd;
}

.placeholder-icon {
  font-size: 48px;
  opacity: 0.7;
}

.placeholder-text {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.6;
}

/* 悬浮层 */
.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    180deg,
    rgba(0, 0, 0, 0.05) 0%,
    rgba(0, 0, 0, 0.65) 100%
  );
  display: flex;
  align-items: flex-end;
  padding: 20px;
  opacity: 0;
  transition: opacity 0.35s ease;
}

.overlay-content {
  width: 100%;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.action-btn {
  border-radius: 12px !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  padding: 10px 22px !important;
  height: auto !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.35) !important;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease !important;

  &:hover {
    transform: translateY(-2px) scale(1.02) !important;
    box-shadow: 0 6px 24px rgba(102, 126, 234, 0.45) !important;
  }

  &--secondary {
    background: rgba(255, 255, 255, 0.95) !important;
    color: #667eea !important;
    border: 1px solid rgba(255, 255, 255, 0.9) !important;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;

    &:hover {
      background: #fff !important;
      box-shadow: 0 6px 24px rgba(0, 0, 0, 0.2) !important;
    }
  }
}

/* 精选标签 */
.featured-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
  font-size: 12px;
  font-weight: 700;
  border-radius: 20px;
  box-shadow: 0 4px 14px rgba(245, 158, 11, 0.35);
  letter-spacing: 0.5px;
}

/* 信息区 */
.app-info {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.95);
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.app-info-left {
  flex-shrink: 0;
}

.creator-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
  font-weight: 600;
  font-size: 16px;
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: 17px;
  font-weight: 700;
  margin: 0 0 6px;
  color: #1e293b;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-meta {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.author {
  color: #64748b;
  font-weight: 600;
}

.divider {
  opacity: 0.4;
}

.time {
  opacity: 0.85;
}
</style>
