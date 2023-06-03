<script lang="ts">
  import { enhance } from "$app/forms";
  import { fly } from "svelte/transition";
  import { invalidate } from "$app/navigation";
  import { apSensorStations } from "$stores/apSensorStations";
  import { onMount } from "svelte";
  import Input from "$lib/components/ui/Input.svelte";
  import StationInfo from "./sensorstation/StationInfo.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
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
  // ----------------------------------
  // ----------------------------------
  let searchTerm = "";
</script>

{#if rendered}
  {#await data.streamed.sensorStations}
    <Spinner />
  {:then promiseData}
    {@const sensorStations = promiseData.sensorStations}
    {#if sensorStations.length > 0}
      <section>
        <div class="mb-4 flex justify-ceter">
          <input
            bind:value={searchTerm}
            type="search"
            name="searchRoom"
            placeholder="Global Search"
            class="input dark:input-bordered w-fit min-w-64 mx-auto dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
          />
        </div>
        <div class="flex justify-center mx-auto">
          <div class="grid grid-rows md:grid-cols-3 gap-4">
            {#each sensorStations as sensorStation, i (sensorStation.sensorStationId)}
              {#if sensorStation.roomName.includes(searchTerm) || sensorStation.bdAddress.includes(searchTerm) || sensorStation.dipSwitchId
                  .toString()
                  .includes(searchTerm)}
                <div
                  class="card w-full border h-fit bg-base-100 dark:border-none shadow-2xl"
                  in:fly|self={{ y: -200, duration: 200, delay: 100 * i }}
                  out:fly|local|self={{ y: 200, duration: 200 }}
                >
                  <div class="card-body">
                    <StationInfo
                      {sensorStation}
                      {form}
                      showDetailLink={true}
                      gardener={data.gardener.items}
                    />
                  </div>
                </div>
              {/if}
            {/each}
          </div>
        </div>
      </section>
    {:else}
      <section class="h-screen">
        <h1 class="text-2xl font-bold flex justify-center">
          No SensorStations
        </h1>
      </section>
    {/if}
  {:catch}
    <p>Something went wrong</p>
  {/await}
{/if}
