<script lang="ts">
  import { enhance } from "$app/forms";

  export let data;
  $: console.log(data);
  $: console.log(data.accessPoints[0]);
  let entries = [
    "roomName",
    "scanActive",
    "connected",
    ~"deleted",
    "transferInterval",
    "unlocked",
  ];
</script>

<section class="mt-14">
  {#if data.accessPoints}
    <div class="grid grid-rows gap-6">
      {#each data.accessPoints as accessPoint, i}
        <form method="POST" action="?/unlock" use:enhance>
          <div class="card bg-base-100 shadow-xl w-fit p-5">
            <div>
              <h1 class="flex justify-center text-2xl font-bold">
                AccessPoint: {accessPoint?.id.slice(0, 5)}
              </h1>

              {#each Object.entries(accessPoint) as [key, value]}
                {#if entries.includes(key)}
                  <div class="flex">
                    <!-- svelte-ignore a11y-label-has-associated-control -->
                    <label class="label">
                      <span class="label-text font-bold items-center"
                        >{key}:</span
                      >
                    </label>
                    <h1 class="ml-2 flex items-center">{value}</h1>
                  </div>
                {/if}
              {/each}
            </div>
            <div class="card-actions mt-10 mx-auto">
              <input
                type="hidden"
                name="accessPointId"
                value={accessPoint?.id}
              />
              <button class="btn btn-primary" formaction="?/search"
                >Search Stations</button
              >
              {#if accessPoint?.unlocked}
                <input type="hidden" name="unlocked" value="false" />
                <button class="btn btn-error" formaction="?/unlock">Lock</button
                >
              {:else}
                <input type="hidden" name="unlocked" value="true" />
                <button class="btn btn-primary" formaction="?/unlock"
                  >Unlock</button
                >
              {/if}
              <button class="btn btn-error" formaction="?/QR-Code">
                Create-QR-Code
              </button>
              <button class="btn btn-info" formaction="?/update">
                Update
              </button>
            </div>
          </div>
        </form>
      {/each}
    </div>
  {/if}
</section>
