import request from '../utils/request'

/**
 * 消息相关API
 */

// 获取私聊消息历史
export function getPrivateMessages(friendId, page = 1, pageSize = 50) {
  return request({
    url: '/message/private',
    method: 'get',
    params: { friendId, page, pageSize }
  })
}

// 获取群聊消息历史
export function getGroupMessages(groupId, page = 1, pageSize = 50) {
  return request({
    url: '/message/group',
    method: 'get',
    params: { groupId, page, pageSize }
  })
}

// 标记消息为已读
export function markMessageRead(messageId) {
  return request({
    url: `/message/read/${messageId}`,
    method: 'put'
  })
}

// 获取未读消息数
export function getUnreadCount() {
  return request({
    url: '/message/unread/count',
    method: 'get'
  })
}

