<script lang="ts">
  export let field: string = "";
  export let label: string = "";
  export let placeholder: string = "";
  export let value: string = "";

  let isPasswordHidden: boolean = true;
</script>

<div>
  <label class="" for={field}>
    <span class="label-text text-xl font-bold">{label}</span>
    <div class="relative">
      <input
        value
        name={field}
        type={isPasswordHidden ? "password" : "text"}
        {placeholder}
        class="input input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black mb-2 mt-2"
      />
      <div class="absolute right-2 top-5">
        {#if isPasswordHidden}
          <button
            type="button"
            class="hover:text-primary"
            on:click={() => (isPasswordHidden = !isPasswordHidden)}
          >
            <i
              class="material-symbols-outlined my-auto flex items-center transition-opacity duration-300"
            >
              visibility
            </i>
          </button>
        {:else}
          <button
            type="button"
            class="hover:text-primary"
            on:click={() => (isPasswordHidden = !isPasswordHidden)}
          >
            <i
              class="material-symbols-outlined my-auto flex items-center transition-opacity duration-300"
            >
              visibility_off
            </i>
          </button>
        {/if}
      </div>
    </div>
  </label>
</div>

<!-- <div>
  let ispasswordHidden: boolean = true;
  let inputType: string;
  $: {
    inputType = ispasswordHidden ? "password" : "text";
  }
  <label class="label">
    <span class="label-text text-xl font-bold">{label}</span>
  </label>
  <div
    class="flex input dark:input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black
    focus-within:ring-2 focus-within:ring-offset-2 first-line:focus-within:ring-opacity-50
    focus-within:ring-gray-300 dark:focus-within:ring-gray-700
    focus-within:ring-offset-theme-offset-color dark:focus-within:ring-offset-gray-900"
  >
    <div class="flex-grow flex">
      <input
        {value}
        name={field}
        type={inputType}
        {placeholder}
        class="w-full dark:input-bordered dark:bg-gray-800 bg-gray-200 dark:text-white text-black focus:outline-none"
      />
    </div>
    <div class="flex-shrink">
      <input
        type="checkbox"
        id={label}
        bind:checked={ispasswordHidden}
        class="hidden"
      />
      <label for={label} class="label text-gray-400 hover:text-gray-600">
        {#if ispasswordHidden}
          <i class="material-symbols-outlined my-auto flex items-center">
            visibility
          </i>
        {:else}
          <i class="material-symbols-outlined my-auto flex items-center">
            visibility_off
          </i>
        {/if}
      </label>
    </div>
  </div>
</div> -->
