<script lang="ts">
  import { slide, fade, fly } from "svelte/transition";
  import Spinner from "$components/ui/Spinner.svelte";
  import { onMount } from "svelte";
  import { unknown } from "zod";
  import { data } from "$lib/components/graph_old/data";

  // ----------------------------------------------- //
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ----------------------------------------------- //
  export let fetchPictures: any;
  $: test = fetchPictures;
  let _data: any = null;
  $: fetchPictures.then((data: any) => {
    _data = data;
  });

  $: pictures = _data;
</script>

{#if rendered}
  {#if !_data}
    <div class="flex justify-center mx-auto">
      <div>
        <Spinner />
        <h1 class="mx-auto">Loading Pictures</h1>
      </div>
    </div>
  {:else if pictures.length === 0}
    <div out:fly={{ y: -200, duration: 500 }} class="mt-6 flex justify-center">
      <h1 class="font-bold">No pictures found</h1>
    </div>
  {:else}
    {#await test}
      <div class="flex justify-center mx-auto">
        <div>
          <Spinner />
          <h1 class="mx-auto">Loading Pictures</h1>
        </div>
      </div>
    {/await}
    <div class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-8 gap-4">
      {#each pictures as { imageRef, creationDate, pictureId }, i (pictureId)}
        <div
          in:fly={{ y: -200, duration: 500, delay: 200 }}
          class="shrink-0 snap-center"
        >
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
{/if}
