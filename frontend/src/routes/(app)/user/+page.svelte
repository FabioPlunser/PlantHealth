<script lang="ts">
  import { fly } from "svelte/transition";
  //---------------------------------------------------
  //---------------------------------------------------
  import {
    SensorStationsModal,
    SensorStation,
  } from "$lib/components/ui/SensorStation/index.js";
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
  let sensorStationModal = false;
  //---------------------------------------------------
  //---------------------------------------------------
  let searchTerm = "";
</script>

{#if rendered}
  <SensorStationsModal
    data={data?.sensorStations}
    bind:showModal={sensorStationModal}
    on:close={() => (sensorStationModal = false)}
  />

  <section>
    <div class="flex justify-center">
      <button
        class="btn btn-primary"
        on:click={() => (sensorStationModal = true)}>SensorStations</button
      >
    </div>

    {#if data?.dashboard?.sensorStations?.length === 0}
      <h1
        class="text-2xl font-bold flex justify-center items-center my-auto mt-2"
      >
        You have no Sensor Stations in your Dashboard yet.
      </h1>
    {:else}
      <div class="m-4 flex justify-ceter">
        <input
          bind:value={searchTerm}
          type="search"
          name="searchRoom"
          placeholder="Global Search"
          class="input dark:input-bordered w-fit min-w-64 mx-auto dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
        />
      </div>
      <div class="grid grid-rows gap-2 mt-2">
        {#each data?.dashboard?.sensorStations as sensorStation, i (sensorStation.sensorStationId)}
          {#if sensorStation.name.includes(searchTerm) || sensorStation.roomName.includes(searchTerm)}
            <div class="">
              <SensorStation {sensorStation} dates={data.dates} />
            </div>
          {/if}
        {/each}
      </div>
    {/if}
  </section>
{/if}
