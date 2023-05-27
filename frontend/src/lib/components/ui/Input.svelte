<script lang="ts">
  export let field: string = "";
  export let label: string = "";
  export let placeholder: string = "";
  export let type: string = "";
  export let value: any = null;
  let _class = "";
  export { _class as class };
</script>

<div>
  <label class="" for={field}>
    <span class="label-text text-xl font-bold">{label}</span>
    <input
      {value}
      name={field}
      {type}
      {placeholder}
      class="input input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black {_class}  mb-2 mt-2"
    />
  </label>
</div>
