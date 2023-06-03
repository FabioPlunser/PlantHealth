<script lang="ts">
  import { enhance } from "$app/forms";
  import { redirect } from "@sveltejs/kit";
  import { string } from "zod";

  export let sensorStationId: string;
  export let iconClass: string = "absolute top-0 right-0 m-4";
</script>

<form action="POST" use:enhance>
  <input type="hidden" name="sensorStationId" bind:value={sensorStationId} />
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
    <i class={`${iconClass} bi bi-trash text-4xl hover:text-red-500`} />
  </button>
</form>
