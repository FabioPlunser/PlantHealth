<script lang="ts">
  import Table from "$components/table/Table.svelte";
  import { flexRender, type ColumnDef } from "@tanstack/svelte-table";
  import {
    TextCell,
    SensorTypeBadgeCell,
    SensorValueCell,
    LocaleDateCell,
  } from "$components/table/cellComponents";
  //-------------------------------------------------------------------
  //-------------------------------------------------------------------
  export let data: any;
  data = data?.data;
  $: console.log(data);
  //-------------------------------------------------------------------
  //-------------------------------------------------------------------
  interface Columns {
    timeStamp: Date;
    type: string;
    value: number;
    alarm: string;
  }

  let tableData: Columns[] = [];
  data.forEach((element: any) => {
    element.values.forEach((value: any) => {
      let values: Columns = {
        timeStamp: new Date(value.timeStamp),
        type: element.sensorType,
        value: value.value,
        alarm: value.alarm,
      };
      tableData.push(values);
    });
  });

  let columns: ColumnDef<Columns>[] = [
    {
      id: "timeStamp",
      accessorKey: "timeStamp",
      header: () => flexRender(TextCell, { text: "Time" }),
      cell: (info) => flexRender(LocaleDateCell, { date: info.getValue() }),
    },
    {
      id: "type",
      accessorKey: "type",
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
      id: "alarm",
      accessorKey: "alarm",
      header: () => flexRender(TextCell, { text: "Alarm" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
  ];
  //-------------------------------------------------------------------
  //-------------------------------------------------------------------
  let mobileColumnVisibility = {
    timeStamp: false,
    alarm: false,
  };
</script>

<div class="m-2">
  <div class="overflow-auto">
    <Table data={tableData} {columns} {mobileColumnVisibility} />
  </div>
</div>
