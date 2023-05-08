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
  import type { NodeJS } from "node:types";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";

  let query = "(min-width: 580px)";
  let isRendered = false;
  let mql;
  let mqlListener;
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

  function addNewListener(query) {
    mql = window.matchMedia(query);
    mqlListener = (v) => (isMediaNotMobile = v.matches);
    mql.addListener(mqlListener);
    isMediaNotMobile = mql.matches;
  }

  function removeActiveListener() {
    if (mql && mqlListener) {
      mql.removeListener(mqlListener);
    }
  }

  /**
   * Array\<T\> where T is the datatype of the object to be displayed
   */
  export let data;
  /**
   * Array\<ColumnDef\<T\>\> where T is the datatype of the object to be displayed
   */
  export let columns;
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

  $: columnVisibility = isMediaNotMobile
    ? defaultColumnVisibility
    : mobileColumnVisibility;

  let globalFilter = "";

  $: options = writable<TableOptions<User>>({
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
      <table class="table table-auto w-full">
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
                  {/if}
                </th>
              {/each}
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
        {$table.getState().pagination.pageIndex + 1}
        /
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
      {#if isMediaNotMobile}
        <select
          value={$table.getState().pagination.pageSize}
          on:change={setPageSize}
          class="btn"
        >
          {#each [5, 10, 20, 40, 80] as pageSize}
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
