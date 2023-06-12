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
    {#if sensorStation.alarms.some((a) => a.alarm !== "n")}
      <div class="blinking flex justify-center mx-auto">
        <span class="material-symbols-outlined"> error </span>
      </div>
    {/if}
    {#if sensorStation.connected}
      <div class="badge badge-success">Connected</div>
    {:else}
      <div class="badge badge-error">Disconnected</div>
    {/if}
  </div>
</div>

<style>
  .blinking {
    position: relative;
    color: red;
  }

  .blinking::before {
    content: "";
    position: absolute;
    color: white;
    animation: blinking-animation 1s ease-in-out infinite;
  }

  @keyframes blinking-animation {
    0% {
      color: red;
      transform: scale(1);
    }
    50% {
      opacity: 0.3;
      color: black;
      transform: scale(1.001);
    }
    100% {
      /* opacity: 1; */
      color: red;
      transform: scale(1);
    }
  }
</style>
