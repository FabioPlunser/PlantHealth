<script lang="ts">
  import { enhance } from "$app/forms";
  import Input from "$components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import toast from "$components/toast";
  import Logo from "$assets/Logo.svg?url";

  export let form;
  $: {
    if (form?.message) {
      toast.error(form?.message);
    }
  }
</script>

<section class="flex justify-center items-center h-screen">
  <div>
    <img
      src={Logo}
      class="flex justify-center mx-auto w-auto h-auto"
      alt="Logo"
    />
    <div class="w-full">
      <form method="POST" action="?/register" use:enhance>
        <Input
          field="username"
          type="text"
          label="Username"
          placeholder="Type here"
        />
        <FormError field="username" {form} />

        <Input
          field="email"
          type="email"
          label="Email"
          placeholder="Type here"
        />
        <FormError field="email" {form} />

        <Input
          field="password"
          type="password"
          label="Password"
          placeholder="Type here"
        />
        <FormError field="password" {form} />

        <Input
          field="passwordConfirm"
          type="password"
          label="Confirm Password"
          placeholder="Type here"
        />
        <FormError field="passwordConfirm" {form} />
        <button
          class="btn btn-primary mt-4 flex justify-center items-center mx-auto"
          >Register</button
        >
      </form>
    </div>
  </div>
</section>
