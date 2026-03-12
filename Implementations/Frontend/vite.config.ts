/// <reference types="vitest" />
import { defineConfig } from 'vite'
import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [
    // The React and Tailwind plugins are both required for Make, even if
    // Tailwind is not being actively used – do not remove them
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      // Alias @ to the src directory
      '@': path.resolve(__dirname, './src'),
    },
  },

  // File types to support raw imports. Never add .css, .tsx, or .ts files to this.
  assetsInclude: ['**/*.svg', '**/*.csv'],

  // Development server configuration
  server: {
    port: 5173,
    proxy: {
      // Proxy API requests to backend during development
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true, // WebSocket support
      },
    },
  },

  // Test configuration (vitest)
  test: {
    environment: 'jsdom',
    globals: true,
    testTimeout: 15000,
    setupFiles: ['./src/test-setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['lcov', 'text'],
      reportsDirectory: './coverage',
      exclude: [
        '**/*.test.ts',
        '**/*.test.tsx',
        '**/test-setup.ts',
        'node_modules/**',
      ],
    },
  },

  // Preview server configuration (for production builds)
  preview: {
    port: 4173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
