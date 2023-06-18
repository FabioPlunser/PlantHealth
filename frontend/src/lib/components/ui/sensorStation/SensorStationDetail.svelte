<script lang="ts">
  import { SensorStationsModal } from "$components/ui/sensorStation";
  import { fly } from "svelte/transition";
  import { enhance } from "$app/forms";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import Spinner from "$components/ui/Spinner.svelte";
  import FormError from "$components/ui/FormError.svelte";
  import DateInput from "$components/datepicker/DateInput.svelte";
  import Graphs from "$lib/components/graph/Graphs.svelte";
  import {
    BigPictureModal,
    UploadPicture,
    LimitsCard,
    SensorStationDetailTable,
  } from "$components/ui/sensorStation";
  // ---------------------------------------------------
  // ---------------------------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------
  // ---------------------------------------------------
  export let data: SensorStationDetailComponent;
  export let form: any;
  let newDates = data.dates;
  let loading = false;
  let state = "graphs";
  let buttonGroup = [
    {
      description: "Show Graphs",
      icon: "monitoring",
      name: "graphs",
      action: () => {
        state = "graphs";
      },
    },
    {
      description: "Show Table",
      icon: "table_rows",
      name: "table",
      action: () => {
        state = "table";
      },
    },
    {
      description: "Show Limits",
      icon: "nest_thermostat_zirconium_eu",
      name: "limits",
      action: () => {
        state = "limits";
      },
    },
    {
      description: "Show Pictures",
      icon: "photo_library",
      name: "pictures",
      action: () => {
        state = "pictures";
      },
    },
  ];
  let showPicture = false;
  let selectedPicture = "";
  // ---------------------------------------------------
  // ---------------------------------------------------
  const customEnhance = () => {
    loading = true;
    return async ({ update }) => {
      await update({ reset: false });
      loading = false;
    };
  };
  // ---------------------------------------------------
  // ---------------------------------------------------
  let sensorStation: SensorStationDetailComponentInner | null = null;
  $: {
    if (data.streamed.sensorStation instanceof Promise) {
      data.streamed.sensorStation.then((res) => {
        sensorStation = res;
      });
    } else {
      sensorStation = data.streamed.sensorStation;
    }
  }
  let searchTerm = "";
</script>

{#if rendered}
  <BigPictureModal
    bind:imageRef={selectedPicture}
    bind:open={showPicture}
    on:close={() => (showPicture = false)}
  />
  <section>
    <!-- <div class="mb-4 flex justify-ceter">
      <input
        bind:value={searchTerm}
        type="search"
        name="searchRoom"
        placeholder="Global Search"
        class="input dark:input-bordered w-fit min-w-64 mx-auto dark:bg-gray-800 bg-gray-200 dark:text-white text-black mt-4"
      />
    </div> -->
    {#if sensorStation}
      {#if sensorStation.name.includes(searchTerm) || sensorStation.roomName.includes(searchTerm)}
        <div
          class="m-0 p-0 w-full sm:max-w-10/12 2xl:max-w-8/12 mx-auto"
          in:fly={{ y: -200, duration: 200, delay: 200 }}
        >
          <div
            class="card bg-base-100 shadow-2xl rounded-2xl p-4 justify-center mx-auto"
          >
            <form
              method="POST"
              action="?/updateSensorStation"
              use:enhance={customEnhance}
            >
              <input
                type="hidden"
                name="sensorStationId"
                value={sensorStation.sensorStationId}
              />
              <!--
                  TODO: find a way fro the page not to fail on delete clicked.
                  <SensorStationDeleteButton sensorStationId={sensorStation.sensorStationId}/>
                -->
              <h1 class="text-xl font-bold">
                Room: {sensorStation.roomName}
              </h1>
              <div class="flex justify-center">
                <div class="flex gap-2">
                  <label>
                    <h1 class="label-text font-bold">Name:</h1>
                    <input
                      class="w-36 rounded-2xl p-2 border dark:bg-gray-800 bg-gray-200 dark:border-gray-700 bg-gray-200 dark:text-white text-black"
                      type="text"
                      name="name"
                      value={sensorStation.name}
                    />
                    <FormError
                      field="name"
                      {form}
                      id={sensorStation.sensorStationId}
                    />
                  </label>
                  <label
                    class="tooltip tooltip-primary"
                    data-tip="Disclaimer: Because of the specification this sets the transfer interval between the access point and the backend so every sensor station from the same access point in your dashboard will be updated"
                  >
                    <h1 class="label-text font-bold">TransferInterval [s]:</h1>
                    <input
                      name="transferInterval"
                      class="w-36 rounded-2xl p-2 border dark:bg-gray-80 dark:border-gray-700 dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
                      value={sensorStation.transferInterval}
                      min="30"
                      max="3600"
                    />
                    <FormError
                      field="transferInterval"
                      {form}
                      id={sensorStation.sensorStationId}
                    />
                  </label>
                </div>
              </div>
              <button
                class="btn btn-primary flex justify-center w-min mx-auto mt-2"
                type="submit">Update</button
              >
            </form>

            {#if sensorStation.deleted}
              <h1 class="text-2xl font-bold flex justify-center">
                SensorStation got deleted
              </h1>
            {:else if !sensorStation.unlocked}
              <h1 class="text-2xl font-bold flex justify-center">
                SensorStation is locked
              </h1>
            {:else if loading}
              <Spinner />
            {:else}
              <div class="btn-group bg-bgase-100 flex justify-center mb-4 mt-4">
                {#each buttonGroup as button (button.description)}
                  <button
                    data-tip={button.description}
                    class="btn tooltip bg-base-200 dark:bg-gray-500 border border-base-300 hover:bg-primary {state ===
                    button.name
                      ? 'btn-active dark:btn-active'
                      : ''}"
                    on:click={button.action}
                  >
                    <i
                      class="material-symbols-outlined text-4xl text-black dark:text-white"
                      >{button.icon}</i
                    >
                  </button>
                {/each}
              </div>

              {#if state === "graphs"}
                <form
                  method="POST"
                  action="?/updateFromTo"
                  use:enhance={customEnhance}
                >
                  <div class="justify-center mx-auto">
                    <div
                      class="flex gap-2 justify-center items-center my-auto mb-2"
                    >
                      <label class="my-auto">
                        From:
                        <input
                          type="hidden"
                          name="from"
                          bind:value={newDates.from}
                        />
                        <DateInput
                          format="dd.MM.yyyy"
                          placeholder={new Date(Date.now()).toLocaleDateString(
                            "de-DE"
                          )}
                          bind:value={newDates.from}
                        />
                      </label>
                      <label class="my-auto">
                        To:
                        <input
                          type="hidden"
                          name="to"
                          bind:value={newDates.to}
                        />
                        <DateInput
                          format="dd.MM.yyyy"
                          placeholder={new Date(Date.now()).toLocaleDateString(
                            "de-DE"
                          )}
                          bind:value={newDates.to}
                        />
                      </label>
                      <div class="mt-5">
                        <button class="btn btn-primary" type="submit"
                          >Update</button
                        >
                      </div>
                    </div>
                  </div>
                </form>
                <Graphs {sensorStation} />
              {:else if state === "table"}
                <form
                  method="POST"
                  action="?/updateFromTo"
                  use:enhance={customEnhance}
                >
                  <div class="justify-center mx-auto">
                    <div
                      class="flex gap-2 justify-center items-center my-auto mb-2"
                    >
                      <label class="my-auto">
                        From:
                        <input
                          type="hidden"
                          name="from"
                          bind:value={newDates.from}
                        />
                        <DateInput
                          format="dd.MM.yyyy"
                          placeholder={new Date(Date.now()).toLocaleDateString(
                            "de-DE"
                          )}
                          bind:value={newDates.from}
                        />
                      </label>
                      <label class="my-auto">
                        To:
                        <input
                          type="hidden"
                          name="to"
                          bind:value={newDates.to}
                        />
                        <DateInput
                          format="dd.MM.yyyy"
                          placeholder={new Date(Date.now()).toLocaleDateString(
                            "de-DE"
                          )}
                          bind:value={newDates.to}
                        />
                      </label>
                      <div class="mt-5">
                        <button class="btn btn-primary" type="submit"
                          >Load Data</button
                        >
                      </div>
                    </div>
                  </div>
                </form>
                <div>
                  {#await sensorStation.data}
                    <Spinner />
                  {:then data}
                    <SensorStationDetailTable {data} />
                  {/await}
                </div>
              {:else if state === "limits"}
                {#if sensorStation.sensorLimits.length === 0}
                  <h1 class="flex justify-center text-3xl font-bold">
                    No Limits
                  </h1>
                {:else if sensorStation.sensorLimits}
                  {@const limits = sensorStation.sensorLimits.sort((a, b) => {
                    return b.sensor.type.localeCompare(a.sensor.type);
                  })}
                  <div
                    class="grid grid-rows sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-4"
                  >
                    {#each limits as limit, i}
                      <LimitsCard
                        {limit}
                        sensorStationId={sensorStation.sensorStationId}
                        {form}
                      />
                    {/each}
                  </div>
                {:else}
                  <Spinner />
                {/if}
              {:else if state === "pictures"}
                <div class="grid grid-cols gap-2">
                  {#if sensorStation.pictures && sensorStation.pictures.length === 0}
                    <h1 class="flex justify-center text-3xl font-bold">
                      No Pictures
                    </h1>
                    <UploadPicture
                      sensorStationId={sensorStation.sensorStationId}
                    />
                  {:else}
                    <div class="flex gap-2">
                      <UploadPicture
                        sensorStationId={sensorStation.sensorStationId}
                      />
                      <form
                        method="POST"
                        action="?/deleteAllPictures"
                        use:enhance
                      >
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
                    <div
                      class="grid grid-rows sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4"
                    >
                      {#if sensorStation.pictures && sensorStation.pictures.length > 0}
                        {#each sensorStation.pictures as picture, i (picture.pictureId)}
                          {#await picture.promise}
                            <Spinner />
                          {:then data}
                            <div class="relative">
                              <div class="absolute top-0 right-0 m-2">
                                <form
                                  method="POST"
                                  action="?/deletePicture"
                                  use:enhance
                                >
                                  <input
                                    type="hidden"
                                    name="pictureId"
                                    value={data.pictureId}
                                  />
                                  <button
                                    class=""
                                    type="submit"
                                    on:click={() => {
                                      if (
                                        !window.confirm(
                                          "You will delete this picture permanently"
                                        )
                                      )
                                        event?.preventDefault();
                                    }}
                                  >
                                    <i
                                      class="bi bi-trash text-2xl text-black hover:text-red-500 hover:scale-105"
                                    />
                                  </button>
                                </form>
                              </div>
                              <!-- svelte-ignore a11y-click-events-have-key-events -->
                              <img
                                src={data.imageRef}
                                alt="SensorStationPicture: {i}"
                                class="rounded-2xl shadow-xl cursor-pointer"
                                on:click={() => {
                                  showPicture = true;
                                  selectedPicture = data.imageRef;
                                }}
                              />
                              <h1 class="flex justify-center">
                                {data.creationDate.toLocaleDateString("de-DE")}
                              </h1>
                            </div>
                          {:catch error}
                            <p class="text-red-500">{error.message}</p>
                          {/await}
                        {/each}
                      {:else}
                        <h1 class="text-2xl font-bold">No pictures found</h1>
                      {/if}
                    </div>
                  {/if}
                </div>
              {/if}
            {/if}
          </div>
        </div>
      {/if}
    {:else}
      <div class="flex justify-center">
        <Spinner />
      </div>
    {/if}
  </section>
{/if}
