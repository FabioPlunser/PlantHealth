<!--
  @component
  This component accesses `sensorStation.sensorStationId`, `sensorStation.name` and`sensorStation.gardener.personId` inside a form to
  updade the sensor station.

  This component makes use of the `?/update` form action so make sure it is present in `+page.server.ts`
  
  @param sensorStation \{SensorStation} - The sensor station that should be updated.
-->
<script lang="ts">
  import { enhance } from "$app/forms";
  export let sensorStation: Responses.SensorStationBaseResponse;
  $: gardener = sensorStation.gardener ? sensorStation.gardener.personId : "";

  const customEnhance = () => {
    return async ({ update }) => {
      await update({ reset: false });
    };
  };
</script>

<form method="post" use:enhance>
  <input
    type="hidden"
    name="sensorStationId"
    bind:value={sensorStation.sensorStationId}
  />
  <input type="hidden" name="gardener" bind:value={gardener} />

  <input type="hidden" name="name" bind:value={sensorStation.name} />
  <button formaction="?/update" class="btn btn-primary">Update</button>
</form>
