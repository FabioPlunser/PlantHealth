<script lang="ts">
  import { onMount } from "svelte";
  import { enhance } from "$app/forms";
  import { fly } from "svelte/transition";
  import Spinner from "$components/ui/Spinner.svelte";
  import ImagesGrid from "./ImagesGrid.svelte";

  import Camera from "$lib/assets/icons/Camera.svg?component";
  import type { SubmitFunction } from "./$types.js";
  import { BigPictureModal } from "$components/ui/SensorStation";
  // ----------------------------------------------- //
  let isRendered = false;
  onMount(() => {
    isRendered = true;
  });
  // ----------------------------------------------- //
  export let data;
  // ----------------------------------------------- //
  let imageToUpload: any;

  function onFileSelected(e: any) {
    let file = e.target.files[0];
    let reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (e) => {
      imageToUpload = e.target?.result;
    };
  }

  let submitting = false;

  const addPicture: SubmitFunction = () => {
    submitting = true;

    return async ({ update }) => {
      submitting = false;
      imageToUpload = null;
      await update();
    };
  };

  // ----------------------------------------------- //
</script>

{#if isRendered}
  <section>
    <div class="flex justify-between border-b-4 py-4">
      <div class="text-2xl font-bold">
        <h1>Room: {data?.roomName || "undefined"}</h1>
        <h1>Plant: {data?.plantName || "undefined"}</h1>
      </div>
    </div>

    <form
      method="POST"
      enctype="”multipart/form-data”"
      use:enhance={addPicture}
    >
      <div class="">
        <div>
          <div class="flex justify-center">
            <label
              for="file-input"
              class="btn btn-primary mt-3 hover:dark:fill-black hover:fill-white"
            >
              <Camera class="w-12 dark:fill-white" />
            </label>
            <input
              id="file-input"
              class="hidden"
              type="file"
              accept="image/*"
              name="picture"
              capture="environment"
              on:change={(e) => onFileSelected(e)}
            />
          </div>
          {#if imageToUpload && !submitting}
            <div class="mx-auto w-1/3">
              <img
                class="flext justify-center mx-auto mt-4 border-gray-500 border-2 rounded-xl shadow-xl"
                src={imageToUpload}
                alt="ImageToUpload"
              />
              <button
                class="btn btn-primary flex justify-center mx-auto mt-4"
                type="submit">Submit</button
              >
            </div>
          {:else if submitting}
            <div class="mt-4">
              <Spinner />
              <h1 class="flex justify-center">Uploading</h1>
            </div>
          {/if}
        </div>
      </div>
    </form>

    <div class="mt-6">
      <ImagesGrid pictures={data.streamed.pictures} />
    </div>
  </section>
{/if}
