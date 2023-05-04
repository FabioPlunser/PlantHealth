<script lang="ts">
  import Modal from "$components/ui/Modal.svelte";
  import Input from "$lib/components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import type { ActionData } from "./$types";
  import { enhance } from "$app/forms";
  import { invalidate } from "$app/navigation";

  export let showModal: boolean;
  export let form: ActionData;
</script>

<Modal
  open={showModal}
  on:close={() => (showModal = false)}
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

    <label class="label" for="permissions">
      <span class="label-text font-bold">Permission</span>
    </label>
    <select
      name="permissions"
      class="select w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
    >
      <option selected>USER</option>
      <option>GARDENER</option>
      <option>ADMIN</option>
    </select>

    <div class="flex justify-between mt-4">
      <!-- TODO: modal should not close if input validation failed-->
      <button
        type="submit"
        on:click={() => {
          invalidate("all-users:load");
        }}
        class="btn btn-primary">Add User</button
      >
      <button class="btn btn-info" on:click={() => (showModal = false)}
        >Close</button
      >
    </div>
  </form>
</Modal>
