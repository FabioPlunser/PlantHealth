<script lang="ts">
  import type {
    ColumnDef,
    TableOptions,
    SortDirection,
  } from "@tanstack/svelte-table";
  import {
    createSvelteTable,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
  } from "@tanstack/svelte-table";
  import { writable } from "svelte/store";
  import RolePills from "./RolePills.svelte";
  import Edit from "$lib/assets/icons/edit.svg?component";
  import SortUp from "$lib/assets/icons/sortUp.svg?component";
  import SortDown from "$lib/assets/icons/sortDown.svg?component";
  import TextCell from "./TextCell.svelte";

  type User = {
    username: string;
    token: string;
    permissions: string[];
    email: string;
    personId: string;
  };

  export let users: User[];
  export let selectedUser: User;
  export let showEditModal: boolean;

  function getSortSymbol(isSorted: boolean | SortDirection) {
    return isSorted ? (isSorted === "asc" ? "up" : "down") : "";
  }

  const defaultColumns: ColumnDef<User>[] = [
    {
      accessorKey: "username",
      header: () => "Username",
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      accessorKey: "email",
      header: () => "Email",
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      accessorKey: "permissions",
      header: () => "Permissions",
      cell: (info) => flexRender(RolePills, { roles: info.getValue() }),
    },
  ];

  const options = writable<TableOptions<User>>({
    data: users,
    columns: defaultColumns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  });

  const table = createSvelteTable(options);
</script>

<table class="table">
  <thead class="">
    {#each $table.getHeaderGroups() as headerGroup}
      <tr>
        {#each headerGroup.headers as header}
          <th colspan={header.colSpan}>
            {#if !header.isPlaceholder}
              <div
                class:cursor-pointer={header.column.getCanSort()}
                class:select-none={header.column.getCanSort()}
                on:click={header.column.getToggleSortingHandler()}
                on:keyup={header.column.getToggleSortingHandler()}
              >
                <svelte:component
                  this={flexRender(
                    header.column.columnDef.header,
                    header.getContext()
                  )}
                />
                {getSortSymbol(header.column.getIsSorted())}
              </div>
              <!--
                  <SortDown class="dark:fill-white fill-black"/>
                -->
            {/if}
          </th>
        {/each}
        <th>EDIT</th>
        <th>DELETE</th>
      </tr>
    {/each}
  </thead>
  <tbody>
    {#each $table.getRowModel().rows as row}
      <tr class="table-row">
        {#each row.getVisibleCells() as cell}
          <td class="table-cell">
            <div class="flex justify-center">
              <!--
              -->
              <svelte:component
                this={flexRender(cell.column.columnDef.cell, cell.getContext())}
              />
            </div>
          </td>
        {/each}
        <td class="table-cell">
          <label class="button">
            <button
              on:click={() => {
                selectedUser = row.original;
                showEditModal = true;
              }}
              class="dark:fill-white hover:fill-gray-500"
              ><Edit class="w-8 " /></button
            >
            <!-- TODO figure out how to change color on hover seems like hover:fill-gray-500 does not work-->
          </label>
        </td>
        <td class="table-cell">
          <div>
            <label class="button">
              <!-- TODO make delete user action with verification Modal-->
              <button
                on:click={() => {
                  selectedUser = row.original;
                }}
                class=""
              >
                <i class="bi bi-trash text-3xl hover:text-red-500 shadow-2xl" />
              </button>
            </label>
          </div>
        </td>
      </tr>
    {/each}
  </tbody>
</table>
