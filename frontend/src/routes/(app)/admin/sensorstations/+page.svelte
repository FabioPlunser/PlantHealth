<script lang="ts">
  import { enhance } from "$app/forms";
  import { fly } from "svelte/transition";
  import { invalidate } from "$app/navigation";
  import { apSensorStations } from "$stores/apSensorStations";
  import { onMount } from "svelte";
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
  $: {
    if (data.fromAccessPoints && $apSensorStations.length > 0) {
      data.sensorStations = $apSensorStations;
    }
  }
  // ----------------------------------
  // ----------------------------------
  function setCookie(id: any) {
    document.cookie = `sensorStationId=${id}; path=/;`;
  }
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let searchByRoomName = "";
  let searchByMacAddress = "";
  let searchByDipSwitchId = "";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  $: console.log(searchByDipSwitchId);
  $: console.log(data);
</script>

<a
  href="http://localhost:3000/api/get-sensor-station-qr-code?sensorStationId=e5dc8654-255e-4fdd-b58e-8160f3a8fd7c&roomName=Office1&plantName=Sakura"
  >create</a
>
{#if rendered}
  {#if data.sensorStations}
    {#if data.sensorStations.length > 0}
      <section>
        <div class="mb-2 flex justify-center text-center">
          {#if data.fromAccessPoints && $apSensorStations.length > 0}
            <div class="text-xl font-bold">
              <h1 class="">Sensorstations of AccessPoint:</h1>
              <h1 class="">{data.sensorStations[0].roomName}</h1>
              <button
                on:click={() => invalidate("app:getSensorStations")}
                class="btn btn-primary">Get all SensorStations</button
              >
            </div>
          {:else}
            <h1 class="text-xl font-bold">All SensorStations</h1>
          {/if}
        </div>
        <div class="flex justify-center mb-4">
          <div class="grid grid-rows sm:grid-cols-3 gap-2 justify-center">
            <label class="justify-center mx-auto">
              <h1 class="flex justify-center">Search by Room</h1>
              <input
                bind:value={searchByRoomName}
                name="searchRoom"
                class="input input-border"
                placeholder="Office-1"
              />
            </label>
            <label class="">
              <h1 class="flex justify-center">Search by MacAddress</h1>
              <input
                bind:value={searchByMacAddress}
                class="input input-border"
                placeholder="00-00-00-00-00-00"
              />
            </label>
            <label class="">
              <h1 class="flex justify-center">Search by DipSwitchId</h1>
              <input
                bind:value={searchByDipSwitchId}
                class="input input-border"
                placeholder="129"
              />
            </label>
          </div>
        </div>
        <div class="flex justify-center mx-auto">
          <div class="grid grid-rows md:grid-cols-3 gap-4">
            {#each data.sensorStations as sensorStation, i (sensorStation.sensorStationId)}
              {#if sensorStation.roomName.includes(searchByRoomName) && sensorStation.bdAddress.includes(searchByMacAddress) && sensorStation.dipSwitchId
                  .toString()
                  .includes(searchByDipSwitchId)}
                <form
                  in:fly|self={{ y: -200, duration: 200, delay: 100 * i }}
                  out:fly|local|self={{ y: 200, duration: 200 }}
                  method="POST"
                  use:enhance
                >
                  <input
                    type="hidden"
                    name="sensorStationId"
                    value={sensorStation?.sensorStationId}
                  />
                  <div
                    class="card w-full border h-fit bg-base-100 dark:border-none shadow-2xl"
                  >
                    <div class="absolute top-0 right-0 m-4">
                      <a href="sensorstations/sensorstation">
                        <button
                          on:click={() =>
                            setCookie(sensorStation.sensorStationId)}
                          class="transform transition-transform hover:rotate-90 active:scale-125 animate-spin"
                        >
                          <i
                            class="bi bi-gear-fill text-3xl hover:text-primary"
                          />
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
                      <h1>
                        MacAddress: <span>{sensorStation.bdAddress}</span>
                      </h1>
                      <h1>
                        DipSwitch: <span>{sensorStation.dipSwitchId}</span>
                      </h1>
                      <div class="grid grid-rows gap-1">
                        <div class="mx-auto">
                          {#if sensorStation.connected}
                            <div class="badge badge-success">Connected</div>
                          {:else}
                            <div class="badge badge-error">Disconnected</div>
                          {/if}
                        </div>
                        <button
                          type="button"
                          on:click={() =>
                            createQrCode(sensorStation.sensorStationId)}
                        >
                          <i class="bi bi-qr-code-scan text-4xl" /></button
                        >
                      </div>
                      <div class="flex justify-center my-2 mx-auto gap-4">
                        <button type="submit" class="btn btn-primary"
                          >Update</button
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
  {/if}
{/if}
