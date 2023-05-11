<script lang="ts">
  import { enhance } from "$app/forms";
  import Input from "$components/ui/Input.svelte";
  import FormError from "$lib/helper/formError.svelte";
  import { redirect } from "@sveltejs/kit";

  export let sensorStation: SensorStation;
  export let showDetailLink: boolean = false;
  export let form;
  function setCookie(id: any) {
    document.cookie = `sensorStationId=${id}; path=/;`;
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
        on:click={() => {
          let isDeleteConfirmed = confirm(
            `You will delete this sensor station permanently!`
          );
          if (isDeleteConfirmed) {
            throw redirect(307, "/admin/sensorstations");
          }
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
      <h1>
        <span class="font-bold">Room: </span>
        <span>{sensorStation.roomName}</span>
      </h1>
    </div>
    <div>
      <h1>
        <span class="font-bold">MAC: </span>
        <span>{sensorStation.bdAddress}</span>
      </h1>
    </div>
    <div>
      <h1>
        <span class="font-bold">DIP: </span>
        <span>{sensorStation.dipSwitchId}</span>
      </h1>
    </div>
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
      <button formaction="?/updateName" class="btn btn-primary">Update</button>

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
