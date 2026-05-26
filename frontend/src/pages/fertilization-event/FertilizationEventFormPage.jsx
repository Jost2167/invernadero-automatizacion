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
import fertilizationEventApi, { listAllCropCycle } from '../../api/fertilization-event.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  appliedAt: '',

  fertilizerName: '',

  dose: '',

  unit: '',

  notes: '',

  cropCycleId: '',

}

export default function FertilizationEventFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const editing = id && id !== 'new'
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)

  const [cropCycleOptions, setCropCycleOptions] = useState([])


  useEffect(() => {

    listAllCropCycle().then((data) => setCropCycleOptions(data))

    if (!editing) {
      return
    }
    fertilizationEventApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('fertilizationEvent.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await fertilizationEventApi.update(id, form)
      } else {
        await fertilizationEventApi.create(form)
      }
      navigate('/fertilization-event')
    } catch {
      setError(t('fertilizationEvent.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('fertilizationEvent.form.editTitle') : t('fertilizationEvent.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="datetime-local"
              label={t('fertilizationEvent.form.fields.appliedAt')}
              value={form.appliedAt ?? ''}
              onChange={(event) => updateField('appliedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="text"
              label={t('fertilizationEvent.form.fields.fertilizerName')}
              value={form.fertilizerName ?? ''}
              onChange={(event) => updateField('fertilizerName', event.target.value)}
              
            />





            <TextField
              type="number"
              label={t('fertilizationEvent.form.fields.dose')}
              value={form.dose ?? ''}
              onChange={(event) => updateField('dose', event.target.value)}
              
            />



            <TextField
              select
              label={t('fertilizationEvent.form.fields.unit')}
              value={form.unit ?? ''}
              onChange={(event) => updateField('unit', event.target.value)}
            >

              <MenuItem value="GRAMS">GRAMS</MenuItem>

              <MenuItem value="KILOGRAMS">KILOGRAMS</MenuItem>

              <MenuItem value="MILLILITERS">MILLILITERS</MenuItem>

              <MenuItem value="LITERS">LITERS</MenuItem>

            </TextField>







            <TextField
              type="text"
              label={t('fertilizationEvent.form.fields.notes')}
              value={form.notes ?? ''}
              onChange={(event) => updateField('notes', event.target.value)}
              
            />



            <FormControl fullWidth>
              <InputLabel>{t('fertilizationEvent.field.cropCycle')}</InputLabel>
              <Select
                value={form.cropCycleId ?? ''}
                label={t('fertilizationEvent.field.cropCycle')}
                onChange={(event) => updateField('cropCycleId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { cropCycleOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/fertilization-event')}>{t('fertilizationEvent.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('fertilizationEvent.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
