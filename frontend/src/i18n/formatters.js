import i18n from './index.js'
import { DEFAULT_LOCALE } from './index.js'

function activeLocale() {
  return i18n.resolvedLanguage || i18n.language || DEFAULT_LOCALE
}

export function formatDate(date, options = {}) {
  if (date == null) return ''
  const value = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(value.getTime())) return ''
  return new Intl.DateTimeFormat(activeLocale(), options).format(value)
}

export function formatNumber(value, options = {}) {
  if (value == null || Number.isNaN(value)) return ''
  return new Intl.NumberFormat(activeLocale(), options).format(value)
}

export function formatCurrency(amount, currency = 'USD', options = {}) {
  if (amount == null || Number.isNaN(amount)) return ''
  return new Intl.NumberFormat(activeLocale(), {
    style: 'currency',
    currency,
    ...options,
  }).format(amount)
}
