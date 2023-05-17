<script lang="ts">
  import { fly } from "svelte/transition";
  import { enhance, type SubmitFunction } from "$app/forms";
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
  import { add } from "$lib/components/toast/core/store";
  import UploadPicture from "./uploadPicture.svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------
  // ---------------------------------------------------
  export let data;
  export let form;

  let sensorStations: any[] = [];
  $: {
    data.streamed.sensorStations.then((res: any) => {
      sensorStations = res;
    });
  }
  // ---------------------------------------------------
  // ---------------------------------------------------
  let loading = false;
  let assignedAdded = true;
  let showPicture = false;
  let selectedPicture: any = null;
  let newDates = data.dates;
  let dateNow = new Date(Date.now()).toLocaleDateString();
  let state = "graph";
  let limitsTable = false;
  let sensorStationsModal = false;
  // ---------------------------------------------------
  // ---------------------------------------------------
  const customEnhance: SubmitFunction = () => {
    loading = true;
    return async ({ update }) => {
      await update({ reset: false });
      loading = false;
    };
  };
  // ---------------------------------------------------
  // ---------------------------------------------------
  // let sensorStationsData = {
  //   dates: data.dates,
  //   sensorStations: data.dashboard.assignedSensorStations
  // }
</script>

<!-- 
{#if rendered}
  <BigPictureModal
    bind:imageRef={selectedPicture}
    bind:open={showPicture}
    on:close={() => (showPicture = false)}
  />
  <SensorStationsModal
    data={sensorStations}
    bind:showModal={sensorStationsModal}
    on:close={() => (sensorStationsModal = false)}
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
      <div class="flex justify-center mb-5">
        <button
          class="btn btn-primary"
          on:click={() => {
            sensorStationsModal = true;
          }}>SensorStations</button
        >
      </div>
      {#if data.dashboard.addedSensorStations.length === 0}
        <div class="flex justify-center h-screen">
          <p class="text-xl font-bold">No SensorStations added yet</p>
        </div>
      {/if}
      <div class="grid grid-rows gap-2">
        {#each data.dashboard.addedSensorStations as sensorStation, i (sensorStation.sensorStationId)}
          <div in:fly={{ y: -200, duration: 200, delay: 200 * i }}>
            <SensorStation {sensorStation} dates={data.dates} />
          </div>
        {/each}
      </div>
    {/if}
    {#if assignedAdded}
      <SensorStationDetail data={sensorStationsData} {form}/>
    {/if}
  </section>
{/if} -->
