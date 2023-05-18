<script lang="ts">
  import { enhance } from "$app/forms";
  export let method: string;
  export let action: string;
  export let formActionValue: any;
  export let confirmMessage: string;
  export let iconClass: string;
</script>

<label class="button">
  <form {method} {action} use:enhance>
    <input type="hidden" bind:value={formActionValue} name="personId" />
    <button
      type="submit"
      on:click={(event) => {
        let isConfirmed = confirm(confirmMessage);
        if (!isConfirmed) {
          event.preventDefault();
          return;
        }
      }}
    >
      <i class={iconClass} />
    </button>
  </form>
</label>
