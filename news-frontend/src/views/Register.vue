<template>
  <div class="register-container">
    <div class="register-bg-decoration"></div>
    <el-card class="register-card" shadow="never">
      <h2 class="text-center register-title">用户注册</h2>
      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="w-full register-btn">注 册</el-button>
        </el-form-item>
      </el-form>
      <p class="text-center mt-4 login-link">
        已有账号？<a href="/login">立即登录</a>
      </p>
    </el-card>
  </div>
</template>

<script setup>
    import { ref } from 'vue'
    import axios from '../utils/axios'

    const registerForm = ref({
        username: '',
        password: '',
        email: ''
    })

    const handleRegister = async () => {
        try {
            await axios.post('/auth/register', registerForm.value)
            alert('注册成功，请登录')
            window.location.href = '/login'
        } catch (error) {
            console.error('Register failed:', error)
            alert(error.response?.data?.error || '注册失败')
        }
    }
</script>

<style scoped>
  /* ========== 蓝白背景 ========== */
  .register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    width: 100%;
    background: linear-gradient(135deg, #e8f0fe 0%, #d4e4ff 50%, #b7d2ff 100%);
    position: fixed;
    top: 0;
    left: 0;
    overflow: hidden;
  }

  /* ========== 装饰性蓝色光晕 ========== */
  .register-bg-decoration {
    position: absolute;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.15), transparent 70%);
    border-radius: 50%;
    top: -150px;
    left: -150px;
    pointer-events: none;
  }
  .register-bg-decoration::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle at 80% 70%, rgba(102, 126, 234, 0.12), transparent 70%);
    border-radius: 50%;
    bottom: -200px;
    right: -150px;
  }

  /* ========== 白色卡片 ========== */
  .register-card {
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

  .register-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 50px rgba(64, 158, 255, 0.2);
  }

  /* ========== 标题 ========== */
  .register-title {
    color: #1a3a6b;
    font-weight: 700;
    font-size: 26px;
    letter-spacing: 3px;
    margin-bottom: 28px;
  }

  /* ========== 表单项标签 ========== */
  :deep(.el-form-item__label) {
    color: #3a5a8a !important;
    font-weight: 500;
  }

  /* ========== 输入框 ========== */
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

  :deep(.el-input__inner) {
    color: #1a3a6b !important;
    font-size: 15px;
  }
  :deep(.el-input__inner::placeholder) {
    color: #a0bcd8 !important;
  }

  /* 输入框图标 */
  :deep(.el-input__prefix) {
    color: #7aa9d9 !important;
  }
  :deep(.el-input__wrapper.is-focus .el-input__prefix) {
    color: #409eff !important;
  }

  /* ========== 注册按钮 ========== */
  .register-btn {
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

  .register-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 28px rgba(64, 158, 255, 0.4);
    background: linear-gradient(135deg, #5aafff 0%, #3d8be0 100%);
  }

  .register-btn:active {
    transform: translateY(0px);
    box-shadow: 0 3px 10px rgba(64, 158, 255, 0.25);
  }

  /* ========== 登录链接 ========== */
  .login-link {
    margin-top: 20px;
    color: #6a8aaa;
    font-size: 14px;
  }

  .login-link a {
    color: #409eff;
    text-decoration: none;
    font-weight: 600;
    transition: all 0.3s ease;
    position: relative;
  }

  .login-link a::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: #409eff;
    transition: width 0.3s ease;
  }

  .login-link a:hover {
    color: #2d7fd3;
  }
  .login-link a:hover::after {
    width: 100%;
  }

  /* ========== 工具类 ========== */
  .w-full {
    width: 100%;
  }

  .text-center {
    text-align: center;
  }

  .mb-4 {
    margin-bottom: 16px;
  }

  .mt-4 {
    margin-top: 16px;
  }

  /* ========== 响应式适配 ========== */
  @media (max-width: 480px) {
    .register-card {
      width: 92%;
      padding: 28px 18px 22px;
    }
    .register-title {
      font-size: 22px;
    }
    :deep(.el-form-item__label) {
      font-size: 13px;
    }
    :deep(.el-input__wrapper) {
      border-radius: 10px !important;
    }
    .register-btn {
      height: 44px;
      font-size: 15px;
    }
  }
</style>