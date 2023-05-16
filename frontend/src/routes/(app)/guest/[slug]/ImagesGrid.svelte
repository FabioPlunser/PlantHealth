<script lang="ts">
  import { fly } from "svelte/transition";
  import Spinner from "$components/ui/Spinner.svelte";
  import { onMount } from "svelte";
  import { string } from "zod";

  // ----------------------------------------------- //
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ----------------------------------------------- //

  export let pictures: any = [];
</script>

{#if rendered}
  {#if pictures.lenght === 0}
    <div>
      <h1 class="text-2xl font-bold">No pictures found</h1>
    </div>
  {:else}
    <div class="grid grid-cols-2 md:grid-cols-4 gap-2">
      {#each pictures as picture, i (picture.pictureId)}
        {#await picture.promise}
          <div class="flex justify-center mx-auto">
            <div>
              <Spinner />
              <h1 class="mx-auto">Loading Picture</h1>
            </div>
          </div>
        {:then data}
          <div
            in:fly={{ y: -200, duration: 500, delay: 200 }}
            class="shrink-0 snap-center "
          >
            <img
              alt="Plant"
              class="rounded-xl shadow-xl backdrop-blur-2xl"
              src={data.imageRef}
            />
            <h1 class="flex justify-center">
              Date: {data.creationDate.toLocaleDateString()}
            </h1>
          </div>
        {:catch error}
          <p class="text-red">{error}</p>
        {/await}
      {/each}
    </div>
  {/if}
{/if}
