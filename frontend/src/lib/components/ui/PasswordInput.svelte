<script lang="ts">
  export let field: string = "";
  export let label: string = "";
  export let placeholder: string = "";
  export let value: string = "";

  let ispasswordHidden: boolean = true;
  let inputType: string;
  $: {
    inputType = ispasswordHidden ? "password" : "text";
  }
</script>

<div>
  <!-- svelte-ignore a11y-label-has-associated-control -->
  <label class="label">
    <span class="label-text font-bold">{label}</span>
  </label>
  <!-- it seems like the focus properties of input input-bordered do not translate to a div... therefore I had to imitate the behaviour-->
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
</div>
