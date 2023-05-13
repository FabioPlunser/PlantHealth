<script lang="ts">
  import "../app.css";
  import toast, { Toaster } from "$components/toast";

  export let data;
  $: {
    data?.streamed?.notifications.then((notification) => {
      let position = "top-right";
      let toastId = "";
      if (notification) {
        switch (notification.type) {
          case "success":
            toastId = toast.success(notification.message, { position });
            break;
          case "error":
            toastId = toast.error(notification.message, { position });
            break;
        }
      }
    });
  }
</script>

{#if data?.error}
  <div
    class="bg-red-400 p-4 w-1/2 rounded-2xl shadow-2xl  mx-auto text-black my-auto absolute mt-24 left-0 right-0"
  >
    <div>
      <h1 class="text-3xl font-bold mx-auto flex justify-center">
        {data?.error}
      </h1>
      <h1 class="mx-auto flex justify-center font-bold">
        Please refresh the page
      </h1>
    </div>
  </div>
{/if}
<Toaster />
<main
  class="dark:bg-gradient-to-br from-gray-900 via-gray-900 to-violet-900 bg-fixed overflow-clip"
>
  <slot />
  <div class="pb-96" />
</main>
