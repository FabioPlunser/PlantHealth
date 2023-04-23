import type { UserConfig } from "vite";
import { sveltekit } from "@sveltejs/kit/vite";
import svg from "@poppanator/sveltekit-svg";

const config: UserConfig = {
  plugins: [sveltekit(), svg()],
  // ssr:{
  //   noExternal:['chart.js'],
  // },
};

export default config;
