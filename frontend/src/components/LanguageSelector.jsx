import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  IconButton,
  ListItemText,
  Menu,
  MenuItem,
  Tooltip,
} from '@mui/material'
import TranslateIcon from '@mui/icons-material/Translate'
import { SUPPORTED_LOCALES } from '../i18n/index.js'

export default function LanguageSelector({ color = 'inherit' }) {
  const { i18n, t } = useTranslation()
  const [anchorEl, setAnchorEl] = useState(null)

  const open = Boolean(anchorEl)
  const current = i18n.resolvedLanguage || i18n.language

  const handleOpen = (event) => setAnchorEl(event.currentTarget)
  const handleClose = () => setAnchorEl(null)

  const handleSelect = (lang) => {
    i18n.changeLanguage(lang)
    handleClose()
  }

  return (
    <>
      <Tooltip title={t('language.label')}>
        <IconButton color={color} onClick={handleOpen} aria-label={t('language.label')}>
          <TranslateIcon />
        </IconButton>
      </Tooltip>
      <Menu anchorEl={anchorEl} open={open} onClose={handleClose}>
        {SUPPORTED_LOCALES.map((lang) => (
          <MenuItem
            key={lang}
            selected={lang === current}
            onClick={() => handleSelect(lang)}
          >
            <ListItemText>{t(`language.${lang}`)}</ListItemText>
          </MenuItem>
        ))}
      </Menu>
    </>
  )
}
