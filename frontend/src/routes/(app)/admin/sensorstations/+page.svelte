<script lang="ts">
  import { enhance } from "$app/forms";
  import { fly } from "svelte/transition";
  import { invalidate } from "$app/navigation";
  import { onMount } from "svelte";
  import Input from "$lib/components/ui/Input.svelte";
  import StationInfo from "$lib/components/ui/sensorStation/sensorStationInfo/StationInfo.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import SensorStationsTable from "$lib/components/ui/sensorStation/sensorStationsTable/SensorStationsTable.svelte";
  // ----------------------------------
  // ----------------------------------
  let rendered = false;
  let blinking = false;

  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
    setInterval(() => {
      blinking = !blinking;
    }, 5);
  });
  // ----------------------------------
  // ----------------------------------
  export let data;
  export let form;
  // ----------------------------------
  // ----------------------------------
  let searchTerm = "";
  // ----------------------------------
  // ----------------------------------
  let state = "cards";
  let buttonGroup = [
    {
      description: "Card View",
      icon: "monitoring",
      name: "cards",
      action: () => {
        state = "cards";
      },
    },
    {
      description: "Table View",
      icon: "table_rows",
      name: "table",
      action: () => {
        state = "table";
      },
    },
  ];

  let sensorStations: any = null;
  $: data.streamed.sensorStations.then((data) => {
    sensorStations = data.sensorStations;
  });
</script>

{#if rendered}
  <section>
    {#if !sensorStations}
      <Spinner />
    {:else if sensorStations.length > 0}
      <div class="btn-group bg-bgase-100 flex justify-center mb-4 mt-4">
        {#each buttonGroup as button (button.description)}
          <button
            data-tip={button.description}
            class="btn  tooltip-primary bg-base-200 dark:bg-gray-500 border border-base-300 hover:bg-primary {state ===
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
      {#if state === "cards"}
        <div class="mb-4 flex justify-center">
          <input
            bind:value={searchTerm}
            type="search"
            name="searchRoom"
            placeholder="GloabSearch"
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
                  in:fly|self={{ y: -200, duration: 200, delay: 100 * i }}
                  out:fly|self={{ y: 200, duration: 200 }}
                  class:blinking-border={sensorStation.alarms.some(
                    (a) => a.alarm !== "n"
                  )}
                  class="relative"
                >
                  <div
                    class="card w-full h-fit bg-base-100 dark:border-none shadow-2xl"
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
                </div>
              {/if}
            {/each}
          </div>
        </div>
      {:else}
        <div class="flex justify-center">
          <SensorStationsTable
            gardener={data.gardener.items}
            {sensorStations}
          />
        </div>
      {/if}
    {:else}
      <h1 class="text-2xl font-bold flex justify-center">No SensorStations</h1>
    {/if}
  </section>
{/if}
