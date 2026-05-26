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
import irrigationEventApi, { listAllGreenhouse } from '../../api/irrigation-event.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  startedAt: '',

  endedAt: '',

  waterLiters: '',

  method: '',

  notes: '',

  greenhouseId: '',

}

export default function IrrigationEventFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const editing = id && id !== 'new'
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)

  const [greenhouseOptions, setGreenhouseOptions] = useState([])


  useEffect(() => {

    listAllGreenhouse().then((data) => setGreenhouseOptions(data))

    if (!editing) {
      return
    }
    irrigationEventApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('irrigationEvent.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await irrigationEventApi.update(id, form)
      } else {
        await irrigationEventApi.create(form)
      }
      navigate('/irrigation-event')
    } catch {
      setError(t('irrigationEvent.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('irrigationEvent.form.editTitle') : t('irrigationEvent.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="datetime-local"
              label={t('irrigationEvent.form.fields.startedAt')}
              value={form.startedAt ?? ''}
              onChange={(event) => updateField('startedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="datetime-local"
              label={t('irrigationEvent.form.fields.endedAt')}
              value={form.endedAt ?? ''}
              onChange={(event) => updateField('endedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="number"
              label={t('irrigationEvent.form.fields.waterLiters')}
              value={form.waterLiters ?? ''}
              onChange={(event) => updateField('waterLiters', event.target.value)}
              
            />



            <TextField
              select
              label={t('irrigationEvent.form.fields.method')}
              value={form.method ?? ''}
              onChange={(event) => updateField('method', event.target.value)}
            >

              <MenuItem value="DRIP">DRIP</MenuItem>

              <MenuItem value="SPRINKLER">SPRINKLER</MenuItem>

              <MenuItem value="MANUAL">MANUAL</MenuItem>

            </TextField>







            <TextField
              type="text"
              label={t('irrigationEvent.form.fields.notes')}
              value={form.notes ?? ''}
              onChange={(event) => updateField('notes', event.target.value)}
              
            />



            <FormControl fullWidth>
              <InputLabel>{t('irrigationEvent.field.greenhouse')}</InputLabel>
              <Select
                value={form.greenhouseId ?? ''}
                label={t('irrigationEvent.field.greenhouse')}
                onChange={(event) => updateField('greenhouseId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { greenhouseOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/irrigation-event')}>{t('irrigationEvent.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('irrigationEvent.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
