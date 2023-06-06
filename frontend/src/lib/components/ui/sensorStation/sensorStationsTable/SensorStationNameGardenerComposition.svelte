<script lang="ts">
  import GardenerSelect from "../sensorstationInfo/GardenerSelect.svelte";
  import SensorStationNameInput from "../sensorstationInfo/SensorStationNameInput.svelte";
  import SensorStationUpdateButton from "../sensorstationInfo/SensorStationUpdateButton.svelte";

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
