import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import '@arco-design/web-vue/dist/arco.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './assets/style/global.scss'
import { escapeHtml } from '@/utils/xss'

const app = createApp(App)
app.use(ArcoVue)
app.use(ArcoVueIcon)
app.use(createPinia())
app.use(router)

// 注册全局 XSS 安全指令 v-safe-text
// 用法: <span v-safe-text="userInput"></span>
// 等价于 textContent 赋值，但额外做了 HTML 转义
app.directive('safe-text', {
  mounted(el, binding) {
    el.textContent = binding.value ?? ''
  },
  updated(el, binding) {
    el.textContent = binding.value ?? ''
  }
})

// 注册全局属性，方便模板中使用
app.config.globalProperties.$escapeHtml = escapeHtml

app.mount('#app')
