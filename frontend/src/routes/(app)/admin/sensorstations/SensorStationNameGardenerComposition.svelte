<script lang="ts">
  import GardenerSelect from "./sensorstation/GardenerSelect.svelte";
  import SensorStationNameInput from "./sensorstation/SensorStationNameInput.svelte";
  import SensorStationUpdateButton from "./sensorstation/SensorStationUpdateButton.svelte";

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
