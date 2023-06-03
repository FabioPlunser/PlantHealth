<!--
  @component
  This component implements an input select to assign a gardener to the sensor station
  
  @param gardener - The set of gardeners acquired by the`get-all-gardener` endpoint
  @param sensorStation \{SensorStation} - The sensor station that a gardener should be assigned to
-->
<script lang="ts">
  export let gardener: any;
  export let sensorStation: SensorStation;
</script>

<div>
  <select
    class="flex items-center justify-center text-sm select dark:bg-gray-700 bg-base-100 w-fit h-2 max-w-xs border-2 border-base-200 dark:border-none"
    bind:value={sensorStation.gardener.personId}
  >
    {#each gardener as person, i}
      {#if sensorStation.gardener?.username === person.username}
        <option selected value={person.personId}>{person.username}</option>
        <input type="hidden" name="delete" value="true" />
        <option value={person.personId}>Unassign</option>
      {:else}
        {#if i == 0}
          <option>No gardener assigned</option>
        {/if}
        <option value={person.personId}>{person.username}</option>
      {/if}
    {/each}
  </select>
</div>
