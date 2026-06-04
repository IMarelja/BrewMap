'use client'
import { useState } from 'react'
import Link from 'next/link'
import Image from 'next/image'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5000'}/api/Auth/send-reset-email`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      })
      if (res.ok) {
        setSuccess(true)
      } else {
        const data = await res.json()
        setError(data.message || 'Something went wrong. Please try again.')
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
          <h1 style={{ marginTop: '1.5rem', fontSize: '1.75rem', fontWeight: 600, color: '#2C1A0E' }}>Forgot your password?</h1>
          <p style={{ color: '#6B3F1F', marginTop: '0.5rem' }}>Enter your email and we'll send you a reset link</p>
        </div>

        <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '16px', padding: '2rem' }}>
          {success ? (
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '48px', marginBottom: '1rem' }}>☕</div>
              <h2 style={{ color: '#2C1A0E', fontWeight: 600, marginBottom: '0.5rem' }}>Check your inbox!</h2>
              <p style={{ color: '#6B3F1F', fontSize: '14px' }}>
                If an account exists for <strong>{email}</strong>, a reset link has been sent.
              </p>
              <Link href="/login" style={{
                display: 'block', marginTop: '1.5rem', padding: '12px',
                background: '#2C1A0E', color: '#F5EFE6', borderRadius: '8px',
                textDecoration: 'none', fontSize: '15px', fontWeight: 500, textAlign: 'center'
              }}>
                Back to Sign In
              </Link>
            </div>
          ) : (
            <>
              {error && (
                <div style={{ background: '#FEE2E2', border: '1px solid #FECACA', color: '#991B1B', padding: '12px', borderRadius: '8px', marginBottom: '1rem', fontSize: '14px' }}>
                  {error}
                </div>
              )}
              <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '1.5rem' }}>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>Email address</label>
                  <input
                    type="email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                    placeholder="you@example.com"
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
                  {loading ? 'Sending...' : 'Send Reset Link'}
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