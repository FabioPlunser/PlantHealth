<script lang="ts">
  import { enhance } from "$app/forms";
  import Spinner from "$components/ui/Spinner.svelte";
  //--------------------------------------------
  //--------------------------------------------
  export let sensorStationId: any;
  let imageToUpload: any;
  let submitting = false;
  //--------------------------------------------
  //--------------------------------------------
  function onFileSelect(e: any) {
    let file = e.target.files[0];
    let reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (e) => {
      imageToUpload = e?.target?.result;
    };
  }
  //--------------------------------------------
  //--------------------------------------------
  const addPicture = () => {
    submitting = true;

    return async ({ update }) => {
      submitting = false;
      imageToUpload = null;
      await update();
    };
  };
</script>

<div>
  <form
    method="POST"
    action="?/uploadPicture"
    enctype="multipart/form-data"
    use:enhance={addPicture}
  >
    <input type="hidden" name="sensorStationId" value={sensorStationId} />
    <div>
      <label
        for="file-input"
        data-tip="Upload Image"
        class="tooltip tooltip-primary"
      >
        <i
          class="bi bi-camera-fill text-4xl btn btn-primary hover:text-black"
        />
        <input
          id="file-input"
          class="hidden"
          type="file"
          accept="image/*"
          name="picture"
          capture="environment"
          on:change={(e) => onFileSelect(e)}
        />
      </label>
    </div>
    {#if imageToUpload && !submitting}
      <div class="mx-auto">
        <img
          src={imageToUpload}
          class="flex justify-center mx-auto mt-4 border-gray-500 border-2 rounded-xl shadow-2xl max-w-1/2"
          alt="Submitting"
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
  </form>
</div>
