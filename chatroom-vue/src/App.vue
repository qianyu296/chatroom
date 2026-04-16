<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol',
    'Noto Color Emoji';
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* Global Scrollbar */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Element UI Dialog Global Override */
.el-dialog {
  border-radius: 16px !important;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1) !important;
}

.el-dialog__header {
  padding: 20px 24px 16px !important;
  border-bottom: 1px solid #f1f5f9;
}

.el-dialog__title {
  font-size: 16px !important;
  font-weight: 600 !important;
  color: #1e293b !important;
}

.el-dialog__headerbtn {
  top: 16px !important;
  right: 16px !important;
  width: 32px !important;
  height: 32px !important;
  border-radius: 8px !important;
  transition: all 0.2s !important;
}

.el-dialog__headerbtn:hover {
  background: #f1f5f9 !important;
}

.el-dialog__headerbtn .el-dialog__close {
  color: #64748b !important;
  font-size: 16px !important;
}

.el-dialog__body {
  padding: 24px !important;
  color: #1e293b !important;
}

.el-dialog__footer {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f1f5f9;
}

.el-overlay {
  background: rgba(0, 0, 0, 0.4) !important;
}

.el-message-box {
  border-radius: 16px !important;
  border: 1px solid #e2e8f0;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15) !important;
}
</style>
