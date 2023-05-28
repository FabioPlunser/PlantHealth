<script lang="ts">
  import { enhance } from "$app/forms";
  import Input from "$components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import { redirect } from "@sveltejs/kit";
  import LockUnlockButton from "./LockUnlockButton.svelte";
  import ConnectedDisconnectedBadge from "./ConnectedDisconnectedBadge.svelte";
  import DownloadQrCode from "./DownloadQrCode.svelte";
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
      <div>
        <select
          name="gardener"
          class="flex items-center justify-center text-sm select dark:bg-gray-700 bg-base-100 w-fit h-2 max-w-xs border-2 border-base-200 dark:border-none"
        >
          {#each gardener as person, i}
            {#if sensorStation.gardener?.username === person.username}
              <!-- <option hidden class="hidden">{person.personId}</option> -->
              <option selected value={person.personId}>{person.username}</option
              >
              <input type="hidden" name="delete" value="true" />
              <option value={person.personId}>Unassign</option>
            {:else}
              {#if i == 0}
                <option>No gardener assigned</option>
              {/if}
              <!-- <option hidden class="hidden">{person.personId}</option> -->
              <option value={person.personId}>{person.username}</option>
            {/if}
          {/each}
        </select>
      </div>
    </div>

    <div class="">
      <ConnectedDisconnectedBadge bind:sensorStation />
      <div class="flex justify-center">
        <DownloadQrCode bind:sensorStation />
      </div>
      <div class="flex justify-center mx-auto gap-2 mt-6">
        <button formaction="?/update" class="btn btn-primary">Update</button>

        <LockUnlockButton bind:sensorStation />
      </div>
    </div>
  </div>
</form>
