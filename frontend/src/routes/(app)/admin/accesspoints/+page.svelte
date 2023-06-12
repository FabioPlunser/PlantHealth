<script lang="ts">
  import { enhance } from "$app/forms";
  import { goto } from "$app/navigation";
  import { invalidate } from "$app/navigation";
  import { fly } from "svelte/transition";
  import { apSensorStations } from "$stores/apSensorStations";
  import toast, { Toaster } from "$components/toast";
  import Spinner from "$components/ui/Spinner.svelte";
  // ---------------------------------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  let loading = true;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  export let form;

  let accessPoints: Responses.InnerAccessPoint[] = [];

  $: {
    data.streamed.accessPoints.then((res) => {
      if (res) {
        accessPoints = res.accessPoints;
        loading = false;
      } else {
        if (browser) {
          goto("/logout");
        }
      }
    });
  }
  // ---------------------------------------------------------
  // Set notification for newly found sensor stations
  // ---------------------------------------------------------
  let position: string = "top-right";
  let foundCounter = 0;
  $: {
    for (let accessPoint of accessPoints) {
      accessPoint.sensorStations.forEach((sensorStation) => {
        if (!sensorStation.reported) {
          foundCounter++;
        }
      });
    }

    if (foundCounter > 0) {
      toast.success(`${foundCounter} new sensor stations found`, { position });
    }
  }
  $: $apSensorStations = [];
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  import FormError from "$helper/formError.svelte";
  import { browser } from "$app/environment";
  import { redirect } from "@sveltejs/kit";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  async function invalidateAccessPoints() {
    await setTimeout(async () => {
      await invalidate("app:getAccessPoints");
    }, 1000 * 30);
  }
  $: {
    if (browser && !loading) {
      for (let accessPoint of accessPoints) {
        if (accessPoint.scanActive) {
          invalidateAccessPoints();
        }
      }
    }
  }
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  function setSensorStations(accessPoint: Responses.InnerAccessPoint) {
    $apSensorStations = accessPoint.sensorStations;
  }
  $: console.log(data);
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let searchTerm = "";
</script>

{#if rendered}
  <section class="h-full">
    {#if loading}
      <Spinner />
    {:else if accessPoints.length > 0}
      <div class="mb-4 flex justify-ceter">
        <input
          bind:value={searchTerm}
          type="search"
          name="searchRoom"
          placeholder="Global Search"
          class="input dark:input-bordered w-fit min-w-64 mx-auto dark:bg-gray-800 bg-gray-200 dark:text-white text-black mt-4"
        />
      </div>
      <div class="flex justify-center mx-auto h-full">
        <div class="grid grid-rows md:grid-cols-2 gap-4 xl:grid-cols-3">
          {#each accessPoints as accessPoint, i (accessPoint.accessPointId)}
            {#if accessPoint.roomName.includes(searchTerm)}
              <form
                in:fly|self={{ y: -200, duration: 200, delay: 100 * i }}
                method="POST"
                use:enhance={() => {
                  return async ({ update }) => {
                    await update({ reset: false });
                  };
                }}
              >
                <input
                  type="hidden"
                  name="accessPointId"
                  value={accessPoint.accessPointId}
                />
                <div
                  class="card w-full min-w-full h-fit bg-base-100 shadow-2xl"
                >
                  <div class="card-body">
                    <div>
                      <button
                        type="submit"
                        on:click={(event) => {
                          let isDeleteConfirmed = confirm(
                            `You will delete this access point permanently!`
                          );
                          if (!isDeleteConfirmed) {
                            event.preventDefault();
                            return;
                          }
                        }}
                        formaction="?/delete"
                        formnovalidate={true}
                      >
                        <i
                          class="absolute top-0 right-0 m-4 bi bi-trash text-4xl hover:text-red-500"
                        />
                      </button>
                    </div>
                    <label class="" for="transferInterval">
                      <span class="label-text text-xl font-bold">Room:</span>
                      <input
                        value={accessPoint.roomName}
                        placeholder={accessPoint.roomName}
                        name="roomName"
                        type="text"
                        class="input dark:input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
                      />
                    </label>
                    <FormError field="username" {form} />
                    <!-- <Input type="text" field="transferInterval" label="Transfer Interval" value={accessPoint.transferInterval} /> -->
                    <label class="" for="transferInterval">
                      <span class="label-text text-xl font-bold"
                        >Transfer Interval [s]:</span
                      >
                      <div class="flex">
                        <input
                          value={accessPoint.transferInterval}
                          placeholder={accessPoint.transferInterval}
                          name="transferInterval"
                          type="number"
                          class="input dark:input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
                          min="30"
                          max="240"
                        />
                      </div>
                    </label>

                    <FormError field="username" {form} />

                    <div class="flex justify-center items-center my-2 gap-4">
                      {#if accessPoint.connected}
                        <div class="badge badge-success">Connected</div>
                      {:else}
                        <div class="badge badge-error">Disconnected</div>
                      {/if}
                      {#if accessPoint.sensorStations.length > 0}
                        <div
                          class="tooltip tooltip-primary"
                          data-tip="Go to Sensorstations"
                        >
                          <a
                            href="/admin/accesspoints/sensorstations?accessPointId={accessPoint.accessPointId}"
                          >
                            <button
                              class="badge badge-success hover:scale-110 active:scale-125"
                            >
                              SensorStations: {accessPoint.sensorStations
                                .length}
                            </button>
                            <!-- </a> -->
                          </a>
                        </div>
                      {:else}
                        <div class="badge badge-success">
                          SensorStations: {accessPoint.sensorStations.length}
                        </div>
                      {/if}
                    </div>

                    <div class="card-actions bottom-0 mx-auto">
                      <div class="grid grid-rows md:flex gap-4 mx-auto">
                        <button class="btn btn-primary" formaction="?/update"
                          >Update</button
                        >
                        <input
                          type="hidden"
                          name="scanActive"
                          value={!accessPoint.scanActive}
                        />
                        {#if accessPoint.scanActive}
                          <button
                            class="btn hover:bg-purple-700 bg-purple-700 text-white"
                            formaction="?/scan"
                          >
                            <h1>Scanning</h1>
                            <Spinner />
                          </button>
                          <!-- <div
                            class="flex disable justify-center text-white items-center gap-1 btn hover:bg-purple-700 bg-purple-700 border-none"
                          >
                            <h1>Scanning:</h1>
                            <Spinner
                              w={8}
                              h={8}
                              fill="dark:fill-white fill-info"
                              background="text-black"
                            />
                          </div> -->
                        {:else if !accessPoint.connected || !accessPoint.unlocked}
                          <button
                            disabled
                            class="btn btn-error"
                            formaction="?/scan">Start scanning</button
                          >
                        {:else}
                          <div
                            class="tooltip tooltip-primary"
                            data-tip="Scanning will cancel automatically after 5min"
                          >
                            <button class="btn btn-error" formaction="?/scan"
                              >Start scanning</button
                            >
                          </div>
                        {/if}
                        {#if accessPoint.unlocked}
                          <button class="btn btn-info" formaction="?/unlock"
                            >Unlocked</button
                          >
                          <input type="hidden" name="unlocked" value="false" />
                        {:else}
                          <button class="btn btn-error" formaction="?/unlock"
                            >Locked</button
                          >
                          <input type="hidden" name="unlocked" value="true" />
                        {/if}
                      </div>
                    </div>
                  </div>
                </div>
              </form>
            {/if}
          {/each}
        </div>
      </div>
    {:else}
      <div class="flex justify-center h-screen">
        <h1 class="text-2xl font-bold">No Access Points found</h1>
      </div>
    {/if}
  </section>
{/if}
