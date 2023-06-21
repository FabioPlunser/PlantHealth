<!--
   @component
   A qr-code button that downloads the pdf associated with the sensor station from the `/api/get-sensor-station-qr-code` endpoint
   @param sensorStation \{SensorStation} - the sensor station that the qr-code should be generated for
   
-->
<script lang="ts">
  export let sensorStation: Responses.SensorStationBaseResponse;

  async function downloadQRCode() {
    let response = await fetch(
      `/api/get-sensor-station-qr-code?sensorStationId=${sensorStation.sensorStationId}&roomName=${sensorStation.roomName}&plantName=${sensorStation.name}`
    );
    let data = await response.blob();
    let url = URL.createObjectURL(data);

    const link = document.createElement("a");
    link.href = url;
    link.download = sensorStation.sensorStationId;
    document.body.appendChild(link);
    link.click();
  }
</script>

<div class="mx-auto">
  <button type="button" on:click={downloadQRCode}>
    <i class="bi bi-qr-code-scan text-4xl" />
  </button>
</div>
