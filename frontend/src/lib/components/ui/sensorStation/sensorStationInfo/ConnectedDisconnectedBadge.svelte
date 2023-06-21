<!--
  @component
  A connection status badge based on `SensorStation.connected`
  @param sensorStation \{SensorStation} - the sensor station the status should be displayed for
-->
<script lang="ts">
  export let sensorStation: Responses.SensorStationBaseResponse;
</script>

<div class="flex mx-auto justify-center m-4">
  <div class="mx-auto">
    {#if sensorStation.connected}
      <div class="badge badge-success">Connected</div>
    {:else}
      <div class="badge badge-error">Disconnected</div>
    {/if}
  </div>
</div>
