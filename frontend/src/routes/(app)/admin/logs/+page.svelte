<script lang="ts">
  import { fly } from "svelte/transition";
  import Table from "$components/table/Table.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import { TextCell } from "$components/table/cellComponents";
  import { flexRender } from "@tanstack/svelte-table";
  import type { ColumnDef } from "@tanstack/svelte-table";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  $: console.log(data);
  // $: console.log(data);
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let logs = "backend";

  // ---------------------------------------------------------
  // ---------------------------------------------------------
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
    severity: false,
  };
  // ---------------------------------------------------------
  // ---------------------------------------------------------
</script>

{#if rendered}
  <section>
    <div class="flex justify-center mx-auto mt-10 overflow-auto">
      <div class="btn-group flex justify-center my-2">
        <button
          class="btn {logs === 'backend' ? 'btn-active' : ''}"
          on:click={() => (logs = "backend")}
        >
          Backend
        </button>
        <button
          class="btn {logs === 'frontend' ? 'btn-active' : ''}"
          on:click={() => (logs = "frontend")}
        >
          Frontend
        </button>
      </div>
    </div>

    {#if logs === "backend"}
      {#await data.streamed.backendLogs}
        <Spinner />
      {:then backendLogs}
        <div class="overflow-auto">
          <Table
            data={backendLogs}
            {columns}
            {mobileColumnVisibility}
            maxRowSize={backendLogs?.length ?? 0}
          />
        </div>
      {/await}
    {:else}
      {#await data.streamed.frontendLogs}
        <Spinner />
      {:then frontendLogs}
        <div class="overflow-auto">
          <Table
            data={frontendLogs}
            {columns}
            {mobileColumnVisibility}
            maxRowSize={frontendLogs?.length ?? 0}
          />
        </div>
      {/await}
    {/if}
  </section>
{/if}
