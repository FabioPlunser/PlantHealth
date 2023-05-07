<script lang="ts">
  import Line from "$lib/components/graph/wrapper/Line.svelte";

  export let width = 300;
  export let height = 300;

  import {
    Chart as ChartJS,
    Title,
    Tooltip,
    Legend,
    LineElement,
    LinearScale,
    PointElement,
    CategoryScale,
  } from "chart.js";

  ChartJS.register(
    Title,
    Tooltip,
    Legend,
    LineElement,
    LinearScale,
    PointElement,
    CategoryScale
  );

  export let data: any;
  export let options: any;
</script>

<div class="w-full h-full">
  <Line {data} {options} />
</div>
