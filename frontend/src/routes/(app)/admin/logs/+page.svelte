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
    {#await data.streamed.backendLogs}
      <Spinner />
    {:then backendLogs}
      <div class="overflow-auto">
        <Table data={backendLogs} {columns} {mobileColumnVisibility} />
      </div>
    {/await}
  </section>
{/if}
