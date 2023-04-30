<script lang="ts">
  import type { ColumnDef, TableOptions } from "@tanstack/svelte-table";
  import {
    createSvelteTable,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
  } from "@tanstack/svelte-table";
  import { writable } from "svelte/store";
  import RolePills from "./RolePills.svelte";
  import Edit from "$lib/assets/icons/edit.svg?component";
  import TextCell from "./TextCell.svelte";
  import SortSymbol from "./SortSymbol.svelte";
  import type { NodeJS } from "node:types";
  import Input from "$lib/components/ui/Input.svelte";
  import { page } from "$app/stores";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";
  import { enhance } from "$app/forms";

  let isRendered = false;

  onMount(() => {
    isRendered = true;
  });

  type User = {
    personId: string;
    username: string;
    token: string;
    permissions: string[];
    email: string;
  };

  export let users: User[];

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

  let options;

  $: options = writable<TableOptions<User>>({
    data: users,
    columns: defaultColumns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    state: {
      globalFilter,
      pagination: {
        pageSize: 5,
        pageIndex: 0,
      },
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

  function setCurrentPage(page: number) {
    options.update((old: any) => {
      return {
        ...old,
        state: {
          ...old.state,
          pagination: {
            ...old.state?.pagination,
            pageIndex: page,
          },
        },
      };
    });
  }

  function setPageSize(e: Event) {
    const target = e.target as HTMLInputElement;
    options.update((old: any) => {
      return {
        ...old,
        state: {
          ...old.state,
          pagination: {
            ...old.state?.pagination,
            pageSize: parseInt(target.value),
          },
        },
      };
    });
  }

  let table;
  $: table = createSvelteTable(options);

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

{#if isRendered}
  <div>
    <div class="mb-3" in:slide={{ duration: 400, axis: "y" }}>
      <input
        type="search"
        on:keyup={handleSearch}
        on:search={handleSearch}
        placeholder="Global Search..."
        {...noTypeCheck(null)}
        class="input dark:input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
      />
    </div>
    <div in:slide={{ duration: 400, axis: "y" }}>
      <table class="table border-white w-full">
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
                <a
                  href={`/profile?personId=${row.original.personId}&username=${row.original.username}&userPermissions=${row.original.permissions}&source=${$page.url}`}
                >
                  <i class="bi bi-pencil-square text-3xl hover:text-gray-500" />
                </a>
              </td>
              <td class="table-cell">
                <div>
                  <label class="button">
                    <!-- TODO make delete user action with verification Modal-->
                    <!--on click should call deleteUser action and pass Id-->
                    <form method="POST" action="?/deleteUser" use:enhance>
                      <input
                        type="hidden"
                        bind:value={row.original.personId}
                        name="personId"
                      />
                      <button type="submit" class="">
                        <i class="bi bi-trash text-3xl hover:text-red-500" />
                      </button>
                    </form>
                  </label>
                </div>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>

    <div class=" justify-center flex align-items-center btn-group mt-3">
      <button
        class="btn"
        on:click={() => setCurrentPage(0)}
        class:is-disabled={!$table.getCanPreviousPage()}
        disabled={!$table.getCanPreviousPage()}
      >
        {"<<"}
      </button>
      <button
        class="btn"
        on:click={() =>
          setCurrentPage($table.getState().pagination.pageIndex - 1)}
        class:is-disabled={!$table.getCanPreviousPage()}
        disabled={!$table.getCanPreviousPage()}
      >
        {"<"}
      </button>
      <span class="btn">
        Page
        {$table.getState().pagination.pageIndex + 1}
        {" "}of{" "}
        {$table.getPageCount()}
      </span>
      <button
        class="btn"
        on:click={() =>
          setCurrentPage($table.getState().pagination.pageIndex + 1)}
        class:is-disabled={!$table.getCanNextPage()}
        disabled={!$table.getCanNextPage()}
      >
        {">"}
      </button>
      <button
        class="btn"
        on:click={() => setCurrentPage($table.getPageCount() - 1)}
        class:is-disabled={!$table.getCanNextPage()}
        disabled={!$table.getCanNextPage()}
      >
        {">>"}
      </button>
      <select
        value={$table.getState().pagination.pageSize}
        on:change={setPageSize}
        class="btn"
      >
        {#each [5, 10, 25, 50] as pageSize}
          <option value={pageSize}>
            Show {pageSize}
          </option>
        {/each}
      </select>
      <span class="btn"
        >{$table.getPrePaginationRowModel().rows.length} total Rows</span
      >
    </div>
  </div>
{/if}
