<script lang="ts">
  import AddUserModal from "./AddUserModal.svelte";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";
  import Table from "$components/table/Table.svelte";
  import {
    RoleBadges,
    TextCell,
    HrefWithIcon,
    FormActionButtonConfirm,
  } from "$components/table/cellComponents";
  import { flexRender } from "@tanstack/svelte-table";
  import type { ColumnDef } from "@tanstack/svelte-table";
  import Spinner from "$components/ui/Spinner.svelte";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let isRendered = false;
  onMount(() => {
    isRendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  export let form;
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let addUserModal = false;
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let columns: ColumnDef<User>[] = [
    {
      id: "username",
      accessorKey: "username",
      header: () => flexRender(TextCell, { text: "Username" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "email",
      accessorKey: "email",
      header: () => flexRender(TextCell, { text: "Email" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "permissions",
      accessorKey: "permissions",
      header: () => flexRender(TextCell, { text: "Permissions" }),
      cell: (info) => flexRender(RoleBadges, { roles: info.getValue() }),
    },
    {
      id: "edit",
      accessorKey: "_", // NOTE: blanc accessor so we get the row
      header: () => flexRender(TextCell, { text: "Edit" }),
      cell: ({ row }) =>
        flexRender(HrefWithIcon, {
          href: `/profile?personId=${row.original.personId}&username=${row.original.username}&userPermissions=${row.original.permissions}`,
          iconClass: "bi bi-pencil-square text-3xl hover:text-gray-500",
        }),
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
        }),
    },
  ];
  // ---------------------------------------------------------
  let mobileColumnVisibility: ColumnVisibility = {
    email: false,
    permissions: false,
  };
</script>

{#if addUserModal}
  <AddUserModal bind:showModal={addUserModal} {form} />
{/if}
{#if isRendered}
  <section>
    {#await data.streamed.users}
      <Spinner />
    {:then users}
      <!-- svelte-ignore a11y-click-events-have-key-events -->
      <btn
        class="btn btn-primary flex justify-center w-fit mx-auto m-4"
        on:click={() => (addUserModal = true)}
        in:slide={{ duration: 400, axis: "y" }}>Add User</btn
      >

      <div class="flex justify-center">
        <Table data={users} {columns} {mobileColumnVisibility} />
      </div>
    {:catch error}
      <p class="text-red-500">{error}</p>
    {/await}
  </section>
{/if}
