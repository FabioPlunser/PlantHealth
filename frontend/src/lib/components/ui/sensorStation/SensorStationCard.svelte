<script lang="ts">
  import { fly } from "svelte/transition";
  import StationInfo from "./sensorStationInfo/StationInfo.svelte";
  export let sensorStation: Responses.SensorStationBaseResponse;
  export let form: any;
  export let showDetailLink: boolean = true;
  export let gardener: Responses.ListResponse;
</script>

<div
  in:fly|self={{ y: -200, duration: 200, delay: 100 * i }}
  out:fly|self={{ y: 200, duration: 200 }}
  class:blinking-border-red={sensorStation.alarms.some((alarm) => {
    return alarm.alarm != "n";
  })}
  class="relative"
>
  <div class="card w-full h-fit bg-base-100 dark:border-none shadow-2xl">
    <div class="card-body">
      <StationInfo
        {sensorStation}
        {form}
        {showDetailLink}
        gardener={gardener.items}
      />
    </div>
  </div>
</div>
