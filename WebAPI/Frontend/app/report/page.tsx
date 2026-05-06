'use client'

import { useState } from 'react'
import Navbar from '@/components/ui/navbar'
import ProtectedRoute from '@/components/auth/protected-route'
import api from '@/lib/api'

export default function ReportPage() {
  const [targetType, setTargetType] = useState('location')
  const [targetId, setTargetId] = useState('')
  const [reason, setReason] = useState('')
  const [description, setDescription] = useState('')
  const [success, setSuccess] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    try {
      await api.post('/api/Flag', {
        target: {
          type: targetType,
          id: targetId
        },
        reason,
        description
      })

      setSuccess(true)
      setReason('')
      setDescription('')
      setTargetId('')
    } catch (err) {
      console.error(err)
    }
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-[#F5EFE6]">
        <Navbar />

        <div className="max-w-2xl mx-auto p-8">
          <div className="bg-white border border-[#E8D5B7] rounded-2xl p-8">
            <h1 className="text-4xl font-bold text-[#2C1A0E] mb-6">
              Report Content
            </h1>

            {success && (
              <div className="bg-green-100 text-green-700 p-4 rounded-xl mb-6">
                Report submitted successfully.
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
              <select
                value={targetType}
                onChange={e => setTargetType(e.target.value)}
                className="w-full border border-[#E8D5B7] rounded-xl px-4 py-3"
              >
                <option value="location">Location</option>
                <option value="review">Review</option>
                <option value="product">Product</option>
                <option value="user">User</option>
              </select>

              <input
                placeholder="Target ID"
                value={targetId}
                onChange={e => setTargetId(e.target.value)}
                className="w-full border border-[#E8D5B7] rounded-xl px-4 py-3"
              />

              <input
                placeholder="Reason"
                value={reason}
                onChange={e => setReason(e.target.value)}
                className="w-full border border-[#E8D5B7] rounded-xl px-4 py-3"
              />

              <textarea
                rows={5}
                placeholder="Additional details"
                value={description}
                onChange={e => setDescription(e.target.value)}
                className="w-full border border-[#E8D5B7] rounded-xl px-4 py-3"
              />

              <button
                type="submit"
                className="bg-[#2C1A0E] text-[#F5EFE6] px-8 py-3 rounded-xl font-medium"
              >
                Submit Report
              </button>
            </form>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}