'use client'

import { useEffect, useState } from 'react'
import Navbar from '@/components/ui/navbar'
import AdminRoute from '@/components/auth/admin-route'
import api from '@/lib/api'
import { Flag } from '@/lib/types'

export default function AdminPage() {
  const [flags, setFlags] = useState<Flag[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadFlags()
  }, [])

  const loadFlags = async () => {
    try {
      const res = await api.get('/api/Flag')
      setFlags(res.data)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const updateStatus = async (id: string, status: string) => {
    try {
      await api.put('/api/Flag/status', {
        id,
        status,
        resolvedByAdminId: 'admin'
      })

      loadFlags()
    } catch (err) {
      console.error(err)
    }
  }

  return (
    <AdminRoute>
      <div className="min-h-screen bg-[#F5EFE6]">
        <Navbar />

        <div className="max-w-7xl mx-auto p-8">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-4xl font-bold text-[#2C1A0E]">
                Admin Panel
              </h1>
              <p className="text-[#6B3F1F] mt-2">
                Moderate reports and platform content
              </p>
            </div>
          </div>

          {loading ? (
            <p>Loading...</p>
          ) : (
            <div className="bg-white border border-[#E8D5B7] rounded-2xl overflow-hidden">
              <table className="w-full">
                <thead className="bg-[#F5EFE6]">
                  <tr>
                    <th className="text-left p-4">Target</th>
                    <th className="text-left p-4">Reason</th>
                    <th className="text-left p-4">Status</th>
                    <th className="text-left p-4">Date</th>
                    <th className="text-left p-4">Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {flags.map(flag => (
                    <tr key={flag.id} className="border-t border-[#E8D5B7]">
                      <td className="p-4 capitalize">
                        {flag.target.type}
                      </td>

                      <td className="p-4">
                        {flag.reason}
                      </td>

                      <td className="p-4 capitalize">
                        {flag.status}
                      </td>

                      <td className="p-4">
                        {new Date(flag.createdAt).toLocaleDateString()}
                      </td>

                      <td className="p-4 flex gap-2">
                        <button
                          onClick={() => updateStatus(flag.id, 'reviewed')}
                          className="bg-yellow-500 text-white px-4 py-2 rounded-lg text-sm"
                        >
                          Review
                        </button>

                        <button
                          onClick={() => updateStatus(flag.id, 'resolved')}
                          className="bg-green-600 text-white px-4 py-2 rounded-lg text-sm"
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