import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Container,

  FormControlLabel,

  MenuItem,
  Paper,

  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useTranslation } from 'react-i18next'
import locationApi from '../../api/location.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  name: '',

  description: '',

  active: '',

}

export default function LocationFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const editing = id && id !== 'new'
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)


  useEffect(() => {

    if (!editing) {
      return
    }
    locationApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('location.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await locationApi.update(id, form)
      } else {
        await locationApi.create(form)
      }
      navigate('/location')
    } catch {
      setError(t('location.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('location.form.editTitle') : t('location.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="text"
              label={t('location.form.fields.name')}
              value={form.name ?? ''}
              onChange={(event) => updateField('name', event.target.value)}
              
            />





            <TextField
              type="text"
              label={t('location.form.fields.description')}
              value={form.description ?? ''}
              onChange={(event) => updateField('description', event.target.value)}
              
            />




            <FormControlLabel
              control={
                <Checkbox
                  checked={Boolean(form.active)}
                  onChange={(event) => updateField('active', event.target.checked)}
                />
              }
              label={t('location.form.fields.active')}
            />




            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/location')}>{t('location.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('location.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
