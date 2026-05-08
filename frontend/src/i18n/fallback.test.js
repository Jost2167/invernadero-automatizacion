import { describe, it, expect, beforeEach } from 'vitest'
import i18next from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import es from './es.json'
import en from './en.json'
import { LOCALE_KEY } from '../auth/constants.js'

function freshInstance() {
  const instance = i18next.createInstance()
  return instance.use(LanguageDetector).init({
    resources: {
      es: { translation: es },
      en: { translation: en },
    },
    fallbackLng: 'es',
    supportedLngs: ['es', 'en'],
    nonExplicitSupportedLngs: true,
    load: 'languageOnly',
    interpolation: { escapeValue: false },
    detection: {
      order: ['localStorage', 'navigator'],
      lookupLocalStorage: LOCALE_KEY,
      caches: ['localStorage'],
    },
  }).then(() => instance)
}

function setNavigatorLanguage(lang) {
  Object.defineProperty(window.navigator, 'language', { value: lang, configurable: true })
  Object.defineProperty(window.navigator, 'languages', { value: [lang], configurable: true })
}

describe('i18n language detection', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('falls back to es when navigator language is unsupported (fr)', async () => {
    setNavigatorLanguage('fr-FR')
    const instance = await freshInstance()
    expect(instance.resolvedLanguage).toBe('es')
  })

  it('uses en when navigator language is en-US', async () => {
    setNavigatorLanguage('en-US')
    const instance = await freshInstance()
    expect(instance.resolvedLanguage).toBe('en')
  })

  it('uses es when navigator language is es-CO', async () => {
    setNavigatorLanguage('es-CO')
    const instance = await freshInstance()
    expect(instance.resolvedLanguage).toBe('es')
  })

  it('localStorage preference overrides navigator', async () => {
    setNavigatorLanguage('en-US')
    localStorage.setItem(LOCALE_KEY, 'es')
    const instance = await freshInstance()
    expect(instance.resolvedLanguage).toBe('es')
  })
})
