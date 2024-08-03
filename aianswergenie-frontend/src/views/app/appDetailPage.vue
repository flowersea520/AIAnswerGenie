<template>
  <div id="appDetailPage">
    <a-card>
      <a-row style="margin-bottom: 16px">
        <a-col flex="auto" class="content-wrapper">
          <h2>{{ data.appName }}</h2>
          <p>{{ data.appDesc }}</p>
          <p>应用类型：{{ APP_TYPE_MAP[data.appType] }}</p>
          <p>评分策略：{{ APP_SCORING_STRATEGY_MAP[data.scoringStrategy] }}</p>
          <p>
            <a-space>
              作者：
              <div :style="{ display: 'flex', alignItems: 'center' }">
                <a-avatar
                  :size="24"
                  :image-url="data.user?.userAvatar"
                  :style="{ marginRight: '8px' }"
                />
                <a-typography-text
                  >{{ data.user?.userName ?? "无名" }}
                </a-typography-text>
              </div>
            </a-space>
          </p>
          <p>
            创建时间：{{ dayjs(data.createTime).format("YYYY-MM-DD HH:mm:ss") }}
          </p>
          <a-space size="medium">
            <a-button type="primary" :href="`/answer/do/${id}`"
              >开始答题
            </a-button>
            <a-button @click="doShare">分享应用</a-button>
            <a-button v-if="isMy" :href="`/add/question/${id}`"
              >设置题目
            </a-button>
            <a-button v-if="isMy" :href="`/add/scoring_result/${id}`"
              >设置评分
            </a-button>
            <a-button v-if="isMy" :href="`/add/app/${id}`">修改应用</a-button>
          </a-space>
        </a-col>
        <a-col flex="320px">
          <a-image width="100%" :src="data.appIcon" />
        </a-col>
      </a-row>
    </a-card>
  </div>
  <!--  引入子组件（将链接转换为 二维码的子组件）
 -- 这个子组件默认是关闭的,由父组件调用子组件对象的openModal方法,打开这个子组件模态框-->
  <!--  这个ref就是给子组件起 一个 对象名-->
  <ShareModal :link="shareLink" title="应用分享" ref="shareModalRef" />
</template>

<script setup lang="ts">
import { computed, defineProps, ref, watchEffect, withDefaults } from "vue";
import API from "@/servers/api";
import { getAppVoByIdUsingGet } from "@/servers/api/appController";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { dayjs } from "@arco-design/web-vue/es/_utils/date";
import { useLoginUserStore } from "@/store/userStore";
import { APP_SCORING_STRATEGY_MAP, APP_TYPE_MAP } from "../../constant/app";
import ShareModal from "@/components/ShareModal.vue";

// 定义接收父组件数据的属性对象Prop
interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  // 定义id作为一个prop，Vue会自动将路由参数注入到这个prop中
  id: () => {
    // 给这个id属性默认值
    return "";
  },
});

const router = useRouter();

const data = ref<API.AppVO>({});

// 获取登录用户
const loginUserStore = useLoginUserStore();
let loginUserId = loginUserStore.loginUser?.id;
// 是否为本人创建
const isMy = computed(() => {
  return loginUserId && loginUserId === data.value.userId;
});

/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.id) {
    return;
  }
  // 根据路由传过来的app的id我们去查对象的appVo实体
  const res = await getAppVoByIdUsingGet({
    id: props.id as any,
  });
  if (res.data.code === 0) {
    data.value = res.data.data as any;
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

// eslint-disable-next-line no-irregular-whitespace
// 分享链接变量， 这里肯定要弄动态的协议 +　域名／ip+路由　（这样才能实现动态的跳转到我们的详情页）
// ：window.location.host 返回当前 URL 的主机部分，包括主机名和端口号（如果指定了端口号）。
const shareLink = `${window.location.protocol}//${window.location.host}/app/detail/${props.id}`;
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

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});
</script>

<style scoped>
#appDetailPage {
}

#appDetailPage .content-wrapper > * {
  margin-bottom: 24px;
}
</style>
