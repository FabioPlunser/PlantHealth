<script lang="ts">
  import { page } from "$app/stores";
  import Nav from "$components/ui/Nav.svelte";
  import PageTransition from "$components/pageTransition.svelte";

  export let data: any;
  console.log(data);
</script>

<Nav user={data?.permission} />
<PageTransition url={$page.url.pathname}>
  <main class="mx-4">
    <slot />
  </main>
</PageTransition>
