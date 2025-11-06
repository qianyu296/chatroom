import Vue from 'vue'
import Vuex from 'vuex'
import user from './modules/user'
import friend from './modules/friend'
import message from './modules/message'
import group from './modules/group'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    user,
    friend,
    message,
    group
  }
})

