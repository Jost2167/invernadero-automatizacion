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
import greenhouseAlertApi, { listAllGreenhouse, listAllSensor } from '../../api/greenhouse-alert.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  title: '',

  severity: '',

  message: '',

  detectedAt: '',

  resolved: '',

  greenhouseId: '',

  sensorId: '',

}

export default function GreenhouseAlertFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const editing = id && id !== 'new'
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)

  const [greenhouseOptions, setGreenhouseOptions] = useState([])

  const [sensorOptions, setSensorOptions] = useState([])


  useEffect(() => {

    listAllGreenhouse().then((data) => setGreenhouseOptions(data))

    listAllSensor().then((data) => setSensorOptions(data))

    if (!editing) {
      return
    }
    greenhouseAlertApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('greenhouseAlert.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await greenhouseAlertApi.update(id, form)
      } else {
        await greenhouseAlertApi.create(form)
      }
      navigate('/greenhouse-alert')
    } catch {
      setError(t('greenhouseAlert.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('greenhouseAlert.form.editTitle') : t('greenhouseAlert.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="text"
              label={t('greenhouseAlert.form.fields.title')}
              value={form.title ?? ''}
              onChange={(event) => updateField('title', event.target.value)}
              
            />



            <TextField
              select
              label={t('greenhouseAlert.form.fields.severity')}
              value={form.severity ?? ''}
              onChange={(event) => updateField('severity', event.target.value)}
            >

              <MenuItem value="INFO">INFO</MenuItem>

              <MenuItem value="WARNING">WARNING</MenuItem>

              <MenuItem value="CRITICAL">CRITICAL</MenuItem>

            </TextField>







            <TextField
              type="text"
              label={t('greenhouseAlert.form.fields.message')}
              value={form.message ?? ''}
              onChange={(event) => updateField('message', event.target.value)}
              
            />





            <TextField
              type="datetime-local"
              label={t('greenhouseAlert.form.fields.detectedAt')}
              value={form.detectedAt ?? ''}
              onChange={(event) => updateField('detectedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />




            <FormControlLabel
              control={
                <Checkbox
                  checked={Boolean(form.resolved)}
                  onChange={(event) => updateField('resolved', event.target.checked)}
                />
              }
              label={t('greenhouseAlert.form.fields.resolved')}
            />




            <FormControl fullWidth>
              <InputLabel>{t('greenhouseAlert.field.greenhouse')}</InputLabel>
              <Select
                value={form.greenhouseId ?? ''}
                label={t('greenhouseAlert.field.greenhouse')}
                onChange={(event) => updateField('greenhouseId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { greenhouseOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>{t('greenhouseAlert.field.sensor')}</InputLabel>
              <Select
                value={form.sensorId ?? ''}
                label={t('greenhouseAlert.field.sensor')}
                onChange={(event) => updateField('sensorId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { sensorOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/greenhouse-alert')}>{t('greenhouseAlert.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('greenhouseAlert.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
