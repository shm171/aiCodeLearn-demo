<!-- 雷达图：学习报告知识点掌握度等 -->
<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import BaseChart from './BaseChart.vue'

const props = withDefaults(
  defineProps<{
    indicators: Array<{ name: string; max: number }>
    series: Array<{ name: string; value: number[] }>
    height?: string
  }>(),
  { height: '320px' }
)

const option = computed<EChartsOption>(() => ({
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

<template>
  <BaseChart :option="option" :height="height" />
</template>