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
