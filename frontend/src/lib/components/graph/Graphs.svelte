<script lang="ts">
  import { fly } from "svelte/transition";
  import { sensorsStore } from "$stores/sensorsStore";
  import { createGraphData } from "$components/graph/helper";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import Line from "./Line.svelte";
  import MediaQuery from "$helper/MediaQuery.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import Graph from "../ui/Graph.svelte";
  // ---------------------------------------------------
  // ---------------------------------------------------
  export let sensorStation: SensorStationComponent;
  export let loading = true;
  export let options = {};
  // ---------------------------------------------------
  // ---------------------------------------------------
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
        interaction: {
          intersect: false,
        },

        scales: {
          x: {
            display: true,
            autoSkip: true,
          },
          y: {
            type: "linear",
            display: true,
            position: "left",
            // suggestedMin: 25,
            // suggestedMax: 35,
            ticks: {
              stepSize: 0.01,
            },
          },
        },
      };
    }
  }
  // ---------------------------------------------------
  // ---------------------------------------------------
  let currentSensor: any = null;
  let sensors: any[] = [];
  let storedSensors = $sensorsStore;
  let data = sensorStation.data;
  $: console.log(data);
  data.then(async (res) => {
    for (let sensor of res.data) {
      let storedSensor = storedSensors.find(
        (s) => s.sensorType === sensor.sensorType
      );
      let foundSensor = {
        sensorId: sensor.sensorId,
        sensorType: sensor.sensorType,
        sensorUnit: sensor.sensorUnit,
        bootstrap:
          storedSensor?.bootstrap === "" ? "" : storedSensor?.bootstrap,
        google: storedSensor?.google === "" ? "" : storedSensor?.google,
      };
      sensors = [...sensors, foundSensor];
    }
    sensors.sort((a, b) => b.sensorType.localeCompare(a.sensorType));
    graphData = createGraphData(res.data);
    loading = false;
  });

  $: {
    if (sensors.length > 0) {
      currentSensor = sensors[0];
    }
  }
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
        <h1 class="font-bold">{currentSensor.sensorUnit}</h1>
        <MediaQuery query="(width <= 640px)" let:matches>
          {#key matches}
            <Line data={graphData?.[currentSensor.sensorType]} {options} />
          {/key}
        </MediaQuery>
      {/if}
    </div>

    {#if Object.keys(graphData).length > 0}
      <div
        class="bg-green-400 mx-auto shadow-2xl rounded-2xl flex justify-center items-ceter gap-4 md:grid md:flex-none md:justify-normal md:gap-2 p-2"
      >
        {#each sensors as sensor, i (i)}
          <div
            in:fly|local|self={{ y: -50, duration: 50, delay: 100 * i }}
            class="tooltip tooltip-primary"
            data-tip={sensor.sensorType}
          >
            <button on:click={() => (currentSensor = sensor)}>
              <i
                class="bi {sensor.bootstrap} transform transition-transform active:scale-110 material-symbols-outlined text-2xl sm:text-3xl md:text-4xl xl:text-5xl hover:text-blue-400 hover:scale-105 hover:dark:text-black
              {sensor.sensorType === currentSensor.sensorType
                  ? 'text-black'
                  : 'text-white'}"
                class:alarm={sensorStation.alarms.some(
                  (a) => a.sensor.type === sensor.sensorType && a.alarm !== "n"
                )}
              >
                {sensor?.google}
              </i>
            </button>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>
