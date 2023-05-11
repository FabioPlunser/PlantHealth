<script lang="ts">
  import "../app.css";
  import toast, { Toaster } from "$components/toast";
  import { onMount } from "svelte";

  // onMount(() => {
  //   toast.success("Hello world!");
  // });
  export let data;
  $: {
    data.streamed.notifications.then((notification) => {
      console.log(notification);
      if (notification) {
        switch (notification.type) {
          case "success":
            toast.success(notification.message);
            break;
          case "error":
            toast.error(notification.message);
            break;
        }
      }
    });
  }
</script>

<Toaster />
<main
  class="dark:bg-gradient-to-br from-gray-900 via-gray-900 to-violet-900 bg-fixed overflow-clip"
>
  <slot />
  <div class="pb-96" />
</main>
