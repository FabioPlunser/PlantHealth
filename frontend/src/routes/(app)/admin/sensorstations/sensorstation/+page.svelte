<script lang="ts">
  import { fly } from "svelte/transition";
  import { onMount } from "svelte";
  // ----------------------------------
  // ----------------------------------
  import { SensorStationDetail } from "$components/ui/sensorStation";

  // ----------------------------------
  // ----------------------------------
  let rendered = false;
  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
  });
  // ----------------------------------
  // ----------------------------------
  export let data;
  export let form;
  // ----------------------------------
  // ----------------------------------
  $: sensorStationsData = {
    dates: data.dates,
    sensorStations: Array.of(data.streamed.sensorStation),
  };
  $: console.log(data);
</script>

{#if rendered}
  <section in:fly={{ y: -200, duration: 200 }}>
    <SensorStationDetail data={sensorStationsData} {form} />
  </section>
{/if}
