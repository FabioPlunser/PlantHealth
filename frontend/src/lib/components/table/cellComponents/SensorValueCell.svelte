<script lang="ts">

  export let value: number;
  export let unit: string;
</script>

<span>{value.toPrecision(3)} {unit}</span>
