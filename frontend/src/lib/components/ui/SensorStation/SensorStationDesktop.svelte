<script lang="ts">
  import { fly } from "svelte/transition";
  import { enhance } from "$app/forms";

  import Graphs from "$components/graph/Graphs.svelte";
  import Spinner from "$components/ui/Spinner.svelte";

  export let sensorStation: any;
  export let data: any;

  let loading = false;

  let showPictures = false;

  const customEnhance: SubmitFunction = () => {
    loading = true;
    return async ({ update }) => {
      await setTimeout(async () => {
        await update();
        loading = false;
      }, 2000);
    };
  };

  let width = 0;
</script>

<div class="">
  <div
    in:fly|self={{ y: -200, duration: 200 }}
    class="card bg-base-100 shadow-2xl rounded-2xl p-4 sm:max-w-10/12 2xl:max-w-8/12 mx-auto"
    bind:clientWidth={width}
  >
    <div class="absolute top-ß right-0 m-4">
      <form method="POST" action="?/removeFromDashboard" use:enhance>
        <input
          type="hidden"
          name="sensorStationId"
          value={sensorStation.sensorStationId}
        />
        <button type="submit">
          <i class="bi bi-trash text-3xl hover:text-primary shadow-2xl" />
        </button>
      </form>
    </div>

    <div class="font-bold text-xl">
      <h1>Room: {sensorStation.roomName}</h1>
      <h1>Name: {sensorStation.name}</h1>
    </div>
    <h1>CardSize: width: {width}</h1>
    <div class="">
      <div class="">
        {#if loading}
          <Spinner />
        {:else}
          <Graphs data={sensorStation.data} {width} />
        {/if}
      </div>
    </div>
    <div class="w-full h-full mt-2">
      <form method="POST" action="?/updateFromTo" use:enhance={customEnhance}>
        <div class="grid grid-rows justify-center items-center gap-2" />
      </form>
    </div>
  </div>
</div>
