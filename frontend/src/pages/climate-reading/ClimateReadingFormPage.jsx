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
import climateReadingApi, { listAllSensor, listAllGreenhouse } from '../../api/climate-reading.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  recordedAt: '',

  temperatureCelsius: '',

  humidityPercent: '',

  co2Ppm: '',

  lightLux: '',

  sensorId: '',

  greenhouseId: '',

}

export default function ClimateReadingFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const editing = id && id !== 'new'
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)

  const [sensorOptions, setSensorOptions] = useState([])

  const [greenhouseOptions, setGreenhouseOptions] = useState([])


  useEffect(() => {

    listAllSensor().then((data) => setSensorOptions(data))

    listAllGreenhouse().then((data) => setGreenhouseOptions(data))

    if (!editing) {
      return
    }
    climateReadingApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('climateReading.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await climateReadingApi.update(id, form)
      } else {
        await climateReadingApi.create(form)
      }
      navigate('/climate-reading')
    } catch {
      setError(t('climateReading.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('climateReading.form.editTitle') : t('climateReading.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="datetime-local"
              label={t('climateReading.form.fields.recordedAt')}
              value={form.recordedAt ?? ''}
              onChange={(event) => updateField('recordedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="number"
              label={t('climateReading.form.fields.temperatureCelsius')}
              value={form.temperatureCelsius ?? ''}
              onChange={(event) => updateField('temperatureCelsius', event.target.value)}
              
            />





            <TextField
              type="number"
              label={t('climateReading.form.fields.humidityPercent')}
              value={form.humidityPercent ?? ''}
              onChange={(event) => updateField('humidityPercent', event.target.value)}
              
            />





            <TextField
              type="number"
              label={t('climateReading.form.fields.co2Ppm')}
              value={form.co2Ppm ?? ''}
              onChange={(event) => updateField('co2Ppm', event.target.value)}
              
            />





            <TextField
              type="number"
              label={t('climateReading.form.fields.lightLux')}
              value={form.lightLux ?? ''}
              onChange={(event) => updateField('lightLux', event.target.value)}
              
            />



            <FormControl fullWidth>
              <InputLabel>{t('climateReading.field.sensor')}</InputLabel>
              <Select
                value={form.sensorId ?? ''}
                label={t('climateReading.field.sensor')}
                onChange={(event) => updateField('sensorId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { sensorOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>{t('climateReading.field.greenhouse')}</InputLabel>
              <Select
                value={form.greenhouseId ?? ''}
                label={t('climateReading.field.greenhouse')}
                onChange={(event) => updateField('greenhouseId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { greenhouseOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/climate-reading')}>{t('climateReading.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('climateReading.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
