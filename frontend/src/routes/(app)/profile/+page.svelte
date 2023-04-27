<script lang="ts">
  import { enhance } from "$app/forms";
  import FormError from "$helper/formError.svelte";
  import Input from "$components/ui/Input.svelte";
  import CheckRing from "$lib/assets/icons/checkRing.svg?component";
  import BooleanButton from "$lib/components/ui/BooleanButton.svelte";
  import toast from "$components/toast";
  import PasswordInput from "$lib/components/ui/PasswordInput.svelte";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";

  export let data;

  let isRendered = false;

  onMount(() => {
    isRendered = true;
  });

  const isDisabled: boolean = !data.canActiveUserChangeRoles;

  export let form;

  let roles: string[] = Array.from(Object.keys(data.userPermissions));
</script>

<h1>Profile</h1>

{#if isRendered}
  <section class="w-full">
    <form
      method="POST"
      action="?/profile"
      class="flex justify-center"
      use:enhance
    >
      <div class="w-full max-w-md" in:slide={{ duration: 400, axis: "x" }}>
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

        <!-- TODO implement propper user logic in profile (can not see password) add password confirm-->

        <FormError field="password" {form} />
        <div class="mt-6">
          <ul class="grid w-full gap-6 md:grid-rows-3">
            {#each roles as role}
              <li class="" in:slide={{ duration: 400, axis: "x" }}>
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
          <!-- TODO if admin came from admin/users redirect back to it-->
          <button
            type="submit"
            class="btn btn-primary"
            in:slide={{ duration: 400, axis: "x" }}>submit</button
          >
        </div>
      </div>
    </form>
  </section>
{/if}
