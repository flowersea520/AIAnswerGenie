<template>
  <div id="appStatisticPage">
    <!--    根据用户的回答数量来统计的-->
    <h2>热门应用统计</h2>
    <v-chart :option="appAnswerCountOptions" style="height: 300px" />
    <!--    根据生成的结果数量来统计的-->
    <h2>应用结果统计（这里给出示例appId为1的结果数统计）</h2>

    <div class="searchBar">
      <a-input-search
        :style="{ width: '320px' }"
        placeholder="输入 appId"
        button-text="搜索"
        search-button
        @search="(value) => loadAppAnswerResultCountData(value)"
      />
    </div>
    <div style="margin-bottom: 16px" />
    <v-chart :option="appAnswerResultCountOptions" style="height: 300px" />
  </div>
</template>

<script setup lang="ts">
import VChart from "vue-echarts";
import "echarts";
import { computed, ref, watchEffect } from "vue";
import API from "@/servers/api";
import message from "@arco-design/web-vue/es/message";
import {
  getAppAnswerCountUsingGet,
  getAnswerResultCountUsingGet,
} from "@/servers/api/appStatisticController";
// 应用 回答数统计（对对应着接收我们后端的List<AppAnswerCountDTO>数据）
const appAnswerCountList = ref<API.AppAnswerCountDTO[]>([]);
console.log(appAnswerCountList.value);
// 应用 回答结果统计
const appAnswerResultCountList = ref<API.AppAnswerResultCountDTO[]>([]);

/**
 * 加载数据
 */
const loadAppAnswerCountData = async () => {
  const res = await getAppAnswerCountUsingGet();
  if (res.data.code === 0) {
    appAnswerCountList.value = res.data.data || [];
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

/**
 * 加载数据
 */
const loadAppAnswerResultCountData = async (appId: string) => {
  if (!appId) {
    return;
  }
  const res = await getAnswerResultCountUsingGet({
    appId: appId as any,
  });
  if (res.data.code === 0) {
    appAnswerResultCountList.value = res.data.data || [];
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};
// 应用统计选项图表对象
// 计算属性发生变化时，图表会自动更新。
const appAnswerCountOptions = computed(() => {
  return {
    xAxis: {
      type: "category",
      data: appAnswerCountList.value.map((item) => item.appId),
      name: "应用 id",
    },
    yAxis: {
      type: "value",
      name: "做题用户数",
    },
    series: [
      {
        data: appAnswerCountList.value.map((item) => item.answerCount),
        type: "bar",
      },
    ],
  };
});

// 应用统计选项图表对象
// 计算属性发生变化时，图表会自动更新。
const appAnswerResultCountOptions = computed(() => {
  return {
    tooltip: {
      trigger: "item",
    },
    legend: {
      orient: "vertical",
      left: "left",
    },
    series: [
      {
        type: "pie",
        radius: "50%",
        data: appAnswerResultCountList.value.map((item) => {
          return {
            value: item.resultCount,
            name: item.resultName,
          };
        }),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
      },
    ],
  };
});

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadAppAnswerCountData();
  loadAppAnswerResultCountData("1");
});
</script>

<style scoped>
.searchBar {
  margin-bottom: 28px;
  text-align: center;
}
</style>
