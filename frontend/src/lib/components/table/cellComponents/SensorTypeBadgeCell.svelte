<script lang="ts">
  export let type: string;

  // These colors should be in accordance to the colors specified in wiki/alarm-description/Alarm-Description-of-Sensor-Station.md

  const getTypeClass = (type: string) => {
    if (type === "LIGHTINTENSITY") {
      return "bg-yellow-500";
    } else if (type === "TEMPERATURE") {
      return "bg-red-600";
    } else if (type === "SOILHUMIDITY") {
      return "bg-green-600";
    } else if (type === "AIRQUALITY") {
      return "bg-purple-700";
    } else if (type === "HUMIDITY") {
      return "bg-gray-500"; // white is not really visible so gray was chosen
    } else if (type === "PRESSURE") {
      return "bg-cyan-500";
    } else if (type === "BATTERYLEVEL") {
      return "bg-orange-600";
    } else {
      return "bg-black";
    }
  };
</script>

<span
  class={`
      badge badge-lg border-none
      text-white text-sm
      ${getTypeClass(type)}
    `}
>
  {type.toUpperCase()}
</span>
