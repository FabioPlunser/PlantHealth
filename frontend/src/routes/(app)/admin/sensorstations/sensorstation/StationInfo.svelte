<script lang="ts">
  import { enhance } from "$app/forms";
  import Input from "$components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import { redirect } from "@sveltejs/kit";
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
  //---------------------------------------------------------------
  //---------------------------------------------------------------
</script>

<form
  method="post"
  use:enhance={() => {
    return async ({ update }) => {
      await update({ reset: false });
    };
  }}
>
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
              <option value="null">No gardener assigned</option>
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
            <button type="button" on:click={downloadQRCode}>
              <i class="bi bi-qr-code-scan text-4xl" />
            </button>
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
  </div>
</form>
