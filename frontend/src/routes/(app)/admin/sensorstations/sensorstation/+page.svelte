<script lang="ts">
  import { page } from "$app/stores";
  // ----------------------------------
  // ----------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
  });
  // ----------------------------------
  // ----------------------------------

  export let data;
  $: console.log(data);
</script>

{#if rendered}
  <section class="h-screen">
    <button
      on:click={() => history.back()}
      class="transform transition-transform active:scale-110"
    >
      <i class="bi bi-arrow-left-circle text-3xl" />
    </button>
    {#each Object.keys(data.sensorStation) as key, i}
      <div>
        <h1>{key}</h1>
        <p>{data.sensorStation?.[key]}</p>
      </div>
    {/each}
  </section>
{/if}
