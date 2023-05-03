<script lang="ts">
  import Logo from "$assets/Logo.svg?url";
  import { theme } from "$stores/themeStore";
  import { page } from "$app/stores";
  import Query from "$helper/Query.svelte";

  export let user: string = "Guest";
  export let links: any[] = [];

  let profile = false;

  let AdminLinks = [
    {
      name: "home",
      path: "/admin",
    },
    {
      name: "Plants", //sensorStations
      path: "/admin/plants",
    },
    {
      name: "AP",
      path: "/admin/accessoints",
    },
    // {
    //   name: "Gardener",
    //   path: "/admin/gardener",
    //   icon: Gardener,
    // },
    {
      name: "Users", // includes gardeners
      path: "/admin/users",
    },

    {
      name: "Settings",
      path: "/admin/settings",
    },
  ];
</script>

<!-- @component
Navbar / header component

Usage:
  ```tsx
<Nav/>
``` 
-->

<nav class="navbar">
  <div class="flex justify-center mx-auto">
    <a href="/" class="absolute left-0 ml-4">
      <!-- <img alt="PlantHealth" src={Logo} class="w-14 h-14" /> -->
      <h1 class="font-bold text-2xl">PlantHealth</h1>
    </a>

    <div class="flex justify-center mx-auto">
      <ul class="menu menu-horizontal font-bold text-shadow px-1 text-2xl">
        {#each links as link}
          {#if $page.url.pathname === link.path}
            <li>
              <a class="underline hover:bg-transparent" href={link.path}
                >{link.name}</a
              >
            </li>
          {:else}
            <li>
              <a class="hover:underline hover:bg-transparent" href={link.path}
                >{link.name}</a
              >
            </li>
          {/if}
        {/each}
      </ul>
      <Query query="(min-width: 700px)">
        {#if user === "ADMIN"}
          <ul
            class="navbar flex justify-center text-xl font-bold gap-4 items-center mx-auto"
          >
            {#each AdminLinks as link}
              {#if $page.url.pathname === link.path}
                <li>
                  <a class="underline hover:bg-transparent" href={link.path}
                    >{link.name.toUpperCase()}</a
                  >
                </li>
              {:else}
                <li>
                  <a
                    class="hover:underline hover:bg-transparent"
                    href={link.path}>{link.name.toUpperCase()}</a
                  >
                </li>
              {/if}
            {/each}
          </ul>
        {/if}
      </Query>
      <div class="absolute top-0 right-0 mr-4 -mt-4">
        <div class="flex mx-2 gap-4">
          <label tabindex="-1" class="swap swap-rotate">
            <input
              type="checkbox"
              class="w-full h-full"
              bind:checked={$theme}
            />
            <svg
              class="swap-on fill-current w-10 h-10"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              ><path
                d="M21.64,13a1,1,0,0,0-1.05-.14,8.05,8.05,0,0,1-3.37.73A8.15,8.15,0,0,1,9.08,5.49a8.59,8.59,0,0,1,.25-2A1,1,0,0,0,8,2.36,10.14,10.14,0,1,0,22,14.05,1,1,0,0,0,21.64,13Zm-9.5,6.69A8.14,8.14,0,0,1,7.08,5.22v.27A10.15,10.15,0,0,0,17.22,15.63a9.79,9.79,0,0,0,2.1-.22A8.11,8.11,0,0,1,12.14,19.73Z"
              /></svg
            >
            <svg
              class="swap-off fill-current w-10 h-10"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              ><path
                d="M5.64,17l-.71.71a1,1,0,0,0,0,1.41,1,1,0,0,0,1.41,0l.71-.71A1,1,0,0,0,5.64,17ZM5,12a1,1,0,0,0-1-1H3a1,1,0,0,0,0,2H4A1,1,0,0,0,5,12Zm7-7a1,1,0,0,0,1-1V3a1,1,0,0,0-2,0V4A1,1,0,0,0,12,5ZM5.64,7.05a1,1,0,0,0,.7.29,1,1,0,0,0,.71-.29,1,1,0,0,0,0-1.41l-.71-.71A1,1,0,0,0,4.93,6.34Zm12,.29a1,1,0,0,0,.7-.29l.71-.71a1,1,0,1,0-1.41-1.41L17,5.64a1,1,0,0,0,0,1.41A1,1,0,0,0,17.66,7.34ZM21,11H20a1,1,0,0,0,0,2h1a1,1,0,0,0,0-2Zm-9,8a1,1,0,0,0-1,1v1a1,1,0,0,0,2,0V20A1,1,0,0,0,12,19ZM18.36,17A1,1,0,0,0,17,18.36l.71.71a1,1,0,0,0,1.41,0,1,1,0,0,0,0-1.41ZM12,6.5A5.5,5.5,0,1,0,17.5,12,5.51,5.51,0,0,0,12,6.5Zm0,9A3.5,3.5,0,1,1,15.5,12,3.5,3.5,0,0,1,12,15.5Z"
              /></svg
            >
          </label>
          <!-- svelte-ignore a11y-mouse-events-have-key-events -->
          <!-- svelte-ignore a11y-click-events-have-key-events -->
          <div
            on:mouseover={() => (profile = true)}
            on:click={() => (profile = !profile)}
            on:mouseleave={() => (profile = false)}
          >
            <!-- <a> -->
            <div class="cursor-pointer">
              <div class="avatar flex-items-center mt-5">
                <div class="w-12 rounded-full bg-gray-300 p-2">
                  <img alt="Avatar" src={Logo} />
                </div>
              </div>
              <h1 class="-mt-2 flex justify-center">{user}</h1>
            </div>
            <!-- </a> -->
            {#if profile && user != "GUEST"}
              <div
                class="absolute bg-base-300 p-8 rounded-2xl right-0 drop-shadow-2xl z-[999]"
              >
                <a
                  data-sveltekit-preload-code
                  href="/profile"
                  class="hover:text-gray-600 text-xl">Profile</a
                >
                <a
                  data-sveltekit-preload-code
                  href="/logout"
                  class="hover:text-gray-600 text-xl">Logout</a
                >
              </div>
            {/if}
          </div>
        </div>
      </div>
    </div>
  </div>
</nav>
