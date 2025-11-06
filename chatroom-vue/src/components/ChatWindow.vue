<template>
  <div class="chat-window">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="chat-info">
        <el-avatar :size="40" :src="chatInfo.avatar">
          {{ chatInfo.name?.charAt(0) || 'C' }}
        </el-avatar>
        <div class="chat-details">
          <div class="chat-name">{{ chatInfo.name }}</div>
          <div class="chat-status">
            {{ chatInfo.type === 'friend' ? '在线' : `${chatInfo.memberCount || 0} 成员` }}
          </div>
        </div>
      </div>
      <el-dropdown @command="handleChatCommand">
        <span class="el-dropdown-link">
          <i class="el-icon-more"></i>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="info">查看详情</el-dropdown-item>
          <el-dropdown-item
            v-if="chatInfo.type === 'group'"
            command="members"
          >
            群成员
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
    
    <!-- 消息列表 -->
    <div ref="messageList" class="message-list">
      <div
        v-for="message in messages"
        :key="message.id || message.timestamp"
        class="message-item"
        :class="{ 'own-message': message.isOwn }"
      >
        <div class="message-content">
          <div v-if="chatInfo.type === 'group' && !message.isOwn" class="message-sender">
            {{ message.senderName }}
          </div>
          <div class="message-bubble">
            {{ message.content }}
          </div>
          <div class="message-time">
            {{ formatTime(message.timestamp) }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入框 -->
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="输入消息..."
        @keyup.ctrl.enter.native="handleSend"
      />
      <div class="input-actions">
        <el-button
          type="primary"
          :disabled="!inputMessage.trim()"
          @click="handleSend"
        >
          发送 (Ctrl+Enter)
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatWindow',
  props: {
    chatInfo: {
      type: Object,
      required: true
    },
    messages: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      inputMessage: ''
    }
  },
  watch: {
    messages: {
      handler() {
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      },
      deep: true
    }
  },
  mounted() {
    this.scrollToBottom()
  },
  methods: {
    handleSend() {
      if (!this.inputMessage.trim()) return
      
      this.$emit('send-message', this.inputMessage.trim())
      this.inputMessage = ''
    },
    
    scrollToBottom() {
      const messageList = this.$refs.messageList
      if (messageList) {
        messageList.scrollTop = messageList.scrollHeight
      }
    },
    
    formatTime(timestamp) {
      if (!timestamp) return ''
      
      const date = new Date(timestamp)
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
      
      if (messageDate.getTime() === today.getTime()) {
        // 今天
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else {
        // 其他日期
        return date.toLocaleString('zh-CN', {
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      }
    },
    
    handleChatCommand(command) {
      if (command === 'info') {
        // TODO: 查看详情
        this.$message.info('查看详情功能开发中')
      } else if (command === 'members') {
        // TODO: 群成员
        this.$message.info('群成员功能开发中')
      }
    }
  }
}
</script>

<style scoped>
.chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 1px solid rgba(228, 231, 237, 0.5);
  backdrop-filter: blur(10px);
}

.chat-info {
  display: flex;
  align-items: center;
}

.chat-info >>> .el-avatar {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  border: 2px solid white;
}

.chat-details {
  margin-left: 12px;
}

.chat-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.chat-status {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
}

.chat-status::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #67c23a;
  border-radius: 50%;
  margin-right: 6px;
}

.el-dropdown-link {
  cursor: pointer;
  font-size: 20px;
  color: #909399;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s;
}

.el-dropdown-link:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: rotate(90deg);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: 
    linear-gradient(135deg, rgba(102, 126, 234, 0.02) 0%, rgba(118, 75, 162, 0.02) 100%),
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 40px,
      rgba(102, 126, 234, 0.03) 40px,
      rgba(102, 126, 234, 0.03) 41px
    );
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: transparent;
}

.message-list::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.5);
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.own-message {
  justify-content: flex-end;
}

.message-content {
  max-width: 65%;
  display: flex;
  flex-direction: column;
}

.message-item.own-message .message-content {
  align-items: flex-end;
}

.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: 500;
  padding: 0 4px;
}

.message-bubble {
  padding: 12px 16px;
  background: white;
  border-radius: 18px;
  word-wrap: break-word;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 1px 2px rgba(0, 0, 0, 0.05);
  position: relative;
  transition: all 0.2s;
  line-height: 1.5;
}

.message-bubble:hover {
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.12),
    0 2px 4px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.message-item.own-message .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item:not(.own-message) .message-bubble {
  border-bottom-left-radius: 4px;
}

.message-item.own-message .message-bubble::after {
  content: '';
  position: absolute;
  right: -8px;
  bottom: 12px;
  width: 0;
  height: 0;
  border: 8px solid transparent;
  border-left-color: #764ba2;
  border-right: none;
}

.message-item:not(.own-message) .message-bubble::after {
  content: '';
  position: absolute;
  left: -8px;
  bottom: 12px;
  width: 0;
  height: 0;
  border: 8px solid transparent;
  border-right-color: white;
  border-left: none;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 6px;
  padding: 0 4px;
}

.message-item.own-message .message-time {
  color: #c0c4cc;
}

.chat-input {
  border-top: 1px solid rgba(228, 231, 237, 0.5);
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.chat-input >>> .el-textarea__inner {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.5;
  transition: all 0.3s;
  resize: none;
}

.chat-input >>> .el-textarea__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.input-actions >>> .el-button {
  border-radius: 20px;
  padding: 10px 24px;
  font-weight: 500;
  transition: all 0.3s;
}

.input-actions >>> .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.input-actions >>> .el-button--primary:hover {
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
}

.input-actions >>> .el-button--primary:active {
  transform: translateY(0);
}

.input-actions >>> .el-button.is-disabled {
  background: #e4e7ed;
  box-shadow: none;
}
</style>

