<template>
  <a-card id="appCard" hoverable @click="doCardClick">
    <template #actions>
      <!--      todo 这个点赞后续我们可以自己实现-->
      <span class="icon-hover">
        <IconThumbUp />
      </span>
      <!--      点击分享按钮，执行对应的分享链接逻辑(使用 e.stopPropagation() 可以阻止点击子元素span时，触发父元素a-card对象 的点击事件。)-->
      <span class="icon-hover" @click="doShare"> <IconShareInternal /> </span>
    </template>
    <template #cover>
      <div
        :style="{
          height: '204px',
          overflow: 'hidden',
        }"
      >
        <img
          :style="{ width: '100%', transform: 'translateY(-20px)' }"
          :alt="app.appName"
          :src="app.appIcon"
        />
      </div>
    </template>
    <a-card-meta :title="app.appName" :description="app.appDesc">
      <template #avatar>
        <div
          :style="{ display: 'flex', alignItems: 'center', color: '#1D2129' }"
        >
          <a-avatar :size="24" :style="{ marginRight: '8px' }"> A</a-avatar>
          <a-typography-text>Username</a-typography-text>
        </div>
      </template>
    </a-card-meta>
  </a-card>
  <!--  引入子组件（将链接转换为 二维码的子组件）
   -- 这个子组件默认是关闭的,由父组件调用子组件对象的openModal方法,打开这个子组件模态框-->
  <!--  这个ref就是给子组件起 一个 对象名-->
  <ShareModal :link="shareLink" title="应用分享" ref="shareModalRef" />
</template>

<script setup lang="ts">
import {
  IconThumbUp,
  IconShareInternal,
  IconMore,
} from "@arco-design/web-vue/es/icon";
import API from "@/servers/api";
import { defineProps, onMounted, onUnmounted, ref, withDefaults } from "vue";
import { useRouter } from "vue-router";
import ShareModal from "@/components/ShareModal.vue";

const router = useRouter();

// 这是ts的定义类型的语法
interface Props {
  // 规定了这种类型的对象必须要有这个属性
  app: API.AppVO;
}

//  withDefaults 函数是用来给这些 props 提供默认值的
// 这里给子组件定义属性了，用来接收父组件的数据，这里的属性值一定要用函数式的写法，这是一种约定
const props = withDefaults(defineProps<Props>(), {
  app: () => {
    return {};
  },
});

const doCardClick = () => {
  router.push(`/app/detail/${props.app.id}`);
};
// eslint-disable-next-line no-irregular-whitespace
// 分享链接变量， 这里肯定要弄动态的协议 +　域名／ip+路由　（这样才能实现动态的跳转到我们的详情页）
// ：window.location.host 返回当前 URL 的主机部分，包括主机名和端口号（如果指定了端口号）。
const shareLink = `${window.location.protocol}//${window.location.host}/app/detail/${props.app.id}`;
// 这里是子组件的对象，子组件的对象必须用ref来定义; 先初始子组件的默认对象为null,页面一挂载后,我们的shareModalRef子组件对象就会被实际的子组件对象所替代

const shareModalRef = ref();
// 点击卡片的 分享按钮, 我们会弹子组件的Modal模态框,然后会根据父组件传入的 链接,生成对应的 二维码
// 使用 e.stopPropagation() 可以阻止点击子元素时，触发父元素的点击事件。
const doShare = (e: Event) => {
  if (shareModalRef.value) {
    //  调用子组件的showModal方法
    shareModalRef.value.openModal();
  }
  // 阻止冒泡到父元素的事件方法,我们就只触发子元素的事件
  e.stopPropagation();
};
</script>
<style scoped>
#appCard {
  cursor: pointer;
}

.icon-hover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  transition: all 0.1s;
}

.icon-hover:hover {
  background-color: rgb(var(--gray-2));
}
</style>
