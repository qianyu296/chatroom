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
.dialog-footer {
  text-align: right;
}
</style>

