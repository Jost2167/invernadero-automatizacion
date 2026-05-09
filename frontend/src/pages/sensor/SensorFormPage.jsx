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
import sensorApi, { listAllLocation } from '../../api/sensor.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  name: '',

  type: '',

  lastReadingAt: '',

  batteryLevel: '',

  active: '',

  locationId: '',

}

export default function SensorFormPage() {
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
    sensorApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('sensor.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await sensorApi.update(id, form)
      } else {
        await sensorApi.create(form)
      }
      navigate('/sensor')
    } catch {
      setError(t('sensor.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('sensor.form.editTitle') : t('sensor.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="text"
              label={t('sensor.form.fields.name')}
              value={form.name ?? ''}
              onChange={(event) => updateField('name', event.target.value)}
              
            />



            <TextField
              select
              label={t('sensor.form.fields.type')}
              value={form.type ?? ''}
              onChange={(event) => updateField('type', event.target.value)}
            >

              <MenuItem value="TEMPERATURE">TEMPERATURE</MenuItem>

              <MenuItem value="HUMIDITY">HUMIDITY</MenuItem>

              <MenuItem value="LIGHT">LIGHT</MenuItem>

              <MenuItem value="SOIL_MOISTURE">SOIL_MOISTURE</MenuItem>

            </TextField>







            <TextField
              type="datetime-local"
              label={t('sensor.form.fields.lastReadingAt')}
              value={form.lastReadingAt ?? ''}
              onChange={(event) => updateField('lastReadingAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="number"
              label={t('sensor.form.fields.batteryLevel')}
              value={form.batteryLevel ?? ''}
              onChange={(event) => updateField('batteryLevel', event.target.value)}
              
            />




            <FormControlLabel
              control={
                <Checkbox
                  checked={Boolean(form.active)}
                  onChange={(event) => updateField('active', event.target.checked)}
                />
              }
              label={t('sensor.form.fields.active')}
            />




            <FormControl fullWidth>
              <InputLabel>{t('sensor.field.location')}</InputLabel>
              <Select
                value={form.locationId ?? ''}
                label={t('sensor.field.location')}
                onChange={(event) => updateField('locationId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { locationOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/sensor')}>{t('sensor.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('sensor.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
