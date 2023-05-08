<script lang="ts">
  import Desktop from "$helper/Desktop.svelte";
  import Mobile from "$helper/Mobile.svelte";
  import SensorStationDesktop from "./SensorStationDesktop.svelte";
  import SensorStationMobile from "./SensorStationMobile.svelte";

  export let sensorStation: any;
  export let dates: any;
</script>

<Mobile>
  <SensorStationMobile {sensorStation} {dates} />
</Mobile>

<Desktop>
  <SensorStationDesktop {sensorStation} />
</Desktop>
