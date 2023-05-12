<script lang="ts">
  import { fly } from "svelte/transition";
  import { sensorsStore } from "$stores/sensorsStore";
  import { createGraphData } from "$components/graph/helper";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import Graph from "$components/graph/graph.svelte";
  import MediaQuery from "$helper/MediaQuery.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  // ---------------------------------------------------
  // ---------------------------------------------------
  export let data: any;
  export let loading = false;
  export let options = {};
  // ---------------------------------------------------
  // ---------------------------------------------------
  let sensors = $sensorsStore;
  let currentSensor: any = sensors[0].sensorType;
  let graphData: any = {};
  let width = 0;
  $: {
    if (width <= 700) {
      options = {
        responsive: true,
        scales: {
          x: {
            display: false,
          },
        },
      };
    } else {
      options = {
        responsive: true,
        scales: {
          x: {
            display: true,
          },
          y: {
            type: "linear",
            display: true,
            position: "left",
          },
        },
      };
    }
  }
  // ---------------------------------------------------
  // ---------------------------------------------------
  async function addMissingSensors(dynamicSensors: any) {
    for (let sensor of dynamicSensors) {
      if (!sensors.some((s) => s.sensorType === sensor.sensorType)) {
        let newSensor = {
          sensorType: sensor.sensorType,
          sensorUnit: sensor.sensorUnit,
          bootstrap: "",
          google: "sensors",
        };
        sensors = [...sensors, newSensor];
      }
    }
  }
  if (data instanceof Promise) {
    data
      .then(async (res: any) => {
        addMissingSensors(res.data);
        graphData = createGraphData(res.data);
      })
      .catch((err: any) => {
        console.log(err);
      });
  } else {
    addMissingSensors(data);
    graphData = createGraphData(data);
  }
  // ---------------------------------------------------
  // ---------------------------------------------------
</script>

<!-- @component
This the graphs component 
it produces a graph with GraphJS and a sidebar to change between the graphs 
Usage example: 
```html
<Graphs data={sensorStation.data} />
```
-->

<div>
  <div class="md:flex items-center" bind:clientWidth={width}>
    <div class="w-full h-full">
      {#if loading}
        <div class="mb-2">
          <Spinner fill="fill-primary" />
        </div>
      {:else if Object.keys(graphData).length === 0}
        <h1 class="font-bold text-4xl flex justify-center">No data found</h1>
      {:else}
        <MediaQuery query="(width <= 640px)" let:matches>
          {#key matches}
            <Graph data={graphData?.[currentSensor]} {options} />
          {/key}
        </MediaQuery>
      {/if}
    </div>

    <div
      class="bg-green-400 mx-auto shadow-2xl rounded-2xl flex justify-center items-ceter gap-4 md:grid md:flex-none md:justify-normal md:gap-2 p-2"
    >
      {#each sensors as sensor, i (i)}
        <div
          in:fly|self={{ y: -50, duration: 50, delay: 100 * i }}
          class="tooltip"
          data-tip={sensor.sensorType}
        >
          <button on:click={() => (currentSensor = sensor.sensorType)}>
            <i
              class="bi {sensor.bootstrap} transform transition-transform active:scale-110 material-symbols-outlined text-2xl sm:text-3xl md:text-4xl xl:text-5xl hover:text-blue-400 hover:scale-105 hover:dark:text-black
              {sensor.sensorType === currentSensor
                ? 'text-black'
                : 'text-white'}"
            >
              {sensor?.google}
            </i>
          </button>
        </div>
      {/each}
    </div>
  </div>
</div>
