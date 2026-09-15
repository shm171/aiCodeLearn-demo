<template>
  <BaseChart :option="option" :height="height" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from './BaseChart.vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  height: { type: String, default: '320px' },
  ring: { type: Boolean, default: true },
})

const colors = ['#c4b5fd', '#60a5fa', '#34d399', '#fbbf24', '#fb7185']

const option = computed(() => ({
  color: colors,
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)',
    backgroundColor: 'rgba(24, 24, 27, 0.96)',
    borderColor: 'rgba(255, 255, 255, 0.12)',
    textStyle: { color: '#f4f4f5' },
  },
  legend: {
    bottom: 0,
    type: 'scroll',
    textStyle: { color: '#a1a1aa' },
  },
  series: [
    {
      type: 'pie',
      radius: props.ring ? ['42%', '68%'] : '70%',
      center: ['50%', '46%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: 'rgba(10, 10, 12, 0.9)',
        borderWidth: 2,
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        color: '#d4d4d8',
      },
      data: props.data,
    },
  ],
}))
</script>