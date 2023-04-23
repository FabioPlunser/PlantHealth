<script lang="ts">
  import AdminNav from "$components/ui/adminNav.svelte";
  import { onMount, onDestroy } from "svelte";
  import { slide, fly } from "svelte/transition";
  import Wifi from "$assets/icons/wifi.svg?component";
  import Plant from "$assets/icons/potted-plant.svg?component";
  import Gardener from "$assets/icons/gardening-shears.svg?component";
  import User from "$assets/icons/user.svg?component";
  import Graph from "$lib/components/graph/Graphs.svelte";

  let rendered = false;
  onMount(() => {
    rendered = true;
  });

  let infoBadges = [
    {
      icon: Wifi,
      number: 4,
      size: 32,
    },
    {
      icon: Plant,
      number: 8,
      size: 34,
    },
    {
      icon: Gardener,
      number: 2,
      size: 34,
    },
    {
      icon: User,
      number: 20,
      size: 32,
    },
  ];
</script>

{#if rendered}
  <div
    class="flex justify-center gap-6 mt-12"
    in:fly={{ y: -200, duration: 400 }}
  >
    {#each infoBadges as badges}
      <div
        class="relative rounded-full border-2 dark:border-none bg-base-100 drop-shadow-xl p-8"
      >
        <div class="absolute top-1 left-[16px] m-auto">
          <svelte:component
            this={badges.icon}
            width={badges.size}
            height={badges.size}
            class="dark:fill-white rounded-full flex items-center justify-center "
          />
          <h1 class="flex justify-center rounded-full m-0 p-0">
            {badges.number}
          </h1>
        </div>
      </div>
    {/each}
  </div>

  <div class="mt-20">
    {#each { length: 3 } as _, i}
      <div class="mt-4">
        <Graph data={null} />
      </div>
    {/each}
  </div>
{/if}
