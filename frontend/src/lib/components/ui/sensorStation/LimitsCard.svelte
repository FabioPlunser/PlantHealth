<script lang="ts">
  import Input from "$components/ui/Input.svelte";
  import { enhance } from "$app/forms";
  import FormError from "$components/ui/FormError.svelte";
  export let sensorStationId: string;
  export let limit: any;
  export let form: any;

  const customEnhance = () => {
    return async ({ update }) => {
      await update({ reset: false });
    };
  };
</script>

<div class="flex justify-center">
  <div
    class="card border h-fit dark:bg-gray-700 bg-base-100 dark:border-none shadow-2xl w-60"
  >
    <div class="card-body">
      <div class="flex text-center">
        <h1 class="mx-auto font-semibold">
          {limit.sensor.type}
          <br />
          <span class="ml-2 justify-center">[{limit.sensor.unit}]</span>
        </h1>
      </div>
      <form method="post" use:enhance={customEnhance}>
        <input type="hidden" name="sensorStationId" value={sensorStationId} />
        <input type="hidden" name="sensor" value={limit.sensor.type} />
        <Input
          field="upperLimit"
          type="number"
          label="UpperLimit: "
          value={limit.upperLimit}
        />
        <FormError field="upperLimit" {form} id={limit.sensor.type} />
        <Input
          field="lowerLimit"
          type="number"
          label="LowerLimit: "
          value={limit.lowerLimit}
        />
        <FormError field="lowerLimit" {form} id={limit.sensor.type} />
        <Input
          field="thresholdDuration"
          type="number"
          label="Threshold [s]:"
          value={limit.thresholdDuration}
        />
        <FormError field="thresholdDuration" {form} id={limit.sensor.type} />
        <div class="card-actions mx-auto justify-center mt-4">
          <button class="btn btn-primary" formaction="?/updateLimit"
            >Set Limit</button
          >
        </div>
      </form>
    </div>
  </div>
</div>
