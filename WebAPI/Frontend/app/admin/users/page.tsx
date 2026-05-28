'use client'

import { useState } from 'react'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'

interface ModeratedUser {
  id: string
  username: string
  email: string
  role: string
  isActive: boolean
  createdAt: string
}

export default function AdminUsersPage() {
  const [keyword, setKeyword] = useState('')
  const [users, setUsers] = useState<ModeratedUser[]>([])
  const [loading, setLoading] = useState(false)

  const [toast, setToast] = useState<{
    message: string
    type: 'success' | 'error'
  } | null>(null)

  const showToast = (
    message: string,
    type: 'success' | 'error'
  ) => {
    setToast({ message, type })

    setTimeout(() => {
      setToast(null)
    }, 3000)
  }

  const searchUsers = async () => {
    if (!keyword.trim()) return

    setLoading(true)

    try {
      const res = await api.get(
        `/api/Moderation/users/search?keyword=${keyword}`
      )

      setUsers(res.data)
    } catch (err) {
      console.error(err)
      showToast('Failed to search users', 'error')
    } finally {
      setLoading(false)
    }
  }

  const toggleAdmin = async (user: ModeratedUser) => {
    try {
      const newRole =
        user.role === 'admin' ? 'user' : 'admin'

      await api.patch(`/api/Moderation/user/${user.id}`, {
        role: newRole
      })

      setUsers(prev =>
        prev.map(u =>
          u.id === user.id
            ? { ...u, role: newRole }
            : u
        )
      )

      showToast(
        newRole === 'admin'
          ? 'User promoted to admin'
          : 'Admin revoked',
        'success'
      )
    } catch (err) {
      console.error(err)
      showToast('Failed to update role', 'error')
    }
  }

  const toggleSuspend = async (user: ModeratedUser) => {
    try {
      const suspended = user.isActive

      await api.patch(`/api/Moderation/user/${user.id}`, {
        suspended
      })

      setUsers(prev =>
        prev.map(u =>
          u.id === user.id
            ? { ...u, isActive: !suspended }
            : u
        )
      )

      showToast(
        suspended
          ? 'User suspended'
          : 'User unsuspended',
        'success'
      )
    } catch (err) {
      console.error(err)
      showToast('Failed to update user', 'error')
    }
  }

  return (
    <ProtectedRoute>
      <div
        style={{
          minHeight: '100vh',
          background: '#F5EFE6'
        }}
      >
        <Navbar />

        {toast && (
          <div
            style={{
              position: 'fixed',
              top: 20,
              right: 20,
              background:
                toast.type === 'success'
                  ? '#166534'
                  : '#991B1B',
              color: '#fff',
              padding: '10px 16px',
              borderRadius: '8px',
              zIndex: 9999
            }}
          >
            {toast.message}
          </div>
        )}

        <div
          style={{
            maxWidth: '900px',
            margin: '0 auto',
            padding: '2rem'
          }}
        >
          <h1
            style={{
              color: '#2C1A0E',
              marginBottom: '2rem'
            }}
          >
            User Moderation
          </h1>

          {/* SEARCH */}
          <div
            style={{
              display: 'flex',
              gap: '10px',
              marginBottom: '2rem'
            }}
          >
            <input
              value={keyword}
              onChange={e => setKeyword(e.target.value)}
              placeholder="Search by username or email..."
              style={{
                flex: 1,
                padding: '12px',
                borderRadius: '10px',
                border: '1px solid #D6C2A8',
                background: '#fff',
                fontSize: '14px'
              }}
            />

            <button
              onClick={searchUsers}
              disabled={loading}
              style={{
                background: '#2C1A0E',
                color: '#fff',
                border: 'none',
                padding: '12px 20px',
                borderRadius: '10px',
                cursor: 'pointer'
              }}
            >
              {loading ? 'Searching...' : 'Search'}
            </button>
          </div>

          {/* USERS */}
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '1rem'
            }}
          >
            {users.map(user => (
              <div
                key={user.id}
                style={{
                  background: '#fff',
                  border: '1px solid #E8D5B7',
                  borderRadius: '14px',
                  padding: '1.5rem'
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    gap: '1rem',
                    flexWrap: 'wrap'
                  }}
                >
                  <div>
                    <h2
                      style={{
                        margin: 0,
                        color: '#2C1A0E'
                      }}
                    >
                      {user.username}
                    </h2>

                    <p
                      style={{
                        margin: '6px 0',
                        color: '#6B3F1F'
                      }}
                    >
                      {user.email}
                    </p>

                    <div
                      style={{
                        display: 'flex',
                        gap: '10px',
                        marginTop: '10px'
                      }}
                    >
                      <span
                        style={{
                          background:
                            user.role === 'admin'
                              ? '#D97706'
                              : '#E8D5B7',
                          color:
                            user.role === 'admin'
                              ? '#fff'
                              : '#6B3F1F',
                          padding: '4px 10px',
                          borderRadius: '999px',
                          fontSize: '12px'
                        }}
                      >
                        {user.role}
                      </span>

                      <span
                        style={{
                          background: user.isActive
                            ? '#DCFCE7'
                            : '#FEE2E2',
                          color: user.isActive
                            ? '#166534'
                            : '#991B1B',
                          padding: '4px 10px',
                          borderRadius: '999px',
                          fontSize: '12px'
                        }}
                      >
                        {user.isActive
                          ? 'Active'
                          : 'Suspended'}
                      </span>
                    </div>
                  </div>

                  <div
                    style={{
                      display: 'flex',
                      gap: '10px',
                      alignItems: 'center'
                    }}
                  >
                    <button
                      onClick={() =>
                        toggleAdmin(user)
                      }
                      style={{
                        background:
                          user.role === 'admin'
                            ? '#6B7280'
                            : '#D97706',
                        color: '#fff',
                        border: 'none',
                        padding: '10px 14px',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        fontSize: '13px'
                      }}
                    >
                      {user.role === 'admin'
                        ? 'Remove Admin'
                        : 'Promote Admin'}
                    </button>

                    <button
                      onClick={() =>
                        toggleSuspend(user)
                      }
                      style={{
                        background: user.isActive
                          ? '#991B1B'
                          : '#166534',
                        color: '#fff',
                        border: 'none',
                        padding: '10px 14px',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        fontSize: '13px'
                      }}
                    >
                      {user.isActive
                        ? 'Suspend'
                        : 'Unsuspend'}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}