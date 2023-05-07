<script lang="ts">
  import Desktop from "$helper/Desktop.svelte";
  import Mobile from "$helper/Mobile.svelte";
  import SensorStationDesktop from "./SensorStationDesktop.svelte";
  import SensorStationMobile from "./SensorStationMobile.svelte";

  export let sensorStation: any;
</script>

<Mobile>
  <SensorStationMobile {sensorStation} />
</Mobile>

<Desktop>
  <SensorStationDesktop {sensorStation} />
</Desktop>
