<script lang="ts">
  import { fly } from "svelte/transition";
  import { enhance } from "$app/forms";

  import MobileGraphs from "$components/graph/MobileGraphs.svelte";
  import Spinner from "$components/ui/Spinner.svelte";

  export let sensorStation: any;

  let showPictures = false;
</script>

<div class="m-0 p-0">
  <div
    in:fly|self={{ y: -200, duration: 300 }}
    class="card bg-base-100 shadow-2xl rounded-2xl p-4"
  >
    <div class="absolute top-0 right-0 m-4">
      <form method="POST" action="?/removeFromDashboard" use:enhance>
        <input
          type="hidden"
          name="sensorStationId"
          value={sensorStation.sensorStationId}
        />
        <button
          type="submit"
          on:click={() => console.log("removeFromDashboard")}
        >
          <i class="bi bi-trash text-3xl hover:text-primary shadow-2xl" />
        </button>
      </form>
    </div>

    <div class="font-bold text-xl">
      <h1>Name: {sensorStation.name}</h1>
      <h1>Room: {sensorStation.roomName}</h1>
    </div>
    <div class="w-full">
      <div>
        <MobileGraphs data={sensorStation.data} />
      </div>
    </div>
    <div class="w-full h-full mt-8 mb-4">
      <div class="absolute bottom-0 right-0 mr-4 mb-4">
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
      class="-mt-2"
    >
      <div class="bg-base-100 shadow-2xl rounded-2xl">
        <div class="w-full h-full p-4 m-4">
          <div class="carousel space-x-4">
            {#if sensorStation.pictures}
              {#each sensorStation?.pictures as picture}
                {#await picture}
                  <Spinner />
                {:then data}
                  <div class="carousel-item">
                    <img
                      src={data.encodedImage}
                      alt="SensorStationPicture"
                      class="w-48 h-64 rounded-2xl shadow-xl"
                    />
                  </div>
                {/await}
              {/each}
            {:else}
              <h1>Not Pictures found</h1>
            {/if}
          </div>
        </div>
      </div>
    </div>
  {/if}
</div>
