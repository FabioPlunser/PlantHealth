<script lang="ts">
  import type { ColumnDef, TableOptions } from "@tanstack/svelte-table";
  import {
    createSvelteTable,
    flexRender,
    getCoreRowModel,
  } from "@tanstack/svelte-table";
  import { writable } from "svelte/store";
  import RolePills from "./RolePills.svelte";
  import Edit from "$lib/assets/icons/edit.svg?component";

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

  const defaultColumns: ColumnDef<User>[] = [
    {
      accessorKey: "personId",
      header: () => "ID",
      cell: (info) => info.getValue(),
    },
    {
      accessorKey: "username",
      header: () => "Username",
      cell: (info) => info.getValue(),
    },
    {
      accessorKey: "email",
      header: () => "Email",
      cell: (info) => info.getValue(),
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
              <svelte:component
                this={flexRender(
                  header.column.columnDef.header,
                  header.getContext()
                )}
              />
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
              <svelte:component
                this={flexRender(cell.column.columnDef.cell, cell.getContext())}
              />
            </div>
          </td>
        {/each}
        <td class="table-cell">
          <label class="button ">
            <button
              on:click={() => {
                selectedUser = row.original;
                showEditModal = true;
              }}
              class="hidden"
            />
            <!-- TODO figure out how to change color on hover seems like hover:dark:fill-gray-500 does not work...-->
            <Edit class="w-8 hover:dark:fill-gray-500 dark:fill-white " />
          </label>
        </td>
        <td class="table-cell">
          <div>
            <label class="button">
              <button
                on:click={() => {
                  selectedUser = row.original;
                  showEditModal = true;
                }}
                class="hidden"
              />
              <i class="bi bi-trash text-3xl hover:text-red-500 shadow-2xl" />
            </label>
          </div>
        </td>
      </tr>
    {/each}
  </tbody>
</table>
