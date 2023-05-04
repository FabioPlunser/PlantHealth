<script lang="ts">
  import { page } from "$app/stores";
  import { fly, scale } from "svelte/transition";
  import { onMount } from "svelte";

  import Query from "$helper/Query.svelte";

  let rendered = false;
  $: path = $page.url.pathname;
  onMount(() => {
    rendered = true;
  });

  let icons = [
    {
      name: "Home",
      path: "/admin",
      icon: "bi bi-house",
    },
    {
      name: "Plants", //sensorStations
      path: "/admin/sensorstations",
      icon: "bi bi-globe-europe-africa",
    },
    {
      name: "AP",
      path: "/admin/accesspoints",
      icon: "bi bi-router-fill",
    },
    {
      name: "Users", // includes gardeners
      path: "/admin/users",
      icon: "bi bi-people-fill",
    },

    {
      name: "Settings",
      path: "/admin/settings",
      icon: "bi bi-gear",
    },
  ];
</script>

{#if rendered}
  <Query query="(max-width: 700px)">
    <div class="mx-2" in:fly={{ y: 200, duration: 400 }}>
      <div
        class=" p-4 rounded-2xl bg-base-100 border-2 dark:border-none dark:bg-white/10 dark:backdrop-blur-2xl drop-shadow-3xl "
      >
        <div
          class="flex items-center gap-8 justify-center"
          in:fly={{ x: -100, duration: 400, delay: 300 }}
        >
          {#each icons as icon}
            <div>
              <a href={icon.path}>
                <div>
                  <i
                    class="{icon.icon} transform transition-transform active:scale-125 animation-spin flex justify-center text-4xl mx-auto shadow-2xl drop-shadow-2xl w-8 rouned-full {path ===
                    icon.path
                      ? 'rounded-full text-primary dark:text-primary'
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
