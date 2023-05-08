<script lang="ts">
  import { page } from "$app/stores";
  import Nav from "$components/ui/Nav.svelte";
  import PageTransition from "$components/pageTransition.svelte";
  import { Toaster } from "$components/toast";
  export let data;
</script>

<Toaster />
<Nav user={data?.permission} />
<PageTransition url={$page.url.pathname}>
  <main class="mx-4 h-screen">
    <slot />
  </main>
</PageTransition>
