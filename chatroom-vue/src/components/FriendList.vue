<template>
  <div class="friend-list">
    <div class="friend-list-header">
      <span>好友列表</span>
      <el-button
        type="text"
        icon="el-icon-plus"
        size="small"
        @click="$emit('add-friend')"
      >
        添加好友
      </el-button>
    </div>
    
    <div class="friend-list-content">
      <div
        v-for="friend in filteredFriendList"
        :key="friend.id"
        class="friend-item"
        :class="{ active: currentChat?.type === 'friend' && currentChat?.id === friend.id }"
        @click="$emit('select-friend', friend)"
      >
        <el-avatar :size="40" :src="friend.avatar">
          {{ friend.nickname?.charAt(0) || 'F' }}
        </el-avatar>
        <div class="friend-info">
          <div class="friend-name">{{ friend.nickname }}</div>
          <div class="friend-status">{{ friend.status || '离线' }}</div>
        </div>
        <div
          v-if="unreadCounts[friend.id] > 0"
          class="unread-badge"
        >
          {{ unreadCounts[friend.id] > 99 ? '99+' : unreadCounts[friend.id] }}
        </div>
      </div>
      
      <div v-if="filteredFriendList.length === 0" class="empty-list">
        <p>暂无好友</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'FriendList',
  props: {
    friendList: {
      type: Array,
      default: () => []
    },
    currentChat: {
      type: Object,
      default: null
    }
  },
  computed: {
    ...mapState('message', ['unreadCounts']),
    
    filteredFriendList() {
      // TODO: 根据搜索关键词过滤
      return this.friendList
    }
  }
}
</script>

<style scoped>
/* Clean Minimal White Friend List */
.friend-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.friend-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #ffffff;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.friend-list-header >>> .el-button {
  color: #64748b;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  transition: all 0.2s;
}

.friend-list-header >>> .el-button:hover {
  background: #f1f5f9;
  color: #0ea5e9;
}

.friend-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.friend-list-content::-webkit-scrollbar {
  width: 4px;
}

.friend-list-content::-webkit-scrollbar-track {
  background: transparent;
}

.friend-list-content::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 2px;
}

.friend-list-content::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin: 2px 8px;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s;
  position: relative;
}

.friend-item:hover {
  background: #f8fafc;
}

.friend-item.active {
  background: #f0f9ff;
}

.friend-item >>> .el-avatar {
  background: #0ea5e9;
  color: #ffffff;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
}

.friend-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.friend-status {
  font-size: 12px;
  color: #94a3b8;
}

.friend-item.active .friend-name {
  color: #0ea5e9;
}

.unread-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: #ef4444;
  color: white;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-list {
  padding: 60px 20px;
  text-align: center;
  color: #94a3b8;
}

.empty-list p {
  font-size: 14px;
  margin-top: 10px;
}
</style>

