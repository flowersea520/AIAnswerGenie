<template>
  <a-form
    :model="formSearchParams"
    :style="{ marginBottom: '20px' }"
    layout="inline"
    @submit="doSearch"
  >
    <a-form-item field="userName" label="用户名">
      <a-input
        allow-clear
        v-model="formSearchParams.userName"
        placeholder="请输入用户名"
      />
    </a-form-item>
    <a-form-item field="userProfile" label="用户简介">
      <a-input
        allow-clear
        v-model="formSearchParams.userProfile"
        placeholder="请输入用户简介"
      />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" html-type="submit" style="width: 100px">
        搜索
      </a-button>
    </a-form-item>
  </a-form>

  <a-table
    :columns="columns"
    :data="dataList"
    :pagination="{
      // 分页配置
      showTotal: true,
      current: initSearchParams.current, // 当前页码
      pageSize: initSearchParams.pageSize, // 每页显示的记录数
      total,
    }"
    @page-change="onPageChange"
  >
    <!--    通过 #columns 插槽和 <a-table-column> 组件可以使用模板的方法自定义列渲染。
    注意：在使用 #columns 插槽后，将会屏蔽 columns 属性
    record 就是当前插槽这一行的记录对象
    -->
    <template #userAvatar="{ record }">
      <a-image width="64" :src="record.userAvatar" />
    </template>
    <template #createTime="{ record }">
      {{ dayjs(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
    </template>
    <template #updateTime="{ record }">
      {{ dayjs(record.updateTime).format("YYYY-MM-DD HH:mm:ss") }}
    </template>
    <template #optional="{ record }">
      <a-space>
        <a-button status="danger" @click="doDelete(record)">删除</a-button>
      </a-space>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import { reactive, ref, watchEffect } from "vue";
import {
  deleteUserUsingPost,
  listUserByPageUsingPost,
} from "@/servers/api/userController";
import message from "@arco-design/web-vue/es/message";
import API from "@/servers/api";
import { dayjs } from "@arco-design/web-vue/es/_utils/date";
// 这里的响应数据和表单的输入框进行了绑定，在输入框中输入数据，这里就可以获取到
// 将formSearchParams传给表单标签对象，可以变相的实现防抖（就是不会每次输入一个字就向后端发送一次请求，因为我们用了watchEffect函数钩子）
const formSearchParams = ref<API.UserQueryRequest>({});
// 初始化搜索条件（不应该被修改）
const initSearchParams = {
  current: 1,
  pageSize: 10,
};

const searchParams = ref<API.UserQueryRequest>({ ...initSearchParams });
// 对应这mybatisplus中page对象的records，当页的所有对象的集合
const dataList = ref<API.User[]>([]);
const total = ref(0);

/**
 * 加载数据
 */
const loadData = async () => {
  const res = await listUserByPageUsingPost(searchParams.value);
  if (res.data.code === 0) {
    dataList.value = res.data.data?.records || [];
    console.log(dataList.value, "参考");
    total.value = res.data.data?.total || 0;
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

/**
 * 执行搜索
 */
const doSearch = () => {
  searchParams.value = {
    ...initSearchParams,
    // 表单搜索框中 输入的值，都会传入后端，变成搜索条件，进行查询（如果是字符串就是模糊查询）
    ...formSearchParams.value,
  };
};

/**
 *  点击页面跳转（当分页变化时，改变搜索条件，触发数据加载）
 * @param page
 */
const onPageChange = (page: number) => {
  console.log("测试", page);
  searchParams.value = {
    // 把之前的老值init属性先复制过来
    ...searchParams.value,
    // 相同的属性进行覆盖
    current: page,
  };
};
/**
 *  删除页面用户记录
 * @param record
 */
const doDelete = async (record: API.User) => {
  const res = await deleteUserUsingPost({
    id: record.id,
  });
  if (res.data.code === 0) {
    // 删除成功，重新渲染页面
    loadData();
    message.success("删除成功");
  } else {
    message.error("删除失败，" + res.data.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载（监听响应式数据的改变）
 * watchEffect函数内部调用了loadData函数，因此它会监视loadData函数内部使用的响应式数据，以便在这些数据改变时重新调用loadData函数。
 *
 * 这就意味着，如果loadData函数内部使用了响应式数据，比如searchParams.value、dataList.value、total.value等，当这些响应式数据发生变化时，watchEffect会自动触发loadData函数的重新执行，以重新加载数据并更新界面。
 */
watchEffect(() => {
  loadData();
});
// 表格列配置
const columns = [
  {
    title: "id",
    dataIndex: "id",
  },
  {
    title: "账号",
    dataIndex: "userAccount",
  },
  {
    title: "用户名",
    dataIndex: "userName",
  },
  // 利用了插槽，相对于图片这一列的数据我们自定义渲染
  {
    title: "用户头像",
    dataIndex: "userAvatar",
    slotName: "userAvatar",
  },
  {
    title: "用户简介",
    dataIndex: "userProfile",
  },
  {
    title: "权限",
    dataIndex: "userRole",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];
</script>
