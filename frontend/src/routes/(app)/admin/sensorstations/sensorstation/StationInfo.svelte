<script lang="ts">
  import { enhance } from "$app/forms";
  import Input from "$components/ui/Input.svelte";

  export let sensorStation: SensorStation;
</script>

<form method="post" use:enhance>
  <div class="float margin-right text-2xl">
    <input
      type="hidden"
      name="sensorStationId"
      value={sensorStation.sensorStationId}
    />
    <div class=""><i class="bi bi-trash text-3xl hover:text-red-500" /></div>
    <Input
      field="name"
      label="Name"
      placeholder="Plant1"
      type="text"
      value={sensorStation.name}
    />
    <h1>
      <span class="font-bold">Room: </span><span>{sensorStation.roomName}</span>
    </h1>
    <h1>
      <span class="font-bold">MAC: </span><span>{sensorStation.bdAddress}</span>
    </h1>
    <h1>
      <span class="font-bold">DIP: </span><span
        >{sensorStation.dipSwitchId}</span
      >
    </h1>
    <div class="flex mx-auto justify-center m-4">
      {#if sensorStation.connected}
        <div class="badge badge-success">Connected</div>
      {:else}
        <div class="badge badge-error">Disconnected</div>
      {/if}
    </div>
    <div class="flex justify-center">
      <div class="flex">
        <div class="mx-auto">
          <a
            href="http://localhost:3000/api/get-sensor-station-qr-code?sensorStationId=e5dc8654-255e-4fdd-b58e-8160f3a8fd7c&roomName=Office1&plantName=Sakura"
          >
            <i class="bi bi-qr-code-scan text-4xl" />
          </a>
        </div>
      </div>
    </div>

    <div class="flex justify-center mx-auto gap-2 mt-6">
      <button formaction="?/update" class="btn btn-primary">Update</button>

      {#if sensorStation.unlocked}
        <button class="btn btn-info" formaction="?/unlock">Unlocked</button>
        <input type="hidden" name="unlocked" value="false" />
      {:else}
        <button class="btn btn-error" formaction="?/unlock">Locked</button>
        <input type="hidden" name="unlocked" value="true" />
      {/if}
    </div>
  </div>
</form>
