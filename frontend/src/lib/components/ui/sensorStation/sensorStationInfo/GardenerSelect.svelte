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

  function handleSelect() {
    if (value === true) {
      sensorStation.unassign = true;
    } else {
      sensorStation.unassign = false;
      if (sensorStation.gardener) {
        sensorStation.gardener.personId = value;
        return;
      } else {
        let gardener: Responses.Person = {
          personId: value,
          username: "",
          password: "",
          token: "",
          permissions: [],
          sensorStationPersonReferences: [],
          email: "",
        };
        sensorStation.gardener = gardener;
        sensorStation.gardener.personId = value;
      }
    }
  }
  $: console.log(gardener);
  $: console.log(sensorStation);
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
      {#each gardener as person, i}
        {#if sensorStation.gardener && sensorStation.gardener.username === person.username}
          <option selected value={person.personId}>{person.username}</option>
          <option value={true}> Unassign </option>
        {:else if !sensorStation.gardener || i == 0}
          <option selected value={"No gardener assigned"}
            >No gardener assigned</option
          >
          <option value={person.personId}>{person.username}</option>
        {:else}
          <option value={person.personId}>{person.username}</option>
        {/if}
      {/each}
    </select>
  {/if}
</div>
