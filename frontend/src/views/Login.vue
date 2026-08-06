<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">进销存管理系统</h2>
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input placeholder="请输入用户名（演示系统暂不校验）" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" placeholder="请输入密码（演示系统暂不校验）" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

function handleLogin() {
  // 演示系统暂不接入认证，直接进入首页
  ElMessage.success('登录成功（演示模式）')
  router.push('/product')
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
}

.login-card {
  width: 420px;
  padding: 20px 10px;

  .login-title {
    text-align: center;
    margin-bottom: 24px;
    color: #303133;
  }
}
</style>
