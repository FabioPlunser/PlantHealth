<script lang="ts">
  import { fly } from "svelte/transition";

  import Graph from "./Graph.svelte";

  export let data: any;

  let sensors = [
    {
      type: "TEMPERATURE",
      icon: "bi-thermometer-half",
      google: "",
    },
    {
      type: "HUMIDITY",
      icon: "bi-droplet-half",
      google: "",
    },
    {
      type: "LIGHTINTENSITY",
      icon: "bi-sun",
      google: "",
    },
    {
      type: "PRESSURE",
      icon: "",
      google: "speed",
    },
    {
      type: "GASPRESSURE",
      icon: "",
      google: "nest_thermostat_zirconium_eu",
    },
    {
      type: "SOILHUMIDITY",
      icon: "bi-moisture",
      google: "",
    },
  ];

  let currentSensor: any = sensors[0].type;

  let graphData: any = {};

  data.then(async (res: any) => {
    createGraphData(res.data);
  });

  function createGraphData(data: any) {
    // console.log(data);
    for (let sensor of data) {
      let sensorType = sensor.sensorType;
      let sensorUnit = sensor.sensorUnit;
      let labels = [];
      let datasets = [];

      let sensorData: any = [];
      for (let data of sensor.values) {
        let label = new Date(data.timestamp).toLocaleString("de-DE");
        labels.push(label);
        // console.log(data.value);
        // sensorData data.value in an array seperated by ,
        sensorData.push(data.value);
      }
      let dataset = {
        label: sensorType,
        fill: true,
        lineTension: 0.5,
        backgroundColor: "rgba(75,192,192,0.4)",
        borderColor: "rgba(75,192,192,1)",
        data: sensorData,
      };
      datasets.push(dataset);

      graphData[sensorType] = {
        labels: labels,
        datasets: datasets,
      };
    }
  }
  let options = {
    responsive: true,
    scales: {
      x: {
        display: false,
      },
    },
  };
  $: console.log(currentSensor);

  // $: console.log("graphData", graphData);
</script>

<div>
  <div class="md:flex items-center">
    <div class="w-[340px] h-full">
      <Graph {options} data={graphData?.[currentSensor]} />
    </div>

    <div
      class="bg-green-400 my-auto mx-auto shadow-2xl rounded-2xl p-4 flex flex-cols gap-4"
    >
      {#each sensors as sensor, i (i)}
        <div
          in:fly|self={{ x: -50, duration: 50, delay: 100 * i }}
          class="tooltip"
          data-tip={sensor.type}
        >
          <button on:click={() => (currentSensor = sensor.type)}>
            <i
              class="bi {sensor.icon} flex items-center my-auto transform transition-transform active:scale-110 material-symbols-outlined text-4xl hover:text-white dark:text-white hover:dark:text-black {sensor.type ===
              currentSensor
                ? 'text-black dark:text-black'
                : ''}">{sensor?.google}</i
            >
          </button>
        </div>
      {/each}
    </div>
  </div>
</div>
