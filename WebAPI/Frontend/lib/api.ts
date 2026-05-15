import axios from 'axios'
import Cookies from 'js-cookie'

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5000',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config: any) => {
  if (typeof window !== 'undefined') {
    const token = Cookies.get('token')  // ← was localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

api.interceptors.response.use(
  (response: any) => response,
  (error: any) => {
    if (error.response?.status === 401) {
      Cookies.remove('token')           // ← was localStorage.removeItem('token')
      Cookies.remove('refreshToken')    // clean up refresh token too
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api