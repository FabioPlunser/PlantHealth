<!--
  @component
  This component implements an input select to assign a gardener to the sensor station
  
  @param gardener - The set of gardeners acquired by the`get-all-gardener` endpoint
  @param sensorStation \{SensorStation} - The sensor station that a gardener should be assigned to
-->
<script lang="ts">
  export let gardener: Responses.Person[] = [];
  export let sensorStation: any;
  let value: any = undefined;

  function handleSelect() {
    if (value === "unassign") {
      sensorStation.gardener = null;
    } else {
      let gardener: Responses.Person = {
        personId: value.personId,
        username: value.username,
        password: "",
        token: "",
        permissions: [],
        sensorStationPersonReferences: [],
        email: "",
      };
      sensorStation.gardener = gardener;
    }
  }
</script>

<div>
  {#if gardener.length === 0}
    <p>No gardner</p>
  {:else}
    <select
      class="flex items-center justify-center text-sm select dark:bg-gray-700 bg-base-100 w-fit h-2 max-w-xs border-2 border-base-200 dark:border-none"
      bind:value
      on:change={handleSelect}
    >
      {#key sensorStation}
        {#each gardener as person, i (person.personId)}
          {#if i === 0 && !sensorStation.gardener}
            <option selected value={"unassign"}>Unassigned</option>
          {:else if i === 0}
            <option value={"unassign"}>Unassigned</option>
          {/if}
          {#if sensorStation.gardener && sensorStation.gardener.username === person.username}
            <option selected value={person}>{person.username}</option>
          {:else}
            <option value={person}>{person.username}</option>
          {/if}
        {/each}
      {/key}
    </select>
  {/if}
</div>
