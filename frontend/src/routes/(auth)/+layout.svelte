<script lang="ts">
  import PageTransition from "$components/pageTransition.svelte";
  import { page } from "$app/stores";
</script>

<PageTransition url={$page.url.pathname}>
  <main class="mx-4 h-screen">
    <slot />
  </main>
</PageTransition>
