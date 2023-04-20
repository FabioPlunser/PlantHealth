<script lang="ts">
  import type { PageData, ActionData } from "./$types";
  import toast from "$components/toast";
  import Trash from "$assets/icons/trash.svg?component";
  import Edit from "$assets/icons/settingVert.svg?component";
  import Modal from "$components/ui/Modal.svelte";
  import Input from "$lib/components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";

  import { enhance } from "$app/forms";

  export let data: PageData;

  $: {
    if (data?.message) {
      toast.error(data.message);
    }
  }

  export let form: ActionData;

  let editModal = false;
  let addUserModal = false;
  let selectedUser: any = null;
</script>

{#if editModal}
  <Modal
    open={editModal}
    on:close={() => (editModal = false)}
    closeOnBodyClick={false}
  >
    <Input
      value={selectedUser.username}
      field="username"
      type="text"
      label="Username"
    />
    <FormError field="username" {form} />

    <Input
      value={selectedUser.email}
      field="email"
      type="email"
      label="Email"
    />
    <FormError field="email" {form} />

    <Input field="password" type="text" label="Password" />
    <FormError field="password" {form} />

    <div class="flex justify-between mt-4">
      <button class="btn btn-primary">Update</button>
      <button class="btn btn-info" on:click={() => (editModal = false)}
        >Close</button
      >
    </div>
  </Modal>
{/if}

{#if addUserModal}
  <Modal
    open={addUserModal}
    on:close={() => (addUserModal = false)}
    closeOnBodyClick={false}
  >
    <form method="POST" action="?/createUser" use:enhance>
      <Input field="username" type="text" label="Username" />
      <FormError field="username" {form} />

      <Input field="email" type="email" label="Email" />
      <FormError field="email" {form} />

      <Input field="password" type="password" label="Password" />
      <FormError field="password" {form} />

      <Input field="passwordConfirm" type="password" label="Confirm password" />
      <FormError field="passwordConfirm" {form} />

      <label class="label">
        <span class="label-text font-bold">Permission</span>
      </label>
      <select name="permissions" class="select w-full text-white bg-gray-800">
        <option selected>USER</option>
        <option>GARDENER</option>
        <option>ADMIN</option>
      </select>

      <div class="flex justify-between mt-4">
        <button type="submit" class="btn btn-primary">Add User</button>
        <button class="btn btn-info" on:click={() => (addUserModal = false)}
          >Close</button
        >
      </div>
    </form>
  </Modal>
{/if}

<btn
  class="btn btn-primary flex justify-center w-fit mx-auto m-4"
  on:click={() => (addUserModal = true)}>Add User</btn
>
<!-- TODO: Add filtering to every column add option to show/hide columns as needed -->
{#if data?.success}
  <div class=" flex justify-center">
    <table class="table table-compact table-zebra 2-xl">
      <thead class="">
        <tr>
          <th>ID</th>
          <th>Username</th>
          <th>Email</th>
          <th>Delete</th>
          <th>Edit</th>
        </tr>
        <!-- <tr>
        <th></th>
        <th><input class="input bg-gray-800 max-w-xs"/></th>
        <th><input class="input bg-gray-800 max-w-xs"/></th>
        <th><input class="input bg-gray-800 max-w-xs"/></th>
        <th><input class="input bg-gray-800 max-w-xs"/></th>
      </tr> -->
      </thead>
      <tbody>
        {#each data.users.items as row, i}
          <tr>
            <td>
              <span>{i + 1}</span>
            </td>
            <td>
              {row.username}
            </td>
            <td>
              {row.email}
            </td>
            <td>
              <!-- // TODO: should be a form action -->
              <button class="" on:click={() => (editModal = true)}
                ><Trash class="hover:fill-primary dark:fill-white" /></button
              >
            </td>
            <td>
              <button
                class=""
                on:click={() => {
                  (editModal = true), (selectedUser = row);
                }}
                ><Edit
                  class="hover:stroke-primary dark:stroke-white stroke-black stroke-4"
                /></button
              >
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
{/if}
