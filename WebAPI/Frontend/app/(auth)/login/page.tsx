'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import Image from 'next/image'
import { login } from '@/lib/auth'

export default function LoginPage() {
  const router = useRouter()
  const [user, setUser] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await login(user, password, rememberMe)
      router.push('/explore')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid credentials. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main style={{ minHeight: '100vh', background: '#F5EFE6', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '2rem' }}>
      <div style={{ width: '100%', maxWidth: '420px' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <Link href="/" style={{ textDecoration: 'none' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
              <Image src="/logo.png" alt="BrewMap" width={36} height={36} />
              <span style={{ fontFamily: 'Playfair Display, serif', fontSize: '24px', fontWeight: 700, color: '#2C1A0E' }}>BrewMap</span>
            </div>
          </Link>
          <h1 style={{ marginTop: '1.5rem', fontSize: '1.75rem', fontWeight: 600, color: '#2C1A0E' }}>Welcome back</h1>
          <p style={{ color: '#6B3F1F', marginTop: '0.5rem' }}>Sign in to your account</p>
        </div>

        <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '16px', padding: '2rem' }}>
          {error && (
            <div style={{ background: '#FEE2E2', border: '1px solid #FECACA', color: '#991B1B', padding: '12px', borderRadius: '8px', marginBottom: '1rem', fontSize: '14px' }}>
              {error}
            </div>
          )}
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>Username or Email</label>
              <input
                type="text"
                value={user}
                onChange={e => setUser(e.target.value)}
                required
                placeholder="your username or email"
                style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>Password</label>
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
                placeholder="••••••••"
                style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input
                type="checkbox"
                id="rememberMe"
                checked={rememberMe}
                onChange={e => setRememberMe(e.target.checked)}
                style={{ width: '16px', height: '16px', cursor: 'pointer' }}
              />
              <label htmlFor="rememberMe" style={{ fontSize: '14px', color: '#6B3F1F', cursor: 'pointer' }}>Remember me</label>
            </div>
            <button type="submit" disabled={loading} style={{
              width: '100%', padding: '12px', background: loading ? '#9CA3AF' : '#2C1A0E',
              color: '#F5EFE6', border: 'none', borderRadius: '8px', fontSize: '16px',
              fontWeight: 500, cursor: loading ? 'not-allowed' : 'pointer'
            }}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        </div>
        <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '14px', color: '#6B3F1F' }}>
          Don't have an account?{' '}
          <Link href="/register" style={{ color: '#2C1A0E', fontWeight: 600, textDecoration: 'none' }}>Sign up</Link>
        </p>
      </div>
    </main>
  )
}