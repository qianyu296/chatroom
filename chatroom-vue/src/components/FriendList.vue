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
.friend-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

.friend-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: white;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.friend-list-header >>> .el-button {
  color: #667eea;
  font-weight: 500;
  padding: 5px 10px;
}

.friend-list-header >>> .el-button:hover {
  background: rgba(102, 126, 234, 0.1);
}

.friend-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 5px 0;
}

.friend-list-content::-webkit-scrollbar {
  width: 6px;
}

.friend-list-content::-webkit-scrollbar-track {
  background: transparent;
}

.friend-list-content::-webkit-scrollbar-thumb {
  background: #e4e7ed;
  border-radius: 3px;
}

.friend-list-content::-webkit-scrollbar-thumb:hover {
  background: #c1c1c1;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  margin: 2px 10px;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.friend-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 3px 3px 0;
  transition: height 0.3s;
}

.friend-item:hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.08) 0%, rgba(102, 126, 234, 0.03) 100%);
  transform: translateX(3px);
}

.friend-item:hover::before {
  height: 60%;
}

.friend-item.active {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.15) 0%, rgba(102, 126, 234, 0.05) 100%);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.friend-item.active::before {
  height: 70%;
}

.friend-item >>> .el-avatar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid white;
  transition: all 0.3s;
}

.friend-item:hover >>> .el-avatar {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.friend-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
  min-width: 0;
}

.friend-name {
  font-size: 15px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.friend-status {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
}

.friend-item.active .friend-name {
  color: #667eea;
  font-weight: 600;
}

.unread-badge {
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  border-radius: 11px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(255, 107, 107, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.empty-list {
  padding: 60px 20px;
  text-align: center;
  color: #909399;
}

.empty-list p {
  font-size: 14px;
  margin-top: 10px;
}
</style>

