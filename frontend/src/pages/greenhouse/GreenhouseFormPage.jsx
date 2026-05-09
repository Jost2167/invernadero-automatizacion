import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Container,

  FormControl,

  FormControlLabel,

  InputLabel,

  MenuItem,
  Paper,

  Select,

  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useTranslation } from 'react-i18next'
import greenhouseApi, { listAllLocation } from '../../api/greenhouse.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  code: '',

  name: '',

  areaSquareMeters: '',

  status: '',

  active: '',

  locationId: '',

}

export default function GreenhouseFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const editing = id && id !== 'new'
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)

  const [locationOptions, setLocationOptions] = useState([])


  useEffect(() => {

    listAllLocation().then((data) => setLocationOptions(data))

    if (!editing) {
      return
    }
    greenhouseApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('greenhouse.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await greenhouseApi.update(id, form)
      } else {
        await greenhouseApi.create(form)
      }
      navigate('/greenhouse')
    } catch {
      setError(t('greenhouse.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('greenhouse.form.editTitle') : t('greenhouse.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="text"
              label={t('greenhouse.form.fields.code')}
              value={form.code ?? ''}
              onChange={(event) => updateField('code', event.target.value)}
              
            />





            <TextField
              type="text"
              label={t('greenhouse.form.fields.name')}
              value={form.name ?? ''}
              onChange={(event) => updateField('name', event.target.value)}
              
            />





            <TextField
              type="number"
              label={t('greenhouse.form.fields.areaSquareMeters')}
              value={form.areaSquareMeters ?? ''}
              onChange={(event) => updateField('areaSquareMeters', event.target.value)}
              
            />



            <TextField
              select
              label={t('greenhouse.form.fields.status')}
              value={form.status ?? ''}
              onChange={(event) => updateField('status', event.target.value)}
            >

              <MenuItem value="ACTIVE">ACTIVE</MenuItem>

              <MenuItem value="INACTIVE">INACTIVE</MenuItem>

              <MenuItem value="MAINTENANCE">MAINTENANCE</MenuItem>

            </TextField>






            <FormControlLabel
              control={
                <Checkbox
                  checked={Boolean(form.active)}
                  onChange={(event) => updateField('active', event.target.checked)}
                />
              }
              label={t('greenhouse.form.fields.active')}
            />




            <FormControl fullWidth>
              <InputLabel>{t('greenhouse.field.location')}</InputLabel>
              <Select
                value={form.locationId ?? ''}
                label={t('greenhouse.field.location')}
                onChange={(event) => updateField('locationId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { locationOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/greenhouse')}>{t('greenhouse.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('greenhouse.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
