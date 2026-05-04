'use client'
import { useState, useEffect } from 'react'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { getUser } from '@/lib/auth'

export default function ProfilePage() {
  const user = getUser()
  const [username, setUsername] = useState(user?.username || '')
  const [email, setEmail] = useState(user?.email || '')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setMessage('')
    setError('')
    try {
      await api.patch('/api/User/me', { username, email })
      setMessage('Profile updated successfully.')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update profile.')
    } finally { setSaving(false) }
  }

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setMessage('')
    setError('')
    try {
      await api.post('/api/User/change-password', { currentPassword, newPassword })
      setMessage('Password changed successfully.')
      setCurrentPassword('')
      setNewPassword('')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to change password.')
    } finally { setSaving(false) }
  }

  return (
    <ProtectedRoute>
      <div style={{ minHeight: '100vh', background: '#F5EFE6' }}>
        <Navbar />
        <div style={{ maxWidth: '600px', margin: '0 auto', padding: '2rem' }}>
          <h1 style={{ fontFamily: 'Playfair Display, serif', fontSize: '2rem', color: '#2C1A0E', marginBottom: '2rem' }}>My Profile</h1>

          {message && <div style={{ background: '#D1FAE5', border: '1px solid #6EE7B7', color: '#065F46', padding: '12px', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '14px' }}>{message}</div>}
          {error && <div style={{ background: '#FEE2E2', border: '1px solid #FECACA', color: '#991B1B', padding: '12px', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '14px' }}>{error}</div>}

          <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '16px', padding: '2rem', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '16px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1.5rem' }}>Account Details</h2>
            <form onSubmit={handleSave}>
              {[
                { label: 'Username', value: username, setter: setUsername, type: 'text' },
                { label: 'Email', value: email, setter: setEmail, type: 'email' },
              ].map(f => (
                <div key={f.label} style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>{f.label}</label>
                  <input type={f.type} value={f.value} onChange={e => f.setter(e.target.value)}
                    style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }} />
                </div>
              ))}
              <button type="submit" disabled={saving} style={{
                background: '#2C1A0E', color: '#F5EFE6', border: 'none', padding: '10px 24px',
                borderRadius: '8px', fontSize: '14px', fontWeight: 500, cursor: saving ? 'not-allowed' : 'pointer'
              }}>{saving ? 'Saving...' : 'Save Changes'}</button>
            </form>
          </div>

          <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '16px', padding: '2rem' }}>
            <h2 style={{ fontSize: '16px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1.5rem' }}>Change Password</h2>
            <form onSubmit={handlePasswordChange}>
              {[
                { label: 'Current Password', value: currentPassword, setter: setCurrentPassword },
                { label: 'New Password', value: newPassword, setter: setNewPassword },
              ].map(f => (
                <div key={f.label} style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>{f.label}</label>
                  <input type="password" value={f.value} onChange={e => f.setter(e.target.value)} required
                    style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }} />
                </div>
              ))}
              <button type="submit" disabled={saving} style={{
                background: '#2C1A0E', color: '#F5EFE6', border: 'none', padding: '10px 24px',
                borderRadius: '8px', fontSize: '14px', fontWeight: 500, cursor: saving ? 'not-allowed' : 'pointer'
              }}>{saving ? 'Saving...' : 'Change Password'}</button>
            </form>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}