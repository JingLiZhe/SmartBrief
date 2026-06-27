<template>
  <div class="history-container">
    <el-card class="history-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>操作历史</span>
          <el-button type="primary" size="small" @click="goBack">返回个人中心</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="mb-4">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="搜索记录" name="search"></el-tab-pane>
        <el-tab-pane label="简报记录" name="summary"></el-tab-pane>
        <el-tab-pane label="文本记录" name="text"></el-tab-pane>
      </el-tabs>

      <div v-if="filteredHistory.length > 0" class="history-list">
        <div
                v-for="(item, index) in filteredHistory"
                :key="item.id || index"
                class="history-item"
        >
          <div class="item-header">
            <span class="item-type" :class="item.typeClass || ''">{{ item.typeLabel || '未知' }}</span>
            <div class="item-actions">
              <button class="copy-btn" @click="copyItem(item)" title="复制内容">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                </svg>
                <span v-if="!item.copied">复制</span>
                <span v-else class="copied-text">已复制</span>
              </button>
              <span class="item-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="item-content">
            <p class="item-title">{{ item.title || '无标题' }}</p>
            <p v-if="item.description" class="item-desc">{{ item.description }}</p>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-icon class="empty-icon"><Clock /></el-icon>
        <p>暂无操作记录</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
    import { ref, computed, onMounted } from 'vue'
    import { useRouter } from 'vue-router'
    import { Clock } from '@element-plus/icons-vue'
    import axios from '../utils/axios'

    const router = useRouter()
    const goBack = () => {
      router.push({ path: '/profile' })
    }

    const activeTab = ref('all')
    const historyList = ref([])

    const filteredHistory = computed(() => {
        if (!Array.isArray(historyList.value)) return []
        if (activeTab.value === 'all') return historyList.value
        return historyList.value.filter(item => item.type === activeTab.value)
    })

    const formatTime = (dateStr) => {
        if (!dateStr) return ''
        const date = new Date(dateStr)
        if (isNaN(date.getTime())) return ''
        const now = new Date()
        const diff = now - date
        if (diff < 60000) return '刚刚'
        if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
        if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
        return date.toLocaleDateString('zh-CN')
    }

    const copyItem = async (item) => {
        const content = item.description || item.title || ''
        if (!content) return
        try {
            await navigator.clipboard.writeText(content)
            item.copied = true
            setTimeout(() => {
                item.copied = false
            }, 2000)
        } catch (error) {
            console.error('复制失败:', error)
            // 降级方案
            const textarea = document.createElement('textarea')
            textarea.value = content
            textarea.style.position = 'fixed'
            textarea.style.opacity = '0'
            document.body.appendChild(textarea)
            textarea.select()
            document.execCommand('copy')
            document.body.removeChild(textarea)
            item.copied = true
            setTimeout(() => {
                item.copied = false
            }, 2000)
        }
    }

    const fetchHistory = async () => {
        try {
            // 1. 获取 token（根据实际存储位置调整）
            const token = localStorage.getItem('token') || sessionStorage.getItem('token')
            
            // 2. 请求接口，如果有token则携带Authorization头，禁用缓存
            const config = {
                headers: {
                    'Cache-Control': 'no-cache',
                    'Pragma': 'no-cache',
                    'Expires': '0'
                }
            }
            if (token) {
                config.headers['Authorization'] = 'Bearer ' + token
            }
            
            const response = await axios.get('/history', config)

            // 3. 处理响应（axios拦截器已将响应处理为response.data）
            if (response && response.code === 200) {
                const list = response.data
                if (Array.isArray(list)) {
                    historyList.value = list   // 后端字段已完全匹配，无需额外映射
                    console.log('历史数据加载成功:', list)
                } else {
                    console.warn('返回的 data 不是数组:', list)
                    historyList.value = []
                }
            } else {
                console.warn('接口返回错误:', response?.msg || '未知错误')
                historyList.value = []
            }
        } catch (error) {
            console.error('获取历史记录失败:', error)
            historyList.value = []
        }
    }

    onMounted(() => {
        fetchHistory()
    })
</script>

<style scoped>
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .history-container { max-width: 800px; margin: 0 auto; padding: 20px; }
  .history-card { margin-top: 20px; }
  .history-list { margin-top: 16px; }
  .history-item { padding: 16px; border-bottom: 1px solid #f0f0f0; transition: background-color 0.2s; }
  .history-item:hover { background-color: #fafafa; }
  .history-item:last-child { border-bottom: none; }
  .item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
  .item-type { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
  .item-type.search { background-color: #e6f7ff; color: #1890ff; }
  .item-type.summary { background-color: #f6ffed; color: #52c41a; }
  .item-type.text { background-color: #fff7e6; color: #fa8c16; }
  .item-actions { display: flex; align-items: center; gap: 12px; }
  .item-time { font-size: 12px; color: #999; }
  .item-content { margin-top: 8px; }
  .item-title { font-weight: 500; margin-bottom: 4px; }
  .item-desc { font-size: 13px; color: #666; line-height: 1.5; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .empty-state { text-align: center; padding: 60px 0; color: #999; }
  .empty-icon { font-size: 48px; margin-bottom: 16px; color: #ccc; }
  .mb-4 { margin-bottom: 16px; }
  
  .copy-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    border: 1px solid #e4e7ed;
    background: white;
    color: #606266;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .copy-btn:hover {
    border-color: #409eff;
    color: #409eff;
    background: rgba(64, 158, 255, 0.05);
  }
  
  .copied-text {
    color: #52c41a;
  }
</style>
