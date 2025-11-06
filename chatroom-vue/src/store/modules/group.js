import { getGroupList, createGroup, joinGroup, exitGroup } from '@/api/group'

const state = {
  groupList: [],
  currentChatGroup: null
}

const mutations = {
  SET_GROUP_LIST(state, list) {
    state.groupList = list
  },
  ADD_GROUP(state, group) {
    state.groupList.push(group)
  },
  REMOVE_GROUP(state, groupId) {
    state.groupList = state.groupList.filter(g => g.id !== groupId)
  },
  SET_CURRENT_CHAT_GROUP(state, group) {
    state.currentChatGroup = group
  }
}

const actions = {
  // 获取群组列表
  async getGroupList({ commit }) {
    const res = await getGroupList()
    if (res.data) {
      commit('SET_GROUP_LIST', res.data)
    }
    return res
  },

  // 创建群组
  async createGroup({ commit }, data) {
    const res = await createGroup(data)
    if (res.data) {
      commit('ADD_GROUP', res.data)
    }
    return res
  },

  // 加入群组
  async joinGroup({ commit }, groupId) {
    const res = await joinGroup(groupId)
    if (res.data) {
      commit('ADD_GROUP', res.data)
    }
    return res
  },

  // 退出群组
  async exitGroup({ commit }, groupId) {
    await exitGroup(groupId)
    commit('REMOVE_GROUP', groupId)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

