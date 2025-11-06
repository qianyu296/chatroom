<template>
  <div class="group-list">
    <div class="group-list-header">
      <span>群组列表</span>
      <el-button
        type="text"
        icon="el-icon-plus"
        size="small"
        @click="$emit('create-group')"
      >
        创建群组
      </el-button>
    </div>
    
    <div class="group-list-content">
      <div
        v-for="group in filteredGroupList"
        :key="group.id"
        class="group-item"
        :class="{ active: currentChat?.type === 'group' && currentChat?.id === group.id }"
        @click="$emit('select-group', group)"
      >
        <el-avatar :size="40" :src="group.avatar">
          {{ group.name?.charAt(0) || 'G' }}
        </el-avatar>
        <div class="group-info">
          <div class="group-name">{{ group.name }}</div>
          <div class="group-desc">{{ group.description || '群组' }}</div>
        </div>
        <div
          v-if="unreadCounts[group.id] > 0"
          class="unread-badge"
        >
          {{ unreadCounts[group.id] > 99 ? '99+' : unreadCounts[group.id] }}
        </div>
      </div>
      
      <div v-if="filteredGroupList.length === 0" class="empty-list">
        <p>暂无群组</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'GroupList',
  props: {
    groupList: {
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
    
    filteredGroupList() {
      // TODO: 根据搜索关键词过滤
      return this.groupList
    }
  }
}
</script>

<style scoped>
.group-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

.group-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: white;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.group-list-header >>> .el-button {
  color: #667eea;
  font-weight: 500;
  padding: 5px 10px;
}

.group-list-header >>> .el-button:hover {
  background: rgba(102, 126, 234, 0.1);
}

.group-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 5px 0;
}

.group-list-content::-webkit-scrollbar {
  width: 6px;
}

.group-list-content::-webkit-scrollbar-track {
  background: transparent;
}

.group-list-content::-webkit-scrollbar-thumb {
  background: #e4e7ed;
  border-radius: 3px;
}

.group-list-content::-webkit-scrollbar-thumb:hover {
  background: #c1c1c1;
}

.group-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  margin: 2px 10px;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.group-item::before {
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

.group-item:hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.08) 0%, rgba(102, 126, 234, 0.03) 100%);
  transform: translateX(3px);
}

.group-item:hover::before {
  height: 60%;
}

.group-item.active {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.15) 0%, rgba(102, 126, 234, 0.05) 100%);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.group-item.active::before {
  height: 70%;
}

.group-item >>> .el-avatar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid white;
  transition: all 0.3s;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.group-item:hover >>> .el-avatar {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.group-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
  min-width: 0;
}

.group-name {
  font-size: 15px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-item.active .group-name {
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

