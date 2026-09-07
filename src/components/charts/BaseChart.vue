<!-- 通用 ECharts 容器组件：封装 init / setOption / resize / dispose -->
<template>
  <div ref="el" :style="{ height, width: '100%' }"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' },
})

const el = ref()
let chart = null

function render() {
  if (chart) chart.setOption(props.option)
}

onMounted(() => {
  if (!el.value) return
  chart = echarts.init(el.value)
  render()
})

watch(
  () => props.option,
  () => render(),
  { deep: true }
)

function onResize() {
  chart?.resize()
}
window.addEventListener('resize', onResize)

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})
</script>