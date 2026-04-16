<template>
  <el-dialog
    title="好友申请"
    :visible.sync="dialogVisible"
    width="450px"
    :before-close="handleClose"
  >
    <div class="request-list" v-if="requests.length > 0">
      <div
        v-for="request in requests"
        :key="request.id"
        class="request-item"
      >
        <div class="request-info">
          <el-avatar :size="40">
            {{ request.applicantNickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="request-detail">
            <div class="request-name">{{ request.applicantNickname || '用户' + request.applicantId }}</div>
            <div class="request-message" v-if="request.message">{{ request.message }}</div>
            <div class="request-time">{{ formatTime(request.createTime) }}</div>
          </div>
        </div>
        <div class="request-actions">
          <el-button
            type="primary"
            size="small"
            :loading="request.loading"
            @click="handleAccept(request)"
          >
            添加
          </el-button>
          <el-button
            size="small"
            :loading="request.loading"
            @click="handleReject(request)"
          >
            拒绝
          </el-button>
        </div>
      </div>
    </div>
    <div v-else class="empty-list">
      <el-empty description="暂无好友申请" :image-size="80"></el-empty>
    </div>
  </el-dialog>
</template>

<script>
import { Message } from 'element-ui'
import { getFriendRequests, handleFriendRequest } from '@/api/friend'

export default {
  name: 'FriendRequestList',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      requests: []
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.loadRequests()
      }
    }
  },
  methods: {
    async loadRequests() {
      try {
        const res = await getFriendRequests()
        if (res.data) {
          this.requests = res.data.map(req => ({
            ...req,
            loading: false
          }))
        }
      } catch (error) {
        Message.error('加载好友申请失败')
      }
    },

    async handleAccept(request) {
      this.$set(request, 'loading', true)
      try {
        await handleFriendRequest(request.id, 'accept')
        Message.success('已添加为好友')
        // 清除该申请者的未读消息计数
        this.$store.commit('message/CLEAR_UNREAD_COUNT', request.applicantId)
        // 刷新好友申请列表
        this.$store.dispatch('friend/getReceivedRequests')
        // 从本地列表移除
        this.requests = this.requests.filter(r => r.id !== request.id)
        this.$emit('request-accepted', request)
      } catch (error) {
        Message.error(error.message || '操作失败')
      } finally {
        this.$set(request, 'loading', false)
      }
    },

    async handleReject(request) {
      this.$set(request, 'loading', true)
      try {
        await handleFriendRequest(request.id, 'reject')
        Message.success('已拒绝')
        // 清除该申请者的未读消息计数
        this.$store.commit('message/CLEAR_UNREAD_COUNT', request.applicantId)
        // 刷新好友申请列表
        this.$store.dispatch('friend/getReceivedRequests')
        // 从本地列表移除
        this.requests = this.requests.filter(r => r.id !== request.id)
      } catch (error) {
        Message.error(error.message || '操作失败')
      } finally {
        this.$set(request, 'loading', false)
      }
    },

    handleClose() {
      this.dialogVisible = false
    },

    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'

      return `${date.getMonth() + 1}/${date.getDate()}`
    }
  }
}
</script>

<style scoped>
/* Clean Minimal White Friend Request List */
.request-list {
  max-height: 400px;
  overflow-y: auto;
}

.request-list::-webkit-scrollbar {
  width: 4px;
}

.request-list::-webkit-scrollbar-track {
  background: transparent;
}

.request-list::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 2px;
}

.request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 8px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.2s;
}

.request-item:last-child {
  border-bottom: none;
}

.request-item:hover {
  background: #f8fafc;
}

.request-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.request-info >>> .el-avatar {
  background: #0ea5e9;
  color: #ffffff;
  font-weight: 600;
  flex-shrink: 0;
}

.request-detail {
  margin-left: 12px;
  overflow: hidden;
  min-width: 0;
}

.request-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 2px;
}

.request-message {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.request-time {
  font-size: 12px;
  color: #94a3b8;
}

.request-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.request-actions >>> .el-button {
  border-radius: 8px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.request-actions >>> .el-button--primary {
  background: #0ea5e9;
  border-color: #0ea5e9;
}

.request-actions >>> .el-button--primary:hover {
  background: #0284c7;
  border-color: #0284c7;
}

.request-actions >>> .el-button--default {
  border-color: #e2e8f0;
  color: #64748b;
}

.request-actions >>> .el-button--default:hover {
  border-color: #0ea5e9;
  color: #0ea5e9;
  background: #f0f9ff;
}

.empty-list {
  padding: 40px 0;
}

.empty-list >>> .el-empty__description {
  color: #94a3b8;
}
</style>
