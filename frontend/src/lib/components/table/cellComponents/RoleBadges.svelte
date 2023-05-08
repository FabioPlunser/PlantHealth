<script lang="ts">
  export let roles: string[];
  let rolesToLower = roles.map((role) => role.toUpperCase());

  const getRoleClass = (role: string) => {
    if (role === "ADMIN") {
      return "bg-orange-500";
    } else if (role === "GARDENER") {
      return "bg-green-500";
    } else if (role === "USER") {
      return "bg-cyan-500";
    } else {
      return "bg-gray-500";
    }
  };
</script>

<div class="flex gap-2">
  {#each rolesToLower as role}
    <span
      class={`
      badge badge-lg border-none
      text-white text-sm
      ${getRoleClass(role)}
    `}
    >
      {role}
    </span>
  {/each}
</div>
