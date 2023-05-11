<script lang="ts">
  import { fly, slide } from "svelte/transition";
  import { onMount } from "svelte";
  // ----------------------------------
  // ----------------------------------
  import PictureModal from "./PictureModal.svelte";
  import Input from "$components/ui/Input.svelte";
  import Desktop from "$helper/Desktop.svelte";
  import Mobile from "$helper/Mobile.svelte";
  import SensorLimitsModal from "./SensorLimitsModal.svelte";
  import SensorDataModal from "./SensorDataModal.svelte";
  import { enhance } from "$app/forms";
  import { flexRender, type ColumnDef } from "@tanstack/svelte-table";
  import { TextCell } from "$lib/components/table/cellComponents";
  import Table from "$lib/components/table/Table.svelte";
  import StationInfo from "./StationInfo.svelte";
  // ----------------------------------
  // ----------------------------------
  let rendered = false;
  onMount(() => {
    if (!rendered) {
      rendered = true;
    }
  });
  // ----------------------------------
  // ----------------------------------
  export let data;
  let sensorStation: SensorStation = data.sensorStation;
  //let sensorStation = data.sensorStation;
  let limits: SensorLimit[] = data.sensorStation.sensorLimits;
  // ----------------------------------
  // ----------------------------------
  let sensorDataModal = false;
  let sensorLimitsModal = false;
  let picturesModal = false;
  interface SensorData {
    sensor: { [type: string]: string };
    value: number;
    belowLimit: number;
    aboveLimit: number;
    alarm: string;
  }

  let columns: ColumnDef<SensorData>[] = [
    {
      id: "sensorType",
      accessorKey: "sensor.type",
      header: () => flexRender(TextCell, { text: "Type" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "value",
      accessorKey: "value",
      header: () => flexRender(TextCell, { text: "Value" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "belowLimit",
      accessorKey: "belowLimit",
      header: () => flexRender(TextCell, { text: "Above Limit ?" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "aboveLimit",
      accessorKey: "aboveLimit",
      header: () => flexRender(TextCell, { text: "Below Limit ?" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "alarm",
      accessorKey: "alarm",
      header: () => flexRender(TextCell, { text: "Alarm" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
  ];

  let mobileColumnVisibility: ColumnVisibility = {};
</script>

{#if rendered}
  <!--
  <PictureModal bind:open={picturesModal} pictures={data.pictures} />
-->
  <section in:fly={{ y: -200, duration: 200 }}>
    <div class="flex justify-center mx-auto">
      <div
        in:fly|self={{ y: -200, duration: 200, delay: 100 }}
        out:fly|local|self={{ y: 200, duration: 200 }}
        class="flex card p-8 border h-fit bg-base-100 dark:border-none shadow-2xl"
      >
        <StationInfo {sensorStation} />
        <Mobile>
          <div class="grid grid-rows gap-4 m-4" in:slide>
            <button
              class="btn btn-warning text-white"
              on:click={() => (sensorLimitsModal = true)}>Sensor Limits</button
            >
            <button class="btn" on:click={() => (sensorDataModal = true)}
              >Sensor Data</button
            >
          </div>
        </Mobile>

        <Desktop>
          <div in:slide={{ duration: 200 }}>
            <br />
            <h1 class="text-2xl mx-auto font-bold">SensorLimits</h1>
            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />
            {#if limits.length === 0}
              <h1>There is no Information about the sensorstation yet.</h1>
            {:else}
              <div
                class="mx-auto grid grid-rows md:grid-cols-2 xl:grid-cols-3 gap-4"
              >
                {#each limits as limit}
                  <div class="flex justify-center">
                    <div
                      class="card border h-fit bg-base-100 dark:border-none shadow-2xl w-60"
                    >
                      <div class="card-body">
                        <div class="flex text-center">
                          <h1 class="mx-auto font-semibold">
                            {limit.sensor.type}
                            <br />
                            <span class="ml-2 justify-center"
                              >[{limit.sensor.unit}]</span
                            >
                          </h1>
                        </div>
                        <form method="post">
                          <input
                            type="hidden"
                            name="sensorStationId"
                            value={sensorStation.sensorStationId}
                          />
                          <input
                            type="hidden"
                            name="sensorId"
                            value={limit.sensor.sensorId}
                          />
                          <Input
                            field="upperLimit"
                            type="number"
                            label="UpperLimit: "
                            value={limit.upperLimit}
                          />
                          <Input
                            field="lowerLimit"
                            type="number"
                            label="LowerLimit: "
                            value={limit.lowerLimit}
                          />
                          <div class="card-actions mx-auto justify-center mt-4">
                            <button
                              class="btn btn-primary"
                              formaction="?/setLimit">Set Limit</button
                            >
                          </div>
                        </form>
                      </div>
                    </div>
                  </div>
                {/each}
              </div>
            {/if}

            <br />
            <h1 class="text-2xl mx-auto font-bold">SensorData</h1>
            <div
              class="divider mt-2 dark:bg-white bg-black h-[2px] rounded-xl"
            />
            <!--
                
                {#if sensorStation.sensorData.length === 0}
                  <h1>No sesor data available yet.</h1>
                  {:else}
                  <Table
                  data={sensorStation.sensorData}
                  {columns}
                  {mobileColumnVisibility}
                  />
                {/if}
              -->
          </div>
        </Desktop>
      </div>
    </div>
  </section>
{/if}
