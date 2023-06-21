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
  let allSensorStations: Responses.SensorStationsInnerResponse[] | null | [] =
    null;
  let dashBoardSensorStations: SensorStationComponent[] | null | [] = null;
  let assignedSensorStations: SensorStationDetailComponent[] | null | [] = null;

  $: {
    data.streamed.allSensorStations.then((res) => {
      allSensorStations = res.sensorStations;
    });
    data.streamed.dashBoardSensorStations.then((res) => {
      dashBoardSensorStations = res.sensorStations;
    });
    data.streamed.assignedSensorStations.then((res) => {
      let temp: any = [];
      if (res.length > 0) {
        for (let sensorStation of res) {
          let newStation = {
            streamed: {
              sensorStation: sensorStation,
            },
            dates: data.dates,
          };
          temp.push(newStation);
        }
      }
      assignedSensorStations = temp;
    });
  }
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
      {#if allSensorStations}
        <SensorStationsModal
          data={allSensorStations}
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
      {:else}
        <Spinner />
      {/if}

      {#if dashBoardSensorStations}
        {#if dashBoardSensorStations.length === 0}
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
          {#each dashBoardSensorStations as sensorStation, i (sensorStation.sensorStationId)}
            {#if sensorStation.name.includes(searchTerm) || sensorStation.roomName.includes(searchTerm)}
              <div in:fly={{ y: -200, duration: 200, delay: 200 * i }}>
                <SensorStation {sensorStation} dates={data.dates} />
              </div>
            {/if}
          {/each}
        </div>
      {:else}
        <Spinner />
      {/if}
    {/if}

    {#if assignedAdded}
      {#if assignedSensorStations}
        {#if assignedSensorStations.length === 0}
          <h1 class="text-xl font-bold text-center">
            No SensorStations assigned yet
          </h1>
        {:else}
          <div class="grid grid-row gap-4">
            {#each assignedSensorStations as sensorStation, i (i)}
              <SensorStationDetail data={sensorStation} {form} />
            {/each}
          </div>
        {/if}
      {:else}
        <Spinner />
      {/if}
    {/if}
  </section>
{/if}
