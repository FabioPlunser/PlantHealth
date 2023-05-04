<script lang="ts">
  import toast from "$components/toast";
  import AddUserModal from "./AddUserModal.svelte";
  import UsersTable from "./UsersTable.svelte";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";
  import { enhance } from "$app/forms";

  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let isRendered = false;
  onMount(() => {
    isRendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  $: console.log(data);
  export let form;
  $: console.log(form);
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let addUserModal = false;
</script>

{#if addUserModal}
  <AddUserModal bind:showModal={addUserModal} {form} />
{/if}
{#if isRendered}
  <section class="h-screen">
    <!-- svelte-ignore a11y-click-events-have-key-events -->
    <btn
      class="btn btn-primary flex justify-center w-fit mx-auto m-4"
      on:click={() => (addUserModal = true)}
      in:slide={{ duration: 400, axis: "y" }}>Add User</btn
    >
    <div class="flex justify-center">
      <UsersTable users={data.users} />
    </div>
  </section>
{/if}
