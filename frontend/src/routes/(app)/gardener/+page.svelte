<script lang="ts">
  import { fly } from "svelte/transition";
  import { enhance } from "$app/forms";
  import type { SubmitFunction } from "$app/forms";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import Graphs from "$components/graph/Graphs.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import DateInput from "$components/datepicker/DateInput.svelte";
  import BigPictureModal from "$components/ui/BigPictureModal.svelte";
  import Input from "$components/ui/Input.svelte";
  import Table from "$components/table/Table.svelte";
  import LimitsCard from "$lib/components/ui/SensorStation/LimitsCard.svelte";
  import FormError from "$components/ui/FormError.svelte";
  import {
    SensorStationsModal,
    SensorStation,
    SensorStationDetail,
  } from "$components/ui/SensorStation";
  import { flexRender, type ColumnDef } from "@tanstack/svelte-table";
  import { TextCell } from "$components/table/cellComponents";

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
  $: console.log(data);
  export let form;

  // let sensorStations: any[] = [];
  // $: {
  //   data.streamed.sensorStations.then((res: any) => {
  //     sensorStations = res;
  //   });
  // }
  // ---------------------------------------------------
  // ---------------------------------------------------
  let assignedAdded = false;
  let showPicture = false;
  let selectedPicture: any = null;
  let newDates = data.dates;
  let dateNow = new Date(Date.now()).toLocaleDateString();
  let state = "graph";
  let limitsTable = false;
  let sensorStationsModal = false;
  // ---------------------------------------------------
  // ---------------------------------------------------
  let loading = true;
  const customEnhance: SubmitFunction = () => {
    loading = true;
    return async ({ update }) => {
      await update({ reset: false });
      loading = false;
    };
  };
  // ---------------------------------------------------
  // ---------------------------------------------------
  $: sensorStationsData = {
    dates: data.dates,
    sensorStations: data.streamed.assignedSensorStations,
  };
  // ---------------------------------------------------
  // ---------------------------------------------------
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
        <div class="grid grid-rows gap-2">
          {#each sensorStations as sensorStation, i (sensorStation.sensorStationId)}
            <div in:fly={{ y: -200, duration: 200, delay: 200 * i }}>
              <SensorStation {sensorStation} dates={data.dates} />
            </div>
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
