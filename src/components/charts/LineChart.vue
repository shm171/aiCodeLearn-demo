<!-- 折线图：labels + 多条 series -->
<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import BaseChart from './BaseChart.vue'

const props = withDefaults(
  defineProps<{
    labels: string[]
    series: Array<{ name: string; data: number[] }>
    height?: string
    yName?: string
  }>(),
  { height: '320px', yName: '' }
)

const option = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: props.series.map((s) => s.name), top: 0 },
  grid: { left: 16, right: 24, top: 44, bottom: 16, containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: props.labels },
  yAxis: { type: 'value', name: props.yName },
  series: props.series.map((s) => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data,
    areaStyle: { opacity: 0.08 },
  })),
}))
</script>

<template>
  <BaseChart :option="option" :height="height" />
</template>