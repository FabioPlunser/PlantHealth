<script lang="ts">
  import { page } from "$app/stores";
  import { fly, slide } from "svelte/transition";
  import { horizontalSlide } from "$helper/transitions";

  import Home from "$assets/icons/home.svg?component";
  import Plant from "$assets/icons/potted-plant.svg?component";
  import Gardener from "$assets/icons/gardening-shears.svg?component";
  import Group from "$assets/icons/group.svg?component";
  import Settings from "$assets/icons/gear.svg?component";
  import Wifi from "$assets/icons/wifi.svg?component";

  import { onMount } from "svelte";

  let rendered = false;
  $: path = $page.url.pathname;
  onMount(() => {
    rendered = true;
  });
  let size = "45";

  let icons = [
    {
      name: "home",
      path: "/admin",
      icon: Home,
    },
    {
      name: "Plants",
      path: "/admin/plants",
      icon: Plant,
    },
    {
      name: "Gardener",
      path: "/admin/gardener",
      icon: Gardener,
    },
    {
      name: "Users",
      path: "/admin/users",
      icon: Group,
    },
    {
      name: "AccessPoints",
      path: "/admin/accessPoints",
      icon: Wifi,
    },
    {
      name: "Settings",
      path: "/admin/settings",
      icon: Settings,
    },
  ];
</script>

{#if rendered}
  <div class="mx-2" in:fly={{ y: 200, duration: 400 }}>
    <div
      class=" p-4 rounded-2xl bg-base-100 border-2 dark:border-none border-gray-300 backdrop-blur-4xl dark:bg-white/10 drop-shadow-3xl"
    >
      <div
        class="flex items-center gap-4 justify-center"
        in:fly={{ x: -100, duration: 400, delay: 300 }}
      >
        {#each icons as icon}
          <div>
            <a href={icon.path}>
              <div>
                <svelte:component
                  this={icon.icon}
                  width={size}
                  height={size}
                  class="dark:fill-white mx-auto drop-shadow-2xl {path ===
                  icon.path
                    ? 'rounded-full bg-primary p-1'
                    : ''}"
                />
                <h1>{icon.name}</h1>
              </div>
            </a>
          </div>
        {/each}
      </div>
    </div>
  </div>
{/if}
