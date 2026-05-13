'use client'
import { useState } from 'react'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { getUser } from '@/lib/auth'

// Simple SVG Eye Icon Component to keep code clean
const EyeIcon = ({ visible }: { visible: boolean }) => (
  visible ? (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
      <circle cx="12" cy="12" r="3"></circle>
    </svg>
  ) : (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
      <line x1="1" y1="1" x2="23" y2="23"></line>
    </svg>
  )
);

export default function ProfilePage() {
  const user = getUser()
  const [email, setEmail] = useState(user?.email || '')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmedNewPassword] = useState('')
  
  // Visibility States
  const [showCurrent, setShowCurrent] = useState(false)
  const [showNew, setShowNew] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)

  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true); setMessage(''); setError('')
    try {
      await api.put('/api/User/email', { newEmail: email, currentPassword })
      setMessage('Profile updated successfully.')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update profile.')
    } finally { setSaving(false) }
  }

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true); setMessage(''); setError('')
    try {
      await api.put('/api/User/password', { currentPassword, newPassword, confirmNewPassword })
      setMessage('Password changed successfully.')
      setCurrentPassword(''); setNewPassword(''); setConfirmedNewPassword('')
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

          {/* Account Details Section */}
          <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '16px', padding: '2rem', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '16px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1.5rem' }}>Account Details</h2>
            <form onSubmit={handleSave}>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>New Email</label>
                <input type="email" value={email} onChange={e => setEmail(e.target.value)}
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }} />
              </div>

              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>Current Password</label>
                <div style={{ position: 'relative' }}>
                  <input type={showCurrent ? "text" : "password"} value={currentPassword} onChange={e => setCurrentPassword(e.target.value)}
                    style={{ width: '100%', padding: '10px 45px 10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }} />
                  <button type="button" onClick={() => setShowCurrent(!showCurrent)} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#6B3F1F' }}>
                    <EyeIcon visible={showCurrent} />
                  </button>
                </div>
              </div>

              <button type="submit" disabled={saving} style={{ background: '#2C1A0E', color: '#F5EFE6', border: 'none', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: 500, cursor: saving ? 'not-allowed' : 'pointer' }}>
                {saving ? 'Saving...' : 'Save Changes'}
              </button>
            </form>
          </div>

          {/* Change Password Section */}
          <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '16px', padding: '2rem' }}>
            <h2 style={{ fontSize: '16px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1.5rem' }}>Change Password</h2>
            <form onSubmit={handlePasswordChange}>
              {/* Field 1: Current */}
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>Current Password</label>
                <div style={{ position: 'relative' }}>
                  <input type={showCurrent ? "text" : "password"} value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} required
                    style={{ width: '100%', padding: '10px 45px 10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }} />
                  <button type="button" onClick={() => setShowCurrent(!showCurrent)} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#6B3F1F' }}>
                    <EyeIcon visible={showCurrent} />
                  </button>
                </div>
              </div>

              {/* Field 2: New */}
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>New Password</label>
                <div style={{ position: 'relative' }}>
                  <input type={showNew ? "text" : "password"} value={newPassword} onChange={e => setNewPassword(e.target.value)} required
                    style={{ width: '100%', padding: '10px 45px 10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }} />
                  <button type="button" onClick={() => setShowNew(!showNew)} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#6B3F1F' }}>
                    <EyeIcon visible={showNew} />
                  </button>
                </div>
              </div>

              {/* Field 3: Confirm */}
              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: '#2C1A0E', marginBottom: '6px' }}>Confirmed New Password</label>
                <div style={{ position: 'relative' }}>
                  <input type={showConfirm ? "text" : "password"} value={confirmNewPassword} onChange={e => setConfirmedNewPassword(e.target.value)} required
                    style={{ width: '100%', padding: '10px 45px 10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '15px', background: '#FDFAF7', outline: 'none', boxSizing: 'border-box' }} />
                  <button type="button" onClick={() => setShowConfirm(!showConfirm)} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#6B3F1F' }}>
                    <EyeIcon visible={showConfirm} />
                  </button>
                </div>
              </div>

              <button type="submit" disabled={saving} style={{ background: '#2C1A0E', color: '#F5EFE6', border: 'none', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: 500, cursor: saving ? 'not-allowed' : 'pointer' }}>
                {saving ? 'Saving...' : 'Change Password'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}