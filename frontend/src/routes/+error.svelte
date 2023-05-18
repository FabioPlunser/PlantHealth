<script lang="ts">
  import { page } from "$app/stores";
  import Logo from "$assets/Logo.svg?url";
</script>

<section class="flex justify-center items-center h-screen">
  <div>
    <img
      src={Logo}
      class="flex justify-center mx-auto w-auto h-auto"
      alt="Logo"
    />
    <h1 class="flex justify-center text-5xl font-bold">PlantHealth</h1>
    <br />
    <p class="text-grey font-bold text-3xl flex justify-center">
      {$page.status}
    </p>
    <br />
    <p class="text-4xl flex justify-center text-center">
      {#if $page.status === 404 && $page.error?.message}
        {$page.error.message}
      {:else}
        Oops... {$page.status === 404
          ? "It's not here anymore. Maybe Snoop Dog smoked it."
          : "Something went wrong!"}
      {/if}
    </p>
    <div class="flex justify-center mt-9 ">
      <ul>
        <li><a href="/" class="btn btn-primary w-full"> back to home</a></li>
        <li>
          <a href={$page.url.origin} class="btn btn-primary w-full mt-2">
            back to origin
          </a>
        </li>
        <li>
          <a
            href="/logout"
            data-sveltekit-reload
            class="btn btn-primary w-full mt-2">logout</a
          >
        </li>
      </ul>
    </div>
  </div>
</section>
