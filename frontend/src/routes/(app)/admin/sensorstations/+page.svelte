<script>
  import { enhance } from "$app/forms";
  import { fly } from "svelte/transition";
  // ----------------------------------
  // ----------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
  });
  // ----------------------------------
  // ----------------------------------
  export let data;
  $: console.log(data);
  // ----------------------------------
  // ----------------------------------
</script>

{#if rendered}
  <section>
    {#if data.sensorStations}
      <div class="flex justify-center mx-auto">
        <div class="grid grid-rows md:grid-cols-3 gap-4">
          {#each data.sensorStations as sensorStation, i (sensorStation.sensorStationId)}
            <form
              in:fly|self={{ y: -200, duration: 200, delay: 100 * i }}
              method="POST"
              use:enhance
            >
              <input
                type="hidden"
                name="sensorStationId"
                value={sensorStation?.sensorStationId}
              />
              <div
                class="card w-full border h-fit bg-base-100 dark:border-none  shadow-2xl"
              >
                <div class="absolute top-0 right-0 m-4">
                  <a
                    href="sensorstations/sensorstation?sensorStationdId={sensorStation?.sensorStationId}"
                  >
                    <button
                      class="transform transition-transform hover:rotate-90 active:scale-125 animate-spin"
                    >
                      <i class="bi bi-gear-fill text-3xl hover:text-primary" />
                    </button>
                  </a>
                </div>
                <div class="card-body">
                  <label for="name">
                    <h1 class="label-text font-bold mb-2 ml-2">Name:</h1>
                    <input
                      type="text"
                      name="name"
                      class="input input-bordered bg-gray-800 w-full text-white"
                      value={sensorStation.name}
                      placeholder="Name"
                    />
                  </label>
                  <h1>RoomName: <span>{sensorStation.roomName}</span></h1>
                  <h1>MacAddress: <span>{sensorStation.bdAddress}</span></h1>
                  <h1>DipSwitch: <span>{sensorStation.dipSwitchId}</span></h1>
                  <div class="flex gap-4">
                    {#if sensorStation.connected}
                      <div class="badge badge-success">Connected</div>
                    {:else}
                      <div class="badge badge-error">Disconnected</div>
                    {/if}
                  </div>
                  <div class="flex justify-center my-2 gap-4">
                    <button type="submit" class="btn btn-primary">Update</button
                    >
                    {#if sensorStation.unlocked}
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
            </form>
          {/each}
        </div>
      </div>
    {/if}
  </section>
{/if}
