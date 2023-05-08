<script lang="ts">
  import { fly } from "svelte/transition";
  import Modal from "$components/ui/Modal.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
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
  // array of promises
  export let pictures: Promise<any>[] = [];
  export let open = false;
  // ----------------------------------
  // ----------------------------------
</script>

{#if rendered}
  <div>
    <Modal {open} on:close={() => (open = false)} closeOnBodyClick={false}>
      <div>
        {#if pictures.length > 0}
          <h1>Await promises</h1>
          {#each pictures as picture, i}
            {#await picture}
              <div in:fly|self={{ x: -200, duration: 200 }}>
                <Spinner />
                <h1>Loading...</h1>
              </div>
            {:then data}
              <div in:fly|self={{ x: -200, duration: 200 }}>
                <p>{data}</p>
              </div>
            {:catch error}
              <h1>Error: {error.message}</h1>
            {/await}
          {/each}
        {:else}
          <h1>No Pictures</h1>
        {/if}
      </div>
    </Modal>
  </div>
{/if}
