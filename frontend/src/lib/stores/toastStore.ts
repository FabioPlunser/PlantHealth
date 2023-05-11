import { writable } from "svelte/store";
import { logger } from "$helper/logger";

const createToastStore = () => {
  const toasts = new Map();

  const { subscribe, set, update } = writable(toasts);

  /**
   * The function adds a toast message with a given id, type, and message to a map and logs the action.
   * @param id - The id parameter is a unique identifier for the toast message being added.
   * @param type - The `type` parameter in the `addToast` function is used to specify the type of toast
   * message being added. It could be a success message, an error message, a warning message, or any
   * other type of message that the application needs to display to the user. The `type` parameter
   * @param message - The `message` parameter is a string that represents the content of the toast
   * message that will be displayed to the user.
   */
  const addToast = (id, type, message) => {
    logger.info(
      "addToast: " +
        id +
        ": " +
        JSON.stringify(type) +
        " " +
        JSON.stringify(message)
    );
    update((map) => {
      map.set(id, { type, message });
      return map;
    });
  };

  /**
   * The function removes a toast message from a map object.
   * @param id - The `id` parameter is a unique identifier for a toast message that needs to be removed
   * from a map or collection. The `removeToast` function takes this `id` as an argument and uses it to
   * delete the corresponding toast message from the map.
   */
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
