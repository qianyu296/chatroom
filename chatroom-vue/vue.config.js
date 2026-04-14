const { defineConfig } = require('@vue/cli-service')
const path = require('path')

module.exports = defineConfig({
  transpileDependencies: true,
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  },
  devServer: {
    port: 5000,
    // 允许所有host访问，解决ngrok等内网穿透工具的Invalid Host header问题
    allowedHosts: 'all',
    // 或者可以指定允许的host
    // allowedHosts: [
    //   '.ngrok.io',
    //   '.ngrok-free.app',
    //   '.ngrok.app'
    // ],
    // 禁用Host检查（旧版本Vue CLI使用，Vue CLI 5.x推荐使用allowedHosts）
    // disableHostCheck: true,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, PATCH, OPTIONS',
      'Access-Control-Allow-Headers': 'X-Requested-With, content-type, Authorization'
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
        secure: false,
        // 如果需要，可以添加日志
        // logLevel: 'debug'
      }
    }
  }
})
