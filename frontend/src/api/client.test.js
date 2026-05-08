import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

vi.mock('../i18n/index.js', () => ({
  default: { resolvedLanguage: 'en', language: 'en' },
}))

import api from './client.js'
import i18n from '../i18n/index.js'
import { TOKEN_KEY } from '../auth/constants.js'

function getRequestInterceptor() {
  return api.interceptors.request.handlers.find((h) => h && h.fulfilled).fulfilled
}

describe('axios client request interceptor', () => {
  beforeEach(() => {
    localStorage.clear()
    i18n.resolvedLanguage = 'en'
    i18n.language = 'en'
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('adds Accept-Language header from i18n', () => {
    const interceptor = getRequestInterceptor()
    const config = interceptor({ headers: {} })
    expect(config.headers['Accept-Language']).toBe('en')
  })

  it('updates Accept-Language when i18n language changes (hot)', () => {
    const interceptor = getRequestInterceptor()

    i18n.resolvedLanguage = 'es'
    const first = interceptor({ headers: {} })
    expect(first.headers['Accept-Language']).toBe('es')

    i18n.resolvedLanguage = 'en'
    const second = interceptor({ headers: {} })
    expect(second.headers['Accept-Language']).toBe('en')
  })

  it('adds Bearer token when present in localStorage', () => {
    localStorage.setItem(TOKEN_KEY, 'abc.def.ghi')
    const interceptor = getRequestInterceptor()
    const config = interceptor({ headers: {} })
    expect(config.headers.Authorization).toBe('Bearer abc.def.ghi')
  })

  it('does not add Authorization when no token', () => {
    const interceptor = getRequestInterceptor()
    const config = interceptor({ headers: {} })
    expect(config.headers.Authorization).toBeUndefined()
  })
})
