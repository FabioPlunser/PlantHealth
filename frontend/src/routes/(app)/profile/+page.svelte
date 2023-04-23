<script lang="ts">
  import Input from "$components/ui/Input.svelte";
</script>

<h1>Profile</h1>

<section class="w-full">
  <div class="flex justify-center">
    <div class="w-full max-w-md">
      <Input field="username" label="Username" />
      <Input field="email" label="Email" />

      <div class="grid grid-rows gap-4">
        {#each {length: 4} as _, i}
        <div class="btn rounded-xl shadow-2xl ">
          <h1 class="">Hure {i}</h1>
          <input type="checkbox" class="float-right checkbox"/>
        </div>
        {/each}
      </div>
    </div>
  </div>
</section>
