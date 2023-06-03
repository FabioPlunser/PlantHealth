<script lang="ts">
  export let field: string;
  export let form: any;
  $: console.log(form);
</script>

{#if form?.error}
  {#each form?.errors as error}
    {#if error.field === field}
      <p class="text-red-500 text-xs italic">{error.message}</p>
    {/if}
  {/each}
{/if}
