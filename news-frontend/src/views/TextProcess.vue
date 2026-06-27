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
          智能文本处理
        </div>
        <span class="badge">AI 驱动</span>
      </div>
      <div class="card-body">
        <el-input
                v-model="text"
                type="textarea"
                :rows="8"
                placeholder="请输入新闻文本，或粘贴内容..."
                class="textarea-custom"
        />
        <div class="btn-group">
          <el-button
                  type="primary"
                  @click="summarize"
                  :loading="summarizing"
                  class="btn-primary"
                  icon="document"
          >
            AI 总结
          </el-button>
          <el-button
                  type="success"
                  @click="search"
                  :loading="searching"
                  class="btn-success"
                  icon="search"
          >
            AI 搜索相关信息
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="summaryResult" class="card-wrapper result-card">
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
        <div class="result-content">{{ summaryResult }}</div>
        <div class="result-actions">
          <el-button type="text" size="small" @click="copyText(summaryResult)">
            复制结果
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="searchResult" class="card-wrapper result-card">
      <div class="card-header">
        <div class="card-header-title">
          <span class="icon-wrapper info">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
          </span>
          相关搜索结果
        </div>
        <span class="result-badge">已完成</span>
      </div>
      <div class="card-body">
        <div class="result-content">{{ searchResult }}</div>
        <div class="result-actions">
          <el-button type="text" size="small" @click="copyText(searchResult)">
            复制结果
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
    import { ref } from 'vue'
    import { ElMessage } from 'element-plus'    // ✅ 补充导入
    import axios from '../utils/axios'

    const text = ref('')
    const summarizing = ref(false)
    const searching = ref(false)
    const summaryResult = ref('')
    const searchResult = ref('')

    const summarize = async () => {
        if (!text.value.trim()) {
            return ElMessage.warning('请输入文本')
        }

        summarizing.value = true
        searchResult.value = ''
        try {
            const res = await axios.post('/summarize', { text: text.value })
            summaryResult.value = res.data
        } catch (error) {
            console.error('Summarize failed:', error)
            ElMessage.error('总结失败，请重试')
        } finally {
            summarizing.value = false
        }
    }

    const search = async () => {
        if (!text.value.trim()) {
            return ElMessage.warning('请输入文本')
        }

        searching.value = true
        summaryResult.value = ''
        try {
            const res = await axios.post('/search', { text: text.value })
            if (res.data && res.data.result) {
                searchResult.value = res.data.result.replace(/\\n/g, '\n')
            } else {
                searchResult.value = res.data || '未找到相关信息'
            }
        } catch (error) {
            console.error('Search failed:', error)
            ElMessage.error('搜索失败，请重试')
        } finally {
            searching.value = false
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
  /* ===== 所有 SCSS 嵌套已展开为纯 CSS ===== */

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

  /* 展开 .icon-wrapper 的变体 */
  .icon-wrapper.success {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  }

  .icon-wrapper.info {
    background: linear-gradient(135deg, #909399 0%, #b4bccc 100%);
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

  .textarea-custom {
    width: 100%;
    border-radius: 12px;
    border: 2px solid #e4e7ed;
    transition: all 0.3s ease;
    font-size: 15px;
    line-height: 1.6;
    resize: none;
  }

  /* 展开 :focus */
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

  .btn-primary,
  .btn-success {
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
  }

  .btn-primary {
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    color: white;
  }

  .btn-primary:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
  }

  .btn-success {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
    color: white;
  }

  .btn-success:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
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