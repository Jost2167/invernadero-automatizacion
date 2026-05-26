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
import pestInspectionApi, { listAllCropCycle } from '../../api/pest-inspection.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  inspectedAt: '',

  pestType: '',

  severity: '',

  affectedAreaSquareMeters: '',

  treatmentApplied: '',

  cropCycleId: '',

}

export default function PestInspectionFormPage() {
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
    pestInspectionApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('pestInspection.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await pestInspectionApi.update(id, form)
      } else {
        await pestInspectionApi.create(form)
      }
      navigate('/pest-inspection')
    } catch {
      setError(t('pestInspection.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('pestInspection.form.editTitle') : t('pestInspection.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="datetime-local"
              label={t('pestInspection.form.fields.inspectedAt')}
              value={form.inspectedAt ?? ''}
              onChange={(event) => updateField('inspectedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="text"
              label={t('pestInspection.form.fields.pestType')}
              value={form.pestType ?? ''}
              onChange={(event) => updateField('pestType', event.target.value)}
              
            />



            <TextField
              select
              label={t('pestInspection.form.fields.severity')}
              value={form.severity ?? ''}
              onChange={(event) => updateField('severity', event.target.value)}
            >

              <MenuItem value="LOW">LOW</MenuItem>

              <MenuItem value="MEDIUM">MEDIUM</MenuItem>

              <MenuItem value="HIGH">HIGH</MenuItem>

              <MenuItem value="CRITICAL">CRITICAL</MenuItem>

            </TextField>







            <TextField
              type="number"
              label={t('pestInspection.form.fields.affectedAreaSquareMeters')}
              value={form.affectedAreaSquareMeters ?? ''}
              onChange={(event) => updateField('affectedAreaSquareMeters', event.target.value)}
              
            />




            <FormControlLabel
              control={
                <Checkbox
                  checked={Boolean(form.treatmentApplied)}
                  onChange={(event) => updateField('treatmentApplied', event.target.checked)}
                />
              }
              label={t('pestInspection.form.fields.treatmentApplied')}
            />




            <FormControl fullWidth>
              <InputLabel>{t('pestInspection.field.cropCycle')}</InputLabel>
              <Select
                value={form.cropCycleId ?? ''}
                label={t('pestInspection.field.cropCycle')}
                onChange={(event) => updateField('cropCycleId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { cropCycleOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/pest-inspection')}>{t('pestInspection.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('pestInspection.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
