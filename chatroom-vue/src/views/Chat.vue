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

      <!-- 好友申请按钮 -->
      <div class="request-button">
        <el-button
          type="primary"
          plain
          size="small"
          @click="showFriendRequestList = true"
        >
          好友申请
          <el-badge
            v-if="receivedRequests.length > 0"
            :value="receivedRequests.length"
            :max="99"
            class="badge"
          />
        </el-button>
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

    <!-- 好友申请列表对话框 -->
    <FriendRequestList
      :visible.sync="showFriendRequestList"
      @request-accepted="handleFriendAdded"
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
import FriendRequestList from '@/components/FriendRequestList.vue'
import wsManager from '@/utils/websocket'

export default {
  name: 'ChatRoom',
  components: {
    FriendList,
    GroupList,
    ChatWindow,
    AddFriendDialog,
    CreateGroupDialog,
    FriendRequestList
  },
  data() {
    return {
      activeTab: 'friends',
      searchKeyword: '',
      currentChat: null, // { type: 'friend' | 'group', id, name, avatar }
      showAddFriendDialog: false,
      showCreateGroupDialog: false,
      showFriendRequestList: false,
      wsOpenListener: null
    }
  },
  computed: {
    ...mapState('user', ['userInfo']),
    ...mapState('friend', ['friendList', 'receivedRequests']),
    ...mapState('group', ['groupList']),
    ...mapState('message', ['privateMessages', 'groupMessages']),
    
    userAvatar() {
      return this.userInfo?.avatar || ''
    },

    currentMessages() {
      if (!this.currentChat) return []

      if (this.currentChat.type === 'friend') {
        const key = String(this.currentChat.id)
        return this.privateMessages[key] || []
      } else {
        return this.groupMessages[String(this.currentChat.id)] || []
      }
    }
  },
  async mounted() {
    // 检查登录状态
    if (!this.$store.state.user.isLoggedIn) {
      this.$router.push('/login')
      return
    }

    // 注册WebSocket消息处理器
    this.setupWebSocket()

    // 初始化数据
    await this.initData()

    // 设置WebSocket连接成功后的回调
    this.wsOpenListener = () => {
      this.subscribeMessages()
    }
    wsManager.addOpenListener(this.wsOpenListener)

    // 如果WebSocket已经连接，立即订阅
    if (wsManager.isConnected()) {
      this.subscribeMessages()
    }
  },
  beforeDestroy() {
    // 移除WebSocket消息处理器
    wsManager.offMessage('PRIVATE_MESSAGE')
    wsManager.offMessage('GROUP_MESSAGE')
    wsManager.offMessage('FRIEND_REQUEST')
    wsManager.offMessage('FRIEND_REQUEST_ACCEPTED')
    wsManager.offMessage('FRIEND_REQUEST_REJECTED')
    wsManager.offMessage('PRESENCE_CHANGE')
    if (this.wsOpenListener) {
      wsManager.removeOpenListener(this.wsOpenListener)
      this.wsOpenListener = null
    }
  },
  methods: {
    async initData() {
      try {
        // 获取好友列表
        await this.$store.dispatch('friend/getFriendList')
        // 获取群组列表
        await this.$store.dispatch('group/getGroupList')
        // 获取收到的好友申请
        await this.$store.dispatch('friend/getReceivedRequests')
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

      // 注册好友申请消息处理器
      wsManager.onMessage('FRIEND_REQUEST', () => {
        Message.success('收到新的好友申请')
        // 可以触发刷新好友申请列表
        this.$store.dispatch('friend/getReceivedRequests')
      })

      wsManager.onMessage('FRIEND_REQUEST_ACCEPTED', () => {
        Message.success('您的好友申请已被接受')
        // 刷新好友列表
        this.$store.dispatch('friend/getFriendList')
      })

      wsManager.onMessage('FRIEND_REQUEST_REJECTED', () => {
        Message.warning('您的好友申请被拒绝了')
      })

      // 注册好友状态变化处理器
      wsManager.onMessage('PRESENCE_CHANGE', (data) => {
        const { userId, status } = data
        const newStatus = status === '在线' ? '在线' : '离线'
        // 更新好友列表中对应好友的状态
        this.$store.commit('friend/UPDATE_FRIEND_STATUS', {
          friendId: userId,
          status: newStatus
        })
      })
    },

    subscribeMessages() {
      const userId = this.$store.state.user.userInfo?.id
      if (!userId) {
        return
      }

      // 取消之前的订阅（避免重复订阅）
      wsManager.unsubscribe('/user/queue/private')
      wsManager.unsubscribe('/user/queue/friendRequest')
      wsManager.unsubscribe('/user/queue/presence')

      // 订阅私聊消息
      wsManager.subscribe('/user/queue/private', (data) => {
        wsManager.handleMessage(data)
      })
      // 订阅好友申请队列
      wsManager.subscribe('/user/queue/friendRequest', (data) => {
        wsManager.handleMessage(data)
      })
      // 订阅好友状态变化
      wsManager.subscribe('/user/queue/presence', (data) => {
        wsManager.handleMessage(data)
        // 更新好友状态
        if (data.type === 'PRESENCE_CHANGE') {
          this.$store.commit('friend/UPDATE_FRIEND_STATUS', {
            friendId: data.userId,
            status: data.status === '在线' ? '在线' : '离线'
          })
        }
      })
    },
    
    handleSelectFriend(friend) {
      // 取消之前订阅的群聊消息
      if (this.currentChat?.type === 'group' && this.currentChat?.id) {
        wsManager.unsubscribe(`/topic/group/${String(this.currentChat.id)}`)
      }

      this.currentChat = {
        type: 'friend',
        id: friend.id,
        name: friend.nickname,
        avatar: friend.avatar,
        status: friend.status || '离线'
      }
      this.$store.commit('friend/SET_CURRENT_CHAT_FRIEND', friend)

      // 加载消息历史
      this.loadMessages()

      // 清除未读消息数
      this.$store.commit('message/CLEAR_UNREAD_COUNT', friend.id)
    },

    handleSelectGroup(group) {
      // 取消之前订阅的群聊消息
      if (this.currentChat?.type === 'group' && this.currentChat?.id) {
        wsManager.unsubscribe(`/topic/group/${String(this.currentChat.id)}`)
      }

      this.currentChat = {
        type: 'group',
        id: group.id,
        name: group.name,
        avatar: group.avatar
      }
      this.$store.commit('group/SET_CURRENT_CHAT_GROUP', group)

      // 订阅该群聊的消息
      wsManager.subscribe(`/topic/group/${String(group.id)}`, (data) => {
        wsManager.handleMessage(data)
      })

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
/* Clean Minimal White Theme */
.chat-container {
  display: flex;
  height: 100vh;
  background: #f8fafc;
  position: relative;
  overflow: hidden;
}

/* Left Sidebar */
.chat-sidebar {
  width: 300px;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

/* User Info Header */
.user-info {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.user-avatar {
  margin-right: 12px;
  position: relative;
}

.user-avatar .el-avatar {
  background: #0ea5e9;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
}

.user-status::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #22c55e;
  border-radius: 50%;
  margin-right: 6px;
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

/* Search Box */
.search-box {
  padding: 12px 16px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.search-box >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding-left: 12px;
  font-size: 14px;
  background: #f8fafc;
  transition: all 0.2s;
}

.search-box >>> .el-input__inner:focus {
  border-color: #0ea5e9;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

.search-box >>> .el-input__prefix {
  color: #94a3b8;
}

/* Request Button */
.request-button {
  padding: 12px 16px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.request-button >>> .el-button {
  width: 100%;
  border-radius: 10px;
  border-color: #e2e8f0;
  color: #64748b;
  background: #f8fafc;
  font-weight: 500;
  transition: all 0.2s;
}

.request-button >>> .el-button:hover {
  border-color: #0ea5e9;
  color: #0ea5e9;
  background: #f0f9ff;
}

.request-button >>> .badge {
  margin-left: 8px;
}

.request-button >>> .el-badge__content {
  background: #ef4444;
}

/* Tabs */
.chat-tabs {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.chat-tabs >>> .el-tabs__header {
  margin: 0;
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.chat-tabs >>> .el-tabs__nav-wrap::after {
  display: none;
}

.chat-tabs >>> .el-tabs__item {
  font-weight: 500;
  color: #94a3b8;
  font-size: 14px;
  transition: all 0.2s;
}

.chat-tabs >>> .el-tabs__item.is-active {
  color: #0ea5e9;
  font-weight: 600;
}

.chat-tabs >>> .el-tabs__active-bar {
  background: #0ea5e9;
  height: 2px;
  border-radius: 2px;
}

.chat-tabs >>> .el-tabs__content {
  flex: 1;
  overflow: hidden;
}

.chat-tabs >>> .el-tab-pane {
  height: 100%;
  overflow: hidden;
}

/* Main Chat Area */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  position: relative;
  z-index: 1;
}

/* Empty State */
.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  color: #94a3b8;
}

.empty-chat i {
  font-size: 64px;
  margin-bottom: 16px;
  color: #cbd5e1;
}

.empty-chat p {
  font-size: 15px;
  color: #64748b;
  font-weight: 500;
}
</style>

