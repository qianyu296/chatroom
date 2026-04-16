<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>千语聊天室</h1>
        <p>欢迎回来</p>
      </div>
      
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="el-icon-user"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="el-icon-lock"
            show-password
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { Message } from 'element-ui'

export default {
  name: 'LoginPage',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (!valid) return
        
        this.loading = true
        try {
          await this.$store.dispatch('user/login', this.loginForm)
          Message.success('登录成功')
          // 等待状态更新完成后再跳转
          await this.$nextTick()
          this.$router.push('/').catch(() => {})
        } catch (error) {
          Message.error(error.message || '登录失败，请检查用户名和密码')
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
/* Clean Minimal White Login */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f8fafc;
}

.login-box {
  width: 380px;
  padding: 40px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 24px;
  color: #1e293b;
  margin-bottom: 8px;
  font-weight: 700;
}

.login-header p {
  color: #64748b;
  font-size: 14px;
}

.login-form {
  margin-top: 24px;
}

.login-form >>> .el-form-item {
  margin-bottom: 20px;
}

.login-form >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding-left: 14px;
  height: 44px;
  font-size: 14px;
  transition: all 0.2s;
  background: #f8fafc;
}

.login-form >>> .el-input__inner:focus {
  border-color: #0ea5e9;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

.login-form >>> .el-input__prefix {
  color: #94a3b8;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  background: #0ea5e9;
  border-color: #0ea5e9;
  transition: all 0.2s;
}

.login-button:hover {
  background: #0284c7;
  border-color: #0284c7;
  transform: translateY(-1px);
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  color: #64748b;
  font-size: 14px;
}

.login-footer a {
  color: #0ea5e9;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>

