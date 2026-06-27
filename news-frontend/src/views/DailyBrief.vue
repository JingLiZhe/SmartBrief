<template>
  <div class="page-container">
    <div class="card-wrapper">
      <div class="card-header">
        <div class="card-header-title">
          <span class="icon-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </span>
          每日简报
        </div>
        <span class="badge">AI 生成</span>
      </div>
      <div class="card-body">
        <div class="category-selector">
          <span class="selector-label">新闻类型：</span>
          <div class="category-tags">
            <button
                    v-for="cat in categories"
                    :key="cat.code"
                    :class="['category-tag', { active: selectedCategory === cat.code }]"
                    @click="selectCategory(cat.code)"
            >
              {{ cat.name }}
            </button>
          </div>
        </div>

        <div v-if="loading" class="progress-container">
          <div class="progress-header">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            <span>AI 正在生成简报，请稍候...</span>
          </div>
          <div class="progress-bar-wrapper">
            <div class="progress-bar">
              <div class="progress-fill"></div>
              <div class="progress-glow"></div>
            </div>
            <div class="progress-steps">
              <span :class="{ active: progressStep >= 1 }">获取新闻</span>
              <span :class="{ active: progressStep >= 2 }">分析内容</span>
              <span :class="{ active: progressStep >= 3 }">生成简报</span>
            </div>
          </div>
        </div>

        <div v-else-if="brief" class="brief-content-wrapper">
          <div class="brief-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
            <span>{{ formatDate(brief.briefDate) }} 每日简报</span>
            <button class="copy-btn" @click="copyContent" title="复制简报内容">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
              <span v-if="!copied">复制</span>
              <span v-else class="copied-text">已复制</span>
            </button>
          </div>

          <div class="brief-content" v-html="formatContent(brief.content)"></div>

          <div class="brief-meta">
            <span class="meta-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              {{ brief.briefDate }}
            </span>
            <span class="meta-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 7h-9"></path>
                <path d="M14 17H5"></path>
                <circle cx="17" cy="17" r="3"></circle>
                <circle cx="7" cy="7" r="3"></circle>
              </svg>
              {{ brief.category }}
            </span>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </div>
          <div class="empty-text">暂无今日简报</div>
          <div class="empty-hint">点击上方分类按钮生成简报</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
    import { ref, onMounted } from 'vue'
    import axios from '../utils/axios'

    const brief = ref(null)
    const loading = ref(false)
    const progressStep = ref(0)
    const selectedCategory = ref('domestic')
    const copied = ref(false)
    const categories = ref([
        { code: 'domestic', name: '国内' },
        { code: 'international', name: '国际' }
    ])

    const formatDate = (dateStr) => {
        if (!dateStr) return ''
        const date = new Date(dateStr)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        return `${year}年${month}月${day}日`
    }

    const formatContent = (content) => {
        if (!content) return ''
        return content
            .replace(/### (.*)/g, '<h3>$1</h3>')
            .replace(/## (.*)/g, '<h2>$1</h2>')
            .replace(/\*\*(.*)\*\*/g, '<strong>$1</strong>')
            .replace(/\n/g, '<br>')
    }

    const copyContent = async () => {
        if (!brief.value || !brief.value.content) return
        try {
            await navigator.clipboard.writeText(brief.value.content)
            copied.value = true
            setTimeout(() => {
                copied.value = false
            }, 2000)
        } catch (error) {
            console.error('复制失败:', error)
            const textarea = document.createElement('textarea')
            textarea.value = brief.value.content
            textarea.style.position = 'fixed'
            textarea.style.opacity = '0'
            document.body.appendChild(textarea)
            textarea.select()
            document.execCommand('copy')
            document.body.removeChild(textarea)
            copied.value = true
            setTimeout(() => {
                copied.value = false
            }, 2000)
        }
    }

    const fetchBrief = async (category = selectedCategory.value) => {
        loading.value = true
        progressStep.value = 0
        brief.value = null

        try {
            progressStep.value = 1
            const res = await axios.get('/briefs', { params: { category } })
            progressStep.value = 2
            await new Promise(resolve => setTimeout(resolve, 300))
            progressStep.value = 3
            brief.value = res.data
        } catch (error) {
            console.error('Fetch brief failed:', error)
            brief.value = null
        } finally {
            loading.value = false
        }
    }

    const selectCategory = (category) => {
        selectedCategory.value = category
        fetchBrief(category)
    }

    onMounted(() => {
        fetchBrief()
    })
</script>
<style scoped>
  /* ====== 所有 SCSS 嵌套已展开为纯 CSS ====== */

  .page-container {
    max-width: 900px;
    margin: 0 auto;
    padding: 30px 20px;
  }

  .card-wrapper {
    background: white;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    overflow: hidden;
  }

  .card-wrapper:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #f2f6fc;
    background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f4 100%);
  }

  .card-header-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    color: white;
  }

  .badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
    color: #409eff;
  }

  .card-body {
    padding: 24px;
  }

  .category-selector {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid #f2f6fc;
  }

  .selector-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }

  .category-tags {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }

  /* ====== 展开的 .category-tag 及其伪类 ====== */
  .category-tag {
    padding: 8px 18px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
    border: 2px solid #e4e7ed;
    background: white;
    color: #606266;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .category-tag:hover {
    border-color: #409eff;
    color: #409eff;
  }

  .category-tag.active {
    border-color: #409eff;
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
    color: #409eff;
  }

  /* ====== 其他样式保持不变 ====== */

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 60px 20px;
  }

  .brief-content-wrapper {
    animation: fadeIn 0.3s ease;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .brief-title {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 22px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 2px solid #f2f6fc;
  }

  .brief-title svg {
    color: #409eff;
  }

  .copy-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 500;
    border: 1px solid #e4e7ed;
    background: white;
    color: #606266;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-left: auto;
  }

  .copy-btn:hover {
    border-color: #409eff;
    color: #409eff;
    background: rgba(64, 158, 255, 0.05);
  }

  .copy-btn svg {
    color: inherit;
  }

  .copied-text {
    color: #52c41a;
  }

  .brief-content {
    font-size: 15px;
    line-height: 1.8;
    color: #606266;
  }

  .brief-content h2 {
    font-size: 18px;
    font-weight: 600;
    margin-top: 24px;
    margin-bottom: 12px;
    color: #303133;
    padding-left: 12px;
    border-left: 4px solid #409eff;
  }

  .brief-content h3 {
    font-size: 16px;
    font-weight: 600;
    margin-top: 20px;
    margin-bottom: 10px;
    color: #303133;
  }

  .brief-content p {
    margin-bottom: 12px;
  }

  .brief-content ul {
    padding-left: 24px;
    margin-bottom: 16px;
  }

  .brief-content li {
    margin-bottom: 8px;
    position: relative;
  }

  .brief-content li::marker {
    color: #409eff;
  }

  .brief-content strong {
    color: #303133;
    font-weight: 600;
  }

  .brief-meta {
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #f2f6fc;
    display: flex;
    gap: 24px;
    color: #909399;
    font-size: 13px;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
  }

  .empty-icon {
    color: #c0c4cc;
    margin-bottom: 16px;
  }

  .empty-text {
    color: #909399;
    font-size: 15px;
    margin-bottom: 8px;
  }

  .empty-hint {
    color: #c0c4cc;
    font-size: 13px;
  }

  /* 进度条样式 */
  .progress-container {
    padding: 40px 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }

  .progress-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
    color: #409eff;
    font-weight: 500;
  }

  .progress-bar-wrapper {
    width: 100%;
    max-width: 400px;
  }

  .progress-bar {
    position: relative;
    height: 8px;
    background: linear-gradient(90deg, #e8f4fd 0%, #e4e7ed 100%);
    border-radius: 4px;
    overflow: hidden;
  }

  .progress-fill {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    background: linear-gradient(90deg, #409eff 0%, #667eea 50%, #409eff 100%);
    background-size: 200% 100%;
    border-radius: 4px;
    animation: progressAnimation 2s ease-in-out infinite;
    transition: width 0.5s ease;
  }

  .progress-glow {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 100%;
    background: linear-gradient(90deg, transparent 0%, rgba(64, 158, 255, 0.3) 50%, transparent 100%);
    animation: glowAnimation 2s ease-in-out infinite;
  }

  @keyframes progressAnimation {
    0% {
      width: 10%;
      background-position: 200% 0;
    }
    50% {
      width: 60%;
      background-position: 0 0;
    }
    100% {
      width: 90%;
      background-position: 200% 0;
    }
  }

  @keyframes glowAnimation {
    0% {
      transform: translateX(-100%);
    }
    100% {
      transform: translateX(100%);
    }
  }

  .progress-steps {
    display: flex;
    justify-content: space-between;
    margin-top: 16px;
    font-size: 13px;
    color: #c0c4cc;
  }

  .progress-steps span {
    transition: all 0.3s ease;
    position: relative;
  }

  .progress-steps span.active {
    color: #409eff;
    font-weight: 500;
  }

  .progress-steps span.active::before {
    content: '';
    position: absolute;
    bottom: -4px;
    left: 50%;
    transform: translateX(-50%);
    width: 8px;
    height: 8px;
    background: #409eff;
    border-radius: 50%;
    animation: dotPulse 1s ease-in-out infinite;
  }

  @keyframes dotPulse {
    0%, 100% {
      transform: translateX(-50%) scale(1);
      opacity: 1;
    }
    50% {
      transform: translateX(-50%) scale(1.3);
      opacity: 0.7;
    }
  }

  @media (max-width: 768px) {
    .page-container {
      padding: 20px 15px;
    }

    .brief-title {
      font-size: 18px;
    }
  }
</style>
