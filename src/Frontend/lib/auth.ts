import api from './api'
import Cookies from 'js-cookie'
import { jwtDecode } from "jwt-decode"
import { AuthResponse, User } from './types'

const TOKEN_KEY = 'token'
const REFRESH_KEY = 'refreshToken'

// Helper: set cookie options
const getCookieOpts = (rememberMe: boolean) =>
  rememberMe ? { expires: 30 } : undefined // 30 days or session-only

export async function login(
  user: string,
  password: string,
  rememberMe: boolean = false
): Promise<AuthResponse> {
  const res = await api.patch('/api/Auth/login', { user, password, rememberMe })
  // Get the tokens from the response
  const token = res.data.token || res.data.accessToken || res.data.jwt
  const refreshToken = res.data.refreshToken

  // Save tokens to cookies (not localStorage)
  if (token) Cookies.set(TOKEN_KEY, token, getCookieOpts(rememberMe))
  if (refreshToken) Cookies.set(REFRESH_KEY, refreshToken, getCookieOpts(rememberMe))

  // User data still in localStorage 
  if (res.data.user) {
    localStorage.setItem('user', JSON.stringify(res.data.user))
  }
  return res.data
}

export async function register(email: string, username: string, password: string): Promise<void> {
  await api.post('/api/Auth/register', { email, username, password })
}

export function logout(): void {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_KEY)
  localStorage.removeItem('user')
  window.location.href = '/login'
}

export function getToken(): string | null {
  return Cookies.get(TOKEN_KEY) || null
}

export function getRefreshToken(): string | null {
  return Cookies.get(REFRESH_KEY) || null
}

export function getUser(): User | null {
  if (typeof window === 'undefined') return null
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
}

export function isLoggedIn(): boolean {
  return !!Cookies.get(TOKEN_KEY)
}

export function getUserFromToken() {
  const token = Cookies.get("token");
  if (!token) return null;

  try {
    return jwtDecode<any>(token);
  } catch {
    return null;
  }
}
export function isAdmin(): boolean {
  const decoded = getUserFromToken()
  return decoded?.role === 'admin'
}