<script lang="ts">
  import { onMount } from "svelte";
  import { fly } from "svelte/transition";
  import Table from "$components/table/Table.svelte";
  import { TextCell } from "$components/table/cellComponents";
  import { flexRender } from "@tanstack/svelte-table";
  import type { ColumnDef } from "@tanstack/svelte-table";
  import { browser } from "$app/environment";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  let frontendData: any[];
  data.streamed.frontend.then((file) => {
    frontendData = file;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------

  // ---------------------------------------------------------
  // ---------------------------------------------------------
  //severity, timeStamp, message, className, callerId
  let columns: ColumnDef<any>[] = [
    {
      id: "timeStamp",
      accessorKey: "timeStamp",
      header: () => flexRender(TextCell, { text: "timeStamp" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "severity",
      accessorKey: "severity",
      header: () => flexRender(TextCell, { text: "severity" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "message",
      accessorKey: "message",
      header: () => flexRender(TextCell, { text: "message" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
  ];
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let mobileColumnVisibility: ColumnVisibility = {
    timeStamp: false,
    className: false,
    callerId: false,
  };
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let infoBadges = [
    {
      icon: "bi bi-router-fill",
      number: data.numbers.numOfConnectedAccessPoints,
      size: 32,
    },
    {
      icon: "bi bi-globe-europe-africa",
      number: data.numbers.numOfConnectedSensorStations,
      size: 34,
    },
    {
      icon: "bi bi-people-fill",
      number: data.numbers.numOfUsers,
      size: 32,
    },
  ];
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let logs = "backend";
</script>

{#if rendered}
  <section class="overflow-auto">
    <div
      class="flex justify-center gap-6 mt-12"
      in:fly={{ y: -200, duration: 400 }}
    >
      {#each infoBadges as badges}
        <div
          class="relative rounded-full border-2 dark:border-none bg-base-100 drop-shadow-xl p-10"
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

    <div class="flex justify-center mx-auto mt-10 overflow-auto">
      <div>
        <h1 class="text-3xl font-bold mx-auto flex justify-center">Logs</h1>
        <div class="btn-group flex justify-center my-2">
          <button
            class="btn {logs === 'backend' ? 'btn-active' : ''}"
            on:click={() => (logs = "backend")}>Backend</button
          >
          <button
            class="btn {logs === 'frontend' ? 'btn-active' : ''}"
            on:click={() => (logs = "frontend")}>Frontend</button
          >
        </div>
        {#if logs === "backend"}
          <Table
            data={data.backend}
            {columns}
            {mobileColumnVisibility}
            pageSizeOptions={[15, 30, 100, data.backend.length]}
          />
        {:else}
          <Table
            data={frontendData}
            {columns}
            {mobileColumnVisibility}
            pageSizeOptions={[15, 30, 100, frontendData.length]}
          />
        {/if}
      </div>
    </div>
  </section>
{/if}

<style>
</style>
