<script lang="ts">
  import { onMount } from "svelte";
  import { fly } from "svelte/transition";
  import { enhance } from "$app/forms";
  import type { SubmitFunction } from "$app/forms";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import Graphs from "$components/graph/Graphs.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import DateInput from "$components/datepicker/DateInput.svelte";
  import BigPictureModal from "$components/ui/BigPictureModal.svelte";
  // ---------------------------------------------------
  // ---------------------------------------------------
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------
  // ---------------------------------------------------
  export let sensorStation: any;
  export let dates: any;
  // ---------------------------------------------------
  // ---------------------------------------------------
  let loading = false;
  let showPictures = false;
  let newDates = dates;
  let dateNow = new Date(Date.now()).toLocaleDateString();
  // ---------------------------------------------------
  // ---------------------------------------------------
  const customEnhance: SubmitFunction = () => {
    loading = true;
    return async ({ update }) => {
      await update();
      loading = false;
    };
  };

  $: console.table("sensorStation", sensorStation);
  let openPictureModal = false;
  let selectedPicture = "";
</script>

<BigPictureModal
  on:close={() => (openPictureModal = false)}
  imageRef={selectedPicture}
  open={openPictureModal}
/>

{#if rendered}
  <section>
    <div class="flex justify-center">
      <div class="m-0 p-0 w-full sm:max-w-10/12 2xl:max-w-8/12">
        <div
          in:fly|self={{ y: -200, duration: 200 }}
          class="card bg-base-100 shadow-2xl rounded-2xl p-4 mx-auto"
        >
          <div class="absolute right-0 mr-6">
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

          {#if !sensorStation.unlocked}
            <h1 class="text-2xl font-bold flex justify-center">
              SensorStation is locked
            </h1>
          {:else if sensorStation.deleted}
            <h1 class="text-2xl font-bold flex justify-center">
              SensorStation got deleted
            </h1>
          {:else}
            <div class="">
              <div class="">
                {#if loading}
                  <Spinner fill="fill-primary" />
                {:else}
                  <Graphs data={sensorStation.data} />
                {/if}
              </div>
            </div>
            <div class="w-full h-full mt-2">
              <form
                method="POST"
                action="?/updateFromTo"
                use:enhance={customEnhance}
              >
                <div
                  class="grid grid-rows sm:flex justify-center items-center gap-2"
                >
                  <div class="flex gap-2">
                    <label class="my-auto">
                      From:
                      <input
                        type="hidden"
                        name="from"
                        bind:value={newDates.from}
                      />
                      {#key sensorStation}
                        <DateInput
                          format="dd.MM.yyyy"
                          placeholder={dateNow}
                          bind:value={newDates.from}
                        />
                      {/key}
                    </label>
                  </div>
                  <div class="flex gap-2">
                    <label class="my-auto">
                      To:
                      <input type="hidden" name="to" bind:value={newDates.to} />
                      {#key sensorStation}
                        <DateInput
                          format="dd.MM.yyyy"
                          placeholder={dateNow}
                          bind:value={newDates.to}
                        />
                      {/key}
                    </label>
                  </div>
                  <div class="my-auto flex items-center justify-center mt-6">
                    <button
                      class="btn btn-primary flex items-center justify-center"
                      type="submit"
                    >
                      Update
                    </button>
                  </div>
                </div>
              </form>
            </div>
            <div class="absolute right-0 mr-[4.5em] md:bottom-0 md:m-6">
              <button
                on:click={() => (showPictures = !showPictures)}
                class="hover:text-primary active:scale-125"
              >
                <i class="bi bi-image text-3xl" />
              </button>
            </div>
          {/if}
        </div>
        {#if showPictures}
          <div
            in:fly={{ y: -50, duration: 32 }}
            out:fly={{ y: -50, duration: 300 }}
            class="-mt-2"
          >
            <div class="bg-base-100 shadow-2xl rounded-2xl">
              <div class="p-4 m-4">
                <div class="carousel space-x-4">
                  {#if sensorStation.pictures}
                    {#each sensorStation?.pictures as picture}
                      {#await picture}
                        <Spinner fill="fill-primary" />
                      {:then data}
                        <div class="carousel-item">
                          <div>
                            <!-- svelte-ignore a11y-click-events-have-key-events -->
                            <img
                              on:click={() => {
                                selectedPicture = data.imageRef;
                                openPictureModal = true;
                              }}
                              src={data.imageRef}
                              alt="SensorStationPicture"
                              class="rounded-2xl shadow-xl cursor-pointer w-64"
                            />
                            <h1 class="flex justify-center">
                              {data.creationDate.toLocaleDateString()}
                            </h1>
                          </div>
                        </div>
                      {:catch error}
                        <h1>Error: {error.message}</h1>
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
    </div>
  </section>
{/if}

<!-- <SensorStationDesktop {sensorStation} {dates} /> -->
