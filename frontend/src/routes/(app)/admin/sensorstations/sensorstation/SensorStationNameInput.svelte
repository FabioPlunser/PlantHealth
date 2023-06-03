<!--
  @component
  This component implements an input for the `sensorStation.name` field.
  This component will bind the value of name so it can be used outside this component.
  
  @param sensorStation \{SensorStation} - The sensor station that the name should be changed for
  @param name \{string} - the name of the input field default value `sensorStationName`
-->
<script lang="ts">
  export let sensorStation: SensorStation;
  export let name: string = "sensorStationName";
  export let label: string = "";
</script>

<div>
  <label class="" for={name}>
    <span class="label-text text-xl font-bold">{label}</span>
    <input
      bind:value={sensorStation.name}
      {name}
      type="text"
      placeholder="Plant1"
      class="input input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black mb-2 mt-2"
    />
  </label>
</div>
