<script lang="ts">
  import { onMount } from "svelte";
  import { fly } from "svelte/transition";
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  export let data;
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let rendered = false;
  onMount(() => {
    rendered = true;
  });
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let infoBadges = [
    {
      icon: "bi bi-router-fill",
      number: data.numbers.numOfConnectedAccessPoints,
      size: 32,
    },
    {
      icon: "bi bi-globe-europe-africa",
      number: data.numbers.numOfConnectedSensorStations,
      size: 34,
    },
    {
      icon: "bi bi-people-fill",
      number: data.numbers.numOfUsers,
      size: 32,
    },
  ];
  // ---------------------------------------------------------
  // ---------------------------------------------------------
  let logs = "backend";
</script>

{#if rendered}
  <section class="overflow-auto">
    <div
      class="flex justify-center gap-6 mt-12"
      in:fly={{ y: -200, duration: 400 }}
    >
      {#each infoBadges as badges}
        <div
          class="relative rounded-full border-2 dark:border-none bg-base-100 drop-shadow-xl p-10"
        >
          <div class="mx-auto top-1 absolute -ml-[17px]">
            <i class="{badges.icon} mx-auto justify-center text-4xl" />
            <h1 class="flex justify-center rounded-full m-0 p-0 text-xl">
              {badges.number}
            </h1>
          </div>
        </div>
      {/each}
    </div>
  </section>
{/if}
