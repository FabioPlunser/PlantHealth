<script lang="ts">
  import { enhance } from "$app/forms";
  import Input from "$components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import { redirect } from "@sveltejs/kit";
  import LockUnlockButton from "./LockUnlockButton.svelte";
  import ConnectedDisconnectedBadge from "./ConnectedDisconnectedBadge.svelte";
  import DownloadQrCode from "./DownloadQrCode.svelte";
  import GardenerSelect from "./GardenerSelect.svelte";
  import SensorStationNameInput from "./SensorStationNameInput.svelte";
  import SensorStationDeleteButton from "./SensorStationDeleteButton.svelte";
  import SensorStationUpdateButton from "./SensorStationUpdateButton.svelte";
  import SensorStationSettingsButton from "./SensorStationSettingsButton.svelte";
  //---------------------------------------------------------------
  //---------------------------------------------------------------
  export let sensorStation: Responses.InnerResponse;
  export let showDetailLink: boolean = false;
  export let gardener: any;
  export let form: any;
  //---------------------------------------------------------------
  //---------------------------------------------------------------
</script>

<div class="float margin-right text-2xl">
  {#if !sensorStation.accessPointUnlocked}
    <div class="font-bold text-2xl flex justify-center">
      <h1>AccessPoint {sensorStation.roomName} is locked</h1>
    </div>
  {:else}
    <div>
      <SensorStationDeleteButton
        sensorStationId={sensorStation.sensorStationId}
      />
    </div>
    {#if showDetailLink}
      <SensorStationSettingsButton
        sensorStationId={sensorStation.sensorStationId}
      />
    {/if}
    <div>
      <SensorStationNameInput bind:sensorStation label="Name" />
    </div>
    <div>
      <span class="font-bold">Room: </span>
      <span>{sensorStation.roomName}</span>
    </div>
    <div>
      <span class="font-bold">MAC: </span>
      <span>{sensorStation.bdAddress}</span>
    </div>
    <div>
      <span class="font-bold">DIP: </span>
      <span>{sensorStation.dipSwitchId}</span>
    </div>
    <div class="xl:flex gap-4">
      <span class="font-bold flex items-center">Gardener: </span>
      <GardenerSelect {gardener} bind:sensorStation />
    </div>

    <div class="">
      <ConnectedDisconnectedBadge bind:sensorStation />
      <div class="flex justify-center">
        <DownloadQrCode bind:sensorStation />
      </div>
      <div class="flex justify-center mx-auto gap-2 mt-6">
        <SensorStationUpdateButton bind:sensorStation />
        <LockUnlockButton bind:sensorStation />
      </div>
    </div>
  {/if}
</div>
