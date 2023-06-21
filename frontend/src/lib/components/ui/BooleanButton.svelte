<script lang="ts">
  export let text: string;
  export let bool: boolean;
  export let disabled: boolean;
  export let field: string;
</script>

<input
  type="checkbox"
  id={text}
  bind:checked={bool}
  {disabled}
  name={field}
  class=" hidden peer"
/>
<label
  for={text}
  class="btn-primary inline-flex w-full p-4 border-2 rounded-2xl cursor-pointer justify-between dark:peer-checked:border-white"
>
  <div>
    <h1 class="text-lg font-semibold capitalize">{text}</h1>
  </div>
  <div>
    {#if bool}
      <span class="material-symbols-outlined"> check_circle </span>
    {/if}
  </div>
</label>
