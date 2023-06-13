<script lang="ts">
  import { enhance, type SubmitFunction } from "$app/forms";
  import { fly, slide } from "svelte/transition";

  import Spinner from "$components/ui/Spinner.svelte";
  import Modal from "$components/ui/Modal.svelte";

  export let showModal: boolean;
  export let data: any = [];
  let searchTerm = "";
</script>

<Modal
  open={showModal}
  on:close={() => (showModal = false)}
  closeOnBodyClick={false}
  _class="dark:bg-white/10 backdrop-blur-xl w-fit"
>
  <div class="dark:text-white text-black min-w-[50vw] min-h-[35vh]">
    {#if data.length === 0}
      <h1 class="font-bold">No sensor stations available</h1>
    {:else}
      {#key showModal}
        <div class="mb-4 flex justify-ceter">
          <input
            bind:value={searchTerm}
            type="search"
            name="searchRoom"
            placeholder="Global Search"
            class="input dark:input-bordered w-fit min-w-64 mx-auto dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
          />
        </div>
        <div class="grid grid-rows sm:grid-cols-2 md:grid-cols-3 gap-4 w-fit">
          {#each data as sensorStation, i (sensorStation.sensorStationId)}
            {#if sensorStation.roomName.includes(searchTerm) || sensorStation.name.includes(searchTerm)}
              <div
                in:fly|self={{ x: -200, duration: 300, delay: 200 * i }}
                class="card w-64 h-92 dark:bg-base-100 bg-base-100 shadow-2xl border-gray-200 border dark:border-none"
              >
                {#await sensorStation?.newestPicture}
                  <div>
                    <Spinner />
                  </div>
                {:then picture}
                  {#if picture}
                    <!-- svelte-ignore a11y-img-redundant-alt -->
                    <figure>
                      <img src={picture} alt="newest image" class="w-fit" />
                    </figure>
                  {:else}
                    <div
                      class="h-full flex sensorStations-center justify-center"
                    >
                      <h1>No picture found</h1>
                    </div>
                  {/if}
                {:catch err}
                  <p>{err}</p>
                {/await}
                <div class="card-body">
                  <form method="POST" action="?/addToDashboard" use:enhance>
                    <input
                      type="hidden"
                      name="sensorStationId"
                      value={sensorStation.sensorStationId}
                    />
                    <h2 class="card-title">Room: {sensorStation.roomName}</h2>
                    <h2 class="card-title">Name: {sensorStation.name}</h2>
                    <div class="card-actions flex justify-center">
                      <button class="btn btn-primary mt-4" type="submit"
                        >Add to Dashboard</button
                      >
                    </div>
                  </form>
                </div>
              </div>
            {/if}
          {/each}
        </div>
      {/key}
    {/if}
  </div>

  <div class="flex justify-center mt-4">
    <button class="btn btn-info" on:click={() => (showModal = false)}
      >Close</button
    >
  </div>
</Modal>
