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
/* Clean Minimal White Group List */
.group-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.group-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #ffffff;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.group-list-header >>> .el-button {
  color: #64748b;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  transition: all 0.2s;
}

.group-list-header >>> .el-button:hover {
  background: #f1f5f9;
  color: #0ea5e9;
}

.group-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.group-list-content::-webkit-scrollbar {
  width: 4px;
}

.group-list-content::-webkit-scrollbar-track {
  background: transparent;
}

.group-list-content::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 2px;
}

.group-list-content::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}

.group-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin: 2px 8px;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s;
  position: relative;
}

.group-item:hover {
  background: #f8fafc;
}

.group-item.active {
  background: #f0f9ff;
}

.group-item >>> .el-avatar {
  background: #64748b;
  color: #ffffff;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
}

.group-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
  min-width: 0;
}

.group-name {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.group-desc {
  font-size: 12px;
  color: #94a3b8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-item.active .group-name {
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

