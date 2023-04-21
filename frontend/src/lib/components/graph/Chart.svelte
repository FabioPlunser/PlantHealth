<script lang="ts">
  import { onMount, afterUpdate, onDestroy } from "svelte";
  import { Chart as ChartJS } from "chart.js";
  import { darkOptions, lightOptions } from "./options";
  import { theme } from "$stores/themeStore";

  let props = $$props;

  let canvasRef: HTMLCanvasElement;
  let chart: any = null;

  export let type: any;
  export let data = {
    datasets: [],
  };
  // export let options: any = {};
  export let plugins: any = [];

  export let width: number;
  export let height: number;

  let options: any = null;
  // $: options = $theme === "dark" ? darkOptions : lightOptions;
  $: {
    options = $theme ? darkOptions : lightOptions;
    if (chart) {
      chart.options = options;
      chart.update();
    }
  }

  function random_rgba() {
    let o = Math.round;
    let ra = Math.random;
    let s = 255;

    let r = o(ra() * s);
    let g = o(ra() * s);
    let b = o(ra() * s);

    let color = {
      background: "rgba(" + r + "," + g + "," + b + "," + 0.75 + ")",
      border: "rgba(" + r + "," + g + "," + b + "," + 1 + ")",
    };
    return color;
  }

  $: {
    let color = random_rgba();
    console.log(data);
    if (data?.datasets) {
      data.datasets[0].backgroundColor = color.background;
      data.datasets[0].borderColor = color.border;
    }
  }

  // $: options = darkOptions;

  onMount(() => {
    chart = new ChartJS(canvasRef, {
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
    chart.update();
  });
  onDestroy(() => {
    if (chart) chart.destroy();
    chart = null;
  });
</script>

<div
  class="relative m-auto"
  style="position: relative; height: {height}px; width: {width}px"
>
  <canvas bind:this={canvasRef} {...props} />
</div>
