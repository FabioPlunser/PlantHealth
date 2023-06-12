<!--
  @component
  This component displays the given data in a table.

  @template T - The type of data in the table.
  @param data \{Array\<T\>} - The data for the table
  @param columns \{Array\<ColumnDef\<T\>\>} - The format for the columns you want to display
  @param defaultColumnVisibility \{ColumnVisibility} - default: {} An array containing the columns that should not be displayed on desktop
  @param mobileColumnVisibility \{ColumnVisibility} - An array containing the columns that should not be displayed on mobile or tablet based on the following media query: `(min-width: 580px)`
  @param paginationOptions \{Array\<number\>} - default: [5, 10, 20, 40] An array containing the number of rows that should be choosable for the table
  @example
  ``` html
    <script lang="ts">
	import { tailwind } from 'tailwindcss';
      export let data : {
        users: User[],
      };
      let columns: ColumnDef<User>[] = [
        {
          id: "username",
          accessorKey: "username",
          header: () => flexRender(TextCell, { text: "Username" }),
          cell: (info) => flexRender(TextCell, { text: info.getValue() }),
        },
        {
          id: "delete",
          accessorKey: "_", // NOTE: blanc accessor so we get the row
          header: () => flexRender(TextCell, { text: "Delete" }),
          cell: ({ row }) =>
            flexRender(FormActionButtonConfirm, {
              method: "POST",
              action: "?/deleteUser",
              formActionValue: row.original.personId,
              confirmMessage: `You will delete this user ${row.original.username.toUpperCase()} permanently!`,
              iconClass: "bi bi-trash text-3xl hover:text-red-500",
            }
          ),
        },  
      ];
      let mobileColumnVisibility: ColumnVisibility = {
        email: false,
        permissions: false,
      };
    </script>
    <Table data={data.users} {columns} {mobileColumnVisibility}/>
  ```
  
-->
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
  import SortSymbol from "./SortSymbol.svelte";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";

  let query = "(min-width: 580px)";
  let isRendered = false;
  let mql: any;
  let mqlListener: any;
  let wasMounted = false;
  let isMediaNotMobile = false;

  onMount(() => {
    isRendered = true;
    wasMounted = true;
    return () => {
      removeActiveListener();
    };
  });

  $: {
    if (wasMounted) {
      removeActiveListener();
      addNewListener(query);
    }
  }

  function addNewListener(query: any) {
    mql = window.matchMedia(query);
    mqlListener = (v: any) => (isMediaNotMobile = v.matches);
    mql.addListener(mqlListener);
    isMediaNotMobile = mql.matches;
  }

  function removeActiveListener() {
    if (mql && mqlListener) {
      mql.removeListener(mqlListener);
    }
  }

  type T = $$Generic;

  /**
   * The array containing the data to be displayed in the table.
   * @template T - The type of data in the array.
   * @type {Array\<T\>}
   */
  export let data: T[];
  /**
   * An array of column definitions for the table.
   * @template T - The type of data in the columns.
   * @type {Array\<ColumnDef\<T\>\>}
   * @example 
   * ```javascript
      interface User {
        personId: string;
        username: string;
        token: string;
        permissions: string[];
        email: string;
      }
      
      let columns: ColumnDef<User>[] = [
        {
          id: "username",
          accessorKey: "username",
          header: () => flexRender(TextCell, { text: "Username" }),
          cell: (info) => flexRender(TextCell, { text: info.getValue() }),
        },
        {
          id: "delete",
          accessorKey: "_", // NOTE: blanc accessor so we get the row
          header: () => flexRender(TextCell, { text: "Delete" }),
          cell: ({ row }) =>
            flexRender(FormActionButtonConfirm, {
              method: "POST",
              action: "?/deleteUser",
              formActionValue: row.original.personId,
              confirmMessage: `You will delete this user ${row.original.username.toUpperCase()} permanently!`,
              iconClass: "bi bi-trash text-3xl hover:text-red-500",
            }
          ),
        },  
      ];
   *  ```
   */
  export let columns: ColumnDef<T>[];
  /**
   * column visibility if media is not mobile
   * @default {}
   * @type {ColumnVisibility}
   */
  export let defaultColumnVisibility: ColumnVisibility = {};
  /**
   * column visibility if media is mobile
   * @type {ColumnVisibility}
   */
  export let mobileColumnVisibility: ColumnVisibility;

  /**
   * An array of selectable row sizes for the table.
   * @type {Array\<number\>}
   * @default [5, 10, 20, 40]
   * @example
   *```javascript
   *  let rowSize: number[] = [5, 10, 20]
   *```
   * Allows the user to choose between 5, 10, or 20 rows to be displayed per page.
   */
  export let paginationOptions: number[] = [5, 10, 20, 40];

  $: columnVisibility = isMediaNotMobile
    ? defaultColumnVisibility
    : mobileColumnVisibility;

  let globalFilter = "";

  $: options = writable<TableOptions<any>>({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    state: {
      globalFilter,
      columnVisibility,
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
      <table class="myTable">
        <thead class="">
          {#each $table.getHeaderGroups() as headerGroup}
            <tr class="">
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
                  {/if}
                </th>
              {/each}
            </tr>
          {/each}
        </thead>
        <tbody>
          {#each $table.getRowModel().rows as row}
            <tr class="">
              {#each row.getVisibleCells() as cell}
                <td class="">
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
            </tr>
          {/each}
        </tbody>
      </table>
    </div>

    <div class=" justify-center flex align-items-center btn-group mt-3">
      <button
        class="btn"
        type="button"
        on:click={() => setCurrentPage(0)}
        class:is-disabled={!$table.getCanPreviousPage()}
        disabled={!$table.getCanPreviousPage()}
      >
        {"<<"}
      </button>
      <button
        class="btn"
        type="button"
        on:click={() =>
          setCurrentPage($table.getState().pagination.pageIndex - 1)}
        class:is-disabled={!$table.getCanPreviousPage()}
        disabled={!$table.getCanPreviousPage()}
      >
        {"<"}
      </button>
      <span class="btn">
        {$table.getState().pagination.pageIndex + 1}
        /
        {$table.getPageCount()}
      </span>
      <button
        class="btn"
        type="button"
        on:click={() =>
          setCurrentPage($table.getState().pagination.pageIndex + 1)}
        class:is-disabled={!$table.getCanNextPage()}
        disabled={!$table.getCanNextPage()}
      >
        {">"}
      </button>
      <button
        class="btn"
        type="button"
        on:click={() => setCurrentPage($table.getPageCount() - 1)}
        class:is-disabled={!$table.getCanNextPage()}
        disabled={!$table.getCanNextPage()}
      >
        {">>"}
      </button>
      {#if isMediaNotMobile}
        <select
          value={$table.getState().pagination.pageSize}
          on:change={setPageSize}
          class="btn"
        >
          {#each paginationOptions as pageSize}
            <option value={pageSize}>
              Show {pageSize}
            </option>
          {/each}
        </select>
        <span class="btn">
          {$table.getPrePaginationRowModel().rows.length} total Rows
        </span>
      {/if}
    </div>
  </div>
{/if}

<style>
  .myTable {
    position: relative;
    width: 100%;
    text-align: left;
    font-size: 0.875rem;
    line-height: 1.25rem;
    --tw-bg-opacity: 1;
    background-color: hsl(var(--b1) / var(--tw-bg-opacity));
    border-radius: 1rem !important;
  }

  .myTable td,
  th {
    padding-left: 1rem;
    padding-right: 1rem;
    padding-top: 0.75rem;
    padding-bottom: 0.75rem;
    vertical-align: middle;
    border-bottom: 2px solid hsl(var(--b2));
  }
  /* .myTable tbody tr:hover {
    background-color: hsl(var(--p) / var(--tw-bg-opacity)) !important;
    border-radius: 10rem !important;
  } */
  .myTable thead th:first-child {
    border-radius: 1rem 0rem 0rem 0rem !important;
  }
  .myTable thead th:last-child {
    border-radius: 0rem 1rem 0rem 0rem !important;
  }

  .myTable tbody tr:last-child td:first-child {
    border-radius: 0rem 0rem 0rem 1rem !important;
  }
  .myTable tbody tr:last-child td:last-child {
    border-radius: 0rem 0rem 1rem 0rem !important;
  }
  /* .myTable tbody tr:nth-child(odd) {
    background-color: hsl(var(--b1));
  } */
  .myTable thead {
    background-color: hsl(var(--b3));
  }
</style>
