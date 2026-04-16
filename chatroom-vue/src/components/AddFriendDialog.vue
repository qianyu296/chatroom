<template>
  <el-dialog
    title="添加好友"
    :visible.sync="dialogVisible"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="addFriendForm"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入要添加的用户名"
        />
      </el-form-item>
      
      <el-form-item label="验证消息" prop="message">
        <el-input
          v-model="form.message"
          type="textarea"
          :rows="3"
          placeholder="请输入验证消息（可选）"
        />
      </el-form-item>
    </el-form>
    
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        发送申请
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { Message } from 'element-ui'
import { addFriend } from '@/api/friend'

export default {
  name: 'AddFriendDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: this.visible,
      form: {
        username: '',
        message: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (!val) {
        this.handleClose()
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('close')
      }
    }
  },
  methods: {
    async handleSubmit() {
      this.$refs.addFriendForm.validate(async (valid) => {
        if (!valid) return
        
        this.loading = true
        try {
          await addFriend(this.form)
          Message.success('好友申请已发送')
          this.$emit('success')
          this.handleClose()
        } catch (error) {
          Message.error(error.message || '添加好友失败')
        } finally {
          this.loading = false
        }
      })
    },
    
    handleClose() {
      this.dialogVisible = false
      this.$refs.addFriendForm.resetFields()
      this.form = {
        username: '',
        message: ''
      }
    }
  }
}
</script>

<style scoped>
/* Clean Minimal White Dialog */
.dialog-footer {
  text-align: right;
}

.dialog-footer >>> .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.2s;
}

.dialog-footer >>> .el-button--default {
  border-color: #e2e8f0;
  color: #64748b;
}

.dialog-footer >>> .el-button--default:hover {
  border-color: #0ea5e9;
  color: #0ea5e9;
  background: #f0f9ff;
}

.dialog-footer >>> .el-button--primary {
  background: #0ea5e9;
  border-color: #0ea5e9;
}

.dialog-footer >>> .el-button--primary:hover {
  background: #0284c7;
  border-color: #0284c7;
}

/* Form styling */
>>> .el-form-item__label {
  color: #1e293b;
  font-weight: 500;
}

>>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

>>> .el-input__inner:focus {
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

>>> .el-textarea__inner {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

>>> .el-textarea__inner:focus {
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}
</style>

