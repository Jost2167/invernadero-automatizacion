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
import cropCycleApi, { listAllGreenhouse } from '../../api/crop-cycle.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  cropName: '',

  variety: '',

  startedAt: '',

  expectedHarvestAt: '',

  status: '',

  greenhouseId: '',

}

export default function CropCycleFormPage() {
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
    cropCycleApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('cropCycle.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await cropCycleApi.update(id, form)
      } else {
        await cropCycleApi.create(form)
      }
      navigate('/crop-cycle')
    } catch {
      setError(t('cropCycle.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('cropCycle.form.editTitle') : t('cropCycle.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="text"
              label={t('cropCycle.form.fields.cropName')}
              value={form.cropName ?? ''}
              onChange={(event) => updateField('cropName', event.target.value)}
              
            />





            <TextField
              type="text"
              label={t('cropCycle.form.fields.variety')}
              value={form.variety ?? ''}
              onChange={(event) => updateField('variety', event.target.value)}
              
            />





            <TextField
              type="date"
              label={t('cropCycle.form.fields.startedAt')}
              value={form.startedAt ?? ''}
              onChange={(event) => updateField('startedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="date"
              label={t('cropCycle.form.fields.expectedHarvestAt')}
              value={form.expectedHarvestAt ?? ''}
              onChange={(event) => updateField('expectedHarvestAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />



            <TextField
              select
              label={t('cropCycle.form.fields.status')}
              value={form.status ?? ''}
              onChange={(event) => updateField('status', event.target.value)}
            >

              <MenuItem value="PLANNED">PLANNED</MenuItem>

              <MenuItem value="GROWING">GROWING</MenuItem>

              <MenuItem value="HARVESTED">HARVESTED</MenuItem>

              <MenuItem value="CANCELLED">CANCELLED</MenuItem>

            </TextField>





            <FormControl fullWidth>
              <InputLabel>{t('cropCycle.field.greenhouse')}</InputLabel>
              <Select
                value={form.greenhouseId ?? ''}
                label={t('cropCycle.field.greenhouse')}
                onChange={(event) => updateField('greenhouseId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { greenhouseOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/crop-cycle')}>{t('cropCycle.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('cropCycle.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
