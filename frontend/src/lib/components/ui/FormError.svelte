<script lang="ts">
  export let field: string;
  export let id: string = "";
  export let form: any;
</script>

{#if form?.error}
  {#each form?.errors as error}
    {#if error.field === field && id !== "" && form?.id === id}
      <p class="text-red-500 text-xs italic">{error.message}</p>
    {/if}
  {/each}
{/if}
