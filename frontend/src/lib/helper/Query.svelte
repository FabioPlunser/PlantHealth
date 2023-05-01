<script lang="ts">
  import MediaQuery from "./MediaQuery.svelte";
  export let query: string = ""; 
</script>

<MediaQuery query={query} let:matches>
  {#if matches}
    <slot />
  {/if}
</MediaQuery>
