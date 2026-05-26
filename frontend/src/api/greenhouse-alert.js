import api from './client.js'

const RESOURCE = '/api/greenhouse-alert'

export const greenhouseAlertApi = {
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

export default greenhouseAlertApi



export const listAllGreenhouse = () =>
  api.get('/api/greenhouse?size=1000').then((response) => response.data.content ?? response.data)


export const listAllSensor = () =>
  api.get('/api/sensor?size=1000').then((response) => response.data.content ?? response.data)



