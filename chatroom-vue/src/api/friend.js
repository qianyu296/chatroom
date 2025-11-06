import request from '../utils/request'

/**
 * 好友相关API
 */

// 获取好友列表
export function getFriendList() {
  return request({
    url: '/friend/list',
    method: 'get'
  })
}

// 获取好友分组列表
export function getFriendGroups() {
  return request({
    url: '/friend/groups',
    method: 'get'
  })
}

// 添加好友
export function addFriend(data) {
  return request({
    url: '/friend/add',
    method: 'post',
    data
  })
}

// 删除好友
export function deleteFriend(friendId) {
  return request({
    url: `/friend/delete/${friendId}`,
    method: 'delete'
  })
}

// 创建好友分组
export function createFriendGroup(data) {
  return request({
    url: '/friend/group/create',
    method: 'post',
    data
  })
}

// 更新好友分组
export function updateFriendGroup(groupId, data) {
  return request({
    url: `/friend/group/update/${groupId}`,
    method: 'put',
    data
  })
}

// 删除好友分组
export function deleteFriendGroup(groupId) {
  return request({
    url: `/friend/group/delete/${groupId}`,
    method: 'delete'
  })
}

// 移动好友到分组
export function moveFriendToGroup(data) {
  return request({
    url: '/friend/group/move',
    method: 'put',
    data
  })
}

// 获取好友申请列表
export function getFriendRequests() {
  return request({
    url: '/friend/requests',
    method: 'get'
  })
}

// 处理好友申请
export function handleFriendRequest(requestId, action) {
  return request({
    url: `/friend/request/${requestId}`,
    method: 'put',
    data: { action } // accept or reject
  })
}

