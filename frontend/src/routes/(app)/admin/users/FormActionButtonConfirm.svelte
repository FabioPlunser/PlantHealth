<script lang="ts">
  import { enhance } from "$app/forms";
  export let method: string;
  export let action: string;
  export let formActionValue: any;
  export let confirmMessage: string;
  export let iconClass: string;
</script>

<label class="button">
  <!-- TODO make delete user action with verification Modal-->
  <!--on click should call deleteUser action and pass Id-->
  <form {method} {action} use:enhance>
    <input type="hidden" bind:value={formActionValue} name="personId" />
    <button
      type="submit"
      on:click={() => {
        return confirm(confirmMessage);
      }}
    >
      <i class={iconClass} />
    </button>
  </form>
</label>
