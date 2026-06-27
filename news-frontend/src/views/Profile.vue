<template>
  <div class="profile-container">
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <span>个人中心</span>
      </template>
      <div class="profile-content">
        <div class="user-info">
          <el-avatar :size="100" class="mb-4" :src="avatarUrl">
            {{ username.charAt(0).toUpperCase() }}
          </el-avatar>
          <h3 class="username">{{ username }}</h3>
          <p class="email">{{ email || '未绑定邮箱' }}</p>
          <p class="like-count">点赞数：{{ likeCount }}</p>
          <p class="like-note">点赞数每周一 8:00 更新</p>
        </div>
        <el-divider />
        <div class="menu-list">
          <!-- 关键改动：default-active 绑定到 activeIndex，初始为空字符串，实现无默认高亮 -->
          <el-menu
                  :default-active="activeIndex"
                  mode="vertical"
                  @select="handleSelect"
          >
            <el-menu-item index="1" @click="goToEdit">
              <template #icon><el-icon><User /></el-icon></template>
              基本信息
            </el-menu-item>
            <el-menu-item index="2" @click="goToHistory">
              <template #icon><el-icon><Clock /></el-icon></template>
              历史记录
            </el-menu-item>
            <el-menu-item index="5" @click="goToFavorites">
              <template #icon><el-icon><Star /></el-icon></template>
              我的收藏
            </el-menu-item>
            <el-menu-item index="3" @click="goToNotifications">
              <template #icon><el-icon><Bell /></el-icon></template>
              消息通知
            </el-menu-item>
            <!-- 退出登录单独处理：不设置 index，避免高亮干扰 -->
            <el-menu-item @click="handleLogout">
              <template #icon><el-icon><ArrowRight /></el-icon></template>
              退出登录
            </el-menu-item>
          </el-menu>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
    import { ref, onMounted } from 'vue'
    import { useRouter } from 'vue-router'
    import { User, Clock, Bell, ArrowRight, Star } from '@element-plus/icons-vue'
    import axios from '../utils/axios'

    const router = useRouter()

    // ---------- 用户信息 ----------
    const username = ref('用户')
    const email = ref('')
    const avatarUrl = ref('')
    const likeCount = ref(0)

    // ---------- 菜单激活状态 ----------
    // 初始为空字符串 → 没有任何菜单项被高亮
    const activeIndex = ref('')

    // 菜单选中事件：点击时将该菜单的 index 赋给 activeIndex，实现高亮
    const handleSelect = (index) => {
        activeIndex.value = index
    }

    // ---------- 导航方法 ----------
    const goToEdit = () => {
        router.push({ path: '/profile/edit' })
    }

    const goToHistory = () => {
        router.push({ path: '/history' })
    }

    const goToFavorites = () => {
        router.push({ path: '/favorites' })
    }

    const goToNotifications = () => {
        router.push({ path: '/notifications' })
    }

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('avatar')
        window.location.href = '/login'
    }

    // ---------- 加载用户信息 ----------
    const loadUserInfo = async () => {
        try {
            const response = await axios.get('/user/info')
            if (response.code === 200) {
                const user = response.data
                username.value = user.username
                email.value = user.email || ''
                avatarUrl.value = user.avatar || ''
                likeCount.value = user.likeCount || 0

                localStorage.setItem('username', user.username)
                if (user.avatar) {
                    localStorage.setItem('avatar', user.avatar)
                }
            }
        } catch (error) {
            console.error('加载用户信息失败', error)
            const savedUsername = localStorage.getItem('username')
            if (savedUsername && savedUsername.trim()) {
                username.value = savedUsername
            }
            const savedAvatar = localStorage.getItem('avatar')
            if (savedAvatar) {
                avatarUrl.value = savedAvatar
            }
        }
    }

    onMounted(() => {
        loadUserInfo()
    })
</script>

<style scoped>
  .profile-container {
    padding: 20px;
    max-width: 600px;
    margin: 0 auto;
  }

  .profile-card {
    margin-top: 20px;
  }

  .profile-content {
    padding-top: 16px;
  }

  .user-info {
    text-align: center;
  }

  .mb-4 {
    margin-bottom: 16px;
  }

  .username {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 8px;
  }

  .email {
    color: #999;
  }

  .like-count {
    font-size: 16px;
    color: #409eff;
    margin-top: 8px;
  }

  .like-note {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }

  .el-divider {
    margin: 20px 0;
  }

  .menu-list {
    margin-top: 16px;
  }
</style>