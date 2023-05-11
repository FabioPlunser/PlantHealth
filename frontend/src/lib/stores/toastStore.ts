import { writable } from "svelte/store";
import { logger } from "$helper/logger";

const createToastStore = () => {
  const toasts = new Map();

  const { subscribe, set, update } = writable(toasts);

  const addToast = (id, type, message) => {
    logger.info("addToast: " + id + ": " + message);
    update((map) => {
      map.set(id, { type, message });
      return map;
    });
  };

  const removeToast = (id) => {
    update((map) => {
      map.delete(id);
      return map;
    });
  };

  return {
    subscribe,
    addToast,
    removeToast,
    get: (id) => {
      return {
        subscribe: (run) => {
          subscribe((map) => {
            run(map.get(id));
          });
        },
        set,
        update,
      };
    },
  };
};

export const toasts = createToastStore();

// <script>
//   import { toasts } from "./toastStore.js";
// </script>

// {#each $toasts as [id, { message, type }]}
//   <div class="toast" class:type>
//     <span>{message}</span>
//     <button on:click={() => toasts.removeToast(id)}>Close</button>
//   </div>
// {/each}
