<script lang="ts">
  import { enhance } from "$app/forms";
  import { Html5Qrcode } from "html5-qrcode";
  import { onDestroy, onMount } from "svelte";
  import { browser } from "$app/environment";
  import { getDeviceType } from "$helper/getDeviceType";
  import { fly } from "svelte/transition";
  import Input from "$components/ui/Input.svelte";
  import Mobile from "$lib/helper/Mobile.svelte";
  import { redirect } from "@sveltejs/kit";

  let scanning = false;
  let isMobile = false;
  let found = false;
  let rendered = false;
  let html5Qrcode: any = null;
  let device: any = null;
  let form: any;
  let sensorStationIdQr: any = null;

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

  async function onScanSuccess(decodedText: any, decodedResult: any) {
    sensorStationIdQr = new URL(decodedText).searchParams.get(
      "sensorStationId"
    );
    if (sensorStationIdQr === null) {
      alert("Invalid QR-Code");
    }
    await stop();
    form.submit();
  }

  function onScanFailure(error: any) {
    // alert(`Code scan error = ${error}`);
    // console.warn(`Code scan error = ${error}`);
  }
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
    <div class="max-w-xl mx-auto">
      <form method="POST" bind:this={form} use:enhance>
        <h1 class="flex justify-center text-2xl">Type in Plant Code</h1>
        <Input
          field="sensorStationId"
          type="text"
          value={sensorStationIdQr}
          placeholder="123456789"
        />
        <div class="flex justify-center mt-4">
          <button class="btn btn-primary">Find</button>
        </div>
      </form>
    </div>
  {/if}
</section>
