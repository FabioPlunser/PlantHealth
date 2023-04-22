<script lang="ts">
  type Picture = {
    imageRef: string;
    creationDate: Date;
  };

  import ImagesGrid from "./ImagesGrid.svelte";
  import Camera from "$lib/assets/icons/Camera.svg?component";
  import { fade, fly, slide } from "svelte/transition";
  import { onMount, onDestroy } from "svelte";

  let isRendered = false;

  onMount(() => {
    isRendered = true;
  });

  export let data;

  let test: any, fileinput: any;

  function onFileSelected(e: any) {
    console.log(e);
    let image = e.target.files[0];
    let reader = new FileReader();
    reader.readAsDataURL(image);
    reader.onload = (e) => {
      // TODO: POST to endpoint and add image from response to data.pictures
      // TODO: figure out how to do this on server side
      test = e?.target?.result;
      let response: Picture = {
        imageRef: "https://picsum.photos/200/300",
        creationDate: new Date(),
      };
      data.pictures.unshift(response);
      data.pictures = data.pictures;
    };
  }
</script>

{#if isRendered}
  <section class="mt-4">
    <div class="flex justify-between border-b-4 py-4">
      <div class="text-2xl font-bold">
        <h1>Room: {data.roomName}</h1>
        <h1>Plant: {data.plantName}</h1>
      </div>
      <button
        in:slide={{ duration: 400, axis: "x" }}
        on:click={() => fileinput.click()}
        class="btn btn-primary mt-3 hover:dark:fill-black hover:fill-white"
      >
        <Camera class="w-8 dark:fill-white" />
        <!-- NOTE: it might make sence to change accept if there are restrictions from the backend-->
        <input
          class="hidden"
          type="file"
          accept="image/*"
          capture="environment"
          on:change={(e) => onFileSelected(e)}
          bind:this={fileinput}
        />
      </button>
    </div>

    <div>
      {#if test}
        <img src={test} alt="plant" />
      {/if}
    </div>

    <div class="mt-6" transition:fly={{ y: -200, duration: 200 }}>
      <ImagesGrid pictures={data.pictures} />
    </div>
  </section>
{/if}
