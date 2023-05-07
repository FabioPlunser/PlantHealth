<script lang="ts">
  import { onMount, afterUpdate, onDestroy } from "svelte";
  import { Chart } from "chart.js";
  import { theme } from "$stores/themeStore";

  $: props = $$props;

  let canvasRef: HTMLCanvasElement;

  export let type: any = "Line";
  export let data: any = {
    datasets: [],
  };

  export let options: any = {};
  export let plugins: any[] = [];

  export let updateMode: any = undefined;
  export let chart: any = null;

  export let width: number = 300;
  export let height: number = 300;

  console.log("chartOptions", options);

  onMount(() => {
    chart = new Chart(canvasRef, {
      type,
      data,
      options,
      plugins,
    });
  });

  afterUpdate(() => {
    if (!chart) return;

    chart.data = data;
    Object.assign(chart.options, options);
    chart.update(updateMode);
  });

  onDestroy(() => {
    if (!chart) return;
    chart.destroy();
  });
</script>

<div class="chart min-w-xl">
  <canvas bind:this={canvasRef} {...props} />
</div>

<style>
  .chart {
    position: relative;
    width: 100%;
    height: 100%;
  }
</style>
