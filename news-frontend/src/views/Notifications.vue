<template>
  <div class="notifications-container">
    <el-card class="notifications-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>消息中心</span>
          <div class="header-actions">
            <el-button 
              v-if="notifications.length > 0" 
              type="primary" 
              size="small" 
              @click="markAllAsRead"
              :loading="markingAll"
            >
              全部已读
            </el-button>
            <el-button type="primary" size="small" @click="goBack">返回个人中心</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="mb-4">
        <el-tab-pane label="所有消息" name="all"></el-tab-pane>
        <el-tab-pane label="点赞通知" name="like"></el-tab-pane>
        <el-tab-pane label="评论管理" name="comments"></el-tab-pane>
      </el-tabs>

      <!-- 通知列表 -->
      <div v-if="activeTab !== 'comments'" class="notification-list">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
        </div>

        <div v-else-if="filteredNotifications.length > 0" class="notification-items">
          <div
            v-for="notification in filteredNotifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.read }"
            @click="markAsRead(notification)"
          >
            <div class="notification-icon">
              <svg v-if="notification.type === 'like'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
            <div class="notification-content">
              <p class="notification-text">{{ notification.content }}</p>
              <p class="notification-related">来自: {{ notification.relatedUsername || '未知用户' }}</p>
              <p class="notification-news-title">新闻: {{ notification.relatedNewsTitle || '未知新闻' }}</p>
              <p class="notification-time">{{ formatTime(notification.createTime) }}</p>
            </div>
            <div class="notification-actions">
              <button 
                v-if="!notification.read" 
                class="mark-read-btn" 
                @click.stop="markAsRead(notification)"
                title="标记已读"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </button>
              <button 
                class="delete-btn" 
                @click.stop="deleteNotification(notification)"
                title="删除"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <el-icon class="empty-icon"><Bell /></el-icon>
          <p>暂无消息</p>
        </div>
      </div>

      <!-- 评论管理 -->
      <div v-if="activeTab === 'comments'" class="comment-management">
        <div v-if="loadingComments" class="loading-container">
          <div class="loading-spinner"></div>
        </div>

        <div v-else-if="myComments.length > 0" class="my-comments">
          <div
            v-for="comment in myComments"
            :key="comment.id"
            class="my-comment-item"
          >
            <div class="comment-header">
              <span class="comment-news-title">{{ comment.newsTitle || comment.newsId }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            </div>
            <div class="comment-body">
              <p class="comment-text">{{ comment.content }}</p>
              <div v-if="comment.files && comment.files.length > 0" class="comment-files">
                <div v-for="file in comment.files" :key="file.id" class="comment-file-item">
                  <!-- 图片 -->
                  <img 
                    v-if="isImageFile(file.fileUrl)" 
                    :src="getCommentFileUrl(file.fileUrl)" 
                    :alt="file.fileName"
                    class="comment-file-image"
                    @click="openFile(file.fileUrl)"
                    @error="handleFileError"
                  />
                  <!-- 视频 -->
                  <video 
                    v-else-if="isVideoFile(file.fileUrl)" 
                    :src="getCommentFileUrl(file.fileUrl)" 
                    controls
                    class="comment-file-video"
                  ></video>
                  <!-- 其他文件 -->
                  <a 
                    v-else 
                    :href="getCommentFileUrl(file.fileUrl)" 
                    target="_blank"
                    class="comment-file-link"
                    :title="file.fileName"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                    </svg>
                    <span class="file-link-name">{{ file.fileName }}</span>
                  </a>
                </div>
              </div>
            </div>
            <div class="comment-footer">
              <span class="comment-likes">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
                </svg>
                {{ comment.likeCount || 0 }}
              </span>
              <div class="comment-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="deleteMyComment(comment)"
                  :loading="deletingCommentId === comment.id"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <el-icon class="empty-icon"><Bell /></el-icon>
          <p>暂无评论</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'

const router = useRouter()
const goBack = () => {
  router.push({ path: '/profile' })
}

const activeTab = ref('all')
const notifications = ref([])
const myComments = ref([])
const loading = ref(true)
const loadingComments = ref(true)
const markingAll = ref(false)
const deletingCommentId = ref(null)

const isImageFile = (fileUrl) => {
  if (!fileUrl) return false
  const lower = fileUrl.toLowerCase()
  return lower.endsWith('.jpg') || lower.endsWith('.jpeg') || 
         lower.endsWith('.png') || lower.endsWith('.gif') || 
         lower.endsWith('.webp') || lower.endsWith('.bmp')
}

const isVideoFile = (fileUrl) => {
  if (!fileUrl) return false
  const lower = fileUrl.toLowerCase()
  return lower.endsWith('.mp4') || lower.endsWith('.avi') || 
         lower.endsWith('.mov') || lower.endsWith('.wmv') || 
         lower.endsWith('.flv') || lower.endsWith('.mkv')
}

const getCommentFileUrl = (fileUrl) => {
  if (!fileUrl) return ''
  // 如果已经是完整URL，直接返回
  if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
    return fileUrl
  }
  // 否则拼接baseURL
  return `http://localhost:8080${fileUrl}`
}

const openFile = (fileUrl) => {
  const url = getCommentFileUrl(fileUrl)
  window.open(url, '_blank')
}

const handleFileError = (e) => {
  console.error('文件加载失败:', e.target.src)
  e.target.style.display = 'none'
}

const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') return notifications.value
  return notifications.value.filter(n => n.type === activeTab.value)
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN')
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const response = await axios.get('/notifications')
    if (response && response.code === 200) {
      notifications.value = response.data || []
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchMyComments = async () => {
  loadingComments.value = true
  try {
    const response = await axios.get('/comments/user')
    if (response && response.code === 200) {
      myComments.value = response.data || []
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    loadingComments.value = false
  }
}

const markAsRead = async (notification) => {
  if (notification.read) return
  try {
    await axios.put(`/notifications/${notification.id}/read`)
    notification.read = true
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const markAllAsRead = async () => {
  markingAll.value = true
  try {
    await axios.put('/notifications/read-all')
    notifications.value.forEach(n => { n.read = true })
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('全部已读失败:', error)
  } finally {
    markingAll.value = false
  }
}

const deleteNotification = async (notification) => {
  try {
    await axios.delete(`/notifications/${notification.id}`)
    notifications.value = notifications.value.filter(n => n.id !== notification.id)
    ElMessage.success('已删除')
  } catch (error) {
    console.error('删除通知失败:', error)
  }
}

const deleteMyComment = async (comment) => {
  deletingCommentId.value = comment.id
  try {
    await axios.delete(`/comments/${comment.id}`)
    myComments.value = myComments.value.filter(c => c.id !== comment.id)
    ElMessage.success('评论已删除')
  } catch (error) {
    console.error('删除评论失败:', error)
  } finally {
    deletingCommentId.value = null
  }
}

onMounted(() => {
  fetchNotifications()
  fetchMyComments()
})
</script>

<style scoped>
.notifications-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.notifications-card {
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

.mb-4 {
  margin-bottom: 16px;
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

.notification-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  background: white;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notification-item:hover {
  background: #fafafa;
  border-color: #e4e7ed;
}

.notification-item.unread {
  background: #f0f7ff;
  border-color: #b3d8ff;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-item.unread .notification-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.notification-item:not(.unread) .notification-icon {
  background: #f5f5f5;
  color: #999;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-text {
  font-size: 14px;
  color: #303133;
  margin: 0 0 4px 0;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.notification-related {
  font-size: 12px;
  color: #409eff;
  margin: 2px 0 0 0;
}

.notification-news-title {
  font-size: 12px;
  color: #67c23a;
  margin: 2px 0 0 0;
}

.notification-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.mark-read-btn,
.delete-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #909399;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.mark-read-btn:hover {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.05);
}

.delete-btn:hover {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.05);
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

/* 评论管理 */
.comment-management {
  margin-top: 16px;
}

.my-comments {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.my-comment-item {
  padding: 16px;
  border-radius: 10px;
  background: white;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

.my-comment-item:hover {
  border-color: #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-news-title {
  font-size: 12px;
  color: #409eff;
  background: rgba(64, 158, 255, 0.05);
  padding: 2px 8px;
  border-radius: 4px;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-body {
  margin-bottom: 12px;
}

.comment-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin: 0;
}

.comment-files {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.comment-file-item {
  max-width: 100%;
}

.comment-file-image {
  max-width: 200px;
  max-height: 150px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
  border: 1px solid #e4e7ed;
  transition: transform 0.2s;
}

.comment-file-image:hover {
  transform: scale(1.05);
}

.comment-file-video {
  max-width: 200px;
  max-height: 150px;
  border-radius: 8px;
}

.comment-file-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 12px;
  color: #409eff;
  text-decoration: none;
  transition: all 0.2s;
}

.comment-file-link:hover {
  background: #e8f0fe;
}

.file-link-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-likes {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.comment-actions {
  display: flex;
  gap: 8px;
}
</style>
