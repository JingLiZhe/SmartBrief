<template>
    <div class="reset-password-container">
        <div class="reset-bg-decoration"></div>
        <el-card class="reset-card" shadow="never">
            <h2 class="text-center reset-title">重置密码</h2>
            <p class="text-center reset-sub">
                {{ step === 1 ? '请输入您的注册邮箱以接收重置链接' : '请粘贴邮件中的重置令牌并设置新密码' }}
            </p>

            <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="reset-form">
                <!-- 第一步：邮箱 -->
                <template v-if="step === 1">
                    <el-form-item prop="email">
                        <el-input v-model="form.email" placeholder="请输入您的注册邮箱" prefix-icon="Message" size="large" clearable />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" class="w-full reset-btn" :loading="loading" @click="sendResetLink">
                            发送重置链接
                        </el-button>
                    </el-form-item>
                    <p class="text-center tip-text">
                        <span>重置链接将发送至您的邮箱，30分钟内有效</span>
                    </p>
                </template>

                <!-- 第二步：令牌 + 新密码 -->
                <template v-else>
                    <el-form-item prop="token">
                        <el-input v-model="form.token" placeholder="请粘贴邮件中的重置令牌token=xxx" prefix-icon="Key" size="large" />
                    </el-form-item>
                    <el-form-item prop="newPassword">
                        <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码（至少6位）" prefix-icon="Lock" size="large" show-password />
                    </el-form-item>
                    <el-form-item prop="confirmPassword">
                        <el-input v-model="form.confirmPassword" type="password" placeholder="请再次确认新密码" prefix-icon="Lock" size="large" show-password />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" class="w-full reset-btn" :loading="loading" @click="resetPassword">
                            确认重置密码
                        </el-button>
                    </el-form-item>
                </template>

                <p class="text-center mt-4 login-link">
                    已有账号？<a href="/login">立即登录</a>
                </p>
            </el-form>
        </el-card>
    </div>
</template>

<script setup>
    import { ref, reactive, nextTick } from 'vue'
    import { ElMessage } from 'element-plus'
    import { useRouter } from 'vue-router'
    import axios from '../utils/axios'

    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const step = ref(1)  // 1: 邮箱输入, 2: 令牌+新密码

    const form = reactive({
        email: '',
        token: '',
        newPassword: '',
        confirmPassword: ''
    })

    const validatePass = (rule, value, callback) => {
        if (value !== form.newPassword) {
            callback(new Error('两次输入的密码不一致'))
        } else {
            callback()
        }
    }

    const rules = {
        email: [
            { required: true, message: '请输入邮箱', trigger: 'blur' },
            { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
        ],
        token: [
            { required: true, message: '请输入重置令牌', trigger: 'blur' }
        ],
        newPassword: [
            { required: true, message: '请输入新密码', trigger: 'blur' },
            { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
            { required: true, message: '请再次输入新密码', trigger: 'blur' },
            { validator: validatePass, trigger: ['blur', 'change'] }
        ]
    }

    // 发送重置链接（第一步）
    const sendResetLink = async () => {
        if (!formRef.value) return
        await formRef.value.validate()

        loading.value = true
        try {
            const res = await axios.post('/auth/forgot-password', { email: form.email })
            console.log('发送成功:', res)
            ElMessage.success('重置链接已发送至您的邮箱，请查收（包括垃圾箱）')

            // 🔥 强制跳转到第二步，并使用 nextTick 确保视图更新
            step.value = 2
            await nextTick()
            console.log('已切换到第二步，step =', step.value)
        } catch (error) {
            console.error('发送失败:', error)
            ElMessage.error(error.response?.data?.message || '发送失败，请稍后重试')
        } finally {
            loading.value = false
        }
    }

    // 重置密码（第二步）
    const resetPassword = async () => {
        if (!formRef.value) return
        await formRef.value.validate()

        loading.value = true
        try {
            const res = await axios.post('/auth/reset-password', {
                token: form.token,
                newPassword: form.newPassword
            })
            ElMessage.success('密码重置成功！请使用新密码登录')
            setTimeout(() => router.push('/login'), 1500)
        } catch (error) {
            ElMessage.error(error.response?.data?.message || '重置失败，请检查令牌是否正确')
        } finally {
            loading.value = false
        }
    }
</script>

<style scoped>
    /* ========== 蓝白背景 ========== */
    .reset-password-container {
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

    .reset-bg-decoration {
        position: absolute;
        width: 500px;
        height: 500px;
        background: radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.15), transparent 70%);
        border-radius: 50%;
        top: -150px;
        left: -150px;
        pointer-events: none;
    }
    .reset-bg-decoration::after {
        content: '';
        position: absolute;
        width: 400px;
        height: 400px;
        background: radial-gradient(circle at 80% 70%, rgba(102, 126, 234, 0.12), transparent 70%);
        border-radius: 50%;
        bottom: -200px;
        right: -150px;
    }

    .reset-card {
        width: 400px;
        padding: 36px 30px 30px;
        border-radius: 20px;
        background: #ffffff;
        border: 1px solid rgba(64, 158, 255, 0.12);
        box-shadow: 0 8px 40px rgba(64, 158, 255, 0.15);
        position: relative;
        z-index: 2;
    }

    .reset-title {
        color: #1a3a6b;
        font-weight: 700;
        font-size: 26px;
        letter-spacing: 3px;
        margin-bottom: 4px;
        text-align: center;
    }

    .reset-sub {
        color: #6a8aaa;
        font-size: 14px;
        text-align: center;
        margin-bottom: 24px;
    }

    .reset-form :deep(.el-form-item) {
        margin-bottom: 20px;
    }

    .reset-form :deep(.el-input__wrapper) {
        background: #f5f9ff !important;
        border: 1.5px solid #dce8f5 !important;
        border-radius: 12px !important;
        box-shadow: none !important;
        transition: all 0.3s ease;
    }

    .reset-form :deep(.el-input__wrapper:hover) {
        border-color: #8bb9f0 !important;
        background: #f0f6fe !important;
    }

    .reset-form :deep(.el-input__wrapper.is-focus) {
        border-color: #409eff !important;
        background: #ffffff !important;
        box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.12) !important;
    }

    .reset-form :deep(.el-input__inner) {
        color: #1a3a6b !important;
        font-size: 15px;
    }
    .reset-form :deep(.el-input__inner::placeholder) {
        color: #a0bcd8 !important;
    }

    .reset-form :deep(.el-input__prefix) {
        color: #7aa9d9 !important;
    }
    .reset-form :deep(.el-input__wrapper.is-focus .el-input__prefix) {
        color: #409eff !important;
    }

    .reset-btn {
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
        width: 100%;
    }

    .reset-btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 28px rgba(64, 158, 255, 0.4);
        background: linear-gradient(135deg, #5aafff 0%, #3d8be0 100%);
    }

    .tip-text {
        color: #8aaccc;
        font-size: 13px;
        text-align: center;
        margin-top: -8px;
        margin-bottom: 4px;
    }

    .login-link {
        margin-top: 20px;
        color: #6a8aaa;
        font-size: 14px;
        text-align: center;
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

    @media (max-width: 480px) {
        .reset-card {
            width: 92%;
            padding: 28px 18px 22px;
        }
        .reset-title {
            font-size: 22px;
        }
        .reset-btn {
            height: 44px;
            font-size: 15px;
        }
    }
</style>