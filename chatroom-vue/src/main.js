import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import zhCN from 'element-ui/lib/locale/lang/zh-CN'
import locale from 'element-ui/lib/locale'

Vue.config.productionTip = false

Vue.use(ElementUI)

// 设置 Element UI 为中文
locale.use(zhCN)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
