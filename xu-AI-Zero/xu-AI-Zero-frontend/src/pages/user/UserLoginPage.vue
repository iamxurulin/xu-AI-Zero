<template>
  <div id="userLoginPage">
    <h2 class="title">AI应用生成-用户登录</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          {required:true,message:'请输入密码'},
        {min:8,message:'密码不能小于8位'},
      ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import {reactive} from "vue";
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/loginUser.js";
import {userLogin} from "@/api/userController.js";
import {message} from "ant-design-vue";

const formState = reactive<API.UserLoginRequest>({
  userAccount:'',
  userPassword:'',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any)=>{
  const res = await userLogin(values)
  //登录成功，把登录态保存到全局状态中
  if(res.data.code === 0 && res.data.data){
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path:'/',
      replace:true,
    })
  }else{
    message.error('登录失败'+res.data.message)
  }
}
</script>

<style scoped>
/*使用了 <style scoped>，意味着这些样式只会在当前组件（登录页）生效，不会影响到项目中其他页面的样式*/

 #userLoginPage {
   max-width: 360px;  /* 限制最大宽度 */
   margin: 0 auto;    /* 水平居中 */
 }

.title {
  text-align: center;   /* 文字居中 */
  margin-bottom: 16px;  /* 下方留白 */
}

/*副标题（描述）样式*/
.desc {
  text-align: center;   /* 文字居中 */
  color: #bbb;          /* 浅灰色字体 */
  margin-bottom: 16px;  /* 下方留白 */
}

/*底部提示（去注册）样式*/
.tips {
  margin-bottom: 16px;
  color: #bbb;          /* 浅灰色字体 */
  font-size: 13px;      /* 小字号 */
  text-align: right;    /* 靠右对齐 */
}
</style>
