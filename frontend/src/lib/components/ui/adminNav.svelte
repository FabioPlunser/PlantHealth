<script lang="ts">
  import { page } from "$app/stores";
  import { fly, slide } from "svelte/transition";
  import { horizontalSlide } from "$helper/transitions";
  import Query from "$helper/Query.svelte";
  import Home from "$assets/icons/home.svg?component";
  import Plant from "$assets/icons/potted-plant.svg?component";
  import Gardener from "$assets/icons/gardening-shears.svg?component";
  import Group from "$assets/icons/group.svg?component";
  import Settings from "$assets/icons/gear.svg?component";
  import Wifi from "$assets/icons/wifi.svg?component";

  import { onMount } from "svelte";
  import Mobile from "$lib/helper/Mobile.svelte";
  import Desktop from "$lib/helper/Desktop.svelte";

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
      name: "Plants", //sensorStations
      path: "/admin/plants",
      icon: Plant,
    },
    {
      name: "AP",
      path: "/admin/accessoints",
      icon: Wifi,
    },
    // {
    //   name: "Gardener",
    //   path: "/admin/gardener",
    //   icon: Gardener,
    // },
    {
      name: "Users", // includes gardeners
      path: "/admin/users",
      icon: Group,
    },

    {
      name: "Settings",
      path: "/admin/settings",
      icon: Settings,
    },
  ];
</script>

{#if rendered}
  <Query query="(max-width: 700px)">
    <div class="mx-2" in:fly={{ y: 200, duration: 400 }}>
      <div
        class=" p-4 rounded-2xl bg-base-100 dark:bg-gray-500 drop-shadow-3xl"
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
                  <h1 class="flex justify-center">{icon.name}</h1>
                </div>
              </a>
            </div>
          {/each}
        </div>
      </div>
    </div>
  </Query>
{/if}
