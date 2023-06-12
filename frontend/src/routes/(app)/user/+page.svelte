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
  let allSensorStations: Responses.SensorStationsInnerResponse[] | null = null;
  let dashBoardSensorStations: SensorStationComponent[] | null = null;
  $: {
    data.streamed.dashBoardSensorStations.then((res) => {
      dashBoardSensorStations = res.sensorStations;
    });
    data.streamed.allSensorStations.then((res) => {
      console.log(res);
      allSensorStations = res.sensorStations;
    });
  }
</script>

{#if rendered}
  <section>
    {#if allSensorStations}
      <SensorStationsModal
        data={allSensorStations}
        bind:showModal={sensorStationModal}
        on:close={() => (sensorStationModal = false)}
      />

      <div class="flex justify-center">
        <button
          class="btn btn-primary"
          on:click={() => (sensorStationModal = true)}>SensorStations</button
        >
      </div>
    {:else}
      <h1 class="flex justify-center">Something went wrong</h1>
    {/if}
    <!-- ----------------------------------------------------------------------- -->
    {#if dashBoardSensorStations}
      {#if dashBoardSensorStations.length === 0}
        <div class="flex justify-center mt-20">
          <h1 class="text-2xl font-bold">
            You have no SensorStations in your Dashboard
          </h1>
        </div>
      {:else}
        <div class="mb-4 flex justify-ceter">
          <input
            bind:value={searchTerm}
            type="search"
            name="searchRoom"
            placeholder="Global Search"
            class="input dark:input-bordered w-fit min-w-64 mx-auto dark:bg-gray-800 bg-gray-200 dark:text-white text-black mt-4"
          />
        </div>
        <div class="flex justify-center mt-20">
          <div class="gap-4 grid jusitfy-center w-full">
            {#each dashBoardSensorStations as sensorStation, i (sensorStation.sensorStationId)}
              {#if sensorStation.name.includes(searchTerm) || sensorStation.roomName.includes(searchTerm)}
                <div
                  in:fly={{ y: -200, duration: 200, delay: 200 * i }}
                  out:fly={{ y: -200, duration: 200 }}
                >
                  <SensorStation {sensorStation} dates={data.dates} />
                </div>
              {/if}
            {/each}
          </div>
        </div>
      {/if}
    {:else}
      <h1 class="flex justify-center">Something went wrong</h1>
    {/if}
  </section>
{/if}
