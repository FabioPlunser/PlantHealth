<script lang="ts">
  import AdminNav from "$components/ui/adminNav.svelte";
  import { onMount, onDestroy } from "svelte";
  import { slide, fly } from "svelte/transition";
  import Wifi from "$assets/icons/wifi.svg?component";
  import Plant from "$assets/icons/potted-plant.svg?component";
  import Gardener from "$assets/icons/gardening-shears.svg?component";
  import User from "$assets/icons/user.svg?component";

  let rendered = false;
  onMount(() => {
    rendered = true;
  });

  let infoBadges = [
    {
      icon: "bi bi-router-fill",
      number: 4,
      size: 32,
    },
    {
      icon: "bi bi-globe-europe-africa",
      number: 8,
      size: 34,
    },
    {
      icon: "bi bi-people-fill",
      number: 20,
      size: 32,
    },
  ];
</script>

{#if rendered}
  <section class="w-full h-screen">
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

<style>
</style>
