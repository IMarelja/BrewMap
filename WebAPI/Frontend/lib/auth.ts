import api from './api'
import { AuthResponse, User } from './types'

export async function login(user: string, password: string, rememberMe: boolean = false): Promise<AuthResponse> {
  const res = await api.patch('/api/Auth/login', { user, password, rememberMe })
  
  // 1. Get the tokens from the response
  const token = res.data.token || res.data.accessToken || res.data.jwt
  const refreshToken = res.data.refreshToken // Added this line
  
  // 2. Save everything to localStorage (this keeps it saved after exit)
  localStorage.setItem('token', token)
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken) // Added this line
  }

  if (res.data.user) {
    localStorage.setItem('user', JSON.stringify(res.data.user))
  }
  
  return res.data
}

export async function register(email: string, username: string, password: string): Promise<void> {
  await api.post('/api/Auth/register', { email, username, password })
}

export function logout(): void {
  // 3. Clear everything on logout
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken') // Added this line
  localStorage.removeItem('user')
  window.location.href = '/login'
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null
  return localStorage.getItem('token')
}

// Added this helper function for your API interceptor later
export function getRefreshToken(): string | null {
  if (typeof window === 'undefined') return null
  return localStorage.getItem('refreshToken')
}

export function getUser(): User | null {
  if (typeof window === 'undefined') return null
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
}

export function isLoggedIn(): boolean {
  return !!getToken()
}