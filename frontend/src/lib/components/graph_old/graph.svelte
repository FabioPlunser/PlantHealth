<script lang="ts">
  import { createEventDispatcher } from "svelte";
  let dispatch = createEventDispatcher();

  import { fly } from "svelte/transition";

  import { Line } from "./index";
  import Trash from "$assets/icons/trash.svg?component";
  import Humidity from "$assets/icons/humidity.svg?component";
  import Soil from "$assets/icons/soil.svg?component";
  import Sun from "$assets/icons/sun.svg?component";
  import Temperature from "$assets/icons/temperature.svg?component";

  export let data: any;

  let showPictures: any;
  import { data as test } from "./data";
  import Carousel from "./Carousel.svelte";
  $: data = test;
  enum graphType {
    Temperature = "temperature",
    Humidity = "humidity",
    Light = "light",
    Soil = "soil",
  }
  $: graph = graphType.Temperature;
  function setGraph(graphType: graphType) {
    graph = graphType;
  }

  function remove() {
    //TODO: remove graph from user in backend and frontend
    dispatch("remove");
  }

  let width = 500;
  let height = 300;

  let graphComponentWidth: number;
</script>

<!-- TODO 
  Improve styling 
  Animation 
  System for data 
  Auto dark mode 
  Calendar picker select dates 
  Pictures of plants
-->

<div class="inline-grid">
  <div
    class="bg-base-100 dark:bg-white/30 backdrop-blur-2xl rounded-2xl shadow-2xl p-2 w-fit h-fit z-10"
  >
    <button class="absolute top-0 right-0 m-2" on:click={() => remove()}>
      <i class="bi bi-trash text-3xl hover:text-primary shadow-2xl" />
    </button>
    <br class="mt-1" />
    <h1 class="flex justify-center text-2xl">Plant 1</h1>
    <div bind:clientWidth={graphComponentWidth}>
      <div class="flex">
        <div class="">
          <Line bind:width bind:height data={test?.[graph]} />
        </div>
        <div
          class="bg-green-400 mx-2 my-auto  shadow-2xl rounded-3xl grid grid-rows gap-4 p-4 h-min"
        >
          <button
            class="flex justify-center"
            on:click={() => setGraph(graphType.Temperature)}
          >
            <i
              class="bi bi-thermometer-half hover:text-white dark:text-white hover:dark:text-black text-3xl {graph ===
              graphType.Temperature
                ? 'text-blue-400'
                : ''}"
            />
          </button>
          <button on:click={() => setGraph(graphType.Humidity)}>
            <i
              class="bi bi-droplet-half hover:text-white dark:text-white hover:dark:text-black text-3xl {graph ===
              graphType.Humidity
                ? 'text-blue-500'
                : ''}"
            />
          </button>
          <button on:click={() => setGraph(graphType.Light)}>
            <i
              class="bi bi-brightness-high-fill hover:text-white dark:text-white hover:dark:text-black text-3xl {graph ===
              graphType.Light
                ? 'text-yellow-400 font-bold'
                : ''}"
            />
          </button>
          <button on:click={() => setGraph(graphType.Soil)}>
            <i
              class="bi bi-box-fill hover:text-white dark:text-white hover:dark:text-black text-3xl {graph ===
              graphType.Soil
                ? 'text-slate-400'
                : ''}"
            />
          </button>
        </div>
      </div>
      <div class="absolute right-0 bottom-0 gap-4 flex items-center mr-2 mt-8">
        <button
          ><i
            class="bi bi-calendar-event-fill text-2xl hover:text-primary shadow-2xl"
          /></button
        >
        <button on:click={() => (showPictures = !showPictures)}
          ><i
            class="bi bi-card-image text-3xl hover:text-primary shadow-2xl"
          /></button
        >
      </div>
    </div>
  </div>

  {#if showPictures}
    <div
      class="bg-base-100 dark:bg-white/30 backdrop-blur-2xl rounded-2xl shadow-2xl p-2 w-full h-fit mt-1 z-0"
      in:fly={{ y: -50, duration: 200 }}
      out:fly={{ x: -100, duration: 200 }}
    >
      <Carousel bind:width={graphComponentWidth} pictures="" />
    </div>
  {/if}
</div>
