<script lang="ts">
  import { enhance } from "$app/forms";
  import FormError from "$helper/formError.svelte";
  import Input from "$components/ui/Input.svelte";
  import Logo from "$assets/Logo.svg?url";
  import toast from "$components/toast";
  import PasswordInput from "$lib/components/ui/PasswordInput.svelte";
  // export let data: PageData;
  export let form;
  export let data;
  $: {
    if (form?.message) {
      toast.error(form?.message);
    }
  }
</script>

<section class="flex justify-center items-center h-screen">
  <div class="w-full">
    <img
      src={Logo}
      class="flex justify-center mx-auto w-auto h-auto"
      alt="Logo"
    />
    <h1 class="flex justify-center text-5xl font-bold">PlantHealth</h1>
    <br />
    <h1 class="flex justify-center text-4xl font-bold">Login</h1>

    <form method="POST" use:enhance>
      <div class="mx-auto w-fit">
        <Input
          field="username"
          type="text"
          label="Username"
          placeholder="Username"
        />
        <FormError field="username" {form} />
        <PasswordInput
          field="password"
          label="New Password"
          placeholder="Password"
        />
        <FormError field="password" {form} />
        <div class="flex justify-center mt-4 gap-4">
          <button
            type="submit"
            disabled={data?.error != null}
            class="btn btn-primary"
            formaction="?/login">Login</button
          >
          <button
            type="button"
            disabled={data?.error != null}
            class="btn text-white"
          >
            <a href="/register">Register</a>
          </button>
          <button
            type="button"
            disabled={data?.error != null}
            class="btn btn-info text-white"
          >
            <a href="/guest">Guest</a>
          </button>
        </div>
      </div>
    </form>
  </div>
</section>
