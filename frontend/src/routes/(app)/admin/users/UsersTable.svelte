<script lang="ts">
  import type { ColumnDef, TableOptions } from "@tanstack/svelte-table";
  import {
    createSvelteTable,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
    getFilteredRowModel,
  } from "@tanstack/svelte-table";
  import { writable } from "svelte/store";
  import RolePills from "./RolePills.svelte";
  import Edit from "$lib/assets/icons/edit.svg?component";
  import TextCell from "./TextCell.svelte";
  import SortSymbol from "./SortSymbol.svelte";
  import type { NodeJS } from "node:types";

  type User = {
    username: string;
    token: string;
    permissions: string[];
    email: string;
    personId: string;
  };

  export let users: User[];
  export let selectedUser: User;

  const defaultColumns: ColumnDef<User>[] = [
    {
      accessorKey: "username",
      header: () => flexRender(TextCell, { text: "Username" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      accessorKey: "email",
      header: () => flexRender(TextCell, { text: "Email" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      accessorKey: "permissions",
      header: () => flexRender(TextCell, { text: "Permissions" }),
      cell: (info) => flexRender(RolePills, { roles: info.getValue() }),
    },
  ];

  let globalFilter = "";

  const options = writable<TableOptions<User>>({
    data: users,
    columns: defaultColumns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    state: {
      globalFilter,
    },
  });

  function setGlobalFilter(filter: string) {
    globalFilter = filter;
    options.update((old) => {
      return {
        ...old,
        state: {
          ...old.state,
          globalFilter: filter,
        },
      };
    });
  }

  const table = createSvelteTable(options);

  let timer: NodeJS.Timeout;
  function handleSearch(e: Event) {
    clearTimeout(timer);
    timer = setTimeout(() => {
      const target = e.target as HTMLInputElement;
      setGlobalFilter(target.value);
    }, 200);
  }
  const noTypeCheck = (x: any) => x;
</script>

<div>
  <div>
    <input
      type="search"
      class="input border-2 w-full mb-3"
      on:keyup={handleSearch}
      on:search={handleSearch}
      placeholder="Search..."
      {...noTypeCheck(null)}
    />
  </div>
  <div>
    <table class="table border-white">
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
                    <SortSymbol isSorted={header.column.getIsSorted()} />
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
                    this={flexRender(
                      cell.column.columnDef.cell,
                      cell.getContext()
                    )}
                  />
                </div>
              </td>
            {/each}
            <td class="table-cell">
              <a href={`/profile?personid=${row.original.personId}`}>
                <Edit class="w-8 " />
              </a>
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
                    <i
                      class="bi bi-trash text-3xl hover:text-red-500 shadow-2xl"
                    />
                  </button>
                </label>
              </div>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
</div>
