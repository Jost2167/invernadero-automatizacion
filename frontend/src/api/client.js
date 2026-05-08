import axios from 'axios'
import i18n from '../i18n/index.js'
import { API_BASE_URL, TOKEN_KEY } from '../auth/constants.js'

const api = axios.create({
  baseURL: API_BASE_URL,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  const language = i18n.resolvedLanguage || i18n.language
  if (language) {
    config.headers['Accept-Language'] = language
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
    }
    return Promise.reject(error)
  }
)

export default api
