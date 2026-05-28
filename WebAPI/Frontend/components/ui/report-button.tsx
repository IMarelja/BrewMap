'use client'

import { useState } from 'react'
import api from '@/lib/api'

interface Props {
  targetType: 'location' | 'product' | 'review'
  targetId: string
}

export default function ReportButton({
  targetType,
  targetId
}: Props) {
  const [open, setOpen] = useState(false)
  const [reason, setReason] = useState('')
  const [description, setDescription] = useState('')
  const [loading, setLoading] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  const submitReport = async () => {
    if (!reason.trim()) return

    setLoading(true)

    try {
      await api.post('/api/Flag', {
        target: {
          type: targetType,
          id: targetId
        },
        reason,
        description
      })

      setToast('Report submitted')
      setOpen(false)
      setReason('')
      setDescription('')
    } catch (err) {
      console.error(err)
      setToast('Failed to submit report')
    } finally {
      setLoading(false)

      setTimeout(() => {
        setToast(null)
      }, 3000)
    }
  }

  return (
    <>
      <button
        onClick={() => setOpen(true)}
        title="Report"
        style={{
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            fontSize: '18px',
            padding: '4px',
            lineHeight: 1,
            transition: 'transform 0.15s ease'
        }}
      >
          🚩
      </button>

      {open && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,0.4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999
          }}
        >
          <div
            style={{
              background: '#fff',
              padding: '24px',
              borderRadius: '12px',
              width: '400px',
              maxWidth: '90%'
            }}
          >
            <h2
              style={{
                marginBottom: '1rem',
                color: '#2C1A0E'
              }}
            >
              Report Content
            </h2>

            <input
              value={reason}
              onChange={e => setReason(e.target.value)}
              placeholder="Reason"
              style={{
                width: '100%',
                marginBottom: '1rem',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid #ddd'
              }}
            />

            <textarea
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Additional details"
              rows={4}
              style={{
                width: '100%',
                marginBottom: '1rem',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid #ddd'
              }}
            />

            <div
              style={{
                display: 'flex',
                justifyContent: 'flex-end',
                gap: '10px'
              }}
            >
              <button
                onClick={() => setOpen(false)}
                style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  border: '1px solid #ccc',
                  background: '#fff'
                }}
              >
                Cancel
              </button>

              <button
                onClick={submitReport}
                disabled={loading}
                style={{
                  background: '#991B1B',
                  color: '#fff',
                  border: 'none',
                  padding: '8px 14px',
                  borderRadius: '8px'
                }}
              >
                {loading ? 'Submitting...' : 'Submit'}
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && (
        <div
          style={{
            position: 'fixed',
            top: 20,
            right: 20,
            background: '#2C1A0E',
            color: '#fff',
            padding: '10px 16px',
            borderRadius: '8px',
            zIndex: 10000
          }}
        >
          {toast}
        </div>
      )}
    </>
  )
}