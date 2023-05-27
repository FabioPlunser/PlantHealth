<script lang="ts">
  import Table from "$components/table/Table.svelte";
  import { flexRender, type ColumnDef } from "@tanstack/svelte-table";
  import {
    TextCell,
    SensorTypeBadgeCell,
    SensorValueCell,
    LocaleDateCell,
  } from "$components/table/cellComponents";
  import AlarmCell from "../../table/cellComponents/AlarmCell.svelte";
  //-------------------------------------------------------------------
  //-------------------------------------------------------------------
  export let data: any;
  data = data?.data;
  //-------------------------------------------------------------------
  //-------------------------------------------------------------------
  interface SensorData {
    timeStamp: Date;
    type: string;
    value: number;
    unit: string;
    alarm: string;
  }

  let tableData: SensorData[] = [];
  data.forEach((element: any) => {
    element.values.forEach((value: any) => {
      let values: SensorData = {
        timeStamp: new Date(value.timeStamp),
        type: element.sensorType,
        value: value.value,
        unit: element.unit,
        alarm: value.alarm,
      };
      tableData.push(values);
    });
  });

  let columns: ColumnDef<SensorData>[] = [
    {
      id: "type",
      accessorKey: "type",
      header: () => flexRender(TextCell, { text: "Type" }),
      cell: (info) =>
        flexRender(SensorTypeBadgeCell, { type: info.getValue() }),
    },
    {
      id: "value",
      accessorKey: "value",
      header: () => flexRender(TextCell, { text: "Value" }),
      cell: ({ row }) =>
        flexRender(SensorValueCell, {
          value: row.original.value,
          unit: row.original.unit,
        }),
    },
    {
      id: "alarm",
      accessorKey: "alarm",
      header: () => flexRender(TextCell, { text: "Within Limits" }),
      cell: (info) => flexRender(AlarmCell, { alarm: info.getValue() }),
    },
    {
      id: "timeStamp",
      accessorKey: "timeStamp",
      header: () => flexRender(TextCell, { text: "Time" }),
      cell: (info) => flexRender(LocaleDateCell, { date: info.getValue() }),
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
