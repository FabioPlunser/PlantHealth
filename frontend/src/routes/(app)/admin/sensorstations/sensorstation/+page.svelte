<script lang="ts">
  import { fly, slide } from "svelte/transition";
  import { onMount } from "svelte";
  // ----------------------------------
  // ----------------------------------
  import PictureModel from "./PictureModel.svelte";
  import Input from "$components/ui/Input.svelte";
  import Desktop from "$helper/Desktop.svelte";
  import Mobile from "$helper/Mobile.svelte";
  import SensorLimitsModal from "./SensorLimitsModal.svelte";
  import SensorDataModal from "./SensorDataModal.svelte";
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
  let sensorStation = data.sensorStation;
  let sensors = data.sensors;
  $: console.log(data);
  // ----------------------------------
  // ----------------------------------
  let sensorDataModal = false;
  let sensorLimitsModal = false;
  let picturesModal = false;
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  $: console.log(data);
</script>

{#if rendered}
  <PictureModel bind:open={picturesModal} pictures={data.pictures} />
  <SensorLimitsModal bind:open={sensorLimitsModal} {sensorStation} {sensors} />
  <SensorDataModal bind:open={sensorDataModal} data={data.data} />
  <section in:fly={{ y: -200, duration: 200 }} class="h-screen">
    <button
      on:click={() => history.back()}
      class="transform transition-transform active:scale-110 mb-2"
    >
      <i class="bi bi-arrow-left-circle text-3xl" />
    </button>
    <div class="flex justify-center mx-auto">
      <div class="">
        <div class="text-2xl">
          <Input field="name" label="Name:" value={sensorStation.name} />
          <!-- <label for="name" class="">
            <h1 class="font-bold">Name:</h1>
            <input
              value={sensorStation.name}
              type="text"
              name="name"
              class="input input-bordered bg-gray-800 text-white h- ml-4"
              placeholder="Office"
            />
          </label> -->
          <h1 class="font-bold">MacAddress: {sensorStation.bdAddress}</h1>
          <h1 class="font-bold">DipSwitchId: {sensorStation.dipSwitchId}</h1>
          <h1 class="font-bold">Connected: {sensorStation.connected}</h1>

          <Mobile>
            <div class="grid grid-rows gap-2" in:slide>
              <button
                class="btn btn-warning text-white"
                on:click={() => (sensorLimitsModal = true)}
                >Sensor Limits</button
              >
              <button class="btn" on:click={() => (sensorDataModal = true)}
                >Sensor Data</button
              >
            </div>
            <div class="flex justify-center m-2">
              <div class="flex">
                <div class="tooltip" data-tip="Create QR-Code">
                  <button
                    disabled
                    class="transform transition-transform active:scale-110 transf"
                  >
                    <i class="bi bi-qr-code-scan text-4xl" />
                  </button>
                </div>
              </div>
            </div>
          </Mobile>

          <Desktop>
            <div in:slide={{ duration: 200 }}>
              <br />
              <h1 class="text-2xl mx-auto font-bold">SensorLimits</h1>
              <div
                class="divider -mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
              />
              {#if sensorStation.sensorLimits.length === 0 && sensors.length === 0}
                <h1>Their is no Information about the sensorstation yet</h1>
              {/if}

              {#if sensorStation.sensorLimits.length == 0}
                <div
                  class="mx-auto grid grid-rows md:grid-cols-2 xl:grid-cols-3 gap-4"
                >
                  {#each sensors as limit}
                    <div class="flex justify-center">
                      <div
                        class="card dark:bg-slate-800 w-fit bg-base-300 shadow-2xl"
                      >
                        <div class="card-body">
                          <div class="flex">
                            <h1 class="mx-auto font-bold">
                              {limit.type}
                              <span class="ml-2">[{limit.unit}]</span>
                            </h1>
                          </div>
                          <Input
                            field="LowerLimit"
                            type="number"
                            label="LowerLimit: "
                          />
                          <Input
                            field="UpperLimit"
                            type="number"
                            label="UpperLimit: "
                          />
                          <div class="card-actions mx-auto">
                            <button class="btn btn-primary">Set Limit</button>
                          </div>
                        </div>
                      </div>
                    </div>
                  {/each}
                </div>
              {:else}
                <div class="mx-auto grid grid-rows xl:grod-cols-2 gap-4">
                  {#each sensorStation.sensorLimits as limit}
                    <div class="flex justify-center">
                      <div
                        class="card dark:bg-slate-800 w-fit bg-base-300 shadow-2xl"
                      >
                        <div class="card-body">
                          <div class="flex">
                            <h1 class="mx-auto font-bold">
                              {limit.type}
                              <span class="ml-2">[{limit.unit}]</span>
                            </h1>
                          </div>
                          <Input
                            field="LowerLimit"
                            type="number"
                            label="LowerLimit: "
                            value={limit.lowerLimit}
                          />
                          <Input
                            field="UpperLimit"
                            type="number"
                            label="UpperLimit: "
                            value={limit.lowerLimit}
                          />
                        </div>
                        <div class="card-actions mx-auto">
                          <button class="btn btn-primary">Set Limit</button>
                        </div>
                      </div>
                    </div>
                  {/each}
                </div>
              {/if}

              <br />
              <h1 class="text-2xl mx-auto font-bold">SensorData</h1>
              <div
                class="divider -mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
              />
              {#if sensorStation.sensorData.length === 0}
                <h1>No Data has been send yet</h1>
              {:else}
                <table class="table table-zebra w-full">
                  <thead>
                    <tr>
                      <th>Date/Time</th>
                      <th>Sensor</th>
                      <th>Value</th>
                      <th>belowLimit</th>
                      <th>aboveLimit</th>
                      <th>alarm</th>
                    </tr>
                  </thead>
                  <tbody>
                    {#each sensorStation.sensorData as sensorData}
                      <tr>
                        <td>{sensorData.timeStamp}</td>
                        <td>{sensorData.sensor.type}</td>
                        <td>{sensorData.value}</td>
                        <td>{sensorData.belowLimit}</td>
                        <td>{sensorData.aboveLimit}</td>
                        <td>{sensorData.alarm}</td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              {/if}
            </div>
          </Desktop>
          <div class="flex justify-center mx-auto gap-2 mt-6">
            <button class="btn btn-primary">Update</button>
            {#if sensorStation.unlocked}
              <button class="btn btn-info text-white">Unlocked</button>
            {:else}
              <button class="btn btn-error text-white">Locked</button>
            {/if}
            <button
              class="btn btn-info bg-blue-600 text-white border-none"
              on:click={() => (picturesModal = true)}>Pictures</button
            >
          </div>
        </div>
      </div>
    </div>
    <!-- {#each data.sensorStation as s, i (s.sensorStationId)}
      <div>
       
      </div>
    {/each} -->
  </section>
{/if}
