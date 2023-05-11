<script lang="ts">
  import { fly } from "svelte/transition";
  //---------------------------------------------------
  //---------------------------------------------------
  import SensorstationModal from "./SensorstationModal.svelte";
  import SensorStation from "$components/ui/SensorStation/SensorStation.svelte";
  import DatePicker from "$lib/components/datepicker/DatePicker.svelte";
  //---------------------------------------------------
  //---------------------------------------------------
  export let data;
  //---------------------------------------------------
  //---------------------------------------------------
  // initial animations
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  //---------------------------------------------------
  //---------------------------------------------------
  let sensorStationModel = false;
  //---------------------------------------------------
  //---------------------------------------------------
  $: console.log(data);
</script>

{#if rendered}
  <SensorstationModal
    data={data?.sensorStations}
    bind:showModal={sensorStationModel}
    on:close={() => (sensorStationModel = false)}
  />

  <section>
    <div class="flex justify-center">
      <button
        class="btn btn-primary"
        on:click={() => (sensorStationModel = true)}>SensorStations</button
      >
    </div>

    <div class="flex justify-center mt-20">
      <div class="gap-4 grid jusitfy-center w-full">
        {#each data?.dashboard?.sensorStations as sensorStation, i (sensorStation.sensorStationId)}
          <div
            in:fly={{ y: -200, duration: 200, delay: 200 * i }}
            out:fly={{ y: -200, duration: 200 }}
          >
            <SensorStation {sensorStation} dates={data.dates} />
          </div>
        {/each}
      </div>
    </div>
    {#if data?.dashboard?.sensorStations.length === 0}
      <div class="flex justify-center mt-20">
        <h1 class="text-2xl font-bold">
          You have no SensorStations in your Dashboard
        </h1>
      </div>
    {/if}
  </section>
{/if}
