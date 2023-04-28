<script lang="ts">
  import type { PageData, ActionData } from "./$types";
  import toast from "$components/toast";
  import AddUserModal from "./AddUserModal.svelte";
  import UsersTable from "./UsersTable.svelte";

  export let data: PageData;

  $: {
    if (data?.message) {
      toast.error(data.message);
    }
  }

  export let form: ActionData;

  let addUserModal = false;
  let selectedUser: any = null;
</script>

{#if addUserModal}
  <AddUserModal bind:showModal={addUserModal} {form} />
{/if}

<btn
  class="btn btn-primary flex justify-center w-fit mx-auto m-4"
  on:click={() => (addUserModal = true)}>Add User</btn
>
<div class="flex justify-center">
  <UsersTable bind:users={data.users} />
</div>
