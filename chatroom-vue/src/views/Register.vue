<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>千语聊天室</h1>
        <p>创建新账号</p>
      </div>
      
      <el-form
        ref="registerForm"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="昵称"
            prefix-icon="el-icon-user-solid"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { Message } from 'element-ui'

export default {
  name: 'RegisterPage',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      registerForm: {
        username: '',
        nickname: '',
        password: '',
        confirmPassword: ''
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
        ],
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    async handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (!valid) return
        
        this.loading = true
        try {
          const formData = {
            username: this.registerForm.username,
            nickname: this.registerForm.nickname,
            password: this.registerForm.password
          }
          await this.$store.dispatch('user/register', formData)
          Message.success('注册成功，请登录')
          this.$router.push('/login')
        } catch (error) {
          Message.error(error.message || '注册失败，请重试')
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
/* Clean Minimal White Register */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f8fafc;
}

.register-box {
  width: 380px;
  padding: 40px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h1 {
  font-size: 24px;
  color: #1e293b;
  margin-bottom: 8px;
  font-weight: 700;
}

.register-header p {
  color: #64748b;
  font-size: 14px;
}

.register-form {
  margin-top: 24px;
}

.register-form >>> .el-form-item {
  margin-bottom: 18px;
}

.register-form >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding-left: 14px;
  height: 44px;
  font-size: 14px;
  transition: all 0.2s;
  background: #f8fafc;
}

.register-form >>> .el-input__inner:focus {
  border-color: #0ea5e9;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

.register-form >>> .el-input__prefix {
  color: #94a3b8;
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  background: #0ea5e9;
  border-color: #0ea5e9;
  transition: all 0.2s;
}

.register-button:hover {
  background: #0284c7;
  border-color: #0284c7;
  transform: translateY(-1px);
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  color: #64748b;
  font-size: 14px;
}

.register-footer a {
  color: #0ea5e9;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>

