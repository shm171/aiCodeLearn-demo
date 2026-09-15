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
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(24, 24, 27, 0.96)',
    borderColor: 'rgba(255, 255, 255, 0.12)',
    textStyle: { color: '#f4f4f5' },
  },
  legend: {
    data: props.series.map((item) => item.name),
    top: 0,
    textStyle: { color: '#a1a1aa' },
  },
  grid: { left: 16, right: 24, top: 44, bottom: 16, containLabel: true },
  xAxis: {
    type: 'category',
    data: props.labels,
    axisLabel: { color: '#a1a1aa' },
    axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.12)' } },
  },
  yAxis: {
    type: 'value',
    name: props.yName,
    nameTextStyle: { color: '#71717a' },
    axisLabel: { color: '#a1a1aa' },
    splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.06)' } },
  },
  series: props.series.map((item) => ({
    name: item.name,
    type: 'bar',
    data: item.data,
    barMaxWidth: 28,
    itemStyle: {
      color: '#a78bfa',
      borderRadius: [6, 6, 0, 0],
    },
  })),
}))
</script>