import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },

  output: "export",
  trailingSlash: true,
};

export default nextConfig;
