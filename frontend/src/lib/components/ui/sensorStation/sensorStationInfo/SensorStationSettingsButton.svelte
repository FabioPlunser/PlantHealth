<script lang="ts">
  export let sensorStationId: string;
  export let iconClass: string = "absolute top-0.5 right-12";
  function setCookie(id: any) {
    document.cookie = `sensorStationId=${id}; path=/;`;
  }
</script>

<div class={`${iconClass} m-4`}>
  <a href="/admin/sensorstations/sensorstation">
    <button
      type="button"
      on:click={() => setCookie(sensorStationId)}
      class="transform transition-transform hover:rotate-90 active:scale-125 animate-spin"
    >
      <i class="bi bi-gear-fill text-3xl hover:text-primary" />
    </button>
  </a>
</div>
