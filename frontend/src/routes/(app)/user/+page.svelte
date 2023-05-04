<script lang="ts">
  import SensorstationModal from "./SensorstationModal.svelte";
  import SensorStation from "$lib/components/ui/SensorStation.svelte";

  //---------------------------------------------------
  export let data;
  //---------------------------------------------------
  // initial animations
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  //---------------------------------------------------
  let sensorStationModel = false;
  //---------------------------------------------------

  // $: console.log(data);
</script>

{#if rendered}
  <SensorstationModal
    data={data?.sensorStations}
    bind:showModal={sensorStationModel}
  />

  <section>
    <div class="flex justify-center">
      <button
        class="btn btn-primary"
        on:click={() => (sensorStationModel = true)}>SensorStations</button
      >
    </div>

    <div class="flex justify-center mt-20">
      <div class="gap-4 grid jusitfy-center">
        {#each data?.dashboard?.sensorStations as sensorStation}
          <SensorStation {sensorStation} />
        {/each}
      </div>
    </div>
  </section>
{/if}
