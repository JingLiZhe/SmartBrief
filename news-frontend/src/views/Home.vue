<template>
  <div class="home-container">
    <!-- 轮播 Hero 区 -->
    <section class="hero-carousel-section" v-if="carouselNews.length">
      <div class="carousel-container">
        <div
                class="carousel-slide"
                v-for="(news, index) in carouselNews"
                :key="news.id || index"
                :class="{ active: currentSlide === index }"
        >
          <img
                  :src="getImageUrl(news.picUrl)"
                  :alt="news.title"
                  class="carousel-image"
          />
          <div class="carousel-overlay"></div>
          <div class="carousel-content" @click="openNews(news)">
            <span class="carousel-source">{{ news.source }}</span>
            <h2 class="carousel-title">{{ news.title }}</h2>
            <p class="carousel-desc">{{ news.description }}</p>
            <span class="carousel-time">{{ formatTime(news.ctime) }}</span>
          </div>
        </div>

        <!-- 指示器 -->
        <div class="carousel-indicators">
          <span
                  v-for="(_, index) in carouselNews"
                  :key="index"
                  :class="['indicator-dot', { active: currentSlide === index }]"
                  @click="goToSlide(index)"
          ></span>
        </div>

        <!-- 切换按钮 -->
        <button class="carousel-btn prev" @click="prevSlide">‹</button>
        <button class="carousel-btn next" @click="nextSlide">›</button>
      </div>
    </section>

    <!-- 分类 + 搜索 -->
    <section class="categories-section">
      <div class="container">
        <div class="category-tabs">
          <button
                  v-for="cat in categories"
                  :key="cat.code"
                  :class="['category-tab', { active: activeCategory === cat.code }]"
                  @click="selectCategory(cat.code)"
          >
            <component :is="cat.icon" />
            <span>{{ cat.name }}</span>
          </button>

          <div class="search-box">
            <input
                    v-model="searchKeyword"
                    type="text"
                    placeholder="搜索新闻标题..."
                    class="search-input"
                    @keyup.enter="debouncedSearch"
            />
            <button class="search-btn" @click="debouncedSearch">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
            </button>
            <button v-if="isSearchMode" class="close-search-btn" @click="clearSearch">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 新闻列表（原有内容不变） -->
    <section class="featured-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">头条新闻</h2>
          <span class="section-subtitle">精选热点资讯</span>
        </div>

        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
        </div>

        <div v-else-if="currentNewsList.length > 0" class="news-grid">
          <div class="featured-card" v-if="currentNewsList[0]" @click="openNews(currentNewsList[0])">
            <div class="featured-image-wrapper">
              <img :src="getImageUrl(currentNewsList[0].picUrl)" :alt="currentNewsList[0].title" class="featured-image" />
              <div class="featured-overlay"></div>
            </div>
            <div class="featured-content">
              <span class="news-source">{{ currentNewsList[0].source }}</span>
              <h3 class="featured-title" v-html="highlightKeyword(currentNewsList[0].title, searchKeyword)"></h3>
              <p class="featured-desc">{{ currentNewsList[0].description || '点击查看详情' }}</p>
              <div class="featured-footer">
                <span class="news-time">{{ formatTime(currentNewsList[0].ctime) }}</span>
                <div class="footer-actions">
                  <button 
                    class="comment-btn" 
                    @click.stop="openCommentDialog(currentNewsList[0])"
                    title="评论"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                    </svg>
                  </button>
                  <button 
                    class="favorite-btn" 
                    :class="{ favorited: isFavorited(currentNewsList[0].newsId) }"
                    @click.stop="toggleFavorite(currentNewsList[0])"
                    title="收藏"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="secondary-grid">
            <div
                    v-for="news in currentNewsList.slice(1, 5)"
                    :key="news.newsId"
                    class="secondary-card"
                    @click="openNews(news)"
            >
              <div class="secondary-image-wrapper">
                <img :src="getImageUrl(news.picUrl)" :alt="news.title" class="secondary-image" />
              </div>
              <div class="secondary-content">
                <h4 class="secondary-title" v-html="highlightKeyword(news.title, searchKeyword)"></h4>
                <div class="secondary-footer">
                  <span class="news-source-small">{{ news.source }}</span>
                  <span class="news-time-small">{{ formatTime(news.ctime) }}</span>
                  <div class="footer-actions">
                    <button 
                      class="comment-btn small" 
                      @click.stop="openCommentDialog(news)"
                      title="评论"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                      </svg>
                    </button>
                    <button 
                      class="favorite-btn small" 
                      :class="{ favorited: isFavorited(news.newsId) }"
                      @click.stop="toggleFavorite(news)"
                      title="收藏"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentNewsList.length > 5" class="news-list-section">
          <div class="section-header">
            <h2 class="section-title">更多新闻</h2>
          </div>
          <div class="news-list">
            <div
                    v-for="news in currentNewsList.slice(5)"
                    :key="news.newsId"
                    class="news-list-item"
                    @click="openNews(news)"
            >
              <img :src="getImageUrl(news.picUrl)" :alt="news.title" class="list-image" />
              <div class="list-content">
                <h4 class="list-title" v-html="highlightKeyword(news.title, searchKeyword)"></h4>
                <p class="list-desc">{{ news.description || '暂无描述' }}</p>
                <div class="list-footer">
                  <span class="news-source-small">{{ news.source }}</span>
                  <span class="news-time-small">{{ formatTime(news.ctime) }}</span>
                  <div class="footer-actions">
                    <button 
                      class="comment-btn small" 
                      @click.stop="openCommentDialog(news)"
                      title="评论"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                      </svg>
                    </button>
                    <button 
                      class="favorite-btn small" 
                      :class="{ favorited: isFavorited(news.newsId) }"
                      @click.stop="toggleFavorite(news)"
                      title="收藏"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="!loading && currentNewsList.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
          </div>
          <p class="empty-text">暂无新闻数据</p>
        </div>
      </div>
    </section>

    <!-- 评论弹窗 -->
    <div v-if="commentDialogVisible" class="comment-overlay" @click.self="closeCommentDialog">
      <div class="comment-dialog">
        <div class="comment-dialog-header">
          <h3>评论 - {{ commentNews?.title?.substring(0, 30) }}...</h3>
          <button class="close-btn" @click="closeCommentDialog">&times;</button>
        </div>
        <div class="comment-dialog-body">
          <div class="comment-list" v-if="comments.length > 0">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-user">
                <img :src="comment.avatar || ''" class="comment-avatar" @error="e => e.target.style.display='none'" />
                <span class="comment-username">{{ comment.username }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <!-- 显示评论文件 -->
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
              <div class="comment-actions">
                <button 
                  class="like-btn" 
                  :class="{ liked: comment.liked }"
                  @click.stop="toggleCommentLike(comment)"
                  title="点赞"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
                  </svg>
                  <span v-if="comment.likeCount > 0">{{ comment.likeCount }}</span>
                </button>
              </div>
            </div>
          </div>
          <div v-else class="no-comments">暂无评论</div>
          <div class="comment-input-area">
            <div class="comment-toolbar">
              <label class="file-upload-btn" title="上传图片">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path>
                </svg>
                <input 
                  type="file" 
                  multiple 
                  accept="image/*"
                  @change="handleFileSelect"
                  class="file-input-hidden"
                />
              </label>
              <span v-if="selectedFiles.length > 0" class="file-count">
                {{ selectedFiles.length }} 张图片
              </span>
              <span class="file-hint">仅支持上传图片</span>
            </div>
            <textarea 
              v-model="commentText" 
              placeholder="输入评论内容..." 
              class="comment-textarea"
              rows="3"
            ></textarea>
            <div v-if="selectedFiles.length > 0" class="file-preview-list">
              <div v-for="(file, index) in selectedFiles" :key="index" class="file-preview-item">
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">{{ formatFileSize(file.size) }}</span>
                <button class="remove-file-btn" @click="removeFile(index)">&times;</button>
              </div>
            </div>
            <button 
              class="submit-comment-btn" 
              @click="submitComment" 
              :disabled="(!commentText.trim() && selectedFiles.length === 0) || submittingComment"
            >
              {{ submittingComment ? '提交中...' : '发表评论' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <footer class="page-footer">
      <div class="container">
        <p class="footer-text">© 2026 智讯 - 实时新闻智能平台</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
    import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
    import { useRouter } from 'vue-router';
    import axios from '../utils/axios';

    const router = useRouter();

    // —— 原有数据 ——
    const newsList = ref([]);
    const newsCount = ref(0);
    const loading = ref(true);
    const activeCategory = ref('all');

    const categories = ref([
        { code: 'all', name: '全部', icon: null },
        { code: 'domestic', name: '国内', icon: null },
        { code: 'international', name: '国际', icon: null },
        { code: 'tech', name: '科技', icon: null },
        { code: 'social', name: '社会', icon: null },
        { code: 'entertainment', name: '娱乐', icon: null },
        { code: 'sports', name: '体育', icon: null },
        { code: 'nba', name: 'NBA', icon: null }
    ]);

    const searchKeyword = ref('');
    const isSearchMode = ref(false);
    const searchResults = ref([]);

    const currentNewsList = computed(() => {
        return isSearchMode.value ? searchResults.value : newsList.value;
    });

    // —— 原有方法 ——
    const fetchNews = async () => {
        loading.value = true;
        try {
            if (activeCategory.value === 'nba') {
                // 获取轮播图数据
                const response = await axios.get('/home-news/nba', { params: { num: 20 } });
                newsList.value = response.data || [];
            } else {
                const params = { limit: 60 };
                if (activeCategory.value && activeCategory.value !== 'all') {
                    params.category = activeCategory.value;
                }
                const response = await axios.get('/home-news', { params });
                newsList.value = response.data || [];
            }
            if (!isSearchMode.value) {
                newsCount.value = newsList.value.length;
            }
        } catch (error) {
            console.error('Fetch news failed:', error);
            newsList.value = [];
        } finally {
            loading.value = false;
        }
    };

    const selectCategory = (category) => {
        if (isSearchMode.value) {
            clearSearch();
        }
        activeCategory.value = category;
        fetchNews();
    };

    let searchTimer = null;
    const debouncedSearch = () => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(() => {
            performSearch();
        }, 300);
    };

    const performSearch = async () => {
        const keyword = searchKeyword.value.trim();
        if (!keyword) {
            clearSearch();
            return;
        }
        isSearchMode.value = true;
        loading.value = true;
        try {
            const res = await axios.get('/home-news/search', { params: { keyword } });
            searchResults.value = res.data || [];
            newsCount.value = searchResults.value.length;
        } catch (error) {
            console.error('搜索失败', error);
            searchResults.value = [];
            newsCount.value = 0;
        } finally {
            loading.value = false;
        }
    };

    const clearSearch = () => {
        searchKeyword.value = '';
        isSearchMode.value = false;
        searchResults.value = [];
        fetchNews();
    };

    const highlightKeyword = (text, keyword) => {
        if (!keyword || !text) return text;
        const escaped = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const reg = new RegExp(`(${escaped})`, 'gi');
        return text.replace(reg, '<span style="color: #409EFF; font-weight: bold;">$1</span>');
    };

    const formatTime = (timeStr) => {
        if (!timeStr) return '';
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now.getTime() - date.getTime();
        const hours = Math.floor(diff / (1000 * 60 * 60));
        if (hours < 1) {
            const minutes = Math.floor(diff / (1000 * 60));
            return `${minutes}分钟前`;
        } else if (hours < 24) {
            return `${hours}小时前`;
        } else {
            return timeStr.split(' ')[0];
        }
    };

    const openNews = (news) => {
        if (news.url) {
            window.open(news.url, '_blank');
        }
    };

    const getImageUrl = (picUrl) => {
        if (!picUrl) return '';
        if (picUrl.includes('img.ithome.com')) {
            return `/api/home-news/proxy-image?url=${encodeURIComponent(picUrl)}`;
        }
        return picUrl;
    };

    const navigate = (path) => {
        router.push(path);
    };

    // —— 评论相关 ——
    const commentDialogVisible = ref(false);
    const commentNews = ref(null);
    const commentText = ref('');
    const comments = ref([]);
    const submittingComment = ref(false);
    const selectedFiles = ref([]);

    const handleFileSelect = (event) => {
      const files = event.target.files;
      if (files) {
        for (let i = 0; i < files.length; i++) {
          const file = files[i];
          // 只允许图片类型
          if (!file.type.startsWith('image/')) {
            alert('仅支持上传图片文件');
            continue;
          }
          selectedFiles.value.push(file);
        }
      }
      // 清空 input 以便重复选择相同文件
      event.target.value = '';
    };

    const removeFile = (index) => {
      selectedFiles.value.splice(index, 1);
    };

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    const openCommentDialog = async (news) => {
      commentNews.value = news;
      commentText.value = '';
      comments.value = [];
      commentDialogVisible.value = true;
      await fetchComments(news.newsId);
    };

    const closeCommentDialog = () => {
      commentDialogVisible.value = false;
      commentNews.value = null;
    };

    const fetchComments = async (newsId) => {
      try {
        const response = await axios.get(`/comments/${newsId}`);
        if (response && response.code === 200) {
          comments.value = response.data || [];
        }
      } catch (error) {
        console.error('获取评论失败:', error);
      }
    };

    const isImageFile = (fileUrl) => {
      if (!fileUrl) return false;
      const lower = fileUrl.toLowerCase();
      return lower.endsWith('.jpg') || lower.endsWith('.jpeg') || 
             lower.endsWith('.png') || lower.endsWith('.gif') || 
             lower.endsWith('.webp') || lower.endsWith('.bmp');
    };

    const isVideoFile = (fileUrl) => {
      if (!fileUrl) return false;
      const lower = fileUrl.toLowerCase();
      return lower.endsWith('.mp4') || lower.endsWith('.avi') || 
             lower.endsWith('.mov') || lower.endsWith('.wmv') || 
             lower.endsWith('.flv') || lower.endsWith('.mkv');
    };

    const getCommentFileUrl = (fileUrl) => {
      if (!fileUrl) return '';
      // 如果已经是完整URL，直接返回
      if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
        return fileUrl;
      }
      // 否则拼接baseURL
      return `http://localhost:8080${fileUrl}`;
    };

    const openFile = (fileUrl) => {
      const url = getCommentFileUrl(fileUrl);
      window.open(url, '_blank');
    };

    const handleFileError = (e) => {
      console.error('文件加载失败:', e.target.src);
      e.target.style.display = 'none';
    };

    const toggleCommentLike = async (comment) => {
      try {
        const response = await axios.post(`/comments/${comment.id}/like`);
        if (response && response.code === 200) {
          comment.liked = response.data;
          if (comment.liked) {
            comment.likeCount = (comment.likeCount || 0) + 1;
          } else {
            comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1);
          }
        }
      } catch (error) {
        console.error('点赞失败:', error);
      }
    };

    const submitComment = async () => {
      if (!commentText.value.trim() && selectedFiles.value.length === 0) return;
      submittingComment.value = true;
      try {
        const formData = new FormData();
        formData.append('newsId', commentNews.value.newsId);
        formData.append('content', commentText.value);
        // 添加文件
        for (const file of selectedFiles.value) {
          formData.append('files', file);
        }
        const response = await axios.post('/comments', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        if (response && response.code === 200) {
          commentText.value = '';
          selectedFiles.value = [];
          await fetchComments(commentNews.value.newsId);
        }
      } catch (error) {
        console.error('提交评论失败:', error);
      } finally {
        submittingComment.value = false;
      }
    };

    // —— 收藏相关 ——
    const favoriteNewsIds = ref(new Set());

    const fetchFavorites = async () => {
        try {
            const response = await axios.get('/favorites');
            if (response && response.code === 200 && Array.isArray(response.data)) {
                favoriteNewsIds.value = new Set(response.data.map(f => f.newsId));
            }
        } catch (error) {
            console.error('获取收藏列表失败:', error);
        }
    };

    const isFavorited = (newsId) => {
        return favoriteNewsIds.value.has(newsId);
    };

    const toggleFavorite = async (news) => {
        if (isFavorited(news.newsId)) {
            try {
                await axios.delete(`/favorites/${news.newsId}`);
                favoriteNewsIds.value.delete(news.newsId);
                favoriteNewsIds.value = new Set(favoriteNewsIds.value);
            } catch (error) {
                console.error('取消收藏失败:', error);
            }
        } else {
            try {
                const favoriteData = {
                    newsId: news.newsId,
                    title: news.title,
                    description: news.description,
                    source: news.source,
                    picUrl: news.picUrl,
                    url: news.url,
                    ctime: news.ctime
                };
                await axios.post('/favorites', favoriteData);
                favoriteNewsIds.value.add(news.newsId);
                favoriteNewsIds.value = new Set(favoriteNewsIds.value);
            } catch (error) {
                console.error('收藏失败:', error);
            }
        }
    };

    // —— 新增轮播相关 ——
    const carouselNews = ref([]);
    const currentSlide = ref(0);
    let autoPlayTimer = null;

    const fetchCarouselNews = async () => {
        try {
            const response = await axios.get('/home-news/nba', { params: { num: 5 } });
            console.log('完整响应:', response);
            const list = response.data || [];
            let newsList = [];
            if (Array.isArray(response.data)) {
                newsList = response.data;
            } else if (response.data && Array.isArray(response.data.data)) {
                newsList = response.data.data;
            } else {
                newsList = [];
            }
            carouselNews.value = newsList;
            currentSlide.value = 0;
            console.log('轮播数据条数:', newsList.length);
            console.log('第一条数据:', newsList[0]);
        } catch (error) {
            console.error('获取轮播新闻失败:', error);
            carouselNews.value = [];
        }
    };

    const nextSlide = () => {
        if (carouselNews.value.length === 0) return;
        currentSlide.value = (currentSlide.value + 1) % carouselNews.value.length;
    };

    const prevSlide = () => {
        if (carouselNews.value.length === 0) return;
        currentSlide.value = (currentSlide.value - 1 + carouselNews.value.length) % carouselNews.value.length;
    };

    const goToSlide = (index) => {
        currentSlide.value = index;
    };

    const startAutoPlay = () => {
        if (autoPlayTimer) clearInterval(autoPlayTimer);
        autoPlayTimer = setInterval(nextSlide, 5000);
    };

    const stopAutoPlay = () => {
        if (autoPlayTimer) {
            clearInterval(autoPlayTimer);
            autoPlayTimer = null;
        }
    };

    // —— 生命周期 ——
    onMounted(() => {
        fetchNews();
        fetchCarouselNews();
        fetchFavorites();
        startAutoPlay();
    });

    onBeforeUnmount(() => {
        stopAutoPlay();
    });
</script>

<style scoped>
  .home-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  }

  /* ===== 轮播 Hero 区 ===== */
  .hero-carousel-section {
    position: relative;
    width: 100%;
    height: 450px;
    overflow: hidden;
    background: #000;
  }

  .carousel-container {
    position: relative;
    width: 100%;
    height: 100%;
  }

  .carousel-slide {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
    transition: opacity 1s ease-in-out;
    pointer-events: none;
  }
  .carousel-slide.active {
    opacity: 1;
    pointer-events: auto;
  }

  .carousel-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .carousel-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60%;
    background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, transparent 100%);
  }

  .carousel-content {
    position: absolute;
    bottom: 40px;
    left: 60px;
    right: 60px;
    color: white;
    cursor: pointer;
    z-index: 2;
  }

  .carousel-source {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(64, 158, 255, 0.2);
    border-radius: 12px;
    font-size: 14px;
  }

  .carousel-title {
    font-size: 32px;
    font-weight: 700;
    margin: 10px 0;
    line-height: 1.3;
  }

  .carousel-desc {
    font-size: 16px;
    opacity: 0.9;
    margin: 8px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .carousel-time {
    font-size: 13px;
    opacity: 0.7;
  }

  /* 指示器 */
  .carousel-indicators {
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 8px;
    z-index: 3;
  }

  .indicator-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: rgba(255,255,255,0.4);
    cursor: pointer;
    transition: background 0.3s;
  }
  .indicator-dot.active {
    background: white;
  }

  /* 切换按钮 */
  .carousel-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(0,0,0,0.3);
    color: white;
    border: none;
    font-size: 28px;
    padding: 10px 16px;
    cursor: pointer;
    z-index: 3;
    border-radius: 50%;
    transition: background 0.3s;
    line-height: 1;
  }
  .carousel-btn:hover {
    background: rgba(0,0,0,0.6);
  }
  .carousel-btn.prev {
    left: 20px;
  }
  .carousel-btn.next {
    right: 20px;
  }

  /* ===== 以下是你原有的样式（保持不变） ===== */
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .categories-section {
    background: white;
    padding: 16px 0;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    position: sticky;
    top: 60px;
    z-index: 100;
  }

  .category-tabs {
    display: flex;
    gap: 8px;
    align-items: center;
    overflow-x: auto;
    padding: 4px 0;
    flex-wrap: nowrap;
  }

  .category-tab {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    border-radius: 25px;
    background: #f5f7fa;
    border: none;
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    cursor: pointer;
    transition: all 0.3s ease;
    white-space: nowrap;
    flex-shrink: 0;
  }
  .category-tab:hover {
    background: #e8f0fe;
    color: #409eff;
  }
  .category-tab.active {
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    color: white;
  }

  .search-box {
    display: flex;
    align-items: center;
    background: #f5f7fa;
    border-radius: 25px;
    padding: 0 12px 0 16px;
    margin-left: auto;
    transition: background 0.3s, box-shadow 0.3s;
    height: 42px;
    flex-shrink: 0;
  }
  .search-box:hover {
    background: #e8f0fe;
  }
  .search-box:focus-within {
    background: #fff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }

  .search-input {
    background: transparent;
    border: none;
    outline: none;
    font-size: 14px;
    color: #303133;
    width: 140px;
    transition: width 0.3s;
    padding: 6px 0;
  }
  .search-input::placeholder {
    color: #a8abb2;
    font-weight: 400;
  }
  .search-input:focus {
    width: 200px;
  }

  .search-btn,
  .close-search-btn {
    background: transparent;
    border: none;
    color: #606266;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 4px;
    transition: color 0.2s;
    flex-shrink: 0;
  }
  .search-btn:hover {
    color: #409eff;
  }
  .close-search-btn {
    color: #909399;
    margin-left: 4px;
  }
  .close-search-btn:hover {
    color: #f56c6c;
  }

  .featured-section {
    padding: 40px 0;
  }

  .section-header {
    margin-bottom: 24px;
  }
  .section-title {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    margin: 0;
  }
  .section-subtitle {
    font-size: 14px;
    color: #909399;
    margin-left: 12px;
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

  .news-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
  @media (max-width: 900px) {
    .news-grid {
      grid-template-columns: 1fr;
    }
  }

  .featured-card {
    grid-column: span 1;
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    cursor: pointer;
    transition: all 0.3s ease;
  }
  .featured-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }

  .featured-image-wrapper {
    position: relative;
    height: 280px;
    overflow: hidden;
  }
  .featured-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
  .featured-card:hover .featured-image {
    transform: scale(1.05);
  }
  .featured-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60%;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
  }
  .featured-content {
    padding: 20px;
    position: relative;
    margin-top: -60px;
  }

  .news-source {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
    font-size: 12px;
    font-weight: 500;
    border-radius: 12px;
    margin-bottom: 12px;
  }
  .featured-title {
    font-size: 20px;
    font-weight: 700;
    color: white;
    margin: 0 0 12px 0;
    line-height: 1.4;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  }
  .featured-desc {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.85);
    margin: 0 0 12px 0;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .news-time {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }

  .featured-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
  }

  .favorite-btn {
    background: transparent;
    border: none;
    cursor: pointer;
    color: rgba(255, 255, 255, 0.6);
    transition: color 0.3s, transform 0.2s;
    padding: 4px;
    display: flex;
    align-items: center;
  }

  .favorite-btn:hover {
    color: #ffd700;
    transform: scale(1.2);
  }

  .favorite-btn.favorited {
    color: #ffd700;
  }

  .favorite-btn.small {
    color: #909399;
  }

  .favorite-btn.small:hover {
    color: #ffd700;
  }

  .favorite-btn.small.favorited {
    color: #ffd700;
  }

  .secondary-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
  @media (max-width: 900px) {
    .secondary-grid {
      grid-template-columns: 1fr 1fr;
    }
  }
  @media (max-width: 600px) {
    .secondary-grid {
      grid-template-columns: 1fr;
    }
  }

  .secondary-card {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    cursor: pointer;
    transition: all 0.3s ease;
  }
  .secondary-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  }
  .secondary-image-wrapper {
    height: 120px;
    overflow: hidden;
  }
  .secondary-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
  .secondary-card:hover .secondary-image {
    transform: scale(1.05);
  }
  .secondary-content {
    padding: 12px;
  }
  .secondary-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .secondary-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .news-source-small {
    font-size: 11px;
    color: #409eff;
  }
  .news-time-small {
    font-size: 11px;
    color: #909399;
  }

  .news-list-section {
    margin-top: 40px;
  }
  .news-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .news-list-item {
    display: flex;
    gap: 16px;
    background: white;
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    cursor: pointer;
    transition: all 0.3s ease;
  }
  .news-list-item:hover {
    background: #fafafa;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  .list-image {
    width: 120px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    flex-shrink: 0;
  }
  .list-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
  }
  .list-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
    line-height: 1.4;
  }
  .list-desc {
    font-size: 13px;
    color: #606266;
    margin: 0 0 8px 0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .list-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
    gap: 8px;
  }

  .empty-state {
    text-align: center;
    padding: 60px 0;
  }
  .empty-icon {
    color: #c0c4cc;
    margin-bottom: 16px;
  }
  .empty-text {
    color: #909399;
    font-size: 15px;
  }

  .page-footer {
    background: #303133;
    padding: 30px 0;
    margin-top: 40px;
  }
  .footer-text {
    text-align: center;
    color: #909399;
    font-size: 14px;
    margin: 0;
  }

  /* ===== 评论弹窗 ===== */
  .comment-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }

  .comment-dialog {
    background: white;
    border-radius: 16px;
    width: 500px;
    max-width: 90%;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  }

  .comment-dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  .comment-dialog-header h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 24px;
    color: #909399;
    cursor: pointer;
    padding: 0 4px;
    line-height: 1;
  }

  .close-btn:hover {
    color: #303133;
  }

  .comment-dialog-body {
    padding: 16px 20px;
    overflow-y: auto;
    flex: 1;
  }

  .comment-list {
    margin-bottom: 16px;
  }

  .comment-item {
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;
  }

  .comment-item:last-child {
    border-bottom: none;
  }

  .comment-user {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .comment-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    object-fit: cover;
    background: #e8f0fe;
  }

  .comment-username {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }

  .comment-time {
    font-size: 12px;
    color: #909399;
    margin-left: auto;
  }

  .comment-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
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

  .comment-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 8px;
  }

  .like-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    border: 1px solid #e4e7ed;
    background: white;
    color: #909399;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .like-btn:hover {
    border-color: #409eff;
    color: #409eff;
    background: rgba(64, 158, 255, 0.05);
  }

  .like-btn.liked {
    border-color: #f56c6c;
    color: #f56c6c;
    background: rgba(245, 108, 108, 0.05);
  }

  .like-btn.liked:hover {
    border-color: #f56c6c;
    color: #f56c6c;
    background: rgba(245, 108, 108, 0.1);
  }

  .no-comments {
    text-align: center;
    color: #909399;
    padding: 40px 0;
    font-size: 14px;
  }

  .comment-input-area {
    border-top: 1px solid #f0f0f0;
    padding-top: 16px;
  }

  .comment-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .file-upload-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: #f5f7fa;
    border: 1px solid #e4e7ed;
    cursor: pointer;
    transition: all 0.3s;
    color: #606266;
  }

  .file-upload-btn:hover {
    background: #e8f0fe;
    border-color: #409eff;
    color: #409eff;
  }

  .file-input-hidden {
    display: none;
  }

  .file-count {
    font-size: 12px;
    color: #409eff;
  }

  .file-hint {
    font-size: 12px;
    color: #909399;
    margin-left: 8px;
  }

  .file-preview-list {
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .file-preview-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 10px;
    background: #f5f7fa;
    border-radius: 6px;
    font-size: 12px;
  }

  .file-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #303133;
  }

  .file-size {
    color: #909399;
    flex-shrink: 0;
  }

  .remove-file-btn {
    background: transparent;
    border: none;
    color: #909399;
    cursor: pointer;
    font-size: 16px;
    line-height: 1;
    padding: 0 2px;
    transition: color 0.2s;
  }

  .remove-file-btn:hover {
    color: #f56c6c;
  }

  .comment-textarea {
    width: 100%;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 10px 12px;
    font-size: 14px;
    resize: none;
    outline: none;
    transition: border-color 0.3s;
    box-sizing: border-box;
  }

  .comment-textarea:focus {
    border-color: #409eff;
  }

  .submit-comment-btn {
    margin-top: 12px;
    padding: 8px 20px;
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    float: right;
  }

  .submit-comment-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }

  .submit-comment-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .footer-actions {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-shrink: 0;
  }

  .comment-btn {
    background: transparent;
    border: none;
    cursor: pointer;
    color: rgba(255, 255, 255, 0.6);
    transition: color 0.3s, transform 0.2s;
    padding: 4px;
    display: flex;
    align-items: center;
  }

  .comment-btn:hover {
    color: #409eff;
    transform: scale(1.2);
  }

  .comment-btn.small {
    color: #909399;
  }

  .comment-btn.small:hover {
    color: #409eff;
  }

  /* ===== 响应式 ===== */
  @media (max-width: 768px) {
    /* 轮播响应式 */
    .hero-carousel-section {
      height: 300px;
    }
    .carousel-title {
      font-size: 24px;
    }
    .carousel-content {
      left: 20px;
      right: 20px;
      bottom: 30px;
    }
    .carousel-desc {
      font-size: 14px;
      -webkit-line-clamp: 2;
    }
    .carousel-btn {
      font-size: 20px;
      padding: 6px 12px;
    }
    .carousel-btn.prev {
      left: 10px;
    }
    .carousel-btn.next {
      right: 10px;
    }

    /* 原有响应式 */
    .featured-image-wrapper {
      height: 200px;
    }
    .featured-content {
      margin-top: -40px;
    }
    .featured-title {
      font-size: 18px;
    }
    .news-list-item {
      flex-direction: column;
    }
    .list-image {
      width: 100%;
      height: 150px;
    }
    .search-input {
      width: 100px;
    }
    .search-input:focus {
      width: 140px;
    }
    .search-box {
      padding: 0 8px 0 12px;
      height: 38px;
    }
  }
</style>
