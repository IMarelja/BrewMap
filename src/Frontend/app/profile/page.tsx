'use client'
import { useEffect, useState } from 'react'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { getUser, logout } from '@/lib/auth'
import { Review } from '@/lib/types'

// Simple SVG Eye Icon 
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

  const [reviews, setReviews] = useState<Review[]>([])
  const [loadingReviews, setLoadingReviews] = useState(true)
  const [deletingAccount, setDeletingAccount] = useState(false)


  const [exporting, setExporting] = useState(false)


  useEffect(() => {
    const fetchReviews = async () => {
      try {
        const res = await api.get('/api/Review/mine')
        setReviews(res.data)
      } catch (err) {
        console.error(err)
      } finally {
        setLoadingReviews(false)
      }
    }

    fetchReviews()
  }, [])

const handleExportData = async () => {
  try {
    setExporting(true)
    setMessage('')
    setError('')

    const res = await api.get('/api/User/me/export')

    const csvContent =
      typeof res.data === 'string'
        ? res.data
        : JSON.stringify(res.data, null, 2)

    const blob = new Blob([csvContent], {
      type: 'text/csv;charset=utf-8;'
    })

    const url = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = 'brewmap-user-data.csv'

    document.body.appendChild(link)
    link.click()

    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    setMessage('Data exported successfully.')
  } catch (err: any) {
    console.error(err)

    setError(
      err.response?.data?.message ||
      'Failed to export data.'
    )
  } finally {
    setExporting(false)
  }
}

  const handleDeleteAccount = async () => {
    const confirmed = window.confirm(
      'Are you sure you want to permanently delete your account? This action cannot be undone.'
    )

    if (!confirmed) return

    try {
      setDeletingAccount(true)
      setMessage('')
      setError('')

      await api.delete('/api/User/me')

      logout()
    } catch (err: any) {
      console.error(err)

      setError(
        err.response?.data?.message ||
        'Failed to delete account.'
      )
    } finally {
      setDeletingAccount(false)
    }
  }

  const deleteReview = async (reviewId: string) => {
  const confirmed = window.confirm(
    'Delete this review?'
  )

  if (!confirmed) return

  try {
    await api.delete(`/api/Review/${reviewId}`)

    setReviews(prev =>
      prev.filter(r => r.id !== reviewId)
    )

    setMessage('Review deleted successfully.')
  } catch (err: any) {
    console.error(err)

    setError(
      err.response?.data?.message ||
        'Failed to delete review.'
    )
  }
}

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

          {/* Delete reviews Section */}
          <div
            style={{
              background: '#fff',
              border: '1px solid #E8D5B7',
              borderRadius: '16px',
              padding: '2rem',
              marginTop: '1.5rem'
            }}
          >
            <h2
              style={{
                fontSize: '16px',
                fontWeight: 600,
                color: '#2C1A0E',
                marginBottom: '1.5rem'
              }}
            >
              My Reviews
            </h2>

            {loadingReviews ? (
              <p style={{ color: '#6B3F1F' }}>
                Loading reviews...
              </p>
            ) : reviews.length === 0 ? (
              <p style={{ color: '#6B3F1F' }}>
                You haven't written any reviews yet.
              </p>
            ) : (
              <div
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '1rem'
                }}
              >
                {reviews.map(review => (
                  <div
                    key={review.id}
                    style={{
                      border: '1px solid #E8D5B7',
                      borderRadius: '12px',
                      padding: '1rem',
                      background: '#FDFAF7'
                    }}
                  >
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginBottom: '10px'
                      }}
                    >
                      <span
                        style={{
                          color: '#2C1A0E',
                          fontWeight: 600
                        }}
                      >
                        {'★'.repeat(review.rating)}
                      </span>

                      <button
                        onClick={() =>
                          deleteReview(review.id)
                        }
                        style={{
                          background: '#991B1B',
                          color: '#fff',
                          border: 'none',
                          padding: '6px 12px',
                          borderRadius: '8px',
                          fontSize: '12px',
                          cursor: 'pointer'
                        }}
                      >
                        Delete
                      </button>
                    </div>

                    <p
                      style={{
                        margin: 0,
                        color: '#2C1A0E',
                        lineHeight: 1.6
                      }}
                    >
                      {review.comment}
                    </p>

                    <p
                      style={{
                        marginTop: '10px',
                        fontSize: '12px',
                        color: '#6B3F1F'
                      }}
                    >
                      {new Date(
                        review.createdAt
                      ).toLocaleDateString()}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>

      <div
        style={{
          background: '#fff',
          border: '1px solid #E8D5B7',
          borderRadius: '16px',
          padding: '2rem',
          marginTop: '1.5rem'
        }}
      >
        <h2
          style={{
            fontSize: '16px',
            fontWeight: 600,
            color: '#2C1A0E',
            marginBottom: '1rem'
          }}
        >
          Data Export
        </h2>

        <p
          style={{
            color: '#6B3F1F',
            fontSize: '14px',
            marginBottom: '1rem'
          }}
        >
          Download a copy of all data associated with your account.
        </p>

        <button
          onClick={handleExportData}
          disabled={exporting}
          style={{
            background: '#2C1A0E',
            color: '#fff',
            border: 'none',
            padding: '10px 24px',
            borderRadius: '8px',
            cursor: exporting ? 'not-allowed' : 'pointer'
          }}
        >
          {exporting
            ? 'Preparing Export...'
            : 'Download My Data'}
        </button>
      </div>

          <div
            style={{
              background: '#fff',
              border: '1px solid #FECACA',
              borderRadius: '16px',
              padding: '2rem',
              marginTop: '1.5rem'
            }}
          >
            <h2
              style={{
                fontSize: '16px',
                fontWeight: 600,
                color: '#991B1B',
                marginBottom: '1rem'
              }}
            >
              Danger Zone
            </h2>

            <p
              style={{
                color: '#7F1D1D',
                fontSize: '14px',
                lineHeight: 1.6,
                marginBottom: '1.5rem'
              }}
            >
              Permanently delete your BrewMap account and all associated data.
              This action cannot be undone.
            </p>

            <button
              onClick={handleDeleteAccount}
              disabled={deletingAccount}
              style={{
                background: '#991B1B',
                color: '#fff',
                border: 'none',
                padding: '10px 24px',
                borderRadius: '8px',
                fontSize: '14px',
                fontWeight: 500,
                cursor: deletingAccount ? 'not-allowed' : 'pointer',
                opacity: deletingAccount ? 0.7 : 1
              }}
            >
              {deletingAccount
                ? 'Deleting Account...'
                : 'Delete My Account'}
            </button>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}