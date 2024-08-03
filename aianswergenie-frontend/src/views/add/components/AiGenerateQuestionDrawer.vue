<template>
  <a-button type="outline" @click="handleClick">AI 生成题目</a-button>
  <a-drawer
    :width="340"
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    unmountOnClose
  >
    <template #title>AI 生成题目</template>
    <div>
      <a-form
        :model="form"
        label-align="left"
        auto-label-width
        @submit="handleSubmit"
      >
        <a-form-item label="应用 id">
          {{ appId }}
        </a-form-item>
        <a-form-item field="questionNumber" label="题目数量">
          <a-input-number
            min="0"
            max="20"
            v-model="form.questionNumber"
            placeholder="请输入题目数量"
          />
        </a-form-item>
        <a-form-item field="optionNumber" label="选项数量">
          <a-input-number
            min="0"
            max="6"
            v-model="form.optionNumber"
            placeholder="请输入选项数量"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button
              :loading="submitting"
              type="primary"
              html-type="submit"
              style="width: 120px"
            >
              {{ submitting ? "生成中" : "一键生成" }}
            </a-button>
            <a-button
              :loading="sseSubmitting"
              style="width: 120px"
              @click="doSSESubmit"
            >
              {{ sseSubmitting ? "生成中" : "实时生成" }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { defineProps, reactive, ref, withDefaults } from "vue";
import API from "@/servers/api";
import { aiGenerateQuestionUsingPost } from "@/servers/api/questionController";
import message from "@arco-design/web-vue/es/message";

/**
 *  Props 接口定义了子组件接收父组件传递的数据和函数
 *  onSuccess这通常是一个在父组件中定义的函数，用于更新父组件的状态或处理某些逻辑。
 */
interface Props {
  appId: string;
  // 父组件：AI 生成题目成功的方法
  onSuccess?: (result: API.QuestionContentDTO[]) => void;
  // 父组件：SSE连接成功的方法（这里的返回类型不再是一个数组，因为我们是通过后端的sse，AI一道题一道题返回）
  onSSESuccess?: (result: API.QuestionContentDTO) => void;
  // 父组件：sse连接开始的方法
  onSSEStart?: (event: any) => void;
  // 父组件连接关闭的方法
  onSSEClose?: (event: any) => void;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const form = reactive({
  optionNumber: 2,
  questionNumber: 10,
} as API.AiGenerateQuestionRequest);

const visible = ref(false);
const submitting = ref(false);
const sseSubmitting = ref(false);

const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};

/**
 * 提交
 */
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  submitting.value = true;
  const res = await aiGenerateQuestionUsingPost({
    appId: props.appId as any,
    ...form,
  });
  if (res.data.code === 0 && res.data.data.length > 0) {
    // 这个函数是在父组件中定义的，所以子组件通过 props 使用它，实际上是在调用父组件的逻辑。
    // 事件驱动：当子组件中的按钮被点击时，会触发 handleSomeAction 函数，该函数调用 props.onSuccess。这是一种常见的事件驱动方式，子组件通过调用父组件传递的函数来触发更新或处理事件。
    if (props.onSuccess) {
      props.onSuccess(res.data.data);
    } else {
      message.success("生成题目成功");
    }
    // 关闭抽屉
    handleCancel();
  } else {
    message.error("操作失败，" + res.data.message);
  }
  submitting.value = false;
};

/**
 *  提交（实时生成）
 */

const doSSESubmit = async () => {
  if (!props.appId) {
    return;
  }
  // 前端会显示：生成中
  sseSubmitting.value = true;
  // 创建SSE 请求连接（这样可以和后端建立连接，后端发送的sse请求我们能收到）
  const eventSource = new EventSource(
    `http://localhost:8081/api/question/ai_generate/sse/?appId=${props.appId}&optionNumber=${form.optionNumber}&questionNumber=${form.questionNumber}`
  );

  // 接收消息，会自动触发该事件（用onmessage事件）
  eventSource.onmessage = function (event) {
    // event 是 EventSource 对象触发 onmessage 事件时传递的事件对象。
    console.log(event.data);
    // 调用父组件的方法，将json字符串解析为js对象传过去
    // 后端sse对象，每次都是返回一道题，所以我们就是一道一道返回的
    props.onSSESuccess?.(JSON.parse(event.data));
  };

  // 接收错误消息（用onerror事件）
  eventSource.onerror = function (event) {
    // event.eventPhase 属性表示事件的阶段。EventSource.CLOSED 是一个常量，表示连接已经关闭。如果 event.eventPhase 等于 EventSource.CLOSED，说明 SSE 连接已经关闭。
    if (event.eventPhase === EventSource.CLOSED) {
      console.log("SSE连接关闭");
      eventSource.close();
      // 调用父组件的 关闭sse的方法
      props.onSSEClose?.(event);
    }
  };

  // 建立连接的时候触发（用onclose事件）
  eventSource.onopen = function (event) {
    console.log("连接成功");
    props.onSSEStart?.(event);
    // 关闭抽屉
    handleCancel();
  };
  sseSubmitting.value = false;
};
</script>
