<template>
  <div class="profile-edit-container">
    <el-card class="profile-edit-card" shadow="hover">
      <template #header>
        <span>修改个人信息</span>
      </template>
      <div class="profile-edit-content">
        <div class="avatar-section">
          <div class="avatar-upload">
            <el-avatar :size="120" class="avatar" :src="avatarUrl" @click="uploadAvatar">
              <User />
            </el-avatar>
            <p class="upload-tip">点击头像上传</p>
            <input
              type="file"
              ref="avatarInput"
              class="avatar-input"
              accept="image/*"
              @change="handleAvatarChange"
            />
          </div>
        </div>

        <el-form :model="form" label-width="120px" class="form">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入新密码（不修改请留空）" />
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input v-model="form.email" type="email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveChanges" :loading="saving">
              保存修改
            </el-button>
            <el-button @click="goBack">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'

const router = useRouter()
const form = reactive({
  username: '',
  password: '',
  email: ''
})
const avatarUrl = ref('')
const saving = ref(false)
const avatarInput = ref(null)

const uploadAvatar = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (file) {
    const formData = new FormData()
    formData.append('file', file)
    
    try {
      const response = await axios.post('/user/avatar', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      if (response.code === 200) {
        const avatarPath = response.data
        avatarUrl.value = avatarPath
        localStorage.setItem('avatar', avatarUrl.value)
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error(response.message || '头像上传失败')
      }
    } catch (error) {
      ElMessage.error('头像上传失败')
    }
  }
}

const saveChanges = async () => {
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  
  if (form.username.trim().length < 2 || form.username.trim().length > 20) {
    ElMessage.warning('用户名长度必须在2-20之间')
    return
  }
  
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }
  
  if (form.password && form.password.length < 6) {
    ElMessage.warning('密码长度至少为6位')
    return
  }
  
  saving.value = true
  try {
    const updateData = {
      username: form.username,
      email: form.email,
      avatar: avatarUrl.value
    }
    if (form.password.trim()) {
      updateData.password = form.password
    }
    
    const response = await axios.put('/user/info', updateData)
    
    if (response.code === 200) {
      ElMessage.success('信息修改成功')
      localStorage.setItem('username', form.username)
      localStorage.setItem('avatar', avatarUrl.value)
      router.push('/profile')
    } else {
      ElMessage.error(response.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/profile')
}

const loadUserInfo = async () => {
  try {
    const response = await axios.get('/user/info')
    if (response.code === 200) {
      const user = response.data
      form.username = user.username
      form.email = user.email || ''
      avatarUrl.value = user.avatar || ''
      
      localStorage.setItem('username', user.username)
      if (user.avatar) {
        localStorage.setItem('avatar', user.avatar)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-edit-container {
  padding: 20px;
  max-width: 500px;
  margin: 0 auto;
}

.profile-edit-card {
  margin-top: 20px;
}

.profile-edit-content {
  padding-top: 16px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-upload {
  position: relative;
  display: inline-block;
}

.avatar {
  cursor: pointer;
  border: 2px dashed #d9d9d9;
  transition: all 0.3s;
}

.avatar:hover {
  border-color: #409eff;
}

.upload-tip {
  margin-top: 12px;
  color: #999;
  font-size: 14px;
}

.avatar-input {
  display: none;
}

.form {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-button {
  margin-right: 12px;
}
</style>
