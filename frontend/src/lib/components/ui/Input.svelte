<script lang="ts">
  export let field: string = "";
  export let label: string = "";
  export let placeholder: string = "";
  export let type: string = "";
  export let value: any = null;
</script>

<label class="label">
  <span class="label-text font-bold">{label}</span>
</label>
<input
  {value}
  name={field}
  {type}
  {placeholder}
  class="input input-bordered w-full bg-gray-800 text-white"
/>
