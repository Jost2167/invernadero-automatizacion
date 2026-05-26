import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import { readFileSync, readdirSync, existsSync } from 'fs'
import { fileURLToPath } from 'url'
import { dirname, join, resolve } from 'path'

const __dirname = dirname(fileURLToPath(import.meta.url))

function erSchemasPlugin() {
  const VIRTUAL_ID = 'virtual:er-schemas'
  const RESOLVED_ID = '\0' + VIRTUAL_ID
  const examplesDir = resolve(__dirname, '../tools/codegen/examples')

  return {
    name: 'er-schemas',
    resolveId(id) {
      if (id === VIRTUAL_ID) return RESOLVED_ID
    },
    load(id) {
      if (id === RESOLVED_ID) {
        if (!existsSync(examplesDir)) return `export default []`
        const files = readdirSync(examplesDir)
          .filter(f => f.endsWith('.json'))
          .sort()
        const schemas = files.map(f =>
          JSON.parse(readFileSync(join(examplesDir, f), 'utf-8'))
        )
        return `export default ${JSON.stringify(schemas)}`
      }
    },
  }
}

export default defineConfig({
  plugins: [react(), erSchemasPlugin()],
  server: {
    port: 5173,
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.js'],
  },
})
