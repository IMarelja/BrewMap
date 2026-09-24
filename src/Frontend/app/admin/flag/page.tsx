'use client'

import { useEffect, useState } from 'react'
import Navbar from '@/components/ui/navbar'
import AdminRoute from '@/components/auth/admin-route'
import api from '@/lib/api'
import { Flag } from '@/lib/types'
import { getUserFromToken } from '@/lib/auth'

export default function AdminPage() {
  const [flags, setFlags] = useState<Flag[]>([])
  const [loading, setLoading] = useState(true)


  const [selectedContent, setSelectedContent] = useState<any>(null)
  const [showModal, setShowModal] = useState(false)
  const [contentLoading, setContentLoading] = useState(false)

  const user = getUserFromToken()
  const [contentUser, setContentUser] = useState<any>(null)
  const [showModalFlagType, setShowModalFlagType] = useState<string | null>(null)
  useEffect(() => {
    loadFlags()
  }, [])

  const loadFlags = async () => {
    try {
      setLoading(true)

      const res = await api.get('/api/flag')

      setFlags(res.data)
    } catch (err) {
      console.error('Failed to load flags:', err)
    } finally {
      setLoading(false)
    }
  }

  const viewContent = async (flag: Flag) => {
    try {
      setContentLoading(true)

      let response

      switch (flag.target.type) {
        case 'review':
          response = await api.get(
            `/api/review/${flag.target.id}`
          )
          break

        case 'location':
          response = await api.get(
            `/api/locations/${flag.target.id}`
          )
          break

        case 'product':
          response = await api.get(
            `/api/drink/${flag.target.id}`
          )
          break

        case 'user':
          response = await api.get(
            `/api/Moderation/user/${flag.target.id}`
          )
          break

        default:
          return
      }
    const content = response.data
      setSelectedContent(response.data)
      setShowModalFlagType(flag.target.type)
    if (content?.userId) {
      try {
        const userRes = await api.get(
          `/api/user/${content.userId}`
        )

        setContentUser(userRes.data)
      } catch (err) {
        console.error('Failed to load user', err)
      }
    }

      setShowModal(true)
    } catch (err) {
      console.error(err)
      alert('Failed to load content')
    } finally {
      setContentLoading(false)
    }
  }

  const updateStatus = async (
    id: string,
    status: 'reviewed' | 'resolved'
  ) => {
    try {
      await api.put('/api/flag/status', {
        id,
        status,
        resolvedByAdminId: user?.nameid || user?.sub
      })

      await loadFlags()
    } catch (err) {
      console.error('Failed to update flag status:', err)
    }
  }

  const suspendUser = async () => {
    if (!selectedContent) return

    try {
      setContentLoading(true)

      await api.patch(`/api/Moderation/user/${selectedContent.id}`, {
        suspended: true
      })

      alert('User suspended successfully')
      setShowModal(false)
      await loadFlags()
    } catch (err) {
      console.error(err)
      alert('Failed to suspend user')
    } finally {
      setContentLoading(false)
    }
  }

  const getTargetLabel = (type: string) => {
    switch (type) {
      case 'review':
        return 'Review'

      case 'location':
        return 'Cafe'

      case 'product':
        return 'Beverage'

      case 'user':
        return 'User'

      default:
        return type
    }
  }

  const deleteContent = async () => {
    if (!selectedContent || !showModalFlagType) return

    try {
      setContentLoading(true)

      switch (showModalFlagType) {
        case 'review':
          await api.delete(`/api/review/${selectedContent.id}`)
          break

        case 'location':
          await api.delete(`/api/locations/${selectedContent.id}`)
          break

        case 'product':
          await api.delete(`/api/drink/${selectedContent.id}`)
          break

        default:
          return
      }

      alert('Content deleted successfully')
      setShowModal(false)
      await loadFlags()
    } catch (err) {
      console.error(err)
      alert('Failed to delete content')
    } finally {
      setContentLoading(false)
    }
  }

  return (
    <AdminRoute>
      <div className="min-h-screen bg-[#F5EFE6]">
        <Navbar />

        <div className="max-w-7xl mx-auto p-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-4xl font-bold text-[#2C1A0E]">
              Content Moderation Panel
            </h1>

            <p className="text-[#6B3F1F] mt-2">
              View and manage all flagged reviews, cafes and beverages.
            </p>
          </div>

          {/* Stats */}
          {!loading && (
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
              <div className="bg-white p-4 rounded-xl border border-[#E8D5B7]">
                <div className="text-sm text-[#6B3F1F]">
                  Total Reports
                </div>

                <div className="text-2xl font-bold text-[#2C1A0E]">
                  {flags.length}
                </div>
              </div>

              <div className="bg-white p-4 rounded-xl border border-[#E8D5B7]">
                <div className="text-sm text-[#6B3F1F]">
                  Pending
                </div>

                <div className="text-2xl font-bold text-yellow-600">
                  {
                    flags.filter(
                      (f) => f.status === 'pending'
                    ).length
                  }
                </div>
              </div>

              <div className="bg-white p-4 rounded-xl border border-[#E8D5B7]">
                <div className="text-sm text-[#6B3F1F]">
                  Reviewed
                </div>

                <div className="text-2xl font-bold text-blue-600">
                  {
                    flags.filter(
                      (f) => f.status === 'reviewed'
                    ).length
                  }
                </div>
              </div>

              <div className="bg-white p-4 rounded-xl border border-[#E8D5B7]">
                <div className="text-sm text-[#6B3F1F]">
                  Resolved
                </div>

                <div className="text-2xl font-bold text-green-600">
                  {
                    flags.filter(
                      (f) => f.status === 'resolved'
                    ).length
                  }
                </div>
              </div>
            </div>
          )}

          {loading ? (
            <div className="text-[#6B3F1F]">
              Loading reports...
            </div>
          ) : flags.length === 0 ? (
            <div className="bg-white border border-[#E8D5B7] rounded-xl p-8 text-center text-[#6B3F1F]">
              No flagged content found.
            </div>
          ) : (
            <div className="bg-white border border-[#E8D5B7] rounded-2xl overflow-hidden shadow-sm">
              <table className="w-full text-sm">
                <thead className="bg-[#F5EFE6]">
                  <tr>
                    <th className="text-left p-4 text-[#2C1A0E]">
                      Content Type
                    </th>

                    <th className="text-left p-4 text-[#2C1A0E]">
                      Content ID
                    </th>

                    <th className="text-left p-4 text-[#2C1A0E]">
                      Reason
                    </th>

                    <th className="text-left p-4 text-[#2C1A0E]">
                      Description
                    </th>

                    <th className="text-left p-4 text-[#2C1A0E]">
                      Status
                    </th>

                    <th className="text-left p-4 text-[#2C1A0E]">
                      Reported
                    </th>

                    <th className="text-left p-4 text-[#2C1A0E]">
                      Actions
                    </th>
                  </tr>
                </thead>

                <tbody>
                  {flags.map((flag) => (
                    <tr
                      key={flag.id}
                      className="border-t border-[#E8D5B7] hover:bg-[#F9F4ED]"
                    >
                      {/* Content Type */}
                      <td className="p-4">
                        <span className="font-medium text-[#2C1A0E]">
                          {getTargetLabel(flag.target.type)}
                        </span>
                      </td>

                      {/* Content ID */}
                      <td className="p-4">
                        <button
                          onClick={() => viewContent(flag)}
                          className="text-blue-600 hover:text-blue-800 underline"
                        >
                          View Content
                        </button>
                      </td>

                      {/* Reason */}
                      <td className="p-4 text-[#2C1A0E]">
                        {flag.reason}
                      </td>

                      {/* Description */}
                      <td className="p-4 text-[#6B3F1F] max-w-sm">
                        {flag.description || '-'}
                      </td>

                      {/* Status */}
                      <td className="p-4">
                        <span
                          className={`px-3 py-1 rounded-full text-xs font-medium ${
                            flag.status === 'pending'
                              ? 'bg-yellow-100 text-yellow-800'
                              : flag.status === 'reviewed'
                              ? 'bg-blue-100 text-blue-800'
                              : 'bg-green-100 text-green-800'
                          }`}
                        >
                          {flag.status}
                        </span>
                      </td>

                      {/* Date */}
                      <td className="p-4 text-[#6B3F1F]">
                        {new Date(
                          flag.createdAt
                        ).toLocaleDateString()}
                      </td>

                      {/* Actions */}
                      <td className="p-4">
                        <div className="flex gap-2">
                          {flag.status === 'pending' && (
                            <button
                              onClick={() =>
                                updateStatus(
                                  flag.id,
                                  'reviewed'
                                )
                              }
                              className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded-lg text-xs"
                            >
                              Reviewed
                            </button>
                          )}

                          {flag.status !== 'resolved' && (
                            <button
                              onClick={() =>
                                updateStatus(
                                  flag.id,
                                  'resolved'
                                )
                              }
                              className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded-lg text-xs"
                            >
                              Resolve
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        
      </div>
      {showModal && (
        <div
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
          onClick={() => setShowModal(false)}
        >
          <div
            className="bg-white rounded-2xl p-6 w-[700px] max-w-[95vw] max-h-[80vh] overflow-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold text-[#2C1A0E]">
                Reported Content
              </h2>

              <button
                onClick={() => setShowModal(false)}
                className="text-gray-500 hover:text-black"
              >
                ✕
              </button>
            </div>

            {contentLoading ? (
              <p>Loading...</p>
            ) : (
              <>
                <pre className="mt-6 bg-gray-100 p-4 rounded-lg text-xs overflow-auto">
                  {JSON.stringify(
                    selectedContent,
                    null,
                    2
                  )}
                </pre>

                <div className="mt-6 flex justify-end border-t pt-4">
                  {showModalFlagType === 'user' ? (
                    <button
                      onClick={suspendUser}
                      className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg disabled:opacity-50"
                      disabled={contentLoading || selectedContent?.isActive === false}
                    >
                      {contentLoading
                        ? 'Suspending...'
                        : selectedContent?.isActive === false
                        ? 'Already Suspended'
                        : 'Suspend'}
                    </button>
                  ) : (
                    <button
                      onClick={deleteContent}
                      className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg"
                      disabled={contentLoading}
                    >
                      {contentLoading ? 'Deleting...' : 'Delete Content'}
                    </button>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </AdminRoute>
  )
  
}
