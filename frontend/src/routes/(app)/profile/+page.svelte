<script lang="ts">
  import { enhance, type SubmitFunction } from "$app/forms";
  import FormError from "$helper/formError.svelte";
  import Input from "$components/ui/Input.svelte";
  import BooleanButton from "$lib/components/ui/BooleanButton.svelte";
  import PasswordInput from "$lib/components/ui/PasswordInput.svelte";
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";

  export let data;

  let isRendered = false;

  onMount(() => {
    isRendered = true;
  });

  const customEnhance: SubmitFunction = () => {
    return async ({ update }) => {
      await update({ reset: false });
    };
  };

  const isDisabled: boolean = !data.canActiveUserChangeRoles;

  export let form;

  let password: string;
  let passwordConfirm: string;
  let email: string;

  let roles: string[] = Array.from(Object.keys(data.permissions));
</script>

{#if isRendered}
  <section class="w-full">
    <form
      method="POST"
      action="?/updateUser"
      class="flex justify-center"
      use:enhance={customEnhance}
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
          label="New Email"
          placeholder="example.mail@planthealth.com"
          value={email}
        />
        <FormError field="email" {form} />
        <PasswordInput
          field="password"
          label="New Password"
          placeholder="Password"
          value={password}
        />

        <FormError field="password" {form} />

        <PasswordInput
          field="passwordConfirm"
          label="Confirm Password"
          placeholder="Confirm Password"
          value={passwordConfirm}
        />
        <FormError field="passwordConfirm" {form} />
        <div class="mt-6">
          <ul class="grid w-full gap-6 md:grid-rows-3">
            {#each roles as role}
              <li class="" in:slide={{ duration: 400, axis: "x" }}>
                <BooleanButton
                  field={`permission[${role}]`}
                  text={role}
                  bind:bool={data.permissions[role]}
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
            in:slide={{ duration: 400, axis: "x" }}
          >
            submit
          </button>
        </div>
      </div>
    </form>
  </section>
{/if}
