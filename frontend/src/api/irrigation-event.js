import api from './client.js'

const RESOURCE = '/api/irrigation-event'

export const irrigationEventApi = {
  list() {
    return api.get(RESOURCE).then((response) => response.data)
  },

  getById(id) {
    return api.get(`${RESOURCE}/${id}`).then((response) => response.data)
  },

  create(payload) {
    return api.post(RESOURCE, payload).then((response) => response.data)
  },

  update(id, payload) {
    return api.put(`${RESOURCE}/${id}`, payload).then((response) => response.data)
  },

  remove(id) {
    return api.delete(`${RESOURCE}/${id}`).then((response) => response.data)
  },
}

export default irrigationEventApi



export const listAllGreenhouse = () =>
  api.get('/api/greenhouse?size=1000').then((response) => response.data.content ?? response.data)



