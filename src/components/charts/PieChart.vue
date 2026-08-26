<!-- 饼图 / 环形图 -->
<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import BaseChart from './BaseChart.vue'

const props = withDefaults(
  defineProps<{
    data: Array<{ name: string; value: number }>
    height?: string
    /** 是否环形 */
    ring?: boolean
  }>(),
  { height: '320px', ring: true }
)

const option = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, type: 'scroll' },
  series: [
    {
      type: 'pie',
      radius: props.ring ? ['42%', '68%'] : '70%',
      center: ['50%', '46%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: props.data,
    },
  ],
}))
</script>

<template>
  <BaseChart :option="option" :height="height" />
</template>