<!--
  @component
  This component takes an alarm value of 'h', 'n' or 'l' it will display a checkmark if the alarm === 'n' and an x otherwise.
  
  @parameter {String} alarm - either 'h' or 'n' or 'l'
-->

<script lang="ts">
  /**
   * The alarm value. Can either be 'h', 'n' or 'l'
   * @type string
   */
  export let alarm: string;

  let isWithinLimit: boolean = alarm === "n";
</script>

{#if !isWithinLimit}
  <i class="material-symbols-outlined my-auto flex items-center text-green-500">
    check_circle
  </i>
{:else}
  <i class="material-symbols-outlined my-auto flex items-center text-red-500">
    cancel
  </i>
{/if}
