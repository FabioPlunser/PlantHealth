<script lang="ts">
  import useToaster from "../core/use-toaster";
  import ToastWrapper from "./ToastWrapper.svelte";
  export let reverseOrder = false;
  export let position: any = "top-center";
  export let toastOptions: any = void 0;
  export let gutter = 8;
  export let containerStyle: any = void 0;
  export const containerClassName: any = void 0;
  const { toasts, handlers } = useToaster(toastOptions);
  let _toasts: any;
  $: _toasts = $toasts.map((toast) => ({
    ...toast,
    position: toast.position || position,
    offset: handlers.calculateOffset(toast, $toasts, {
      reverseOrder,
      gutter,
      defaultPosition: position,
    }),
  }));
</script>

<div
  class="dark:bg-base-300"
  style={containerStyle}
  on:mouseenter={handlers.startPause}
  on:mouseleave={handlers.endPause}
>
  {#each _toasts as toast (toast.id)}
    <ToastWrapper
      {toast}
      setHeight={(height) => handlers.updateHeight(toast.id, height)}
    />
  {/each}
</div>
