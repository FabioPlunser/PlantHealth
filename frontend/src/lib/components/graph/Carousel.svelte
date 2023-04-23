<script lang="ts">
  export let pictures: string;
  export let width: number;
  pictures = "https://picsum.photos/200/300";
</script>

<div class="carousel space-x-4" style="width: {width}px">
  {#each { length: 10 } as _, i}
    <div class="carousel-item">
      <!-- svelte-ignore a11y-click-events-have-key-events -->
      <img src={pictures} class="rounded-box" alt="CarouselItem-{i}" />
    </div>
  {/each}
</div>
