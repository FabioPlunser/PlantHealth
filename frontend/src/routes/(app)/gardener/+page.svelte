<script lang="ts">
  import { fly } from "svelte/transition";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import Spinner from "$components/ui/Spinner.svelte";
  import BigPictureModal from "$components/ui/BigPictureModal.svelte";
  import {
    SensorStationsModal,
    SensorStation,
    SensorStationDetail,
  } from "$components/ui/sensorStation";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------
  // ---------------------------------------------------
  export let data;
  export let form;
  // ---------------------------------------------------
  // ---------------------------------------------------
  let assignedAdded = false;
  let showPicture = false;
  let selectedPicture: any = null;
  let sensorStationsModal = false;
  // ---------------------------------------------------
  // ---------------------------------------------------
  $: sensorStationsData = {
    dates: data.dates,
    sensorStations: data.streamed.assignedSensorStations,
  };
  // ---------------------------------------------------
  // ---------------------------------------------------
  let searchTerm = "";
</script>

{#if rendered}
  <BigPictureModal
    bind:imageRef={selectedPicture}
    bind:open={showPicture}
    on:close={() => (showPicture = false)}
  />

  <section class="mt-12">
    <div class="flex justify-center gap-4 mb-5">
      <button
        class="text-xl font-bold hover:text-primary {!assignedAdded
          ? 'underline'
          : ''}"
        on:click={() => (assignedAdded = false)}>Added Stations</button
      >
      <button
        class="text-xl font-bold hover:text-primary {assignedAdded
          ? 'underline'
          : ''}"
        on:click={() => (assignedAdded = true)}>Assigned Stations</button
      >
    </div>
    {#if !assignedAdded}
      {#await data.streamed.allSensorStations}
        <Spinner />
      {:then sensorStations}
        <SensorStationsModal
          data={sensorStations}
          bind:showModal={sensorStationsModal}
          on:close={() => (sensorStationsModal = false)}
        />
        <div class="flex justify-center mb-5">
          <button
            class="btn btn-primary"
            on:click={() => {
              sensorStationsModal = true;
            }}>SensorStations</button
          >
        </div>
      {:catch err}
        <p class="text-red">{err}</p>
      {/await}

      {#await data.streamed.dashBoardSensorStations}
        <Spinner />
      {:then sensorStations}
        {#if sensorStations.length === 0}
          <div class="flex justify-center h-screen">
            <p class="text-xl font-bold">No SensorStations added yet</p>
          </div>
        {/if}
        <div class="mb-4 flex justify-ceter">
          <input
            bind:value={searchTerm}
            type="search"
            name="searchRoom"
            placeholder="Global Search"
            class="input dark:input-bordered w-fit min-w-64 mx-auto dark:bg-gray-800 bg-gray-200 dark:text-white text-black mt-4"
          />
        </div>
        <div class="grid grid-rows gap-2">
          {#each sensorStations as sensorStation, i (sensorStation.sensorStationId)}
            {#if sensorStation.name.includes(searchTerm) || sensorStation.roomName.includes(searchTerm)}
              <div in:fly={{ y: -200, duration: 200, delay: 200 * i }}>
                <SensorStation {sensorStation} dates={data.dates} />
              </div>
            {/if}
          {/each}
        </div>
      {:catch err}
        <p class="text-red">{err}</p>
      {/await}
    {/if}

    {#if assignedAdded}
      <SensorStationDetail data={sensorStationsData} {form} />
    {/if}
  </section>
{/if}
