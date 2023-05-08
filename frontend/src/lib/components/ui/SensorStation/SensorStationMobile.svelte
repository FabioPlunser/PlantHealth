<script lang="ts">
  import { fly } from "svelte/transition";
  import { enhance } from "$app/forms";
  import type { SubmitFunction } from "./$types.js";

  import MobileGraphs from "$components/graph/MobileGraphs.svelte";
  import Graphs from "$components/graph/Graphs.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import DateInput from "$components/datepicker/DateInput.svelte";

  export let sensorStation: any;
  export let dates: any;

  let showPictures = false;
  let newDates = dates;
  let loading = false;

  const customEnhance: SubmitFunction = () => {
    loading = true;
    return async ({ update }) => {
      await setTimeout(async () => {
        await update();
        loading = false;
      }, 2000);
    };
  };

  $: console.log("dates", dates);
  $: console.log("newDates", newDates);
  $: console.log("sensorStation", sensorStation.data);
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
      <h1>Room: {sensorStation.roomName}</h1>
      <h1>Name: {sensorStation.name}</h1>
    </div>
    <div class="w-full">
      {#key sensorStation.data}
        <MobileGraphs data={sensorStation.data} bind:dates bind:loading />
      {/key}
    </div>
    <div class="w-full h-full mt-2">
      <div class="aboslute bottom-0">
        <form method="POST" action="?/updateFromTo" use:enhance={customEnhance}>
          <div class="grid grid-rows justify-center items-center gap-2">
            <div class="flex gap-2">
              <label class="my-auto"
                >From:
                <input type="hidden" name="from" bind:value={newDates.from} />
                {#key sensorStation}
                  <DateInput
                    format="dd.MM.yyyy"
                    placeholder="2000/31/12"
                    bind:value={newDates.from}
                  />
                {/key}
              </label>
            </div>
            <div class="flex gap-2">
              <label class="my-auto"
                >To:
                <input type="hidden" name="to" bind:value={newDates.to} />
                {#key sensorStation}
                  <DateInput
                    class="w-14"
                    format="dd.MM.yyyy"
                    placeholder="2000/31/12"
                    bind:value={newDates.to}
                  />
                {/key}
              </label>
            </div>
          </div>
          <div class="flex justify-center mt-2 gap-2">
            <button class="btn btn-primary"> Update </button>
            <button
              type="button"
              on:click={() => {
                showPictures = !showPictures;
              }}
              class="btn btn-primary"
            >
              Pictures
            </button>
          </div>
        </form>
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
