<script lang="ts">
  import { enhance } from "$app/forms";
  import { invalidate } from "$app/navigation";
  import { fly } from "svelte/transition";
  import { apSensorStations } from "$stores/apSensorStations";
  import Spinner from "$components/ui/Spinner.svelte";
  // ---------------------------------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  $: accessPoints = data.accessPoints;
  $: $apSensorStations = [];
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  import FormError from "$helper/formError.svelte";
  import { browser } from "$app/environment";
  export let form;
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  async function invalidateAccessPoints() {
    await setTimeout(async () => {
      console.log("invalidate", new Date().toLocaleTimeString());
      await invalidate("app:getAccessPoints");
    }, 1000 * 30);
  }
  $: {
    if (browser) {
      for (let accessPoint of data.accessPoints) {
        if (accessPoint.scanActive) {
          invalidateAccessPoints();
        }
      }
    }
  }
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  function setSensorStations(accessPoint: any) {
    $apSensorStations = accessPoint.sensorStations.sensorStations;
  }
  // ---------------------------------------------------------
  // ---------------------------------------------------------
</script>

{#if rendered}
  <section class="mt-14">
    {#if accessPoints}
      <div class="flex justify-center mx-auto">
        <div class="grid grid-rows md:grid-cols-2 gap-4 xl:grid-cols-3">
          {#each accessPoints as accessPoint, i (accessPoint.accessPointId)}
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
              <div class="card w-full min-w-full h-fit bg-base-100 shadow-2xl">
                <div class="card-body">
                  <label class="" for="transferInterval">
                    <span class="label-text text-xl font-bold">Room:</span>
                    <input
                      value={accessPoint.roomName}
                      placeholder={accessPoint.roomName}
                      name="roomName"
                      type="text"
                      class="input input-bordered w-full bg-gray-800 text-white"
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
                        class="input input-bordered w-full bg-gray-800 text-white"
                        min="30"
                        max="240"
                      />
                    </div>
                  </label>

                  <FormError field="username" {form} />

                  <div class="flex justify-center my-2 gap-4">
                    {#if accessPoint.connected}
                      <div class="badge badge-success">Connected</div>
                    {:else}
                      <div class="badge badge-error">Disconnected</div>
                    {/if}
                    {#if accessPoint.sensorStations.sensorStations.length > 0}
                      <div class="tooltip" data-tip="Go to Sensorstations">
                        <a href="/admin/sensorstations">
                          <button
                            on:click={() => setSensorStations(accessPoint)}
                            class="badge badge-success hover:scale-110 active:scale-125"
                          >
                            SensorStations: {accessPoint.sensorStations
                              .sensorStations.length}
                          </button>
                          <!-- </a> -->
                        </a>
                      </div>
                    {:else}
                      <div class="badge badge-success">
                        SensorStations: {accessPoint.sensorStations
                          .sensorStations.length}
                      </div>
                    {/if}
                  </div>

                  <div class="card-actions bottom-0 mx-auto">
                    <div class="grid grid-rows md:flex gap-4 mx-auto">
                      <button class="btn btn-primary" formaction="?/update"
                        >Update</button
                      >
                      {#if accessPoint.scanActive}
                        <div
                          class="flex justify-center items-center gap-1 btn bg-purple-700 border-none"
                        >
                          <h1>Scanning:</h1>
                          <Spinner
                            w={8}
                            h={8}
                            fill="dark:fill-white dark:text-black"
                          />
                        </div>
                      {:else}
                        <button class="btn btn-error" formaction="?/scan"
                          >Not Scanning</button
                        >
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
          {/each}
        </div>
      </div>
    {:else}
      <div class="flex justify-center">
        <h1 class="text-2xl font-bold">No Access Points found</h1>
      </div>
    {/if}
  </section>
{/if}
