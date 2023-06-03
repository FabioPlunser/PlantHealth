<!--
  @component
  This component implements a button with a formaction, a single value passed to that form and a confirm message on button press.
  This component was intended for a delete button passed an id see the example below.
  
  @param method \{string} - the method parameter for the form
  @param action \{string} - the form action of the form
  @param fromActionValue \{any} - the value of the hidden input in the form
  @param confirmMessage \{string} - the confirm message that should be displayed
  @param iconClass \{string} - the icon string for this button

  ```javascript
  <FormActionButtonConfirm
    method="POST"
    action="?/deleteUser"
    formActionValue={row.original.personId}
    confirmMessage={`You will delete this user ${row.original.username.toUpperCase()} permanently!`}
    iconClass="bi bi-trash text-3xl hover:text-red-500"
  />
  ```
-->

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
