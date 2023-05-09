<script lang="ts">
  import { fly, slide } from "svelte/transition";
  import { onMount } from "svelte";
  // ----------------------------------
  // ----------------------------------
  import PictureModal from "./PictureModal.svelte";
  import Input from "$components/ui/Input.svelte";
  import Desktop from "$helper/Desktop.svelte";
  import Mobile from "$helper/Mobile.svelte";
  import SensorLimitsModal from "./SensorLimitsModal.svelte";
  import SensorDataModal from "./SensorDataModal.svelte";
  import { enhance } from "$app/forms";
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
  let sensorStation;
  $: sensorStation = data.sensorStation;
  //let sensorStation = data.sensorStation;
  let sensors = data.sensors;
  // ----------------------------------
  // ----------------------------------
  let sensorDataModal = false;
  let sensorLimitsModal = false;
  let picturesModal = false;
</script>

{#if rendered}
  <!--
  <PictureModal bind:open={picturesModal} pictures={data.pictures} />
-->
  <SensorLimitsModal bind:open={sensorLimitsModal} {sensorStation} {sensors} />
  <SensorDataModal bind:open={sensorDataModal} data={data.data} />
  <section in:fly={{ y: -200, duration: 200 }} class="h-screen">
    <div class="flex justify-center mx-auto">
      <form
        in:fly|self={{ y: -200, duration: 200, delay: 100 }}
        out:fly|local|self={{ y: 200, duration: 200 }}
        method="POST"
        use:enhance
        class="card p-8 border h-fit bg-base-100 dark:border-none shadow-2xl"
      >
        <div class="text-2xl">
          <input
            type="hidden"
            name="sensorStationId"
            value={sensorStation?.deviceId}
          />
          <Input
            field="name"
            label="Name"
            placeholder="Plant1"
            type="text"
            value={sensorStation.name}
          />
          <h1>
            <span class="font-bold">Room: </span><span
              >{sensorStation.roomName}</span
            >
          </h1>
          <h1>
            <span class="font-bold">MAC: </span><span
              >{sensorStation.bdAddress}</span
            >
          </h1>
          <h1>
            <span class="font-bold">DIP: </span><span
              >{sensorStation.dipSwitchId}</span
            >
          </h1>
          <div class="flex mx-auto justify-center m-4">
            {#if sensorStation.connected}
              <div class="badge badge-success">Connected</div>
            {:else}
              <div class="badge badge-error">Disconnected</div>
            {/if}
          </div>
          <div class="flex justify-center">
            <div class="flex">
              <div class="mx-auto">
                <a
                  href="http://localhost:3000/api/get-sensor-station-qr-code?sensorStationId=e5dc8654-255e-4fdd-b58e-8160f3a8fd7c&roomName=Office1&plantName=Sakura"
                >
                  <i class="bi bi-qr-code-scan text-4xl" />
                </a>
              </div>
            </div>
          </div>

          <Mobile>
            <div class="grid grid-rows gap-4 m-4" in:slide>
              <button
                class="btn btn-warning text-white"
                on:click={() => (sensorLimitsModal = true)}
                >Sensor Limits</button
              >
              <button class="btn" on:click={() => (sensorDataModal = true)}
                >Sensor Data</button
              >
            </div>
          </Mobile>

          <Desktop>
            <div in:slide={{ duration: 200 }}>
              <br />
              <h1 class="text-2xl mx-auto font-bold">SensorLimits</h1>
              <div
                class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
              />
              {#if sensorStation.sensorLimits.length === 0 && sensors.length === 0}
                <h1>There is no Information about the sensorstation yet.</h1>
              {/if}

              {#if sensorStation.sensorLimits.length > 0}
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
                              {limit.sensor.type}
                              <span class="ml-2">[{limit.sensor.unit}]</span>
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
                              {limit.sensor.type}
                              <span class="ml-2">[{limit.sensor.unit}]</span>
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
                class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
              />
              {#if sensorStation.sensorData.length === 0}
                <h1>No Data has been sent yet.</h1>
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
            <button formaction="?/update" class="btn btn-primary">Update</button
            >
            {#if sensorStation.unlocked}
              <button class="btn btn-info" formaction="?/unlock"
                >Unlocked</button
              >
              <input type="hidden" name="unlocked" value="false" />
            {:else}
              <button class="btn btn-error" formaction="?/unlock">Locked</button
              >
              <input type="hidden" name="unlocked" value="true" />
            {/if}
            <!--
              <button
                type="button"
                class="btn btn-info bg-blue-600 text-white border-none"
                on:click={() => (picturesModal = true)}>Pictures</button
              >
            -->
          </div>
        </div>
      </form>
    </div>
  </section>
{/if}
