<template>
  <div class="chat-container">
    <!-- 左侧边栏 -->
    <div class="chat-sidebar">
      <!-- 用户信息 -->
      <div class="user-info">
        <div class="user-avatar">
          <el-avatar :size="40" :src="userAvatar">
            {{ userInfo?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
        <div class="user-details">
          <div class="user-name">{{ userInfo?.nickname || '用户' }}</div>
          <div class="user-status">在线</div>
        </div>
        <el-dropdown @command="handleUserCommand">
          <span class="el-dropdown-link">
            <i class="el-icon-more"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索好友或群组"
          prefix-icon="el-icon-search"
          size="small"
        />
      </div>
      
      <!-- 标签页：好友/群组 -->
      <el-tabs v-model="activeTab" class="chat-tabs">
        <el-tab-pane label="好友" name="friends">
          <FriendList
            :friend-list="friendList"
            :current-chat="currentChat"
            @select-friend="handleSelectFriend"
            @add-friend="showAddFriendDialog = true"
          />
        </el-tab-pane>
        <el-tab-pane label="群组" name="groups">
          <GroupList
            :group-list="groupList"
            :current-chat="currentChat"
            @select-group="handleSelectGroup"
            @create-group="showCreateGroupDialog = true"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <ChatWindow
        v-if="currentChat"
        :chat-info="currentChat"
        :messages="currentMessages"
        @send-message="handleSendMessage"
      />
      <div v-else class="empty-chat">
        <i class="el-icon-chat-line-round"></i>
        <p>选择一个好友或群组开始聊天</p>
      </div>
    </div>
    
    <!-- 添加好友对话框 -->
    <AddFriendDialog
      :visible="showAddFriendDialog"
      @close="showAddFriendDialog = false"
      @success="handleFriendAdded"
    />
    
    <!-- 创建群组对话框 -->
    <CreateGroupDialog
      :visible="showCreateGroupDialog"
      @close="showCreateGroupDialog = false"
      @success="handleGroupCreated"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { Message } from 'element-ui'
import FriendList from '@/components/FriendList.vue'
import GroupList from '@/components/GroupList.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import AddFriendDialog from '@/components/AddFriendDialog.vue'
import CreateGroupDialog from '@/components/CreateGroupDialog.vue'
import wsManager from '@/utils/websocket'

export default {
  name: 'ChatRoom',
  components: {
    FriendList,
    GroupList,
    ChatWindow,
    AddFriendDialog,
    CreateGroupDialog
  },
  data() {
    return {
      activeTab: 'friends',
      searchKeyword: '',
      currentChat: null, // { type: 'friend' | 'group', id, name, avatar }
      showAddFriendDialog: false,
      showCreateGroupDialog: false
    }
  },
  computed: {
    ...mapState('user', ['userInfo']),
    ...mapState('friend', ['friendList']),
    ...mapState('group', ['groupList']),
    ...mapState('message', ['privateMessages', 'groupMessages']),
    
    userAvatar() {
      return this.userInfo?.avatar || ''
    },
    
    currentMessages() {
      if (!this.currentChat) return []
      
      if (this.currentChat.type === 'friend') {
        return this.privateMessages[this.currentChat.id] || []
      } else {
        return this.groupMessages[this.currentChat.id] || []
      }
    }
  },
  async mounted() {
    // 检查登录状态
    if (!this.$store.state.user.isLoggedIn) {
      this.$router.push('/login')
      return
    }
    
    // 初始化数据
    await this.initData()
    
    // 注册WebSocket消息处理器
    this.setupWebSocket()
  },
  beforeDestroy() {
    // 移除WebSocket消息处理器
    wsManager.offMessage('PRIVATE_MESSAGE')
    wsManager.offMessage('GROUP_MESSAGE')
  },
  methods: {
    async initData() {
      try {
        // 获取好友列表
        await this.$store.dispatch('friend/getFriendList')
        // 获取群组列表
        await this.$store.dispatch('group/getGroupList')
      } catch (error) {
        // 如果后端不可用，使用模拟数据
        if (error.message && error.message.includes('Network Error')) {
          console.warn('后端不可用，使用模拟数据')
          this.loadMockData()
        } else {
          Message.error('加载数据失败')
        }
      }
    },
    
    loadMockData() {
      // 模拟好友数据
      const mockFriends = [
        {
          id: 2,
          nickname: '好友1',
          avatar: '',
          status: '在线'
        },
        {
          id: 3,
          nickname: '好友2',
          avatar: '',
          status: '离线'
        }
      ]
      this.$store.commit('friend/SET_FRIEND_LIST', mockFriends)
      
      // 模拟群组数据
      const mockGroups = [
        {
          id: 1,
          name: '技术交流群',
          description: '技术讨论',
          avatar: '',
          memberCount: 10
        },
        {
          id: 2,
          name: '闲聊群',
          description: '日常聊天',
          avatar: '',
          memberCount: 5
        }
      ]
      this.$store.commit('group/SET_GROUP_LIST', mockGroups)
    },
    
    setupWebSocket() {
      // 注册私聊消息处理器
      wsManager.onMessage('PRIVATE_MESSAGE', (data) => {
        this.$store.dispatch('message/receiveMessage', data)
      })
      
      // 注册群聊消息处理器
      wsManager.onMessage('GROUP_MESSAGE', (data) => {
        this.$store.dispatch('message/receiveMessage', data)
      })
    },
    
    handleSelectFriend(friend) {
      this.currentChat = {
        type: 'friend',
        id: friend.id,
        name: friend.nickname,
        avatar: friend.avatar
      }
      this.$store.commit('friend/SET_CURRENT_CHAT_FRIEND', friend)
      
      // 加载消息历史
      this.loadMessages()
      
      // 清除未读消息数
      this.$store.commit('message/CLEAR_UNREAD_COUNT', friend.id)
    },
    
    handleSelectGroup(group) {
      this.currentChat = {
        type: 'group',
        id: group.id,
        name: group.name,
        avatar: group.avatar
      }
      this.$store.commit('group/SET_CURRENT_CHAT_GROUP', group)
      
      // 加载消息历史
      this.loadMessages()
      
      // 清除未读消息数
      this.$store.commit('message/CLEAR_UNREAD_COUNT', group.id)
    },
    
    async loadMessages() {
      if (!this.currentChat) return
      
      try {
        if (this.currentChat.type === 'friend') {
          await this.$store.dispatch('message/getPrivateMessages', {
            friendId: this.currentChat.id
          })
        } else {
          await this.$store.dispatch('message/getGroupMessages', {
            groupId: this.currentChat.id
          })
        }
      } catch (error) {
        Message.error('加载消息失败')
      }
    },
    
    handleSendMessage(content) {
      if (!this.currentChat) return
      
      if (this.currentChat.type === 'friend') {
        this.$store.dispatch('message/sendPrivateMessage', {
          friendId: this.currentChat.id,
          content
        })
      } else {
        this.$store.dispatch('message/sendGroupMessage', {
          groupId: this.currentChat.id,
          content
        })
      }
    },
    
    handleFriendAdded() {
      this.$store.dispatch('friend/getFriendList')
    },
    
    handleGroupCreated() {
      this.$store.dispatch('group/getGroupList')
    },
    
    handleUserCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            await this.$store.dispatch('user/logout')
            this.$router.push('/login')
            Message.success('已退出登录')
          } catch (error) {
            Message.error('退出登录失败')
          }
        })
      } else if (command === 'profile') {
        // TODO: 打开个人资料页面
        Message.info('个人资料功能开发中')
      }
    }
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.chat-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(102, 126, 234, 0.3) 0%, transparent 50%);
  pointer-events: none;
}

.chat-sidebar {
  width: 320px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  margin-right: 12px;
  position: relative;
}

.user-avatar::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #67c23a;
  border: 2px solid white;
  border-radius: 50%;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
}

.user-status {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
}

.user-status::before {
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
  color: rgba(255, 255, 255, 0.9);
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s;
}

.el-dropdown-link:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

.search-box {
  padding: 15px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.search-box >>> .el-input__inner {
  border-radius: 20px;
  border: 1px solid #e4e7ed;
  padding-left: 35px;
  transition: all 0.3s;
}

.search-box >>> .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.chat-tabs {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-tabs >>> .el-tabs__header {
  margin: 0;
  padding: 0 15px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.chat-tabs >>> .el-tabs__nav-wrap::after {
  display: none;
}

.chat-tabs >>> .el-tabs__item {
  font-weight: 500;
  color: #909399;
  transition: all 0.3s;
}

.chat-tabs >>> .el-tabs__item.is-active {
  color: #667eea;
  font-weight: 600;
}

.chat-tabs >>> .el-tabs__active-bar {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  height: 3px;
}

.chat-tabs >>> .el-tabs__content {
  flex: 1;
  overflow: hidden;
}

.chat-tabs >>> .el-tab-pane {
  height: 100%;
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin: 20px;
  margin-left: 0;
  position: relative;
  z-index: 1;
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  color: #909399;
}

.empty-chat i {
  font-size: 80px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  opacity: 0.6;
}

.empty-chat p {
  font-size: 18px;
  color: #909399;
  font-weight: 500;
}
</style>

