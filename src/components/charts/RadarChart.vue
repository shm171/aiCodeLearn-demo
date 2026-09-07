<!-- 雷达图：学习报告知识点掌握度等 -->
<template>
  <BaseChart :option="option" :height="height" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from './BaseChart.vue'

const props = defineProps({
  indicators: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] },
  height: { type: String, default: '320px' },
})

const option = computed(() => ({
  tooltip: {},
  legend: { top: 0, data: props.series.map((s) => s.name) },
  radar: {
    indicator: props.indicators,
    radius: '62%',
    center: ['50%', '54%'],
  },
  series: [
    {
      type: 'radar',
      data: props.series.map((s) => ({ name: s.name, value: s.value, areaStyle: { opacity: 0.15 } })),
    },
  ],
}))
</script>