<template>
  <h2 style="margin-bottom: 16px">用户登录</h2>
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
    <a-form-item>
      <!-- Flex Container -->
      <div class="flex-container">
        <a-button
          type="primary"
          html-type="submit"
          style="width: 120px; margin-right: 16px"
        >
          登录
        </a-button>
        <a-link href="/user/register" class="register-link">新用户注册</a-link>
      </div>
    </a-form-item>
  </a-form>
  {{ form }}
</template>

<script setup lang="ts">
import { reactive } from "vue";
import API from "@/servers/api";
import { userLoginUsingPost } from "@/servers/api/userController";
import { useLoginUserStore } from "@/store/userStore";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

const loginUserStore = useLoginUserStore();
const router = useRouter();

const form = reactive({
  userAccount: "",
  userPassword: "",
} as API.UserLoginRequest);

/**
 * 提交
 */
const handleSubmit = async () => {
  const res = await userLoginUsingPost(form);
  if (res.data.code === 0) {
    await loginUserStore.fetchLoginUser();
    message.success("登录成功");
    router.push({
      path: "/",
      replace: true,
    });
  } else {
    message.error("登录失败，" + res.data.message);
  }
};
</script>
<style scoped>
/* Flex 容器设置 */
.flex-container {
  display: flex;
  align-items: center; /* 确保按钮和链接垂直对齐 */
  justify-content: flex-start; /* 确保内容左对齐 */
}

/* 登录按钮样式 */
.flex-container a-button {
  width: 120px;
  margin-right: 24px; /* 增加与链接的距离 */
}

/* 新用户注册链接样式 */
.register-link {
  margin-left: auto; /* 推到容器右侧 */
  padding-left: 207px; /* 在链接和按钮之间增加一些空间 */
  text-decoration: none; /* 去除下划线 */
}
</style>
