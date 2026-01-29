<template>
  <div id="userRegisterPage">
    <h2 class="title">AI 应用生成 - 用户注册</h2>
    <div class="desc">不写一行代码，生成完整应用</div>

    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <!-- 账号 -->
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <!-- 密码 -->
      <a-form-item name="userPassword" :rules="[{ required: true, message: '请输入密码' }, { min: 8, message: '密码不能小于 8 位' }]">
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <!-- 确认密码（带自定义校验） -->
      <a-form-item name="checkPassword" :rules="[{ required: true, message: '请确认密码' }, { min: 8, message: '密码不能小于 8 位' }, { validator: validateCheckPassword }]">
        <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
      </a-form-item>

      <div class="tips">
        已有账号？ <RouterLink to="/user/login">去登录</RouterLink>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const router = useRouter()

// 表单数据（响应式）
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

// 自定义校验：确认密码必须和密码一致
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单提交回调（验证通过后执行）
const handleSubmit = async (values: API.UserRegisterRequest) => {
  try {
    const res = await userRegister(values)

    if (res.data.code === 0) {           // 假设后端约定 code=0 表示成功
      message.success('注册成功')
      router.push({ path: '/user/login', replace: true })  // replace: true 替换历史记录
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } catch (error) {
    // 实际项目中建议在这里捕获网络错误等
    message.error('注册请求失败')
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
