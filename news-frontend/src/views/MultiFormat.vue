<template>
  <div class="page-container">
    <div class="card-wrapper">
      <div class="card-header">
        <div class="card-header-title">
          <span class="icon-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
          </span>
          多格式文本处理
        </div>
        <span class="badge">AI 驱动</span>
      </div>
      <div class="card-body">
        <div class="upload-area">
          <div class="upload-box" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path>
            </svg>
            <p class="upload-text">点击或拖拽上传文件</p>
            <p class="upload-hint">支持 docx、图片（jpg/png/gif/bmp/webp）</p>
            <input
              type="file"
              ref="fileInput"
              accept=".docx,.jpg,.jpeg,.png,.gif,.bmp,.webp"
              @change="handleFileSelect"
              class="file-input-hidden"
            />
          </div>
          <div v-if="selectedFile" class="file-info">
            <span class="file-name">{{ selectedFile.name }}</span>
            <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
            <button class="remove-file-btn" @click="removeFile">&times;</button>
          </div>
        </div>

        <div class="btn-group">
          <el-button
            type="primary"
            @click="process"
            :loading="processing"
            class="btn-primary"
            icon="document"
          >
            AI 总结
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="result" class="card-wrapper result-card">
      <div class="card-header">
        <div class="card-header-title">
          <span class="icon-wrapper success">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="22 11.08 12 19.08 2 11.08"></polyline>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h1.5"></path>
            </svg>
          </span>
          总结结果
        </div>
        <span class="result-badge">已完成</span>
      </div>
      <div class="card-body">
        <div class="result-content">{{ result }}</div>
        <div class="result-actions">
          <el-button type="text" size="small" @click="copyText(result)">
            复制结果
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'

const text = ref('')
const selectedFile = ref(null)
const processing = ref(false)
const result = ref('')
const fileInput = ref(null)

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    selectedFile.value = file
  }
  event.target.value = ''
}

const handleDrop = (event) => {
  const file = event.dataTransfer.files?.[0]
  if (file) {
    selectedFile.value = file
  }
}

const removeFile = () => {
  selectedFile.value = null
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const process = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请上传文件')
    return
  }

  processing.value = true
  result.value = ''

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const response = await axios.post('/multi-format', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (response && response.code === 200) {
      result.value = response.data
    } else {
      ElMessage.error(response?.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error(error.response?.data?.error || '处理失败')
  } finally {
    processing.value = false
  }
}

const copyText = async (textToCopy) => {
  try {
    await navigator.clipboard.writeText(textToCopy)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
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
  margin-bottom: 24px;
  overflow: hidden;
}

.card-wrapper:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
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

.icon-wrapper.success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
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

.upload-area {
  margin-bottom: 20px;
}

.upload-box {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #909399;
}

.upload-box:hover {
  border-color: #409eff;
  color: #409eff;
  background: rgba(64, 158, 255, 0.02);
}

.upload-text {
  font-size: 15px;
  margin-top: 12px;
  font-weight: 500;
}

.upload-hint {
  font-size: 12px;
  margin-top: 8px;
  color: #c0c4cc;
}

.file-input-hidden {
  display: none;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 12px;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #303133;
  font-size: 14px;
}

.file-size {
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}

.remove-file-btn {
  background: transparent;
  border: none;
  color: #909399;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  padding: 0 4px;
  transition: color 0.2s;
}

.remove-file-btn:hover {
  color: #f56c6c;
}

.divider {
  display: flex;
  align-items: center;
  margin: 20px 0;
  color: #909399;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e4e7ed;
}

.divider span {
  padding: 0 16px;
}

.textarea-custom {
  width: 100%;
  border-radius: 12px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
  font-size: 15px;
  line-height: 1.6;
  resize: none;
}

.textarea-custom:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  outline: none;
}

.btn-group {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-primary {
  flex: 1;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.result-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  min-height: 150px;
}

.result-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.result-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(133, 206, 97, 0.1) 100%);
  color: #67c23a;
}

@media (max-width: 768px) {
  .page-container {
    padding: 20px 15px;
  }

  .btn-group {
    flex-direction: column;
  }
}
</style>
