<script lang="ts">
  import Graph from "./Graph.svelte";
  export let data: any;

  let sensors = [
    {
      type: "TEMPERATURE",
      icon: "bi-thermometer-half",
    },
    {
      type: "HUMIDITY",
      icon: "bi-droplet-half",
    },
    {
      type: "LIGHT",
      icon: "bi-sun",
    },
    {
      type: "SOIL",
      icon: "bi-moisture",
    },
  ];

  let currentSensor = sensors[0];
</script>

<div>
  <div class="flex">
    <div>
      <Graph data={data?.[currentSensor]} />
    </div>
    <div class="aboslute right-0 ml-2">
      <div
        class="bg-green-400 mx-2 my-auto shadow-2xl rounded-3xl grid grid-rows gap-4 p-4 h-min w-min"
      >
        {#each sensors as sensor, i (i)}
          <button on:click={() => (currentSensor = sensor)}>
            <i
              class="bi {sensor.icon} text-4xl hover:text-white dark:text-white hover:dark:text-black {sensor.type ===
              currentSensor.type
                ? 'text-blue-500'
                : ''}"
            />
          </button>
        {/each}
      </div>
    </div>
  </div>
</div>
