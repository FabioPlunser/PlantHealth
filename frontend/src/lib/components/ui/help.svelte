<script lang="ts">
  import { theme } from "$stores/themeStore";
  import { page } from "$app/stores";
  import { slide, fly } from "svelte/transition";
  import Mobile from "$helper/Mobile.svelte";
  import Desktop from "$helper/Desktop.svelte";

  let open = false;
  let links = [
    { text: "Home", href: "/" },
    { text: "About", href: "/about" },
    { text: "Skills", href: "/skills" },
    { text: "Projects", href: "/projects" },
    { text: "Blog", href: "/blog" },
  ];
</script>

<Desktop>
  <nav class="navbar bg-base-100 ">
    <div class="flex">
      <a
        href="/"
        class="font-bold hover:text-gray-500  normal-case text-4xl m-0"
      >
        Portfolio
      </a>
    </div>
    <div class="flex justify-center mx-auto">
      <ul class="menu menu-horizontal font-bold text-shadow px-1 text-3xl">
        {#each links as link}
          {#if link?.children}
            <li tabindex="-1" class="hover:bg-transparent">
              <a href={link.href}>
                {link.text}
                <i>
                  <svg
                    class="fill-current"
                    xmlns="http://www.w3.org/2000/svg"
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    ><path
                      d="M7.41,8.58L12,13.17L16.59,8.58L18,10L12,16L6,10L7.41,8.58Z"
                    /></svg
                  >
                </i>
              </a>
              <ul class="p-3 bg-base-200 font-bold hover:bg-transparent">
                {#each link.children as child}
                  <li class="hover:bg-transparent">
                    <a href={child.href}>{child.text}</a>
                  </li>
                {/each}
              </ul>
            </li>
          {:else if $page.url.pathname === link.href}
            <li>
              <a class="underline hover:bg-transparent" href={link.href}
                >{link.text}</a
              >
            </li>
          {:else}
            <li>
              <a class="hover:underline hover:bg-transparent" href={link.href}
                >{link.text}</a
              >
            </li>
          {/if}
        {/each}
      </ul>
      <div class="absolute right-0 mr-20">
        <label tabindex="-1" class="swap swap-rotate">
          <input type="checkbox" class="w-full h-full" bind:checked={$theme} />
          <svg
            class="swap-on fill-current w-14 h-14"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            ><path
              d="M21.64,13a1,1,0,0,0-1.05-.14,8.05,8.05,0,0,1-3.37.73A8.15,8.15,0,0,1,9.08,5.49a8.59,8.59,0,0,1,.25-2A1,1,0,0,0,8,2.36,10.14,10.14,0,1,0,22,14.05,1,1,0,0,0,21.64,13Zm-9.5,6.69A8.14,8.14,0,0,1,7.08,5.22v.27A10.15,10.15,0,0,0,17.22,15.63a9.79,9.79,0,0,0,2.1-.22A8.11,8.11,0,0,1,12.14,19.73Z"
            /></svg
          >
          <svg
            class="swap-off fill-current w-14 h-14"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            ><path
              d="M5.64,17l-.71.71a1,1,0,0,0,0,1.41,1,1,0,0,0,1.41,0l.71-.71A1,1,0,0,0,5.64,17ZM5,12a1,1,0,0,0-1-1H3a1,1,0,0,0,0,2H4A1,1,0,0,0,5,12Zm7-7a1,1,0,0,0,1-1V3a1,1,0,0,0-2,0V4A1,1,0,0,0,12,5ZM5.64,7.05a1,1,0,0,0,.7.29,1,1,0,0,0,.71-.29,1,1,0,0,0,0-1.41l-.71-.71A1,1,0,0,0,4.93,6.34Zm12,.29a1,1,0,0,0,.7-.29l.71-.71a1,1,0,1,0-1.41-1.41L17,5.64a1,1,0,0,0,0,1.41A1,1,0,0,0,17.66,7.34ZM21,11H20a1,1,0,0,0,0,2h1a1,1,0,0,0,0-2Zm-9,8a1,1,0,0,0-1,1v1a1,1,0,0,0,2,0V20A1,1,0,0,0,12,19ZM18.36,17A1,1,0,0,0,17,18.36l.71.71a1,1,0,0,0,1.41,0,1,1,0,0,0,0-1.41ZM12,6.5A5.5,5.5,0,1,0,17.5,12,5.51,5.51,0,0,0,12,6.5Zm0,9A3.5,3.5,0,1,1,15.5,12,3.5,3.5,0,0,1,12,15.5Z"
            /></svg
          >
        </label>
      </div>
    </div>
  </nav>
</Desktop>

<Mobile>
  <nav class="flex bg-base-100">
    <div class="flex-1">
      <a href="/" class="font-bold btn btn-ghost normal-case text-3xl">
        Portfolio
      </a>
    </div>
    <div class="flex-none">
      <div class="menu menu-horizontal px-1">
        <div class="">
          <button
            on:click={() => (open = !open)}
            class="btn btn-ghost btn-circle "
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              class="inline-block w-8 h-8 stroke-current translate transition-all font-bold {open
                ? 'rotate-90'
                : 'rotate-0'}"
              ><path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 6h16M4 12h16M4 18h16"
              /></svg
            >
          </button>

          <!-- {#if open}
          <div>
            <ul class="right-0 absolute z-50 p-2 shadow-xl bg-base-300 rounded-box w-max">
              {#each links as link}
                <li class="w-full"><a class="w-full" href={link.href}>{link.text}</a></li>
              {/each}
            </ul>
          </div>
        {/if} -->
        </div>

        <div tabindex="-1" class="flex items-center">
          <label tabindex="-1" class="swap swap-rotate">
            <input type="checkbox" class="hidden" bind:checked={$theme} />
            <svg
              class="swap-off fill-current w-12 h-12"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              ><path
                d="M5.64,17l-.71.71a1,1,0,0,0,0,1.41,1,1,0,0,0,1.41,0l.71-.71A1,1,0,0,0,5.64,17ZM5,12a1,1,0,0,0-1-1H3a1,1,0,0,0,0,2H4A1,1,0,0,0,5,12Zm7-7a1,1,0,0,0,1-1V3a1,1,0,0,0-2,0V4A1,1,0,0,0,12,5ZM5.64,7.05a1,1,0,0,0,.7.29,1,1,0,0,0,.71-.29,1,1,0,0,0,0-1.41l-.71-.71A1,1,0,0,0,4.93,6.34Zm12,.29a1,1,0,0,0,.7-.29l.71-.71a1,1,0,1,0-1.41-1.41L17,5.64a1,1,0,0,0,0,1.41A1,1,0,0,0,17.66,7.34ZM21,11H20a1,1,0,0,0,0,2h1a1,1,0,0,0,0-2Zm-9,8a1,1,0,0,0-1,1v1a1,1,0,0,0,2,0V20A1,1,0,0,0,12,19ZM18.36,17A1,1,0,0,0,17,18.36l.71.71a1,1,0,0,0,1.41,0,1,1,0,0,0,0-1.41ZM12,6.5A5.5,5.5,0,1,0,17.5,12,5.51,5.51,0,0,0,12,6.5Zm0,9A3.5,3.5,0,1,1,15.5,12,3.5,3.5,0,0,1,12,15.5Z"
              /></svg
            >
            <svg
              class="swap-on fill-current w-12 h-12"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              ><path
                d="M21.64,13a1,1,0,0,0-1.05-.14,8.05,8.05,0,0,1-3.37.73A8.15,8.15,0,0,1,9.08,5.49a8.59,8.59,0,0,1,.25-2A1,1,0,0,0,8,2.36,10.14,10.14,0,1,0,22,14.05,1,1,0,0,0,21.64,13Zm-9.5,6.69A8.14,8.14,0,0,1,7.08,5.22v.27A10.15,10.15,0,0,0,17.22,15.63a9.79,9.79,0,0,0,2.1-.22A8.11,8.11,0,0,1,12.14,19.73Z"
              /></svg
            >
          </label>
        </div>
      </div>
    </div>
  </nav>
  {#if open}
    <div
      class="fixed z-[999] left-0 top-0 mr-42 bg-base-300 h-full p-20 rounded"
      transition:fly={{ x: -200, duration: 300 }}
    >
      <div class="absolute top-0 right-0">
        <button class="hover:text-gray-400" on:click={() => (open = false)}
          ><i class="bi bi-x text-5xl left-0" /></button
        >
      </div>
      <div transition:slide class="">
        {#each links as link}
          <div class=" mx-1 my-2">
            <a
              class="text-3xl w-full font-bold hover:text-gray-400 transition-all duration-500"
              href={link.href}>{link.text}</a
            >
          </div>
        {/each}
      </div>
    </div>
  {/if}
</Mobile>
