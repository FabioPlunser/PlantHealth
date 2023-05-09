<script lang="ts">
  import { enhance } from "$app/forms";
  import { fly, slide } from "svelte/transition";
  import type { SubmitFunction } from "./$types.js";

  import Spinner from "$components/ui/Spinner.svelte";
  import Modal from "$components/ui/Modal.svelte";

  export let showModal: boolean;
  export let data: any = [];
</script>

<Modal
  open={showModal}
  on:close={() => (showModal = false)}
  closeOnBodyClick={false}
  _class="dark:bg-white/10 backdrop-blur-xl w-fit"
>
  <div class="dark:text-white text-black w-fit">
    {#if data.length === 0}
      <h1 class="font-bold">No sensor stations available</h1>
    {:else}
      {#key showModal}
        <div class="grid grid-rows sm:grid-cols-2 md:grid-cols-3 gap-4 w-fit">
          {#each data as item, i (item.sensorStationId)}
            <div
              in:fly|self={{ x: -200, duration: 300, delay: 200 * i }}
              class="card w-64 h-92 dark:bg-base-100 bg-base-100 shadow-2xl border-gray-200 border dark:border-none"
            >
              {#await item?.picture}
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
                  <div class="h-full flex items-center justify-center">
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
                    value={item.sensorStationId}
                  />
                  <h2 class="card-title">Room: {item.roomName}</h2>
                  <h2 class="card-title">Name: {item.sensorStationName}</h2>
                  <div class="card-actions flex justify-center">
                    <button class="btn btn-primary" type="submit"
                      >Add to Dashboard</button
                    >
                  </div>
                </form>
              </div>
            </div>
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
