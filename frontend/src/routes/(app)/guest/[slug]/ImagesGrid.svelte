<script lang="ts">
  import { fly } from "svelte/transition";
  import Spinner from "$components/ui/Spinner.svelte";
  import { onMount } from "svelte";
  import { string } from "zod";
  import { BigPictureModal } from "$components/ui/SensorStation";
  // ----------------------------------------------- //
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ----------------------------------------------- //

  export let pictures: any = [];

  let openPictureModal = false;
  let selectedPicture = "";
</script>

<BigPictureModal
  on:close={() => (openPictureModal = false)}
  imageRef={selectedPicture}
  open={openPictureModal}
/>

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
            <!-- svelte-ignore a11y-click-events-have-key-events -->
            <img
              alt="Plant"
              class="rounded-xl shadow-xl backdrop-blur-2xl cursor-pointer"
              src={data.imageRef}
              on:click={() => {
                selectedPicture = data.imageRef;
                openPictureModal = true;
              }}
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
