<script lang="ts">
  import type { PageData, ActionData } from "./$types";
  import toast from "$components/toast";
  import AddUserModal from "./AddUserModal.svelte";
  import EditUserModal from "./EditUserModal.svelte";
  import type { ColumnDef, TableOptions } from "@tanstack/svelte-table";
  import {
    createSvelteTable,
    flexRender,
    getCoreRowModel,
  } from "@tanstack/svelte-table";
  import { writable } from "svelte/store";

  type User = {
    username: string;
    token: string;
    permissions: string[];
    email: string;
    personId: string;
  };

  export let data: PageData;

  $: {
    if (data?.message) {
      toast.error(data.message);
    }
  }

  const defaultColumns: ColumnDef<User>[] = [
    {
      accessorKey: "personId",
      header: "ID",
      cell: (info) => info.getValue(),
    },
    {
      accessorKey: "username",
      header: "Username",
      cell: (info) => info.getValue(),
    },
    {
      accessorKey: "email",
      header: "Email",
      cell: (info) => info.getValue(),
    },
    {
      accessorKey: "permissions",
      header: "Permissions",
      cell: (info) => info.getValue(),
    },
  ];

  const options = writable<TableOptions<User>>({
    data: data.users,
    columns: defaultColumns,
    getCoreRowModel: getCoreRowModel(),
  });

  const table = createSvelteTable(options);

  export let form: ActionData;

  let editModal = false;
  let addUserModal = false;
  let selectedUser: any = null;
</script>

{#if editModal}
  <EditUserModal bind:showModal={editModal} {form} {selectedUser} />
{/if}

{#if addUserModal}
  <AddUserModal bind:showModal={addUserModal} {form} />
{/if}

<btn
  class="btn btn-primary flex justify-center w-fit mx-auto m-4"
  on:click={() => (addUserModal = true)}>Add User</btn
>
<div class="flex justify-center">
  <table class="table">
    <thead>
      {#each $table.getHeaderGroups() as headerGroup}
        <tr>
          {#each headerGroup.headers as header}
            <th colspan={header.colSpan}>
              {#if !header.isPlaceholder}
                {header}
              {/if}
            </th>
          {/each}
        </tr>
      {/each}
    </thead>
    <tbody>
      {#each $table.getRowModel().rows as row}
        <tr>
          {#each row.getVisibleCells() as cell}
            <td>
              {cell.getValue()}
            </td>
          {/each}
        </tr>
      {/each}
    </tbody>
  </table>
</div>
