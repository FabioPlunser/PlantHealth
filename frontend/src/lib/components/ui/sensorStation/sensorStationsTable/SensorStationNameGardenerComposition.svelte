<script lang="ts">
  import GardenerSelect from "../sensorStationInfo/GardenerSelect.svelte";
  import SensorStationNameInput from "../sensorStationInfo/SensorStationNameInput.svelte";
  import SensorStationUpdateButton from "../sensorStationInfo/SensorStationUpdateButton.svelte";

  export let sensorStation: SensorStation;
  export let gardener: any;
</script>

<div>
  <SensorStationNameInput bind:sensorStation />
  <div class="mt-2">
    <GardenerSelect {gardener} bind:sensorStation />
  </div>
  <div class=" left-20 mt-2">
    <SensorStationUpdateButton bind:sensorStation />
  </div>
</div>
