<script lang="ts">
  import { fly } from "svelte/transition";
  import { cubicInOut } from "svelte/easing";
  import { toText } from "./date-utils";
  import { parse, createFormat } from "./parse";
  import DateTimePicker from "./DatePicker.svelte";
  import { writable } from "svelte/store";
  import { createEventDispatcher } from "svelte";
  const dispatch = createEventDispatcher();
  const defaultDate = /* @__PURE__ */ new Date();
  const innerStore = writable(null);
  const store = (() => {
    return {
      subscribe: innerStore.subscribe,
      set: (d: any) => {
        if (d === null) {
          innerStore.set(null);
          value = d;
        } else if (d.getTime() !== $innerStore?.getTime()) {
          innerStore.set(d);
          value = d;
        }
      },
    };
  })();
  export let value: any = null;
  $: store.set(value);
  export let min = new Date(defaultDate.getFullYear() - 20, 0, 1);
  export let max = new Date(defaultDate.getFullYear(), 11, 31, 23, 59, 59, 999);
  export let placeholder = "2020-12-31 23:00:00";
  export let valid = true;
  export let disabled = false;
  let classes = "";
  export { classes as class };
  export let format = "yyyy-MM-dd HH:mm:ss";
  let formatTokens = createFormat(format);
  $: formatTokens = createFormat(format);
  export let locale = {};
  function valueUpdate(value2: any, formatTokens2: any) {
    text = toText(value2, formatTokens2);
  }
  $: valueUpdate($store, formatTokens);
  export let text = toText($store, formatTokens);
  function textUpdate(text2: any, formatTokens2: any) {
    if (text2.length) {
      const result = parse(text2, formatTokens2, $store);
      if (result.date !== null) {
        valid = true;
        store.set(result.date);
      } else {
        valid = false;
      }
    } else {
      valid = true;
      if (value) {
        value = null;
        store.set(null);
      }
    }
  }
  $: textUpdate(text, formatTokens);
  export let visible = false;
  export let closeOnSelection = false;
  export let browseWithoutSelecting = false;
  function onFocusOut(e: any) {
    if (
      e?.currentTarget instanceof HTMLElement &&
      e.relatedTarget &&
      e.relatedTarget instanceof Node &&
      e.currentTarget.contains(e.relatedTarget)
    ) {
      return;
    } else {
      visible = false;
    }
  }
  function keydown(e: any) {
    if (e.key === "Escape" && visible) {
      visible = false;
      e.preventDefault();
      e.stopPropagation();
    } else if (e.key === "Enter") {
      visible = !visible;
      e.preventDefault();
    }
  }
  function onSelect(e: any) {
    dispatch("select", e.detail);
    if (closeOnSelection) {
      visible = false;
    }
  }
</script>

<div
  class="date-time-field {classes}"
  on:focusout={onFocusOut}
  on:keydown={keydown}
>
  <input
    class="input input-bordered w-full dark:bg-gray-800 bg-gray-200 dark:text-white text-black caret-red-200"
    class:invalid={!valid}
    type="text"
    value={text}
    {placeholder}
    {disabled}
    on:focus={() => (visible = true)}
    on:mousedown={() => (visible = true)}
    on:input={(e) => {
      if (
        e instanceof InputEvent &&
        e.inputType === "insertText" &&
        typeof e.data === "string" &&
        e.currentTarget.value === text + e.data
      ) {
        // check for missing punctuation, and add if there is any
        let result = parse(text, formatTokens, $store);
        if (
          result.missingPunctuation !== "" &&
          !result.missingPunctuation.startsWith(e.data)
        ) {
          text = text + result.missingPunctuation + e.data;
          return;
        }
      }
      text = e.currentTarget.value;
    }}
  />
  {#if visible && !disabled}
    <div
      class="picker flex justify-center mx-auto rounded-xl"
      class:visible
      transition:fly={{ duration: 80, easing: cubicInOut, y: -5 }}
    >
      <DateTimePicker
        on:focusout={onFocusOut}
        on:select={onSelect}
        bind:value={$store}
        {min}
        {max}
        {locale}
        {browseWithoutSelecting}
      />
    </div>
  {/if}
</div>
