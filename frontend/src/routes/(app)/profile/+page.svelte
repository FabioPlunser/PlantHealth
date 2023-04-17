<script lang="ts">
  import { enhance } from "$app/forms";
  import FormError from "$helper/formError.svelte";
  import Input from "$components/ui/Input.svelte";
  import CheckRing from "$lib/assets/icons/checkRing.svg?component";
  import BooleanButton from "$lib/components/ui/BooleanButton.svelte";
  import toast from "$components/toast";
  import PasswordInput from "$lib/components/ui/PasswordInput.svelte";

  export let data;

  const isDisabled: boolean = !data.isActiveUserAdmin;

  export let form;

  let roles: string[] = Array.from(Object.keys(data.userPermissions));
</script>

<h1>Profile</h1>

<section class="w-full">
  <form
    method="POST"
    action="?/profile"
    class="flex justify-center"
    use:enhance
  >
    <div class="w-full max-w-md">
      <Input
        type="text"
        field="username"
        label="Username"
        placeholder="Username"
        value={data.username}
      />
      <FormError field="username" {form} />
      <Input
        type="email"
        field="email"
        label="Email"
        placeholder="example.mail@planthealth.com"
        value={data.userEmail}
      />
      <FormError field="email" {form} />
      <PasswordInput field="password" value={data.userPassword} />

      <FormError field="password" {form} />
      <div class="mt-6">
        <ul class="grid w-full gap-6 md:grid-rows-3">
          {#each roles as role}
            <li class="">
              <BooleanButton
                text={role}
                bind:bool={data.userPermissions[role]}
                disabled={isDisabled}
              />
            </li>
          {/each}
        </ul>
      </div>
      <div class="flex justify-center mt-6">
        <button type="submit" class="btn btn-primary">submit</button>
      </div>
    </div>
  </form>
</section>
