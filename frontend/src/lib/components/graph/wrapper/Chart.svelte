<script lang="ts">
  import { onMount, afterUpdate, onDestroy } from "svelte";
  import { Chart } from "chart.js";
  import { theme } from "$stores/themeStore";
  import { browser } from "$app/environment";

  $: props = $$props;

  let canvasRef: HTMLCanvasElement;

  export let type: any = "Line";
  export let data: any = {
    datasets: [],
  };

  export let options = {
    responsive: true,
    maintainAspectRatio: false,
  };

  export let plugins: any[] = [];

  export let updateMode: any = undefined;
  export let chart: any = null;

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

<div
  style="width: 100%; height: 0; position: relative"
  class="pb-[60%] sm:pb-[45%]"
>
  <canvas
    id="chart"
    bind:this={canvasRef}
    {...props}
    style="position: absolute; width: 100%; height: 100%;"
  />
</div>
