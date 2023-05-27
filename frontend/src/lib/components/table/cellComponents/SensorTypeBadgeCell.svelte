<script lang="ts">
  export let type: string;

  // These colors should be in accordance to the colors specified in wiki/alarm-description/Alarm-Description-of-Sensor-Station.md

  const getTypeClass = (type: string) => {
    let typeUpperCase = type.toUpperCase();
    if (typeUpperCase === "LIGHT INTENSITY") {
      return "bg-yellow-500";
    } else if (typeUpperCase === "TEMPERATURE") {
      return "bg-red-600";
    } else if (typeUpperCase === "EARTH HUMIDITY") {
      return "bg-green-600";
    } else if (typeUpperCase === "AIR QUALITY") {
      return "bg-purple-700";
    } else if (typeUpperCase === "AIR HUMIDITY") {
      return "bg-gray-500"; // white is not really visible so gray was chosen
    } else if (typeUpperCase === "AIR PRESSURE") {
      return "bg-cyan-500";
    } else if (typeUpperCase === "BATTERY LEVEL") {
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
