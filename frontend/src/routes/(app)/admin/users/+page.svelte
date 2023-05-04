<script lang="ts">
  import toast from "$components/toast";
  import AddUserModal from "./AddUserModal.svelte";
  import UsersTable from "./UsersTable.svelte";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";
  import { enhance } from "$app/forms";
  export let data;

  let isRendered = false;

  $: console.log(data);

  onMount(() => {
    isRendered = true;
  });

  /* TODO update with new error handleing when implemented */
  $: {
    if (data?.message) {
      toast.error(data.message);
    }
  }

  export let form;

  let addUserModal = false;
</script>

{#if addUserModal}
  <AddUserModal bind:showModal={addUserModal} {form} />
{/if}
{#if isRendered}
  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <btn
    class="btn btn-primary flex justify-center w-fit mx-auto m-4"
    on:click={() => (addUserModal = true)}
    in:slide={{ duration: 400, axis: "y" }}>Add User</btn
  >
  <div class="flex justify-center">
    <UsersTable bind:users={data.users} />
  </div>
{/if}
