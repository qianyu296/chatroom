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
            <span v-if="chatInfo.type === 'friend'" :class="['status-dot', { online: chatInfo.status === '在线' }]"></span>
            {{ chatInfo.type === 'friend' ? (chatInfo.status || '离线') : `${chatInfo.memberCount || 0} 成员` }}
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
/* Clean Minimal White Chat Window */
.chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

/* Header */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.chat-info {
  display: flex;
  align-items: center;
}

.chat-info .el-avatar {
  background: #0ea5e9;
  color: #fff;
  font-weight: 600;
}

.chat-details {
  margin-left: 12px;
}

.chat-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2px;
}

.chat-status {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #94a3b8;
  border-radius: 50%;
  margin-right: 6px;
  transition: background 0.2s;
}

.status-dot.online {
  background: #22c55e;
}

.el-dropdown-link {
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #64748b;
  transition: all 0.2s;
}

.el-dropdown-link:hover {
  background: #f1f5f9;
  color: #1e293b;
}

/* Message List */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8fafc;
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: transparent;
}

.message-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Message Items */
.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
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
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-item.own-message .message-content {
  align-items: flex-end;
}

.message-sender {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
  font-weight: 500;
  padding: 0 4px;
}

.message-bubble {
  padding: 10px 14px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  word-wrap: break-word;
  line-height: 1.5;
  font-size: 14px;
  color: #1e293b;
  transition: all 0.2s;
}

.message-bubble:hover {
  border-color: #cbd5e1;
}

.message-item.own-message .message-bubble {
  background: #0ea5e9;
  border-color: #0ea5e9;
  color: #ffffff;
  border-bottom-right-radius: 4px;
}

.message-item:not(.own-message) .message-bubble {
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
  padding: 0 4px;
}

/* Input Area */
.chat-input {
  border-top: 1px solid #e2e8f0;
  padding: 16px 20px;
  background: #ffffff;
}

.chat-input >>> .el-textarea__inner {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.5;
  transition: all 0.2s;
  resize: none;
  background: #f8fafc;
}

.chat-input >>> .el-textarea__inner:focus {
  border-color: #0ea5e9;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.input-actions >>> .el-button {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.2s;
}

.input-actions >>> .el-button--primary {
  background: #0ea5e9;
  border-color: #0ea5e9;
}

.input-actions >>> .el-button--primary:hover {
  background: #0284c7;
  border-color: #0284c7;
  transform: translateY(-1px);
}

.input-actions >>> .el-button.is-disabled {
  background: #f1f5f9;
  border-color: #e2e8f0;
  color: #94a3b8;
}
</style>

