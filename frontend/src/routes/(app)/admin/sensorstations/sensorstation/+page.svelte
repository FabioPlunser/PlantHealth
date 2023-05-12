<script lang="ts">
  import { toast } from "$components/toast";
  import { fly, slide, fade } from "svelte/transition";
  import { onMount } from "svelte";
  import { enhance } from "$app/forms";
  import type { SubmitFunction } from "$app/forms";
  // ----------------------------------
  // ----------------------------------
  import PictureModal from "./PictureModal.svelte";
  import Desktop from "$helper/Desktop.svelte";
  import BigPictureModal from "$components/ui/BigPictureModal.svelte";
  import { flexRender, type ColumnDef } from "@tanstack/svelte-table";
  import { TextCell } from "$lib/components/table/cellComponents";
  import Table from "$lib/components/table/Table.svelte";
  import StationInfo from "./StationInfo.svelte";
  import LimitsCard from "./LimitsCard.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import SensorLimitsModal from "./SensorLimitsModal.svelte";
  import Graphs from "$components/graph/Graphs.svelte";
  import DateInput from "$components/datepicker/DateInput.svelte";
  // ----------------------------------
  // ----------------------------------
  let rendered = false;
  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
  });
  // ----------------------------------
  // ----------------------------------
  export let data;
  export let form;
  // ----------------------------------
  // ----------------------------------
  let sensorStation: SensorStation;
  $: sensorStation = data.sensorStation;

  let sensorStationData: any = null;
  $: {
    sensorStation.data.then((res: any) => {
      console.log(res);
      sensorStationData = res.data;
    });
  }

  let limits: SensorLimit[];
  $: limits = data.sensorStation.sensorLimits;

  let newDates = data.dates;
  let dateNow = new Date(Date.now()).toLocaleDateString();

  let showLimits = false;
  let showDataTable = false;
  let showGraphs = false;
  let showPictures = false;

  let pictureModal = false;
  let selectedPicture = "";
  // ----------------------------------
  // ----------------------------------
  const customEnhance: SubmitFunction = () => {
    sensorStationData = null;
    return async ({ update }) => {
      await update();
    };
  };
  // ----------------------------------
  // ----------------------------------
  interface SensorData {
    sensor: { [type: string]: string };
    value: number;
    belowLimit: number;
    aboveLimit: number;
    alarm: string;
  }

  let columns: ColumnDef<SensorData>[] = [
    {
      id: "sensorType",
      accessorKey: "sensorType",
      header: () => flexRender(TextCell, { text: "Type" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "value",
      accessorKey: "sensor.values",
      header: () => flexRender(TextCell, { text: "Value" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "belowLimit",
      accessorKey: "belowLimit",
      header: () => flexRender(TextCell, { text: "Above Limit ?" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "aboveLimit",
      accessorKey: "aboveLimit",
      header: () => flexRender(TextCell, { text: "Below Limit ?" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "alarm",
      accessorKey: "alarm",
      header: () => flexRender(TextCell, { text: "Alarm" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
  ];

  let mobileColumnVisibility: ColumnVisibility = {};
  // ----------------------------------
  // ----------------------------------
</script>

{#if rendered}
  <BigPictureModal
    on:close={() => (pictureModal = false)}
    bind:open={pictureModal}
    imageRef={selectedPicture}
  />
  <!-- <PictureModal bind:open={picturesModal} pictures={data.pictures} /> -->

  <section in:fly={{ y: -200, duration: 200 }}>
    <div class="flex justify-center mx-auto">
      <div
        in:fly|self={{ y: -200, duration: 200, delay: 100 }}
        class="flex card p-8 border h-fit w-full bg-base-100 dark:border-none shadow-2xl md:max-w-9/12"
      >
        <StationInfo {sensorStation} {form} gardener={data.gardener} />

        <div in:slide={{ duration: 200 }}>
          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">SensorLimits</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showLimits = !showLimits)}
                >
                  {#if showLimits}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>

            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />

            {#if limits.length === 0}
              <h1>There is no Information about the sensorstation yet.</h1>
            {:else if showLimits}
              <div
                class="mx-auto grid grid-rows md:grid-cols-2 xl:grid-cols-4 gap-4"
              >
                {#each limits as limit}
                  <LimitsCard
                    {limit}
                    sensorStationId={sensorStation.sensorStationId}
                    {form}
                  />
                {/each}
              </div>
            {/if}
          </div>

          <br />

          {#if sensorStationData}
            <div>
              <form
                method="POST"
                action="?/updateFromTo"
                use:enhance={customEnhance}
              />
            </div>
          {/if}

          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">SensorData</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showDataTable = !showDataTable)}
                >
                  {#if showDataTable}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>

            <div
              class="justify-start grid grid-rows sm:flex items-center gap-2"
            >
              <div class="flex gap-2">
                <label class="my-auto">
                  From:
                  <input type="hidden" name="from" bind:value={newDates.from} />
                  {#key sensorStation}
                    <DateInput
                      format="dd.MM.yyyy"
                      placeholder={dateNow}
                      bind:value={newDates.from}
                    />
                  {/key}
                </label>
              </div>
              <div class="flex gap-2">
                <label class="my-auto">
                  To:
                  <input type="hidden" name="to" bind:value={newDates.to} />
                  {#key sensorStation}
                    <DateInput
                      format="dd.MM.yyyy"
                      placeholder={dateNow}
                      bind:value={newDates.to}
                    />
                  {/key}
                </label>
              </div>
              <div class="my-auto flex items-center justify-center mt-6">
                <button
                  class="btn btn-primary flex items-center justify-center"
                  type="submit"
                >
                  Update
                </button>
              </div>
            </div>
            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />
            {#if showDataTable}
              {#if !sensorStationData}
                <div>
                  <Spinner
                    fill="fill-primary"
                    background="text-base-300 dark:text-white"
                  />
                </div>
              {:else if sensorStationData.length === 0}
                <h1>No sensor station data available yet.</h1>
              {:else}
                <div class="overflow-auto">
                  <table class="table table-zebra w-full">
                    <thead>
                      <tr>
                        <th>SensorType</th>
                        <th>TimeStamp</th>
                        <th>Value</th>
                        <th>Below Limit ?</th>
                        <th>Above Limit ?</th>
                        <th>Alarm</th>
                      </tr>
                    </thead>
                    <tbody>
                      {#each sensorStationData as sensor}
                        {#each sensor.values as value}
                          <tr>
                            <th>{sensor.sensorType}</th>
                            <td>{new Date(value.timeStamp).toLocaleString()}</td
                            >
                            <td>{value.value}</td>
                            <td>{value.belowLimit}</td>
                            <td>{value.aboveLimit}</td>
                            <td>{value.alarm}</td>
                          </tr>
                        {/each}
                      {/each}
                    </tbody>
                    <!-- <Table data={sensorStationData} {columns} {mobileColumnVisibility} /> -->
                  </table>
                </div>
              {/if}
            {/if}
          </div>

          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">Graphs</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showGraphs = !showGraphs)}
                >
                  {#if showGraphs}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>
            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />
            {#if showGraphs}
              {#if !sensorStationData}
                <Spinner
                  fill="fill-primary"
                  background="text-base-300 dark:text-white"
                />
              {:else if sensorStationData.length == 0}
                <h1>No sensor station data available yet.</h1>
              {:else}
                <Graphs data={sensorStationData} />
              {/if}
            {/if}
          </div>

          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">Pictures</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showPictures = !showPictures)}
                >
                  {#if showPictures}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>
            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />

            {#if showPictures}
              {#if sensorStation.pictures.length > 0}
                <div
                  class="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4"
                >
                  {#if sensorStation.pictures}
                    {#each sensorStation?.pictures as picturePromise}
                      {#await picturePromise}
                        <Spinner fill="fill-primary" />
                      {:then picture}
                        <div>
                          <!-- svelte-ignore a11y-click-events-have-key-events -->
                          <img
                            on:click={() => {
                              selectedPicture = picture.imageRef;
                              pictureModal = true;
                            }}
                            src={picture.imageRef}
                            alt="SensorStation"
                            class="rounded-2xl shadow-xl cursor-pointer"
                          />
                          <h1 class="flex justify-center">
                            {picture.creationDate.toDateString()}
                          </h1>
                        </div>
                      {:catch error}
                        <h1>Error: {error.message}</h1>
                      {/await}
                    {/each}
                  {/if}
                </div>
              {:else}
                <h1>No pictures available yet.</h1>
              {/if}
            {/if}
          </div>
        </div>
      </div>
    </div>
  </section>
{/if}
