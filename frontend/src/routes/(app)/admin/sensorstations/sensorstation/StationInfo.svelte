<script lang="ts">
  import { enhance } from "$app/forms";
  import Input from "$components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import { redirect } from "@sveltejs/kit";
  import LockUnlockButton from "./LockUnlockButton.svelte";
  import ConnectedDisconnectedBadge from "./ConnectedDisconnectedBadge.svelte";
  import DownloadQrCode from "./DownloadQrCode.svelte";
  import GardenerSelect from "./GardenerSelect.svelte";
  //---------------------------------------------------------------
  //---------------------------------------------------------------
  export let sensorStation: SensorStation;
  export let showDetailLink: boolean = false;
  export let gardener: any;
  export let form: any;
  function setCookie(id: any) {
    document.cookie = `sensorStationId=${id}; path=/;`;
  }
  //---------------------------------------------------------------
  //---------------------------------------------------------------
  $: {
    sensorStation.gardener.personId = sensorStation.gardener.personId;
  }
</script>

<form method="post" use:enhance>
  <div class="float margin-right text-2xl">
    <input
      type="hidden"
      name="sensorStationId"
      value={sensorStation.sensorStationId}
    />
    <div>
      <button
        type="submit"
        on:click={(event) => {
          let isDeleteConfirmed = confirm(
            `You will delete this sensor station permanently!`
          );
          if (!isDeleteConfirmed) {
            event.preventDefault();
            return;
          }
          throw redirect(307, "/admin/sensorstations");
        }}
        formaction="?/delete"
      >
        <i
          class="absolute top-0 right-0 m-4 bi bi-trash text-4xl hover:text-red-500"
        />
      </button>
    </div>
    {#if showDetailLink}
      <div class="absolute top-0.5 right-12 m-4">
        <a href="/admin/sensorstations/sensorstation">
          <button
            type="button"
            on:click={() => setCookie(sensorStation.sensorStationId)}
            class="transform transition-transform hover:rotate-90 active:scale-125 animate-spin"
          >
            <i class="bi bi-gear-fill text-3xl hover:text-primary" />
          </button>
        </a>
      </div>
    {/if}
    <div>
      <Input
        field="name"
        label="Name"
        placeholder="Plant1"
        type="text"
        value={sensorStation.name}
      />
      <FormError field="name" {form} />
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
        <input
          type="hidden"
          name="gardener"
          bind:value={sensorStation.gardener.personId}
        />
        <button formaction="?/update" class="btn btn-primary">Update</button>

        <LockUnlockButton bind:sensorStation />
      </div>
    </div>
  </div>
</form>
