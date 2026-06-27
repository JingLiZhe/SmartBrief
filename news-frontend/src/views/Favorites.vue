<template>
  <div class="favorites-container">
    <el-card class="favorites-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>我的收藏</span>
          <div class="header-actions">
            <el-button 
              v-if="favorites.length > 0" 
              type="danger" 
              size="small" 
              @click="clearAllFavorites"
              :loading="clearing"
            >
              清空所有收藏
            </el-button>
            <el-button type="primary" size="small" @click="goBack">返回个人中心</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>

      <div v-else-if="favorites.length > 0" class="favorites-list">
        <div
          v-for="(item, index) in favorites"
          :key="item.id || index"
          class="favorite-item"
        >
          <div class="item-content" @click="openNews(item)">
            <img 
              :src="getImageUrl(item.picUrl)" 
              :alt="item.title" 
              class="item-image"
              @error="handleImageError"
            />
            <div class="item-info">
              <h4 class="item-title">{{ item.title }}</h4>
              <p class="item-desc">{{ item.description || '暂无描述' }}</p>
              <div class="item-footer">
                <span class="item-source">{{ item.source }}</span>
                <span class="item-time">{{ formatTime(item.ctime) }}</span>
              </div>
            </div>
          </div>
          <div class="item-actions">
            <el-button 
              type="danger" 
              size="small" 
              circle 
              @click="removeFavorite(item.newsId)"
              title="取消收藏"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-icon class="empty-icon"><Star /></el-icon>
        <p>暂无收藏内容</p>
        <p class="empty-hint">在首页点击新闻卡片上的星标即可收藏</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '../utils/axios'

const router = useRouter()
const favorites = ref([])
const loading = ref(true)
const clearing = ref(false)

const goBack = () => {
  router.push({ path: '/profile' })
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    const response = await axios.get('/favorites')
    if (response && response.code === 200 && Array.isArray(response.data)) {
      favorites.value = response.data
    } else {
      favorites.value = []
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    favorites.value = []
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (newsId) => {
  try {
    await axios.delete(`/favorites/${newsId}`)
    favorites.value = favorites.value.filter(item => item.newsId !== newsId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败')
  }
}

const clearAllFavorites = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有收藏吗？此操作不可恢复。', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    clearing.value = true
    // 逐个删除
    for (const item of favorites.value) {
      await axios.delete(`/favorites/${item.newsId}`)
    }
    favorites.value = []
    ElMessage.success('已清空所有收藏')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空收藏失败:', error)
      ElMessage.error('清空收藏失败')
    }
  } finally {
    clearing.value = false
  }
}

const openNews = (item) => {
  if (item.url) {
    window.open(item.url, '_blank')
  }
}

const getImageUrl = (picUrl) => {
  if (!picUrl) return ''
  if (picUrl.includes('img.ithome.com')) {
    return `/api/home-news/proxy-image?url=${encodeURIComponent(picUrl)}`
  }
  return picUrl
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) {
    const minutes = Math.floor(diff / (1000 * 60))
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return timeStr.split(' ')[0]
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.favorites-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.favorites-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.favorite-item {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  gap: 12px;
}

.favorite-item:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.item-content {
  display: flex;
  gap: 12px;
  flex: 1;
  cursor: pointer;
  min-width: 0;
}

.item-image {
  width: 100px;
  height: 70px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 8px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-footer {
  display: flex;
  gap: 12px;
  align-items: center;
}

.item-source {
  font-size: 11px;
  color: #409eff;
}

.item-time {
  font-size: 11px;
  color: #909399;
}

.item-actions {
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.empty-hint {
  font-size: 13px;
  color: #c0c4cc;
  margin-top: 8px;
}
</style>
