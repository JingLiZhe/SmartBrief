<template>
  <div class="login-container">
    <div class="login-bg-decoration"></div>

    <!-- 标题：逐字海浪起伏 -->
    <div class="site-title">
      <span
              v-for="(char, index) in titleChars"
              :key="index"
              class="char"
              :style="{ 'animation-delay': (index * 0.08) + 's' }"
      >
        {{ char }}
      </span>
    </div>

    <!-- 水母容器（无重复事件） -->
    <div
            class="jellyfish-wrapper"
            @mouseenter="onAvatarEnter"
            @mouseleave="onAvatarLeave"
            @mousemove="onJellyMouseMove"
    >
      <svg
              class="jellyfish-svg"
              viewBox="0 0 200 260"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
      >
        <defs>
          <radialGradient id="headGrad" cx="40%" cy="30%" r="70%">
            <stop offset="0%" stop-color="#60a5fa" />
            <stop offset="40%" stop-color="#3b82f6" />
            <stop offset="80%" stop-color="#2563eb" />
            <stop offset="100%" stop-color="#1d4ed8" />
          </radialGradient>
          <linearGradient id="tentacleGrad" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="#93c5fd" />
            <stop offset="100%" stop-color="#3b82f6" />
          </linearGradient>
          <radialGradient id="helmetGrad" cx="35%" cy="25%" r="65%">
            <stop offset="0%" stop-color="#ffffff" stop-opacity="0.5" />
            <stop offset="60%" stop-color="#bfdbfe" stop-opacity="0.15" />
            <stop offset="100%" stop-color="#bfdbfe" stop-opacity="0.05" />
          </radialGradient>
          <radialGradient id="shineGrad" cx="30%" cy="20%" r="50%">
            <stop offset="0%" stop-color="#ffffff" stop-opacity="0.6" />
            <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
          </radialGradient>
          <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="6" result="blur" />
            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        <!-- 触手 -->
        <g class="tentacles" :class="{ 'tentacle-active': isHovering }">
          <g class="tentacle t1" style="transform-origin: 48px 140px;">
            <path d="M 48,140 C 30,190 62,210 44,240" stroke="url(#tentacleGrad)" stroke-width="5" fill="none" stroke-linecap="round" filter="url(#glow)" opacity="0.8"/>
            <circle cx="44" cy="240" r="4.5" fill="#93c5fd" opacity="0.6"/>
          </g>
          <g class="tentacle t2" style="transform-origin: 74px 140px;">
            <path d="M 74,140 C 56,200 94,225 76,248" stroke="url(#tentacleGrad)" stroke-width="4.5" fill="none" stroke-linecap="round" filter="url(#glow)" opacity="0.85"/>
            <circle cx="76" cy="248" r="4" fill="#93c5fd" opacity="0.6"/>
          </g>
          <g class="tentacle t3" style="transform-origin: 100px 140px;">
            <path d="M 100,140 C 86,210 118,240 100,260" stroke="url(#tentacleGrad)" stroke-width="5.5" fill="none" stroke-linecap="round" filter="url(#glow)" opacity="0.9"/>
            <circle cx="100" cy="260" r="5" fill="#93c5fd" opacity="0.7"/>
          </g>
          <g class="tentacle t4" style="transform-origin: 126px 140px;">
            <path d="M 126,140 C 144,200 106,225 124,248" stroke="url(#tentacleGrad)" stroke-width="4.5" fill="none" stroke-linecap="round" filter="url(#glow)" opacity="0.85"/>
            <circle cx="124" cy="248" r="4" fill="#93c5fd" opacity="0.6"/>
          </g>
          <g class="tentacle t5" style="transform-origin: 152px 140px;">
            <path d="M 152,140 C 170,190 138,210 156,240" stroke="url(#tentacleGrad)" stroke-width="5" fill="none" stroke-linecap="round" filter="url(#glow)" opacity="0.8"/>
            <circle cx="156" cy="240" r="4.5" fill="#93c5fd" opacity="0.6"/>
          </g>
        </g>

        <!-- 项圈 -->
        <ellipse cx="100" cy="140" rx="44" ry="12" fill="#475569" opacity="0.7" />
        <path d="M 56,140 L 56,150 A 44,12 0 0,0 144,150 L 144,140 Z" fill="#334155" opacity="0.7" />

        <!-- 头部（半圆 + 波浪底） -->
        <g class="jelly-head" :class="{ 'head-active': isHovering }">
          <path
                  d="M 32,140
               C 32,48 168,48 168,140
               Q 158,156 148,140
               Q 138,156 128,140
               Q 118,156 108,140
               Q 98,156 88,140
               Q 78,156 68,140
               Q 58,156 48,140
               Q 38,156 32,140 Z"
                  fill="url(#headGrad)"
                  filter="url(#glow)"
                  opacity="0.92"
          />
          <ellipse cx="100" cy="70" rx="50" ry="30" fill="url(#shineGrad)" opacity="0.5" />
          <circle cx="140" cy="85" r="12" fill="white" opacity="0.15" />
        </g>

        <!-- 头盔 -->
        <g class="helmet" :class="{ 'helmet-active': isHovering }">
          <circle cx="100" cy="90" r="76" stroke="#93c5fd" stroke-width="3" fill="url(#helmetGrad)" opacity="0.5" />
          <path d="M 40,60 A 70,70 0 0,1 145,45" stroke="white" stroke-width="6" stroke-linecap="round" opacity="0.4" fill="none" />
          <circle cx="58" cy="52" r="8" fill="white" opacity="0.5" />
          <circle cx="68" cy="58" r="4" fill="white" opacity="0.25" />
          <circle cx="100" cy="90" r="76" stroke="#60a5fa" stroke-width="3.5" fill="none" opacity="0.5" />
        </g>

        <!-- 眼睛（眼白固定，瞳孔跟随鼠标） -->
        <!-- 左眼 -->
        <circle cx="78" cy="95" r="17" fill="white" />
        <g :transform="`translate(${pupilOffset.x}, ${pupilOffset.y})`">
          <circle cx="78" cy="98" r="11" fill="#1e3a5f" />
          <circle cx="83" cy="93" r="4.5" fill="white" />
          <circle cx="73" cy="101" r="2.5" fill="white" opacity="0.4" />
        </g>

        <!-- 右眼 -->
        <circle cx="122" cy="95" r="17" fill="white" />
        <g :transform="`translate(${pupilOffset.x}, ${pupilOffset.y})`">
          <circle cx="122" cy="98" r="11" fill="#1e3a5f" />
          <circle cx="127" cy="93" r="4.5" fill="white" />
          <circle cx="117" cy="101" r="2.5" fill="white" opacity="0.4" />
        </g>

        <!-- 腮红 -->
        <ellipse cx="60" cy="118" rx="11" ry="7" fill="#fbcfe8" opacity="0.5" />
        <ellipse cx="140" cy="118" rx="11" ry="7" fill="#fbcfe8" opacity="0.5" />

        <!-- 微笑 -->
        <path d="M 88,115 Q 100,126 112,115" stroke="#1e3a5f" stroke-width="2.5" stroke-linecap="round" fill="none" opacity="0.6" />

        <!-- 眨眼眼皮 -->
        <g class="eyelids" :class="{ 'blink-active': isBlinking }">
          <path d="M 61,95 Q 78,76 95,95 L 95,101 Q 78,106 61,101 Z" fill="#3b82f6" />
          <path d="M 105,95 Q 122,76 139,95 L 139,101 Q 122,106 105,101 Z" fill="#3b82f6" />
        </g>

        <!-- 星星 -->
        <text x="28" y="48" font-size="10" fill="#93c5fd" opacity="0.5" class="star">✦</text>
        <text x="165" y="52" font-size="9" fill="#93c5fd" opacity="0.4" class="star">✦</text>
        <text x="178" y="115" font-size="7" fill="#93c5fd" opacity="0.35" class="star">✦</text>
        <text x="20" y="112" font-size="7" fill="#93c5fd" opacity="0.35" class="star">✦</text>
      </svg>
    </div>

    <!-- 右侧：老电脑翻译器 -->
    <div v-if="showTranslator" class="translator">
      <div class="translator-screen">
        <div class="translator-header">
          <span class="crt-led">●</span>
          <span class="crt-title">✦ 星海 · 深空广播 v2.0 ✦</span>
          <span class="crt-led">●</span>
        </div>
        <div class="translator-content">
          <div
                  v-for="(line, index) in dialogue"
                  :key="index"
                  class="dialogue-line jelly-line"
          >
            <span class="prompt">🐚</span>
            <span class="text">{{ line.text }}</span>
          </div>
          <div class="cursor-block">▌</div>
        </div>
        <div class="translator-footer">
          <span class="crt-status">ONLINE</span>
          <span class="crt-time">{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <!-- 登录卡片 -->
    <el-card class="login-card" shadow="never">
      <h2 class="text-center login-title">欢迎登录</h2>
      <el-form :model="loginForm" label-width="80px" ref="loginFormRef">
        <el-form-item
                label="用户名"
                prop="username"
                :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]"
        >
          <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  prefix-icon="User"
          />
        </el-form-item>
        <el-form-item
                label="密码"
                prop="password"
                :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]"
        >
          <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="w-full login-btn">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <p class="text-center mt-4 register-link">
        还没有账号？<a href="/register">立即注册</a>
        <span class="separator">|</span>
        <a href="/reset-password">忘记密码</a>
      </p>
    </el-card>
  </div>
</template>

<script setup>
    import { ref, computed, onMounted, onUnmounted } from 'vue'
    import axios from '../utils/axios'

    // ===== 登录逻辑 =====
    const loginForm = ref({
        username: '',
        password: ''
    })

    const loginFormRef = ref(null)

    const titleText = '智讯——你专属的新闻资讯平台'
    const titleChars = computed(() => titleText.split(''))

    const handleLogin = async () => {
        if (!loginForm.value.username.trim()) {
            alert('请输入用户名')
            return
        }
        if (!loginForm.value.password.trim()) {
            alert('请输入密码')
            return
        }

        try {
            const response = await axios.post('/auth/login', loginForm.value)
            localStorage.setItem('token', response.token)
            localStorage.setItem('username', loginForm.value.username)
            window.location.href = '/'
        } catch (error) {
            console.error('Login failed:', error)
            const errorMessage = error.response?.data?.error || error.response?.data?.message || '登录失败'
            alert(errorMessage)
        }
    }

    // ===== 水母与翻译器交互 =====
    const isHovering = ref(false)
    const isBlinking = ref(false)
    const showTranslator = ref(false)
    let blinkTimer = null

    const onAvatarEnter = () => {
        isHovering.value = true
        showTranslator.value = true
    }

    const onAvatarLeave = () => {
        isHovering.value = false
        showTranslator.value = false
        // 瞳孔归位
        pupilOffset.value.x = 0
        pupilOffset.value.y = 0
    }

    // ===== 瞳孔跟随鼠标 =====
    const pupilOffset = ref({ x: 0, y: 0 })
    const pupilMaxOffset = 8

    const onJellyMouseMove = (e) => {
        const rect = e.currentTarget.getBoundingClientRect()
        const cx = rect.left + rect.width / 2
        const cy = rect.top + rect.height / 2
        const dx = (e.clientX - cx) / (rect.width / 2)
        const dy = (e.clientY - cy) / (rect.height / 2)
        pupilOffset.value.x = Math.max(-1, Math.min(1, dx)) * pupilMaxOffset
        pupilOffset.value.y = Math.max(-1, Math.min(1, dy)) * pupilMaxOffset
    }

    onMounted(() => {
        blinkTimer = setInterval(() => {
            isBlinking.value = true
            setTimeout(() => {
                isBlinking.value = false
            }, 150)
        }, 3500)
    })

    onUnmounted(() => {
        if (blinkTimer) clearInterval(blinkTimer)
    })

    // ===== 水母单向广告文案 =====
    const dialogue = ref([
        { text: '🌌 我是星海，一只在深空漂流的水母。' },
        { text: '1998 年，中国第一个中文媒体资讯服务商——慧科讯业，诞生了。' },
        { text: '从香港中文大学的一个研究项目，到全球最大的中文媒体资讯平台之一。' },
        { text: '智讯，延续了这份属于中国的探索。' },
        { text: '世界等待你去了解。' },
    ])

    // ===== 当前时间 =====
    const currentTime = ref('')
    const updateTime = () => {
        const now = new Date()
        currentTime.value = now.toTimeString().slice(0, 8)
    }
    updateTime()
    setInterval(updateTime, 1000)
</script>

<style scoped>
  /* ========== 蓝白背景 ========== */
  .login-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    width: 100%;
    background: linear-gradient(135deg, #e8f0fe 0%, #d4e4ff 50%, #b7d2ff 100%);
    position: fixed;
    top: 0;
    left: 0;
    overflow: hidden;
    padding: 20px;
    box-sizing: border-box;
  }

  /* ========== 装饰性蓝色光晕 ========== */
  .login-bg-decoration {
    position: absolute;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.15), transparent 70%);
    border-radius: 50%;
    top: -150px;
    left: -150px;
    pointer-events: none;
  }
  .login-bg-decoration::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle at 80% 70%, rgba(102, 126, 234, 0.12), transparent 70%);
    border-radius: 50%;
    bottom: -200px;
    right: -150px;
  }

  /* ========== 海浪起伏标题 ========== */
  .site-title {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
    font-size: 38px;
    font-weight: 700;
    margin-bottom: 30px;
    gap: 10px;
    user-select: none;
    letter-spacing: 2px;
    padding: 0 20px;
    z-index: 2;
    position: relative;
  }

  .char {
    display: inline-block;
    color: #1a3a6b;
    padding: 0 1px;
    animation: wave 2.0s ease-in-out infinite;
  }

  @keyframes wave {
    0% { transform: translateY(0px); color: #1a3a6b; text-shadow: none; }
    20% { transform: translateY(-6px); color: #4a8bc2; text-shadow: 0 2px 10px rgba(64, 158, 255, 0.05); }
    40% { transform: translateY(-14px); color: #409eff; text-shadow: 0 4px 20px rgba(64, 158, 255, 0.15); }
    60% { transform: translateY(-18px); color: #5aafff; text-shadow: 0 6px 28px rgba(64, 158, 255, 0.25); }
    80% { transform: translateY(-10px); color: #409eff; text-shadow: 0 4px 18px rgba(64, 158, 255, 0.15); }
    100% { transform: translateY(0px); color: #1a3a6b; text-shadow: none; }
  }

  /* ========== 水母容器 ========== */
  .jellyfish-wrapper {
    position: absolute;
    left: 180px;
    top: 50%;
    transform: translateY(-50%);
    z-index: 10;
    cursor: pointer;
    animation: float 4s ease-in-out infinite;
    filter: drop-shadow(0 0 30px rgba(59, 130, 246, 0.25));
  }

  @keyframes float {
    0%, 100% { transform: translateY(-50%) translateX(0); }
    50% { transform: translateY(-54%) translateX(-4px); }
  }

  .jellyfish-svg {
    width: 150px;
    height: 210px;
    display: block;
  }

  /* 触手摆动 */
  .tentacle { transition: transform 0.3s ease; }
  .jellyfish-wrapper:hover .t1 { animation: swing1 1.2s ease-in-out infinite; }
  .jellyfish-wrapper:hover .t2 { animation: swing2 1.4s ease-in-out infinite 0.15s; }
  .jellyfish-wrapper:hover .t3 { animation: swing3 1.3s ease-in-out infinite 0.3s; }
  .jellyfish-wrapper:hover .t4 { animation: swing4 1.5s ease-in-out infinite 0.1s; }
  .jellyfish-wrapper:hover .t5 { animation: swing5 1.1s ease-in-out infinite 0.25s; }

  @keyframes swing1 { 0%,100%{transform:rotate(-5deg)}50%{transform:rotate(5deg)} }
  @keyframes swing2 { 0%,100%{transform:rotate(4deg)}50%{transform:rotate(-4deg)} }
  @keyframes swing3 { 0%,100%{transform:rotate(-3deg)}50%{transform:rotate(3deg)} }
  @keyframes swing4 { 0%,100%{transform:rotate(3.5deg)}50%{transform:rotate(-3.5deg)} }
  @keyframes swing5 { 0%,100%{transform:rotate(-4.5deg)}50%{transform:rotate(4.5deg)} }

  /* Q弹交互 */
  .jelly-head { transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1); transform-origin: center 120px; }
  .helmet { transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1); transform-origin: center 90px; }
  .jellyfish-wrapper:hover .jelly-head { transform: scale(1.06) rotate(-2deg); }
  .jellyfish-wrapper:hover .helmet { transform: scale(1.08) rotate(1.5deg); }
  .jellyfish-wrapper:hover .star { animation: starSpin 0.8s ease-in-out infinite alternate; }
  @keyframes starSpin { 0%{transform:scale(1) rotate(0);opacity:.5} 100%{transform:scale(1.3) rotate(15deg);opacity:1} }

  /* 眨眼 */
  .eyelids { transition: opacity 0.1s ease; opacity: 0; }
  .blink-active { opacity: 1; }

  /* 瞳孔跟随过渡 */
  .jellyfish-wrapper g { transition: transform 0.1s ease-out; }

  /* ============================================================ */
  /* ===== 老电脑翻译器 ===== */
  /* ============================================================ */
  .translator {
    position: fixed;
    right: 30px;
    top: 50%;
    transform: translateY(-50%);
    z-index: 20;
    width: 340px;
    max-height: 420px;
    background: #0c0c0c;
    border: 2px solid #33ff33;
    border-radius: 12px 12px 8px 8px;
    box-shadow: 0 0 30px rgba(51, 255, 51, 0.15), inset 0 0 30px rgba(51, 255, 51, 0.03);
    font-family: 'Courier New', Courier, monospace;
    padding: 4px 8px 6px 8px;
    animation: crtFlicker 0.08s infinite alternate;
  }

  @keyframes crtFlicker {
    0% { opacity: 1; }
    100% { opacity: 0.98; }
  }

  .translator::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 10px;
    background: repeating-linear-gradient(
            0deg,
            rgba(0,0,0,0.03) 0px,
            rgba(0,0,0,0.03) 2px,
            transparent 2px,
            transparent 4px
    );
    pointer-events: none;
    z-index: 1;
  }

  .translator-screen {
    background: #0a0a0a;
    border-radius: 8px;
    padding: 8px 10px 6px 10px;
    position: relative;
    z-index: 2;
  }

  .translator-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #33ff33;
    padding-bottom: 4px;
    margin-bottom: 8px;
    color: #33ff33;
    font-size: 11px;
    letter-spacing: 1px;
    text-transform: uppercase;
  }

  .crt-led {
    font-size: 12px;
    color: #33ff33;
    text-shadow: 0 0 6px #33ff33;
  }

  .crt-title {
    font-weight: 600;
    font-size: 11px;
    text-shadow: 0 0 8px #33ff33;
  }

  .translator-content {
    max-height: 280px;
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: #33ff33 #0a0a0a;
    padding-right: 4px;
    margin-bottom: 4px;
  }
  .translator-content::-webkit-scrollbar { width: 4px; }
  .translator-content::-webkit-scrollbar-track { background: #0a0a0a; }
  .translator-content::-webkit-scrollbar-thumb { background: #33ff33; border-radius: 2px; }

  .dialogue-line {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    margin-bottom: 4px;
    font-size: 13px;
    line-height: 1.5;
    color: #33ff33;
    text-shadow: 0 0 4px rgba(51, 255, 51, 0.3);
    word-break: break-word;
  }

  .prompt {
    flex-shrink: 0;
    color: #33ff33;
    opacity: 0.7;
    font-weight: 600;
    user-select: none;
  }

  .jelly-line .text { color: #55ff88; text-shadow: 0 0 8px rgba(85, 255, 136, 0.2); }

  .cursor-block {
    display: inline-block;
    color: #33ff33;
    font-size: 16px;
    animation: blinkCursor 0.9s step-end infinite;
    margin-top: 2px;
    line-height: 1.2;
  }

  @keyframes blinkCursor {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
  }

  .translator-footer {
    display: flex;
    justify-content: space-between;
    border-top: 1px solid #33ff33;
    padding-top: 4px;
    margin-top: 4px;
    font-size: 10px;
    color: #33ff33;
    opacity: 0.7;
    letter-spacing: 0.5px;
  }
  .crt-status { text-shadow: 0 0 6px #33ff33; }
  .crt-time { font-family: 'Courier New', monospace; }

  /* ========== 白色卡片 ========== */
  .login-card {
    width: 400px;
    padding: 36px 30px 30px;
    border-radius: 20px;
    background: #ffffff;
    border: 1px solid rgba(64, 158, 255, 0.12);
    box-shadow: 0 8px 40px rgba(64, 158, 255, 0.15);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    position: relative;
    z-index: 2;
  }
  .login-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 50px rgba(64, 158, 255, 0.2);
  }

  .login-title {
    color: #1a3a6b;
    font-weight: 700;
    font-size: 26px;
    letter-spacing: 3px;
    margin-bottom: 28px;
    text-align: center;
  }

  :deep(.el-form-item__label) { color: #3a5a8a !important; font-weight: 500; }
  :deep(.el-input__wrapper) {
    background: #f5f9ff !important;
    border: 1.5px solid #dce8f5 !important;
    border-radius: 12px !important;
    box-shadow: none !important;
    transition: all 0.3s ease;
  }
  :deep(.el-input__wrapper:hover) {
    border-color: #8bb9f0 !important;
    background: #f0f6fe !important;
  }
  :deep(.el-input__wrapper.is-focus) {
    border-color: #409eff !important;
    background: #ffffff !important;
    box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.12) !important;
  }
  :deep(.el-input__inner) { color: #1a3a6b !important; font-size: 15px; }
  :deep(.el-input__inner::placeholder) { color: #a0bcd8 !important; }
  :deep(.el-input__prefix) { color: #7aa9d9 !important; }
  :deep(.el-input__wrapper.is-focus .el-input__prefix) { color: #409eff !important; }

  .login-btn {
    height: 48px;
    border-radius: 12px;
    font-size: 17px;
    font-weight: 600;
    letter-spacing: 4px;
    background: linear-gradient(135deg, #409eff 0%, #2d7fd3 100%);
    border: none;
    transition: all 0.3s ease;
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
    color: #fff;
  }
  .login-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 28px rgba(64, 158, 255, 0.4);
    background: linear-gradient(135deg, #5aafff 0%, #3d8be0 100%);
  }
  .login-btn:active {
    transform: translateY(0px);
    box-shadow: 0 3px 10px rgba(64, 158, 255, 0.25);
  }

  .register-link {
    margin-top: 20px;
    color: #6a8aaa;
    font-size: 14px;
    text-align: center;
  }
  .register-link a {
    color: #409eff;
    text-decoration: none;
    font-weight: 600;
    transition: all 0.3s ease;
    position: relative;
  }
  .register-link a::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: #409eff;
    transition: width 0.3s ease;
  }
  .register-link a:hover { color: #2d7fd3; }
  .register-link a:hover::after { width: 100%; }

  .w-full { width: 100%; }
  .text-center { text-align: center; }

  /* ========== 响应式 ========== */
  @media (max-width: 768px) {
    .translator { right: 10px; width: 260px; max-height: 300px; padding: 3px 6px 4px 6px; }
    .translator-content { max-height: 180px; }
    .dialogue-line { font-size: 11px; }
    .crt-title { font-size: 9px; }
    .jellyfish-wrapper { display: none; }
  }

  @media (max-width: 480px) {
    .login-card { width: 92%; padding: 28px 18px 22px; }
    .login-title { font-size: 22px; }
    :deep(.el-form-item__label) { font-size: 13px; }
    :deep(.el-input__wrapper) { border-radius: 10px !important; }
    .login-btn { height: 44px; font-size: 15px; }
    .site-title { font-size: 24px; gap: 6px; letter-spacing: 1px; }
    @keyframes wave {
      0% { transform: translateY(0px); }
      20% { transform: translateY(-4px); }
      40% { transform: translateY(-10px); }
      60% { transform: translateY(-14px); }
      80% { transform: translateY(-8px); }
      100% { transform: translateY(0px); }
    }
    .jellyfish-wrapper { display: none; }
    .translator { display: none; }
  }
</style>
