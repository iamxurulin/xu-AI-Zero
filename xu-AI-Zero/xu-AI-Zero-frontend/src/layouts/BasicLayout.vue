<template>
  <a-layout class="basic-layout">
    <div class="bg-layer">
      <div class="mesh-gradient"></div>
      <div class="grid-pattern"></div>
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- 顶部导航栏 -->
    <GlobalHeader />

    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>

    <!-- 底部版权信息 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  position: relative;
  min-height: 100vh;
  background: transparent;
}

.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: visible;
}

.mesh-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 10% 20%, rgba(99, 102, 241, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse 70% 50% at 90% 80%, rgba(168, 85, 247, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse 60% 40% at 50% 50%, rgba(56, 189, 248, 0.05) 0%, transparent 50%),
    radial-gradient(ellipse 90% 70% at 20% 90%, rgba(236, 72, 153, 0.04) 0%, transparent 50%);
  animation: meshFloat 20s ease-in-out infinite alternate;
}

@keyframes meshFloat {
  0% { transform: scale(1) rotate(0deg); }
  100% { transform: scale(1.1) rotate(1deg); }
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 100% 100% at 50% 0%, black 30%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse 100% 100% at 50% 0%, black 30%, transparent 70%);
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
}

.orb-1 {
  width: 500px;
  height: 500px;
  top: -10%;
  right: -5%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%);
  animation: orbFloat1 25s ease-in-out infinite;
}

.orb-2 {
  width: 400px;
  height: 400px;
  bottom: -5%;
  left: -5%;
  background: radial-gradient(circle, rgba(168, 85, 247, 0.12) 0%, transparent 70%);
  animation: orbFloat2 30s ease-in-out infinite;
}

.orb-3 {
  width: 350px;
  height: 350px;
  top: 40%;
  left: 45%;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.1) 0%, transparent 70%);
  animation: orbFloat3 22s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-40px, 30px) scale(1.05); }
  66% { transform: translate(20px, -20px) scale(0.95); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -25px) scale(1.08); }
  66% { transform: translate(-20px, 35px) scale(0.92); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, -30px) scale(1.1); }
}

.main-content {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 0;
  margin: 0;
}
</style>
