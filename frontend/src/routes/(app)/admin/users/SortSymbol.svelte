<script lang="ts">
  import SortUp from "$lib/assets/icons/sortUp.svg?component";
  import SortDown from "$lib/assets/icons/sortDown.svg?component";
  import type { SortDirection } from "@tanstack/svelte-table";

  export let isSorted: boolean | SortDirection;
</script>

<span>
  {#if isSorted}
    {#if isSorted === "asc"}
      <i class="bi bi-caret-up w-8" />
    {:else}
      <i class="bi bi-caret-down" />
    {/if}
  {/if}
</span>
