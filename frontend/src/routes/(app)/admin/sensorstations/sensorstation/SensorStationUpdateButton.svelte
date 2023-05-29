<!--
  @component
  This component accesses `sensorStation.sensorStationId`, `sensorStation.name` and`sensorStation.gardener.personId` inside a form to
  updade the sensor station.

  This component makes use of the `?/update` form action so make sure it is present in `+page.server.ts`
  
  @param sensorStation \{SensorStation} - The sensor station that should be updated.
-->
<script lang="ts">
  export let sensorStation: SensorStation;
</script>

<form method="post">
  <input
    type="hidden"
    name="sensorStationId"
    bind:value={sensorStation.sensorStationId}
  />
  <input
    type="hidden"
    name="gardener"
    bind:value={sensorStation.gardener.personId}
  />
  <input type="hidden" name="name" bind:value={sensorStation.name} />
  <button formaction="?/update" class="btn btn-primary">Update</button>
</form>
