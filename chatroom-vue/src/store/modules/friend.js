import { getFriendList, getFriendGroups, addFriend, deleteFriend } from '@/api/friend'

const state = {
  friendList: [],
  friendGroups: [],
  currentChatFriend: null
}

const mutations = {
  SET_FRIEND_LIST(state, list) {
    state.friendList = list
  },
  SET_FRIEND_GROUPS(state, groups) {
    state.friendGroups = groups
  },
  ADD_FRIEND(state, friend) {
    state.friendList.push(friend)
  },
  REMOVE_FRIEND(state, friendId) {
    state.friendList = state.friendList.filter(f => f.id !== friendId)
  },
  SET_CURRENT_CHAT_FRIEND(state, friend) {
    state.currentChatFriend = friend
  }
}

const actions = {
  // 获取好友列表
  async getFriendList({ commit }) {
    const res = await getFriendList()
    if (res.data) {
      commit('SET_FRIEND_LIST', res.data)
    }
    return res
  },

  // 获取好友分组
  async getFriendGroups({ commit }) {
    const res = await getFriendGroups()
    if (res.data) {
      commit('SET_FRIEND_GROUPS', res.data)
    }
    return res
  },

  // 添加好友
  async addFriend({ commit }, data) {
    const res = await addFriend(data)
    if (res.data) {
      commit('ADD_FRIEND', res.data)
    }
    return res
  },

  // 删除好友
  async deleteFriend({ commit }, friendId) {
    await deleteFriend(friendId)
    commit('REMOVE_FRIEND', friendId)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

