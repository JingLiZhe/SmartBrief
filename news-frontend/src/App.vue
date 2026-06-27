<template>
  <div id="app">
    <el-container v-if="isLoggedIn" class="app-container">
      <el-header class="app-header">
        <div class="header-content">
          <span class="logo">智讯</span>
          <div class="menu">
            <router-link to="/" class="nav-link" :class="{ active: currentRoute === '/' }">首页</router-link>
            <router-link to="/brief" class="nav-link" :class="{ active: currentRoute === '/brief' }">每日简报</router-link>
            <router-link to="/text-process" class="nav-link" :class="{ active: currentRoute === '/text-process' }">文本处理</router-link>
            <router-link to="/multi-format" class="nav-link" :class="{ active: currentRoute === '/multi-format' }">多格式处理</router-link>
            <router-link to="/profile" class="nav-link" :class="{ active: currentRoute === '/profile' }">个人中心</router-link>
          </div>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const isLoggedIn = ref(false)
const currentRoute = ref('/')

const checkLogin = () => {
  isLoggedIn.value = !!localStorage.getItem('token')
}

onMounted(() => {
  checkLogin()
  currentRoute.value = route.path
})

watch(() => route.path, (newPath) => {
  currentRoute.value = newPath
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
}

#app {
  width: 100%;
  min-height: 100vh;
}

.app-container {
  min-height: 100vh;
  width: 100%;
}

.app-header {
  background-color: #409eff;
  color: white;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.menu {
  display: flex;
  background-color: transparent;
  flex: 1;
  justify-content: flex-end;
  gap: 10px;
}

.nav-link {
  display: inline-block;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.nav-link.active {
  background-color: #5dade2;
}



.app-main {
  padding: 20px;
}
</style>
