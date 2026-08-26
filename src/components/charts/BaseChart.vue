<!-- 通用 ECharts 容器组件：封装 init / setOption / resize / dispose -->
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(
  defineProps<{
    /** 图表配置，由父组件传入 */
    option: echarts.EChartsOption
    height?: string
  }>(),
  { height: '320px' }
)

const el = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

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

<template>
  <div ref="el" :style="{ height, width: '100%' }"></div>
</template>