import request from '../utils/request'

/**
 * 群组相关API
 */

// 获取群组列表
export function getGroupList() {
  return request({
    url: '/group/list',
    method: 'get'
  })
}

// 创建群组
export function createGroup(data) {
  return request({
    url: '/group/create',
    method: 'post',
    data
  })
}

// 加入群组
export function joinGroup(groupId) {
  return request({
    url: `/group/join/${groupId}`,
    method: 'post'
  })
}

// 退出群组
export function exitGroup(groupId) {
  return request({
    url: `/group/exit/${groupId}`,
    method: 'delete'
  })
}

// 获取群组信息
export function getGroupInfo(groupId) {
  return request({
    url: `/group/info/${groupId}`,
    method: 'get'
  })
}

// 获取群成员列表
export function getGroupMembers(groupId) {
  return request({
    url: `/group/members/${groupId}`,
    method: 'get'
  })
}

// 更新群组信息
export function updateGroupInfo(groupId, data) {
  return request({
    url: `/group/update/${groupId}`,
    method: 'put',
    data
  })
}

