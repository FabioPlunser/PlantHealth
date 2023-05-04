<script lang="ts">
  import { enhance } from "$app/forms";
  import { fly } from "svelte/transition";

  import Spinner from "./Spinner.svelte";
  import Graphs from "$components/graph/Graphs.svelte";

  export let sensorStation: any;
  $: console.log(sensorStation);
  let showPictures = false;
</script>

<div class="inline-grid">
  <div
    in:fly|self={{ y: -200, duration: 300 }}
    class="bg-base-100 shadow-2xl rounded-2xl p-4 m-4 backdrop-blur-2xl w-full h-full"
  >
    <div class="absolute top-0 right-0  m-4">
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
    <div class="mt-12" />
    <h1 class="flex justify-center text-2xl">Name: {sensorStation.name}</h1>
    <h1 class="flex justify-center text-2xl">Room: {sensorStation.roomName}</h1>
    <div>
      <Graphs data={null} />
    </div>
    <div class="w-full h-full mt-8">
      <div class="absolute bootom-0 right-0 mr-2">
        <div class="gap-4 flex items-center">
          <button>
            <i
              class="bi bi-calendar-event-fill text-2xl hover:text-primary shadow-2xl"
            />
          </button>
          <button on:click={() => (showPictures = !showPictures)}>
            <i
              class="bi bi-card-image text-3xl hover:text-primary shadow-2xl"
            />
          </button>
        </div>
      </div>
    </div>
  </div>
  {#if showPictures}
    <div
      in:fly={{ y: -50, duration: 300 }}
      out:fly={{ y: -50, duration: 300 }}
      class="mt-2"
    >
      <div
        class="bg-base-100 shadow-2xl rounded-2xl p-4 m-4 backdrop-blur-2xl w-full h-full"
      >
        <div class="carousel space-x-4 w-96">
          <!-- {#each sensorStation?.pictures as picture}
            {#await picture}
              <Spinner/>
            {:then data}
              <div class="carousel-item">
                <img src={data.encodedImage} alt="picture" class="w-48 h-64 rounded-2xl shadow-xl"/>
              </div>
            {/await}

          {/each} -->
        </div>
      </div>
    </div>
  {/if}
</div>
