<script lang="ts">
  import { fly } from "svelte/transition";

  import Graph from "./Graph.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import { sensorsStore } from "$stores/sensorsStore";
  import { createGraphData } from "$components/graph/helper";

  export let data: any;
  export let dates: any;
  export let loading = false;

  let sensors = $sensorsStore;

  let currentSensor: any = sensors[0].type;

  let graphData: any = {};

  data.then(async (res: any) => {
    graphData = createGraphData(res.data);
  });

  let options = {
    responsive: true,
    scales: {
      x: {
        display: false,
      },
    },
  };

  $: console.log("graphData", graphData);
  $: console.log(currentSensor);
</script>

<div>
  <div class="md:flex items-center">
    {#key graphData}
      <div class="w-full h-full">
        {#if loading}
          <div class="mb-2">
            <Spinner />
          </div>
        {:else if Object.keys(graphData).length === 0}
          <h1 class="font-bold text-4xl flex justify-center">No data found</h1>
        {:else}
          <Graph {options} data={graphData?.[currentSensor]} />
        {/if}
      </div>
    {/key}

    <div
      class="bg-green-400 my-auto mx-auto shadow-2xl rounded-2xl p-2 flex justify-center items-center flex-cols gap-4"
    >
      {#each sensors as sensor, i (i)}
        <div
          in:fly|self={{ x: -50, duration: 50, delay: 100 * i }}
          class="tooltip"
          data-tip={sensor.type}
        >
          <button on:click={() => (currentSensor = sensor.type)}>
            <i
              class="bi {sensor.icon} flex items-center my-auto transform transition-transform active:scale-110 material-symbols-outlined text-4xl hover:text-white hover:dark:text-black 
              {sensor.type === currentSensor ? 'text-black' : 'text-white'}"
            >
              {sensor?.google}
            </i>
          </button>
        </div>
      {/each}
    </div>
  </div>
</div>
