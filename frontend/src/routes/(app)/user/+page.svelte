<script lang="ts">
  import { fly } from "svelte/transition";
  //---------------------------------------------------
  //---------------------------------------------------
  import {
    SensorStationsModal,
    SensorStation,
  } from "$components/ui/sensorStation";

  import Spinner from "$components/ui/Spinner.svelte";
  //---------------------------------------------------
  //---------------------------------------------------
  export let data;
  //---------------------------------------------------
  //---------------------------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  //---------------------------------------------------
  //---------------------------------------------------
  let sensorStationModal = false;
  //---------------------------------------------------
  //---------------------------------------------------
  let searchTerm = "";
</script>

{#if rendered}
  <section>
    {#await data.streamed.allSensorStations}
      <Spinner />
    {:then sensorStations}
      <SensorStationsModal
        data={sensorStations}
        bind:showModal={sensorStationModal}
        on:close={() => (sensorStationModal = false)}
      />

      <div class="flex justify-center">
        <button
          class="btn btn-primary"
          on:click={() => (sensorStationModal = true)}>SensorStations</button
        >
      </div>
    {/await}

    {#await data.streamed.dashBoardSensorStations}
      <Spinner />
    {:then sensorStations}
      {#if sensorStations.length === 0}
        <div class="flex justify-center mt-20">
          <h1 class="text-2xl font-bold">
            You have no SensorStations in your Dashboard
          </h1>
        </div>
      {:else}
        <div class="flex justify-center mt-20">
          <div class="gap-4 grid jusitfy-center w-full">
            {#each sensorStations as sensorStation, i (sensorStation.sensorStationId)}
              <div
                in:fly={{ y: -200, duration: 200, delay: 200 * i }}
                out:fly={{ y: -200, duration: 200 }}
              >
                <SensorStation {sensorStation} dates={data.dates} />
              </div>
            {/each}
          </div>
        </div>
      {/if}
    {/await}
  </section>
{/if}
