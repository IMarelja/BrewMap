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

  const user = getUserFromToken()

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

  return (
    <AdminRoute>
      <div className="min-h-screen bg-[#F5EFE6]">
        <Navbar />

        <div className="max-w-7xl mx-auto p-8">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-4xl font-bold text-[#2C1A0E]">
                Admin Panel
              </h1>
              <p className="text-[#6B3F1F] mt-2">
                Moderate reported content and manage platform safety
              </p>
            </div>
          </div>

          {/* Content */}
          {loading ? (
            <div className="text-[#6B3F1F]">Loading reports...</div>
          ) : flags.length === 0 ? (
            <div className="text-[#6B3F1F]">
              No flagged content found.
            </div>
          ) : (
            <div className="bg-white border border-[#E8D5B7] rounded-2xl overflow-hidden shadow-sm">
              <table className="w-full text-sm">
                <thead className="bg-[#F5EFE6] text-[#2C1A0E]">
                  <tr>
                    <th className="text-left p-4">Target</th>
                    <th className="text-left p-4">Reason</th>
                    <th className="text-left p-4">Description</th>
                    <th className="text-left p-4">Status</th>
                    <th className="text-left p-4">Date</th>
                    <th className="text-left p-4">Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {flags.map((flag) => (
                    <tr
                      key={flag.id}
                      className="border-t border-[#E8D5B7] hover:bg-[#F9F4ED]"
                    >
                      {/* Target */}
                      <td className="p-4 capitalize text-[#2C1A0E]">
                        <div>
                          <div className="font-medium">
                            {flag.targetType}
                          </div>
                          <div className="text-xs text-[#6B3F1F]">
                            ID: {flag.targetId}
                          </div>
                        </div>
                      </td>

                      {/* Reason */}
                      <td className="p-4 text-[#2C1A0E]">
                        {flag.reason}
                      </td>

                      {/* Description */}
                      <td className="p-4 text-[#6B3F1F]">
                        {flag.description || '-'}
                      </td>

                      {/* Status */}
                      <td className="p-4 capitalize">
                        <span
                          className={`px-3 py-1 rounded-full text-xs font-medium
                            ${
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
                        {new Date(flag.createdAt).toLocaleDateString()}
                      </td>

                      {/* Actions */}
                      <td className="p-4 flex gap-2">
                        <button
                          onClick={() =>
                            updateStatus(flag.id, 'reviewed')
                          }
                          className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded-lg text-xs"
                        >
                          Review
                        </button>

                        <button
                          onClick={() =>
                            updateStatus(flag.id, 'resolved')
                          }
                          className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded-lg text-xs"
                        >
                          Resolve
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </AdminRoute>
  )
}