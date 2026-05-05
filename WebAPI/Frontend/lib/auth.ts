import api from './api'
import { AuthResponse, User } from './types'

export async function login(user: string, password: string, rememberMe: boolean = false): Promise<AuthResponse> {
  const res = await api.patch('/api/Auth/login', { user, password, rememberMe })
  const token = res.data.token || res.data.accessToken || res.data.jwt
  localStorage.setItem('token', token)
  if (res.data.user) {
    localStorage.setItem('user', JSON.stringify(res.data.user))
  }
  return res.data
}

export async function register(email: string, username: string, password: string): Promise<void> {
  await api.post('/api/Auth/register', { email, username, password })
}

export function logout(): void {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  window.location.href = '/login'
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null
  return localStorage.getItem('token')
}

export function getUser(): User | null {
  if (typeof window === 'undefined') return null
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
}

export function isLoggedIn(): boolean {
  return !!getToken()
}