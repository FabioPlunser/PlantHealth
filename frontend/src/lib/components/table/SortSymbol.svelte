<script lang="ts">
  import type { SortDirection } from "@tanstack/svelte-table";

  export let isSorted: boolean | SortDirection;
</script>

<span>
  {#if isSorted}
    {#if isSorted === "asc"}
      <i class="bi bi-caret-up-fill w-8" />
    {:else}
      <i class="bi bi-caret-down-fill" />
    {/if}
  {/if}
</span>
