<script lang="ts">
  import Grid from "svelte-grid";
  import gridHelp from "svelte-grid/build/helper/index.mjs";

  const COLS = 2;

  const id = () => "_" + Math.random().toString(36).substring(2, 9);

  const randomNumberInRange = (min, max) => Math.random() * (max - min) + min;

  let items = [
    {
      [COLS]: gridHelp.item({
        x: 0,
        y: 0,
        w: 2,
        h: 2,
      }),
      id: id(),
    },
    {
      [COLS]: gridHelp.item({
        x: 2,
        y: 0,
        w: 2,
        h: 2,
      }),
      id: id(),
    },
  ];

  const cols = [[1200, 2]];
  let adjustAfterRemove = false;

  const remove = (item: any) => {
    items = items.filter((value) => value.id !== item.id);
    if (adjustAfterRemove) {
      items = gridHelp.adjust(items, cols);
    }
  };
</script>

<div class="demo-container">
  <Grid bind:items rowHeight={100} let:item let:dataItem {cols}>
    <div class="">
      <!-- svelte-ignore a11y-click-events-have-key-events -->
      <span
        on:pointerdown={(e) => e.stopPropagation()}
        on:click={() => remove(dataItem)}
        class="remove"
      >
        ✕
      </span>
      <div class="card bg-base-100 shadow-2xl rounded-2xl w-full h-full">
        <div class="card-title">
          <h3>Item {item.id}</h3>
        </div>
        <div class="card-body">
          <p>Width: {item.w}</p>
          <p>Height: {item.h}</p>
          <p>Position: {item.x}, {item.y}</p>
        </div>
      </div>
    </div>
  </Grid>
</div>

<style>
  .demo-widget {
    background: #f1f1f1;
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .demo-container {
    max-width: 800px;
    width: 100%;
  }

  .remove {
    cursor: pointer;
    position: absolute;
    right: 5px;
    top: 3px;
    user-select: none;
  }
</style>
