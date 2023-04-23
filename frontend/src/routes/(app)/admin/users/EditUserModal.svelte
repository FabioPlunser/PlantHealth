<script lang="ts">
  import Modal from "$components/ui/Modal.svelte";
  import Input from "$lib/components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import type { ActionData } from "./$types";
  import { enhance } from "$app/forms";

  type User = {
    username: string;
    token: string;
    permissions: string[];
    email: string;
    personId: string;
  };

  export let showModal: boolean;
  export let form: ActionData;
  export let selectedUser: User;
</script>

<Modal
  open={showModal}
  on:close={() => (showModal = false)}
  closeOnBodyClick={false}
>
  <Input
    value={selectedUser.username}
    field="username"
    type="text"
    label="Username"
  />
  <FormError field="username" {form} />

  <Input value={selectedUser.email} field="email" type="email" label="Email" />
  <FormError field="email" {form} />

  <Input field="password" type="text" label="Password" />
  <FormError field="password" {form} />

  <div class="flex justify-between mt-4">
    <button class="btn btn-primary">Update</button>
    <button class="btn btn-info" on:click={() => (showModal = false)}
      >Close</button
    >
  </div>
</Modal>
