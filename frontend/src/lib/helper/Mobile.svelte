<script lang="ts">
  import MediaQuery from "./MediaQuery.svelte";
</script>

<MediaQuery query="(max-width: 580px)" let:matches>
  {#if matches}
    <slot />
  {/if}
</MediaQuery>
