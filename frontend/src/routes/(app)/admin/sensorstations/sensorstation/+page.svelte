<script lang="ts">
  import { toast } from "$components/toast";
  import { fly, slide, fade } from "svelte/transition";
  import { onMount } from "svelte";
  import { enhance } from "$app/forms";
  import type { SubmitFunction } from "$app/forms";
  // ----------------------------------
  // ----------------------------------
  import BigPictureModal from "$components/ui/BigPictureModal.svelte";
  import { SensorStationDetail } from "$components/ui/SensorStation";

  // ----------------------------------
  // ----------------------------------
  let rendered = false;
  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
  });
  // ----------------------------------
  // ----------------------------------
  export let data;
  export let form;

  let newDates = data.dates;
  let dateNow = new Date(Date.now()).toLocaleDateString();

  let showLimits = false;
  let showDataTable = false;
  let showGraphs = false;
  let showPictures = false;

  let pictureModal = false;
  let selectedPicture = "";
  // ----------------------------------
  // ----------------------------------
</script>

{#if rendered}
  <BigPictureModal
    on:close={() => (pictureModal = false)}
    bind:open={pictureModal}
    imageRef={selectedPicture}
  />
  <section in:fly={{ y: -200, duration: 200 }}>
    <!-- <SensorStationDetail {sensorStation} {form} /> -->
    <!-- 
    <div class="flex justify-center mx-auto">
      <div
        in:fly|self={{ y: -200, duration: 200, delay: 100 }}
        class="flex card p-8 border h-fit w-full bg-base-100 dark:border-none shadow-2xl md:max-w-9/12"
      >
        <StationInfo {sensorStation} {form} gardener={data.gardener} />

        <div in:slide={{ duration: 200 }}>
          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">SensorLimits</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showLimits = !showLimits)}
                >
                  {#if showLimits}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>

            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />

            {#if limits.length === 0}
              <h1>There is no Information about the sensorstation yet.</h1>
            {:else if showLimits}
              <div
                class="mx-auto grid grid-rows md:grid-cols-2 xl:grid-cols-4 gap-4"
              >
                {#each limits as limit}
                  <LimitsCard
                    {limit}
                    sensorStationId={sensorStation.sensorStationId}
                    {form}
                  />
                {/each}
              </div>
            {/if}
          </div>

          <br />

          {#if sensorStationData}
            <div>
              <form
                method="POST"
                action="?/updateFromTo"
                use:enhance={() => {
                  return async ({ update }) => {
                    await update({ reset: false });
                  };
                }}
              >
                <div
                  class="justify-start grid grid-rows sm:flex items-center gap-2"
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
          {/if}

          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">SensorData</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showDataTable = !showDataTable)}
                >
                  {#if showDataTable}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>

            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />
            {#if showDataTable}
              {#if !sensorStationData}
                <div>
                  <Spinner
                    fill="fill-primary"
                    background="text-base-300 dark:text-white"
                  />
                </div>
              {:else if sensorStationData.length === 0}
                <h1>No sensor station data available yet.</h1>
              {:else}
                <div class="overflow-auto">
                  <Table data={tableData} {columns} {mobileColumnVisibility} />
                </div>
              {/if}
            {/if}
          </div>

          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">Graphs</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showGraphs = !showGraphs)}
                >
                  {#if showGraphs}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>
            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />
            {#if showGraphs}
              {#if !sensorStationData}
                <Spinner
                  fill="fill-primary"
                  background="text-base-300 dark:text-white"
                />
              {:else if sensorStationData.length == 0}
                <h1>No sensor station data available yet.</h1>
              {:else}
                <Graphs data={sensorStationData} />
              {/if}
            {/if}
          </div>

          <br />
          <div>
            <div class="">
              <div class="flex gap-4">
                <h1 class="text-2xl font-bold">Pictures</h1>
                <button
                  class="my-auto flex items-center hover:text-primary hover:scale-110 transition-all"
                  on:click={() => (showPictures = !showPictures)}
                >
                  {#if showPictures}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility
                    </i>
                  {:else}
                    <i
                      class="material-symbols-outlined my-auto flex items-center"
                    >
                      visibility_off
                    </i>
                  {/if}
                </button>
              </div>
            </div>
            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />

            {#if showPictures}
              <div>
                <form method="post" use:enhance>
                  <input
                    type="hidden"
                    name="sensorStationId"
                    value={sensorStation.sensorStationId}
                  />
                  <button
                    type="submit"
                    on:click={(event) => {
                      if (
                        !window.confirm(
                          "Are you sure you want to delete all images?"
                        )
                      ) {
                        event.preventDefault();
                      }
                    }}
                    formaction="?/deleteAllPictures"
                    class="btn btn-error mb-4"
                  >
                    DELETE ALL
                  </button>
                </form>
              </div>
              {#if sensorStation.pictures.length > 0}
                <div
                  class="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4"
                >
                  {#if sensorStation.pictures}
                    {#each sensorStation?.pictures as picturePromise}
                      {#await picturePromise}
                        <Spinner fill="fill-primary" />
                      {:then picture}
                        <div class="relative">
                          <div class="absolute top-0 right-0 m-4">
                            <form method="post" use:enhance>
                              <input
                                type="hidden"
                                name="pictureId"
                                value={picture.pictureId}
                              />
                              <button
                                type="submit"
                                on:click={(event) => {
                                  if (
                                    !window.confirm(
                                      "You will delete this picture permanently!"
                                    )
                                  ) {
                                    event.preventDefault();
                                  }
                                }}
                                formaction="?/deletePicture"
                              >
                                <i
                                  class="bi bi-trash text-2xl text-black hover:text-red-500"
                                />
                              </button>
                            </form>
                          </div>
                          <div class="justify-center">
                            <img
                              on:click={() => {
                                selectedPicture = picture.imageRef;
                                pictureModal = true;
                              }}
                              src={picture.imageRef}
                              alt="SensorStation"
                              class="rounded-2xl shadow-xl cursor-pointer"
                            />
                            <h1 class="flex justify-center">
                              {picture.creationDate.toDateString()}
                            </h1>
                          </div>
                        </div>
                      {:catch error}
                        <h1>Error: {error.message}</h1>
                      {/await}
                    {/each}
                  {/if}
                </div>
              {:else}
                <h1>No pictures available yet.</h1>
              {/if}
            {/if}
          </div>
        </div>
      </div>
    </div> -->
  </section>
{/if}
