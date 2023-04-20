<script lang="ts">
  import { Html5Qrcode } from "html5-qrcode";
  import { onDestroy, onMount } from "svelte";
  import { browser } from "$app/environment";
  import { getDeviceType } from "$helper/getDeviceType";
  import { fly } from "svelte/transition";
  import Input from "$components/ui/Input.svelte";
  import Mobile from "$lib/helper/Mobile.svelte";

  let scanning = false;
  let isMobile = false;
  let found = false;
  let rendered = false;
  let html5Qrcode: any = null;
  let device = null;
  if (browser) {
    device = getDeviceType();
  }

  if (device === "mobile" || device === "tablet") {
    isMobile = true;
    onMount(() => {
      init();
      rendered = true;
    });
  }

  function init() {
    html5Qrcode = new Html5Qrcode("reader");
  }

  function start() {
    html5Qrcode.start(
      { facingMode: "environment" },
      {
        fps: 10,
        qrbox: { width: 250, height: 250 },
      },
      onScanSuccess,
      onScanFailure
    );
    scanning = true;
  }

  async function stop() {
    await html5Qrcode.stop();
    scanning = false;
  }

  function onScanSuccess(decodedText: any, decodedResult: any) {
    alert(`Code matched = ${decodedText}`);
    console.log(decodedText);
    console.log(decodedResult);
    // if found a plant stop scanning and redirect to plant photo page
    stop();
  }

  function onScanFailure(error: any) {
    // alert(`Code scan error = ${error}`);
    // console.warn(`Code scan error = ${error}`);
  }

  // onDestroy(() => {
  //   if (html5Qrcode) {
  //     html5Qrcode.stop();
  //   }
  // });
</script>

<section class="mt-12">
  {#if isMobile}
    <h1 class="flex justify-center text-3xl font-bold">Scan QR-Code</h1>
    {#if !scanning}
      <button
        class="btn btn-primary flex justify-center mx-auto"
        on:click={() => start()}>Start</button
      >
    {/if}
    <div class={scanning ? "block" : "hidden"}>
      <div class="p-4 bg-base-100 rounded-2xl shadow-2xl">
        <reader class="flex items-center justify-center" id="reader" />
      </div>
    </div>
  {/if}
  {#if !scanning}
    <div>
      <form>
        <h1 class="flex justify-center">Type in Plant Code</h1>
        <Input type="text" placeholder="123456789" />
        <div class="flex justify-center mt-4">
          <button class="btn btn-primary">Find</button>
        </div>
      </form>
    </div>
  {/if}
</section>
