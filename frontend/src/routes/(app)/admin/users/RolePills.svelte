<script lang="ts">
  export let roles: string[];
  let rolesToLower = roles.map((role) => role.toLowerCase());

  const getColorClass = (role: string) => {
    if (role === "admin") {
      return "bg-orange-500";
    } else if (role === "gardener") {
      return "bg-green-500";
    } else if (role === "user") {
      return "bg-blue-500";
    } else {
      return "bg-gray-500";
    }
  };
</script>

{#each rolesToLower as role}
  <span
    class={`
    inline-flex items-center px-2 py-1 rounded-full 
    text-white text-sm
    ${getColorClass(role)}
  `}
  >
    {role}
  </span>
{/each}
