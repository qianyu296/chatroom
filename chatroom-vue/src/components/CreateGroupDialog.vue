<template>
  <el-dialog
    title="创建群组"
    :visible.sync="dialogVisible"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="createGroupForm"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="群名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入群名称"
        />
      </el-form-item>
      
      <el-form-item label="群描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入群描述（可选）"
        />
      </el-form-item>
    </el-form>
    
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        创建
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { Message } from 'element-ui'
import { createGroup } from '@/api/group'

export default {
  name: 'CreateGroupDialog',
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
        name: '',
        description: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入群名称', trigger: 'blur' }
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
      this.$refs.createGroupForm.validate(async (valid) => {
        if (!valid) return
        
        this.loading = true
        try {
          await createGroup(this.form)
          Message.success('群组创建成功')
          this.$emit('success')
          this.handleClose()
        } catch (error) {
          Message.error(error.message || '创建群组失败')
        } finally {
          this.loading = false
        }
      })
    },
    
    handleClose() {
      this.dialogVisible = false
      this.$refs.createGroupForm.resetFields()
      this.form = {
        name: '',
        description: ''
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

