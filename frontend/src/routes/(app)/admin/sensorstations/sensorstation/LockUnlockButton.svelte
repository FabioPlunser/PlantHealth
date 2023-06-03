<!--
  @component
  A button that will lock or unlock the sensor station based on `SensorStation.unlocked`.

  This component calls the `?/unlock` form action so make sure it is present in `+page.server.ts`.
  The internal input field has the `name="unlocked"` and will set the `value` to `true` or `false`. 

  @param sensorStation \{SensorStation} - the sensor station that should be locked or unlocked

-->
<script lang="ts">
  import { enhance } from "$app/forms";
  export let sensorStation: SensorStation;
</script>

<div>
  <form action="" method="post" use:enhance>
    <input
      type="hidden"
      name="sensorStationId"
      value={sensorStation.sensorStationId}
    />
    {#if sensorStation.unlocked}
      <button class="btn btn-info" formaction="?/unlock">Unlocked</button>
      <input type="hidden" name="unlocked" value="false" />
    {:else}
      <button class="btn btn-error" formaction="?/unlock">Locked</button>
      <input type="hidden" name="unlocked" value="true" />
    {/if}
  </form>
</div>
