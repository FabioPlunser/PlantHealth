<script lang="ts">
  import { browser } from "$app/environment";
  import Carousel from "./Carousel.svelte";
  import Camera from "$lib/assets/icons/Camera.svg?component";

  export let data;

  let test: any, fileinput: any;

  function onFileSelected(e: any) {
    console.log(e);
    let image = e.target.files[0];
    let reader = new FileReader();
    reader.readAsDataURL(image);
    reader.onload = (e) => {
      test = e?.target?.result;
    };
  }
</script>

<section class="mt-12">
  <div class="flex justify-center gap-12">
    <div>
      <h1 class="text-2xl font-bold">Room: {data.roomName}</h1>
      <h1 class="text-2xl font-bold">Plant: {data.plantName}</h1>
    </div>
    <button
      on:click={() => fileinput.click()}
      class="btn btn-primary h-full w-fit hover:dark:fill-black hover:fill-white"
    >
      <Camera class="w-24 dark:fill-white" />
      <input
        class="hidden"
        type="file"
        accept=".jpg, .jpeg, .png"
        on:change={(e) => onFileSelected(e)}
        bind:this={fileinput}
      />
    </button>
  </div>

  <h1>Uploaded Picture</h1>
  {#if test}
    <img src={test} alt="uploaded image" />
  {/if}

  <div class="mt-12">
    <Carousel pictures={data.pictures} />
  </div>
</section>
