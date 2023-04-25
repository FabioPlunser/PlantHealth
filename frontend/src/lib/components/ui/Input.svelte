<script lang="ts">
  export let field: string = "";
  export let label: string = "";
  export let placeholder: string = "";
  export let type: string = "";
  export let value: string = "";
</script>

<label class="label">
  <span class="label-text font-bold">{label}</span>
</label>
<input
  {value}
  name={field}
  {type}
  {placeholder}
  class="input dark:input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black"
/>
