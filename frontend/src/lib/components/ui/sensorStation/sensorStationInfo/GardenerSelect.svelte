<!--
  @component
  This component implements an input select to assign a gardener to the sensor station
  
  @param gardener - The set of gardeners acquired by the`get-all-gardener` endpoint
  @param sensorStation \{SensorStation} - The sensor station that a gardener should be assigned to
-->
<script lang="ts">
  export let gardener: any;
  export let sensorStation: any;
  let value: any = undefined; 
  
  $: {
    if(value === true){
      sensorStation.unassign = true;
    }else{
      sensorStation.unassign = false; 
      sensorStation.gardener = {personId: value};
    }
  }  
  $: console.log("value", value);
  $: console.log("sensorStation", sensorStation);
</script>

<div>
  <select
    class="flex items-center justify-center text-sm select dark:bg-gray-700 bg-base-100 w-fit h-2 max-w-xs border-2 border-base-200 dark:border-none"
    bind:value
  >
    {#each gardener as person, i}
      {#if sensorStation.gardener === null && i === 0}
        <option selected value={undefined}>No gardener assigned</option>
      {:else}
        {#if sensorStation.gardener?.username === person.username}
          <option selected value={person.personId}>{person.username}</option>
          <option value={true}>
            Unassign
          </option>
        {:else}
          <option value={person.personId}>{person.username}</option>
        {/if}
      {/if}
    {/each}
  </select>
</div>
