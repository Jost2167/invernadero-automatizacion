import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import LanguageSelector from './LanguageSelector.jsx'
import i18n from '../i18n/index.js'
import { LOCALE_KEY } from '../auth/constants.js'

describe('LanguageSelector', () => {
  beforeEach(async () => {
    localStorage.clear()
    await i18n.changeLanguage('es')
  })

  it('opens menu and lists supported languages', async () => {
    const user = userEvent.setup()
    render(<LanguageSelector />)

    await user.click(screen.getByRole('button', { name: /idioma/i }))

    expect(screen.getByRole('menuitem', { name: /español/i })).toBeInTheDocument()
    expect(screen.getByRole('menuitem', { name: /inglés/i })).toBeInTheDocument()
  })

  it('switches language and persists to localStorage', async () => {
    const user = userEvent.setup()
    render(<LanguageSelector />)

    await user.click(screen.getByRole('button', { name: /idioma/i }))
    await user.click(screen.getByRole('menuitem', { name: /inglés/i }))

    expect(i18n.resolvedLanguage).toBe('en')
    expect(localStorage.getItem(LOCALE_KEY)).toBe('en')
  })

  it('updates UI text reactively after language change', async () => {
    const user = userEvent.setup()
    render(<LanguageSelector />)

    expect(screen.getByRole('button', { name: /idioma/i })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /idioma/i }))
    await user.click(screen.getByRole('menuitem', { name: /inglés/i }))

    expect(await screen.findByRole('button', { name: /language/i })).toBeInTheDocument()
  })
})
