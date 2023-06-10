<script lang="ts">
  import { fly } from "svelte/transition";
  import Modal from "$components/ui/Modal.svelte";
  import Spinner from "$components/ui/Spinner.svelte";
  // ----------------------------------
  // ----------------------------------
  import { onMount } from "svelte";
  let rendered = false;
  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
  });
  // ----------------------------------
  // ----------------------------------
  // array of promises
  export let open = false;
  // ----------------------------------
  // ----------------------------------
</script>

{#if rendered}
  <div>
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
  </div>
{/if}
