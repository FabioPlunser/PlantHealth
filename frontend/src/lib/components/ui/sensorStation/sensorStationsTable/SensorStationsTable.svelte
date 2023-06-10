<script lang="ts">
  import { flexRender, type ColumnDef } from "@tanstack/svelte-table";
  import SensorStationNameInput from "../sensorStationInfo/SensorStationNameInput.svelte";
  import { TextCell } from "$components/table/cellComponents";
  import Table from "$lib/components/table/Table.svelte";
  import GardenerSelect from "../sensorStationInfo/GardenerSelect.svelte";
  import ConnectedDisconnectedBadge from "../sensorStationInfo/ConnectedDisconnectedBadge.svelte";
  import DownloadQrCode from "../sensorStationInfo/DownloadQrCode.svelte";
  import LockUnlockButton from "../sensorStationInfo/LockUnlockButton.svelte";
  import SensorStationUpdateButton from "../sensorStationInfo/SensorStationUpdateButton.svelte";
  import SensorStationSettingsButton from "../sensorStationInfo/SensorStationSettingsButton.svelte";
  import SensorStationDeleteButton from "../sensorStationInfo/SensorStationDeleteButton.svelte";
  import SensorStationNameGardenerComposition from "./SensorStationNameGardenerComposition.svelte";

  export let sensorStations: SensorStation[];
  export let gardener: any;

  let columns: ColumnDef<SensorStation>[] = [
    {
      id: "station",
      accessorKey: "_",
      header: () => flexRender(TextCell, { text: "Sensor Station" }),
      cell: ({ row }) =>
        flexRender(SensorStationNameGardenerComposition, {
          gardener: gardener,
          sensorStation: row.original,
        }),
    },
    {
      id: "room",
      accessorKey: "roomName",
      header: () => flexRender(TextCell, { text: "Room" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "mac",
      accessorKey: "bdAddress",
      header: () => flexRender(TextCell, { text: "MAC" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "dip",
      accessorKey: "dipSwitchId",
      header: () => flexRender(TextCell, { text: "DIP" }),
      cell: (info) => flexRender(TextCell, { text: info.getValue() }),
    },
    {
      id: "connected",
      accessorKey: "_",
      header: () => flexRender(TextCell, { text: "Connection Status" }),
      cell: ({ row }) =>
        flexRender(ConnectedDisconnectedBadge, { sensorStation: row.original }),
    },
    {
      id: "qr",
      accessorKey: "_",
      header: () => flexRender(TextCell, { text: "QR" }),
      cell: ({ row }) =>
        flexRender(DownloadQrCode, { sensorStation: row.original }),
    },
    {
      id: "lock",
      accessorKey: "_",
      header: () => flexRender(TextCell, { text: "Lock Status" }),
      cell: ({ row }) =>
        flexRender(LockUnlockButton, { sensorStation: row.original }),
    },
    {
      id: "settings",
      accessorKey: "sensorStationId",
      header: () => flexRender(TextCell, { text: "Settings" }), // NOTE: empty string for iconClass so that the default is not used
      cell: (info) =>
        flexRender(SensorStationSettingsButton, {
          sensorStationId: info.getValue(),
          iconClass: "",
        }),
    },
    {
      id: "delete",
      accessorKey: "sensorStationId",
      header: () => flexRender(TextCell, { text: "Delete" }),
      cell: (info) =>
        flexRender(SensorStationDeleteButton, {
          sensorStationId: info.getValue(),
          iconClass: "",
        }),
    },
  ];

  let mobileColumnVisibility: ColumnVisibility = {
    room: false,
    mac: false,
    dip: false,
    qr: false,
    connected: false,
    lock: false,
    delete: false,
  };
</script>

<Table data={sensorStations} {columns} {mobileColumnVisibility} />
