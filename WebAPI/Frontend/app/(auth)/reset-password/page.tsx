'use client'
import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import Image from 'next/image'

export default function ResetPasswordPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const token = searchParams.get('token')

  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (password !== confirm) { setError('Passwords do not match.'); return }
    setLoading(true)
    setError('')
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5000'}/api/Auth/change-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword: password })
      })
      if (res.ok) {
        setSuccess(true)
        setTimeout(() => router.push('/login'), 3000)
      } else {
        const data = await res.json()
        setError(data.message || 'Invalid or expired link. Please try again.')
      }
    } catch (err) {
      setError('Network error. Please try again.')
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
          <h1 style={{ marginTop: '1.5rem', fontSize: '1.75rem', fontWeight: 600, color: '#2C1A0E' }}>Reset your password</h1>
          <p style={{ color: '#6B3F1F', marginTop: '0.5rem' }}>Enter your new password below</p>
        </div>

        <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '16px', padding: '2rem' }}>
          {success ? (
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '48px', marginBottom: '1rem' }}>☕</div>
              <h2 style={{ color: '#2C1A0E', fontWeight: 600, marginBottom: '0.5rem' }}>Password reset!</h2>
              <p style={{ color: '#6B3F1F', fontSize: '14px' }}>Redirecting you to sign in...</p>
            </div>
          ) : (
            <>
              {error && (
                <div style={{ background: '#FEE2E2', border: '1px solid #FECACA', color: '#991B1B', padding: '12px', borderRadius: '8px', marginBottom: '1rem', fontSize: '14px' }}>
                  {error}
                </div>
              )}
              <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>New Password</label>
                  <input
                    type="password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                    placeholder="••••••••"
                    style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }}
                  />
                </div>
                <div style={{ marginBottom: '1.5rem' }}>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>Confirm Password</label>
                  <input
                    type="password"
                    value={confirm}
                    onChange={e => setConfirm(e.target.value)}
                    required
                    placeholder="••••••••"
                    style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }}
                  />
                </div>
                <button type="submit" disabled={loading} style={{
                  width: '100%', padding: '12px',
                  background: loading ? '#9CA3AF' : '#2C1A0E',
                  color: '#F5EFE6', border: 'none', borderRadius: '8px',
                  fontSize: '16px', fontWeight: 500,
                  cursor: loading ? 'not-allowed' : 'pointer'
                }}>
                  {loading ? 'Resetting...' : 'Reset Password'}
                </button>
              </form>
            </>
          )}
        </div>

        <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '14px', color: '#6B3F1F' }}>
          Remember your password?{' '}
          <Link href="/login" style={{ color: '#2C1A0E', fontWeight: 600, textDecoration: 'none' }}>Sign in</Link>
        </p>
      </div>
    </main>
  )
}