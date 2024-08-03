<template>
  <Editor :value="value" :plugins="plugins" @change="handleChange" />
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import { Editor, Viewer } from "@bytemd/vue";
import { onMounted, ref } from "vue";

const plugins = [
  gfm(),
  // Add more plugins here
];

const value = ref<string>("");

onMounted(() => {
  // 如果plugins有初始化逻辑可以在此处调用
});

const handleChange = (v: string) => {
  value.value = v;
};
</script>
