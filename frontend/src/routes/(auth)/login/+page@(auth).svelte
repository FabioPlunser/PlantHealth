<script lang="ts">
  import { enhance } from "$app/forms";
  import FormError from "$helper/formError.svelte";
  import Input from "$components/ui/Input.svelte";
  import Logo from "$assets/Logo.svg?url";
  import toast from "$components/toast";
  // export let data: PageData;
  export let form;
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

    <form method="POST" action="?/login" use:enhance>
      <div class="mx-auto w-fit">
        <Input
          field="username"
          type="text"
          label="Username"
          placeholder="Type here"
        />
        <FormError field="username" {form} />
        <Input
          field="password"
          type="password"
          label="Password"
          placeholder="Type here"
        />
        <FormError field="password" {form} />
        <div class="flex justify-center mt-4 gap-4">
          <button class="btn btn-primary">Login</button>
          <a href="/register" class="btn text-white">Register</a>
          <a href="/guest" class="btn btn-info text-white">Guest</a>
        </div>
      </div>
    </form>
  </div>
</section>
