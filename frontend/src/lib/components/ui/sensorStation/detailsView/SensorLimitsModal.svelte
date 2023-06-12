<script lang="ts">
  import { enhance } from "$app/forms";
  import { fly } from "svelte/transition";
  import Modal from "$components/ui/Modal.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  import Input from "$components/ui/Input.svelte";
  import { page } from "$app/stores";
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
  // array of promises
  export let sensorStation: any;
  export let sensors: any;
  export let open = false;

  // ----------------------------------
  // ----------------------------------
</script>

{#if rendered}
  <!-- <div class="w-min"> -->
  <Modal {open} on:close={() => (open = false)} closeOnBodyClick={false}>
    <div>
      {#if sensorStation.sensorLimits.length == 0}
        {#if sensors.length > 0}
          <form
            method="POST"
            action="{$page.url.pathname}?/setLimits"
            id="modalSetLimits"
            use:enhance
          >
            <div
              class="mx-auto grid grid-rows md:grid-cols-2 xl:grid-cols-3 gap-4"
            >
              {#each sensors as limit}
                <input type="hidden" name="sensorId" value={limit.sensorId} />
                <div class="flex justify-center">
                  <div
                    class="card dark:bg-slate-800 w-fit bg-base-300 shadow-2xl"
                  >
                    <div class="card-body">
                      <div class="flex">
                        <h1 class="mx-auto font-bold">
                          {limit.type} <span class="ml-2">[{limit.unit}]</span>
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
                      <Input
                        field="Threshold Duration"
                        type="threshold"
                        label="Threshold Duration: "
                        value={limit.threshold}
                      />
                      <!-- <div class="card-actions mx-auto">
                          <button class="btn btn-primary">Set Limit</button>
                        </div> -->
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          </form>
        {:else}
          <p class="w-64">
            Their is no Information for this Sensorstation For for the first
            data transfer than the limits get populated automatically Refresh
            the page in a few seconds
          </p>
        {/if}
      {:else}
        <div class="mx-auto grid grid-rows xl:grod-cols-2 gap-4">
          {#each sensorStation.sensorLimits as limit}
            <div class="flex justify-center">
              <div class="card dark:bg-slate-800 w-fit bg-base-300 shadow-2xl">
                <div class="card-body">
                  <div class="flex">
                    <h1 class="mx-auto font-bold">
                      {limit.type} <span class="ml-2">[{limit.unit}]</span>
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
                  <Input
                    field="Threshold Duration"
                    type="threshold"
                    label="Threshold Duration: "
                    value={limit.threshold}
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
    </div>
    <div class="flex justify-center mt-4 gap-4">
      <button
        class="btn btn-primary"
        formaction="{$page.url.pathname}?/setLimits"
        type="submit"
        form="modalSetLimits">Set Limits</button
      >
      <button class="btn btn-info" on:click={() => (open = false)}>Close</button
      >
    </div>
  </Modal>
{/if}
