<!-- 柱状图：labels + 多条 series -->
<template>
  <BaseChart :option="option" :height="height" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from './BaseChart.vue'

const props = defineProps({
  labels: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] },
  height: { type: String, default: '320px' },
  yName: { type: String, default: '' },
})

const option = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: props.series.map((s) => s.name), top: 0 },
  grid: { left: 16, right: 24, top: 44, bottom: 16, containLabel: true },
  xAxis: { type: 'category', data: props.labels },
  yAxis: { type: 'value', name: props.yName },
  series: props.series.map((s) => ({
    name: s.name,
    type: 'bar',
    data: s.data,
    barMaxWidth: 28,
  })),
}))
</script>