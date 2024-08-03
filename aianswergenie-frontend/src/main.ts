import { createApp } from "vue";
import App from "./App.vue";
import { createPinia } from "pinia";
import router from "./router";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
// 在 main.ts 中引入，即可生效权限校验：(路由设置一定要往main.ts中引入，在这里可以进行应用的初始化和全局设置。）
import "@/access";
import VChart from "vue-echarts";
// 创建根组件对象实例，然后其渲染到 HTML 页面中的挂载点上。
createApp(App).use(createPinia()).use(ArcoVue).use(router).mount("#app");
createApp(App).component("v-chart", VChart);
