import adapter from "@sveltejs/adapter-node";
import { vitePreprocess } from "@sveltejs/kit/vite";
import preprocess from "svelte-preprocess";

/** @type {import('@sveltejs/kit').Config} */
const config = {
  preprocess: [
    vitePreprocess(),
    preprocess({
      postcss: true,
    }),
  ],
  kit: {
    alias: {
      $lib: "src/lib",
      $components: "src/lib/components",
      $helper: "src/lib/helper",
      $stores: "src/lib/stores",
      $types: "src/lib/types",
      $assets: "src/lib/assets",
    },
    adapter: adapter(),
  },
};

export default config;
