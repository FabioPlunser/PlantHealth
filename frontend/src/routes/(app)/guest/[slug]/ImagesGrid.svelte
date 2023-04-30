<script lang="ts">
  import { slide, fade, fly } from "svelte/transition";
  import Spinner from "$components/ui/Spinner.svelte";
  import { onMount } from "svelte";

  export let fetchPictures: any;

  let rendered = false;
  onMount(() => {
    rendered = true;
  });
</script>

{#if rendered}
  {#await fetchPictures}
    <div class="flex justify-center mx-auto">
      <div>
        <Spinner />
        <h1 class="mx-auto">Loading Pictures</h1>
      </div>
    </div>
  {:then pictures}
    {#if pictures.length === 0}
      <div
        out:fly={{ y: -200, duration: 500 }}
        class="mt-6 flex justify-center"
      >
        <h1 class="font-bold">No pictures found</h1>
      </div>
    {:else}
      <div class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-8 gap-4">
        {#each pictures as { imageRef, creationDate, pictureId }, i (pictureId)}
          <div in:fly={{ y: -200, duration: 500 }} class="shrink-0 snap-center">
            <img
              alt="Plant"
              class="rounded-xl shadow-xl backdrop-blur-2xl"
              src={imageRef}
            />
            <h1 class="flex justify-center">
              Date: {creationDate.toLocaleDateString()}
            </h1>
          </div>
        {/each}
      </div>
    {/if}
  {/await}
{/if}
