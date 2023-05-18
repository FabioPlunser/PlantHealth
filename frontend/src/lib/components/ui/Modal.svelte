<script lang="ts">
  import { scale } from "svelte/transition";
  import { createEventDispatcher, onDestroy } from "svelte";
  const dispatch = createEventDispatcher();
  const close = () => dispatch("close");
  /**
   * Id for modal must be unique
   */
  export let open = false;
  export let closeOnBodyClick = true;
  export let _class = "";

  function handleDispatch() {}
  const previously_focused =
    typeof document !== "undefined" && document.activeElement;
  if (previously_focused) {
    onDestroy(() => {
      previously_focused.focus();
      handleDispatch();
    });
  }
  function handleKeyDown(event: any) {
    if (event.key === "Escape") {
      close();
    }
  }
</script>

<!-- @component
This is a Modal component. It is used to display a modal on the screen.
Usage: 
```html

<script lang="ts">
  import Modal from "$components/ui/Modal.svelte";
  export let open = false;
<script/>

<Modal {open} on:close={() => (open = false)} closeOnBodyClick={false}>
  <div>
    <h1>SensorData</h1>
  </div>
  <div class="mx-auto">
    <button class="btn btn-info" on:click={() => (open = false)}
      >Close</button
    >
  </div>
</Modal>
```
-->

<svelte:window on:keydown={handleKeyDown} />

{#if closeOnBodyClick}
  <div
    class="modal cursor-pointer"
    class:modal-open={open}
    on:keydown={handleKeyDown}
    on:click={close}
  >
    <div
      transition:scale={{ duration: 150 }}
      class="modal-box bg-base-300 {_class} max-w-none"
    >
      <slot />
    </div>
  </div>
{:else}
  <div
    class="modal cursor-pointer"
    class:modal-open={open}
    on:keydown={handleKeyDown}
    on:click|self={close}
  >
    <div
      transition:scale={{ duration: 150 }}
      class="modal-box bg-base-100 {_class} max-w-none"
    >
      <slot />
    </div>
  </div>
{/if}
