'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import api from '@/lib/api'
import Navbar from '@/components/ui/navbar'
import ProtectedRoute from '@/components/auth/protected-route'
import { useSearchParams } from 'next/navigation'

export default function AddDrinkPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const searchParams = useSearchParams()
  const locationId = searchParams.get('locationId')
  const [error, setError] = useState('')
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    locationId: locationId || ''
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      await api.post('/api/Drink', {
        name: formData.name,
        description: formData.description,
        locationId: formData.locationId
      })

    router.push(`/cafe/${locationId}`)
    } catch (error) {
      console.error('Failed to create drink', error)
      setError('Drink not created')
    } finally {
      setLoading(false)
    }
  }

  return (
    <ProtectedRoute>
      <div style={{ background: '#F9F3E9', minHeight: '100vh', paddingBottom: '4rem' }}>
        <Navbar />

        <div style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem' }}>
          <h1 style={{ fontSize: '2.5rem', color: '#2C1A0E' }}>
            Add a New Drink
          </h1>
            {error && (
            <div
                style={{
                background: '#FEE2E2',
                color: '#991B1B',
                padding: '10px 14px',
                borderRadius: '8px',
                marginTop: '10px',
                marginBottom: '20px',
                border: '1px solid #FCA5A5'
                }}
            >
                {error}
            </div>
            )}
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>

            <section style={cardStyle}>
              <h3 style={sectionTitle}>Basic Information</h3>

              <input
                style={inputStyle}
                placeholder="Drink name"
                value={formData.name}
                onChange={e => setFormData({ ...formData, name: e.target.value })}
                required
              />

              <textarea
                style={{ ...inputStyle, height: 120 }}
                placeholder="Description"
                value={formData.description}
                onChange={e =>
                  setFormData({ ...formData, description: e.target.value })
                }
              />
            </section>

            {/* ACTIONS */}
            <div style={{ display: 'flex', gap: 10 }}>
              <button
                type="button"
                onClick={() => router.back()}
                style={secondaryBtn}
              >
                Cancel
              </button>

              <button type="submit" disabled={loading} style={primaryBtn}>
                {loading ? 'Adding...' : '+ Add Drink'}
              </button>
            </div>

          </form>
        </div>
      </div>
    </ProtectedRoute>
  )
}

const cardStyle = {
  background: '#FFF',
  padding: '2rem',
  borderRadius: '16px',
  border: '1px solid #E8D5B7'
}

const sectionTitle = {
  fontSize: '1.1rem',
  fontWeight: '700',
  marginBottom: '1.5rem',
  color: '#2C1A0E'
}

const inputStyle = {
  width: '100%',
  padding: '12px',
  borderRadius: '8px',
  border: '1px solid #E8D5B7',
  marginBottom: '1.5rem',
  outline: 'none'
}

const primaryBtn = {
  flex: 2,
  padding: '16px',
  background: '#4A2C19',
  color: '#FFF',
  border: 'none',
  borderRadius: '12px',
  fontWeight: 'bold',
  cursor: 'pointer'
}

const secondaryBtn = {
  flex: 1,
  padding: '16px',
  background: '#FFF',
  border: '1px solid #E8D5B7',
  borderRadius: '12px',
  cursor: 'pointer'
}