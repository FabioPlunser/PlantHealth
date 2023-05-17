<script lang="ts">
  import { onMount } from "svelte";
  import { fly } from "svelte/transition";
  import Spinner from "$components/ui/Spinner.svelte";
  import {
    SensorStationsModal,
    SensorStation,
  } from "$components/ui/sensorStation";
  // import SensorStation from "./SensorStation.svelte";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  $: console.log(data);
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let showSensorStationsModal = false;
  let infoBadges = [
    {
      icon: "bi bi-router-fill",
      number: data.numbers.accessPoints,
      size: 32,
    },
    {
      icon: "bi bi-globe-europe-africa",
      number: data.numbers.sensorStations,
      size: 34,
    },
    {
      icon: "bi bi-people-fill",
      number: data.numbers.users,
      size: 32,
    },
  ];
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let logs = "backend";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let searchTerm = "";
</script>

{#if rendered}
  <section class="w-full h-full mt-4">
    <div class="flex justify-center gap-6" in:fly={{ y: -200, duration: 400 }}>
      {#each infoBadges as badges}
        <div
          class="relative rounded-full border-2 dark:border-none bg-base-100 shadow-md p-10"
        >
          <div class="mx-auto top-1 absolute -ml-[17px]">
            <i class="{badges.icon} mx-auto justify-center text-4xl" />
            <h1 class="flex justify-center rounded-full m-0 p-0 text-xl">
              {badges.number}
            </h1>
          </div>
        </div>
      {/each}
    </div>

    {#await data.streamed.allSensorStations}
      <Spinner fill="fill-primary" />
    {:then sensorStations}
      <div>
        <SensorStationsModal
          data={sensorStations}
          bind:showModal={showSensorStationsModal}
          on:close={() => (showSensorStationsModal = false)}
        />
      </div>
      <div class="flex justify-center mx-auto mt-2">
        <button
          class="btn btn-primary flex justify-center"
          on:click={() => (showSensorStationsModal = true)}
          >SensorStation</button
        >
      </div>
    {:catch error}
      <p class="text-red">Something went wrong: {JSON.stringify(error)}</p>
    {/await}

    {#await data.streamed.dashboardSensorStations}
      <Spinner fill="fill-primary" />
    {:then sensorStations}
      {#if sensorStations?.length === 0}
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
          {#each sensorStations as sensorStation, i (sensorStation.sensorStationId)}
            {#if sensorStation.name.includes(searchTerm) || sensorStation.roomName.includes(searchTerm)}
              <div class="">
                <SensorStation {sensorStation} dates={data.dates} />
              </div>
            {/if}
          {/each}
        </div>
      {/if}
    {:catch error}
      <p class="text-red">Something went wrong: {JSON.stringify(error)}</p>
    {/await}
  </section>
{/if}
