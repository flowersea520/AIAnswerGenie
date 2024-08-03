<template>
  <div id="homePage">
    <div class="searchBar">
      <a-input-search
        :style="{ width: '320px' }"
        placeholder="快速发现答题应用"
        button-text="搜索"
        size="large"
        search-button
      />
    </div>
    <!--    我们这个主页的page页面就是显示多个app应用的-->
    <a-list
      class="list-demo-action-layout"
      :grid-props="{ gutter: [20, 20], sm: 24, md: 12, lg: 8, xl: 6 }"
      :bordered="false"
      :data="dataList"
      :pagination-props="{
        pageSize: searchParams.pageSize,
        total,
      }"
    >
      <template #item="{ item }">
        <!--        :app 表示这是一个动态属性绑定，它将父组件传递的 item 数据传递给子组件 AppCard 的 app 属性。所以，确实 app 是子组件 AppCard 的一个属性。-->
        <AppCard :app="item" />
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import AppCard from "@/components/AppCard.vue";
import { ref, watchEffect } from "vue";
import { listAppVoByPageUsingPost } from "@/servers/api/appController";
import message from "@arco-design/web-vue/es/message";
import { REVIEW_STATUS_ENUM } from "@/constant/app";
import API from "@/servers/api";
import ShareModal from "@/components/ShareModal.vue";
// 初始化搜索条件（不应该被修改）
const initSearchParams = {
  current: 1,
  pageSize: 12,
};

const searchParams = ref<API.AppQueryRequest>({
  ...initSearchParams,
});
const dataList = ref<API.AppVO[]>([]);
const total = ref<number>(0);

/**
 * 加载数据
 */
const loadData = async () => {
  // 这个参数就是为了查询 通过的App应用
  const params = {
    // 这里不用initParams的原因就是它不是响应式的，它变化我们这个函数不会重新执行，选择用searchParams.value可以保证其变化了会执行该loadData方法
    ...searchParams.value,
    reviewStatus: REVIEW_STATUS_ENUM.PASS,
  };
  const res = await listAppVoByPageUsingPost(params);
  if (res.data.code === 0) {
    dataList.value = res.data.data?.records || [];
    total.value = res.data.data?.total || 0;
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

/**
 * 当分页变化时，改变搜索条件，触发数据加载
 * @param page
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

/**
 * 监听 loadData函数中的 响应式 变量的变化，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});
const names = ["Socrates", "Balzac", "Plato"];
</script>

<style scoped>
#homePage {
}

.searchBar {
  margin-bottom: 28px;
  text-align: center;
}

.list-demo-action-layout .image-area {
  width: 183px;
  height: 119px;
  overflow: hidden;
  border-radius: 2px;
}

.list-demo-action-layout .list-demo-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--color-fill-3);
}

.list-demo-action-layout .image-area img {
  width: 100%;
}

.list-demo-action-layout .arco-list-item-action .arco-icon {
  margin: 0 4px;
}
</style>
