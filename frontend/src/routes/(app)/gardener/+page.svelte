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
  import LimitsCard from "$components/ui/SensorStation/LimitsCard.svelte";
  import FormError from "$components/ui/FormError.svelte";
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
  export let form;
  $: console.log(data);
  $: console.log(form);
  // ---------------------------------------------------
  // ---------------------------------------------------
  let loading = false;
  let showPicture = false;
  let newDates = data.dates;
  let dateNowe = new Date(Date.now()).toLocaleDateString();
  let state = "graph";
  let limitsTable = false;
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
  // let columns: ColumnDef<SensorValue>[] = [
  //   {
  //     id: "sensorType",
  //     accessorKey: "sensor.type",
  //     header: () => flexRender(TextCell, { text: "Type" }),
  //     cell: (info) =>
  //       flexRender(SensorTypeBadgeCell, { type: info.getValue() }),
  //   },
  //   {
  //     id: "value",
  //     accessorKey: "value",
  //     header: () => flexRender(TextCell, { text: "Value" }),
  //     cell: ({ row }) =>
  //       flexRender(SensorValueCell, {
  //         value: row.original.value,
  //         unit: row.original.sensor.unit,
  //       }),
  //   },
  //   {
  //     id: "isAboveLimit",
  //     accessorKey: "isAboveLimit",
  //     header: () => flexRender(TextCell, { text: "Below Limit ?" }),
  //     cell: (info) => flexRender(TextCell, { text: info.getValue() }),
  //   },
  //   {
  //     id: "isBelowLimit",
  //     accessorKey: "isBelowLimit",
  //     header: () => flexRender(TextCell, { text: "Above Limit ?" }),
  //     cell: (info) => flexRender(TextCell, { text: info.getValue() }),
  //   },
  //   {
  //     id: "alarm",
  //     accessorKey: "alarm",
  //     header: () => flexRender(TextCell, { text: "Alarm" }),
  //     cell: (info) => flexRender(TextCell, { text: info.getValue() }),
  //   },
  //   {
  //     id: "timestamp",
  //     accessorKey: "timestamp",
  //     header: () => flexRender(TextCell, { text: "Time" }),
  //     cell: ({ row }) =>
  //       flexRender(LocaleDateCell, { date: row.original.timeStamp }),
  //   },
  // ];

  // let mobileColumnVisibility: ColumnVisibility = {
  //   isAboveLimit: false,
  //   isBelowLimit: false,
  //   alarm: false,
  //   timestamp: false,
  // };
  // ----------------------------------
  // ----------------------------------
</script>

{#if rendered}
  <section class="mt-12">
    <div class="grid grid-rows gap-2">
      {#each data.dashboard.sensorStations as sensorStation, i (sensorStation.sensorStationId)}
        <div class="m-0 p-0 w-full sm:max-w-10/12 2xl:max-w-8/12 mx-auto">
          <div
            class="card bg-base-100 shadow-2xl rounded-2xl p-4 justify-center mx-auto"
          >
            <form
              method="POST"
              action="?/updateSensorStation"
              use:enhance={customEnhance}
            >
              <input
                type="hidden"
                name="sensorStationId"
                value={sensorStation.sensorStationId}
              />
              <h1 class="text-xl font-bold">Room: {sensorStation.roomName}</h1>
              <div class="flex justify-center">
                <div class="grid grid-rows md:grid-cols-2 gap-2">
                  <label class="">
                    <h1 class="label-text text-xl font-bold">Name:</h1>
                    <input
                      class="input input-bordered dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
                      type="text"
                      name="name"
                      value={sensorStation.name}
                    />
                    <FormError
                      field="name"
                      {form}
                      id={sensorStation.sensorStationId}
                    />
                  </label>
                  <label
                    class="tooltip"
                    data-tip="Disclaimer: Because of the specification this sets the transfer interval between the access point and the backend so every sensor station from the same access point in your dashboard will be updated"
                  >
                    <h1 class="label-text text-xl font-bold">
                      TransferInterval [s]:
                    </h1>
                    <input
                      class="input input-bordered dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
                      type="number"
                      name="transferInterval"
                      value={sensorStation.transferInterval}
                    />
                    <FormError
                      field="transferInterval"
                      {form}
                      id={sensorStation.sensorStationId}
                    />
                  </label>
                </div>
              </div>
              <button
                class="btn btn-primary flex justify-center w-min mx-auto mt-2"
                type="submit">Update</button
              >
            </form>

            {#if !sensorStation.unlocked}
              <h1 class="text-2xl font-bold flex justify-center">
                SensorStation is locked
              </h1>
            {:else if sensorStation.deleted}
              <h1 class="text-2xl font-bold flex justify-center">
                SensorStation got deleted
              </h1>
            {:else}
              <div>
                {#if loading}
                  <Spinner />
                {:else}
                  <div
                    class="btn-group bg-base-100 flex justify-center mb-4 mt-4"
                  >
                    <button
                      data-tip="Show Graphs"
                      class="btn tooltip bg-base-200 dark:bg-gray-500 border border-base-300 hover:bg-primary {state ===
                      'graph'
                        ? 'btn-active dark:btn-active'
                        : ''}"
                      on:click={() => (state = "graph")}
                    >
                      <span
                        class="material-symbols-outlined text-4xl text-black dark:text-white"
                      >
                        monitoring
                      </span>
                    </button>
                    <button
                      data-tip="Show Table"
                      class="btn tooltip bg-base-200 dark:bg-gray-500 border border-base-300 hover:bg-primary {state ===
                      'table'
                        ? 'btn-active dark:btn-active'
                        : ''}"
                      on:click={() => (state = "table")}
                    >
                      <span
                        class="material-symbols-outlined text-4xl text-black dark:text-white"
                      >
                        table_rows
                      </span>
                    </button>
                    <button
                      data-tip="Show Limits"
                      class="btn tooltip bg-base-200 dark:bg-gray-500 border border-base-300 hover:bg-primary {state ===
                      'limits'
                        ? 'btn-active dark:btn-active'
                        : ''}"
                      on:click={() => (state = "limits")}
                    >
                      <span
                        class="material-symbols-outlined text-3xl text-black dark:text-white"
                      >
                        nest_thermostat_zirconium_eu
                      </span>
                    </button>
                    <button
                      data-tip="Show Pictures"
                      class="btn tooltip bg-base-200 dark:bg-gray-500 border border-base-300 hover:bg-primary {state ===
                      'pictures'
                        ? 'btn-active dark:btn-active'
                        : ''}"
                      on:click={() => (state = "pictures")}
                    >
                      <span
                        class="material-symbols-outlined text-2xl text-black dark:text-white"
                      >
                        <span class="material-symbols-outlined">
                          photo_library
                        </span>
                      </span>
                    </button>
                  </div>
                  {#if state === "graph"}
                    <Graphs data={sensorStation.data} />
                  {:else if state === "table"}
                    <h1>Table</h1>
                  {:else if state === "limits"}
                    {#if sensorStation.limits.length === 0}
                      <h1 class="flex justify-center text-3xl font-bold">
                        No Limits
                      </h1>
                    {:else}
                      {#await sensorStation.limits}
                        <Spinner />
                      {:then limits}
                        <div
                          class="grid grid-rows sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-5 gap-4"
                        >
                          {#each limits as limit, i}
                            <LimitsCard
                              {limit}
                              sensorStationId={sensorStation.sensorStationId}
                              {form}
                            />
                          {/each}
                        </div>
                      {:catch error}
                        <p class="text-red-500">{error.message}</p>
                      {/await}
                    {/if}
                  {:else if state === "pictures"}
                    <div class="grid grid-cols gap-2">
                      {#if sensorStation.pictures.length === 0}
                        <h1 class="flex justify-center text-3xl font-bold">
                          No Pictures
                        </h1>
                      {:else}
                        {#each sensorStation.pictures as picture, i (picture.pictureId)}
                          {#await picture}
                            <Spinner />
                          {:then data}
                            <div>
                              <img
                                src={data.imageRef}
                                alt="SensorStationPicture: {i}"
                                class="rounded-2xl shadow-xl cursor-pointer "
                              />
                              <h1 class="flex justify-center">
                                {data.creationDate.toLocaleDateString()}
                              </h1>
                            </div>
                          {:catch error}
                            <p class="text-red-500">{error.message}</p>
                          {/await}
                        {/each}
                      {/if}
                    </div>
                  {/if}
                {/if}
              </div>
            {/if}
            <div />
          </div>
        </div>
      {/each}
    </div>
  </section>
{/if}
