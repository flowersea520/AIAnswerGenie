<template>
  <a-modal v-model:visible="visible" :footer="false" @close="closeModal">
    <template #title>
      {{ title }}
    </template>
    <h4>复制分享链接</h4>
    <a-typography-paragraph copyable>
      {{ link }}
    </a-typography-paragraph>
    <h4>扫描二维码查看</h4>
    <img :src="codeImg" />
  </a-modal>
</template>

<script setup lang="ts">
// @ts-ignore
import QRCode from "qrcode";
import { defineProps, ref, withDefaults } from "vue";
import { defineExpose } from "vue";
// 二维码图片对象
const codeImg = ref();
// 默认模态框（和弹框差不多）关闭
const visible = ref(false);

/**
 *  打开模态框
 */
const openModal = () => {
  visible.value = true;
};
// 使用 defineExpose 暴露方法
defineExpose({
  openModal,
});
/**
 * 关闭模态框
 */
const closeModal = () => {
  visible.value = false;
};

/**
 * 定义组件属性类型
 *  我们这个是分享的模态框组件，需要传入标题和分享的链接
 */
interface Props {
  title: string;
  link: string;
}

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  title: () => "分享",
  // 这里默认用 鱼皮脱敏后的链接，我们后续可以将自己的简历链接传进来，然后生成我们后续的简历二维码
  link: () => "https://laoyujianli.com/share/yupi",
});

// 二维码生成
QRCode.toDataURL(props.link)
  .then((url: string) => {
    // 这个组件库QRCode返回的url就是二维码的图片url，直接赋值给img的src就可以显示出来
    codeImg.value = url;
  })
  .catch((err: any) => {
    console.error(err);
  });
</script>
