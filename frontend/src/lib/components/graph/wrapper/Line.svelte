<script lang="ts">
  import { Chart as ChartJs, LineController, registerables } from "chart.js";
  import Chart from "./Chart.svelte";

  ChartJs.register(...registerables);
  ChartJs.register(LineController);

  export let chart: any = null;

  $: props = $$props;

  let baseChartRef: Chart;
</script>

<div class="w-full h-full">
  <Chart bind:chart bind:ths={baseChartRef} type="line" {...props} />
</div>
