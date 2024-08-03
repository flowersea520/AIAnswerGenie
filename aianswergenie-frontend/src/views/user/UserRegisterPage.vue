<template>
  <h2 style="margin-bottom: 16px">用户注册</h2>
  <a-form
    style="max-width: 480px; margin: 0 auto"
    label-align="left"
    auto-label-width
    :model="form"
    @submit="handleSubmit"
  >
    <a-form-item field="userAccount" label="账号">
      <a-input v-model="form.userAccount" placeholder="请输入账号" />
    </a-form-item>
    <a-form-item field="userPassword" tooltip="密码不小于8位" label="密码">
      <a-input-password
        v-model="form.userPassword"
        placeholder="请输入你的密码"
      />
    </a-form-item>
    <a-form-item field="checkPassword" tooltip="密码不小于8位" label="确认密码">
      <a-input-password
        v-model="form.checkPassword"
        placeholder="请再输入一遍你的密码"
      />
    </a-form-item>
    <a-form-item>
      <!-- Flex Container -->
      <div class="flex-container">
        <a-button
          type="primary"
          html-type="submit"
          style="width: 120px; margin-right: 16px"
        >
          注册
        </a-button>
        <a-link href="/user/login" class="login-link">老用户登录</a-link>
      </div>
    </a-form-item>
  </a-form>
  {{ form }}
</template>

<script setup lang="ts">
import { reactive } from "vue";
import API from "@/servers/api";
import {
  userLoginUsingPost,
  userRegisterUsingPost,
} from "@/servers/api/userController";
import { useLoginUserStore } from "@/store/userStore";
import { Message } from "@arco-design/web-vue";
import { useRouter } from "vue-router";

const router = useRouter();
// reactive 和ref一样都是响应式的
const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
} as API.UserRegisterRequest);
const handleSubmit = async () => {
  const res = await userRegisterUsingPost(form);
  if (res.data.code === 0 && res.data) {
    // 注册页面不需要将用户信息保存到loginUserStore存储中，注册才要
    Message.success("注册成功");
    // 注册成功，一定是让他跳转到登录页，让用户登录
    router.push({
      path: "/user/login",
      //  replace 属性设置为 true，那么当前导航会替换当前历史记录中的条目，而不是添加一个新的历史记录条目。这意味着用户无法通过“前进”按钮再次返回到上一个路由页面。
      // 只要是注册/注册页面就设置为true
      replace: true,
    });
  } else {
    Message.error("注册失败" + res.data.message);
  }
  console.log(res);
};
</script>
<style scoped>
.flex-container {
  display: flex;
  align-items: center; /* 垂直居中 */
}

.login-link {
  margin-left: auto; /* 推到容器右侧 */
  padding-left: 180px; /* 在链接和按钮之间增加一些空间 */
  text-decoration: none; /* 去除下划线 */
}

/* 如果需要，可以添加额外的样式来调整链接的外观 */
</style>
