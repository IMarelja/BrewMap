'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import dynamic from 'next/dynamic'
import api from '@/lib/api'
import Navbar from '@/components/ui/navbar'
import { useEffect } from 'react'
import ProtectedRoute from '@/components/auth/protected-route'
import { Category, PaymentOption } from '@/lib/types'

// We'll build this component next - it needs to handle the click-to-pin logic
const PinMap = dynamic(() => import('../../components/ui/PinMap'), { ssr: false })

const days = [
  'monday',
  'tuesday',
  'wednesday',
  'thursday',
  'friday',
  'saturday',
  'sunday'
]

export default function AddCafePage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)

  const [categories, setCategories] = useState<Category[]>([])
  const [paymentOptions, setPaymentOptions] = useState<PaymentOption[]>([])
  const [error, setError] = useState('')
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    address: {
      street: '',
      city: 'Zagreb',
      country: 'Croatia',
      postalCode: ''
    },
    latitude: 45.815,
    longitude: 15.9819,
    categoryTag: '',
    paymentOptionTags: [] as string[],
    contact: { website: '' }
  })

  const [openingHours, setOpeningHours] = useState(
    days.reduce((acc, day) => {
      acc[day] = {
        open: '08:00',
        close: '22:00',
        isClosed: false
      }
      return acc
    }, {} as any)
  )

  useEffect(() => {
    const load = async () => {
      try {
        const [catRes, payRes] = await Promise.all([
          api.get('/api/Category'),
          api.get('/api/PaymentOption')
        ])

        setCategories(catRes.data)
        setPaymentOptions(payRes.data)
      } catch (err) {
        console.error('Failed loading metadata', err)
      }
    }

    load()
  }, [])

  const togglePayment = (tag: string) => {
    const exists = formData.paymentOptionTags.includes(tag)

    setFormData({
      ...formData,
      paymentOptionTags: exists
        ? formData.paymentOptionTags.filter(t => t !== tag)
        : [...formData.paymentOptionTags, tag]
    })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const formattedOpeningHours = Object.fromEntries(
        Object.entries(openingHours).map(([day, value]: any) => [
          day,
          value.isClosed
            ? { open: null, close: null, isClosed: true }
            : value
        ])
      )

      await api.post('/api/Locations', {
        name: formData.name,
        description: formData.description,

        latitude: formData.latitude,
        longitude: formData.longitude,

        categoryTag: formData.categoryTag,
        paymentOptionTags: formData.paymentOptionTags,

        openingHours: formattedOpeningHours,

        contact: formData.contact,

        address: formData.address
      })

      router.push('/explore')
    } catch (error) {
      console.error('Submission failed', error)
      setError('Cafe not created')
    } finally {
      setLoading(false)
    }
  }

  return (
    <ProtectedRoute>
      <div style={{ background: '#F9F3E9', minHeight: '100vh', paddingBottom: '4rem' }}>
        <Navbar />

        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
          <h1 style={{ fontSize: '2.5rem', color: '#2C1A0E' }}>
            Add a New Location
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

            {/* BASIC INFO */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>Basic Information</h3>

              <input
                style={inputStyle}
                placeholder="Cafe name"
                onChange={e => setFormData({ ...formData, name: e.target.value })}
                required
              />

              <input
                style={inputStyle}
                placeholder="Street address"
                onChange={e =>
                  setFormData({
                    ...formData,
                    address: { ...formData.address, street: e.target.value }
                  })
                }
              />

              <textarea
                style={{ ...inputStyle, height: 100 }}
                placeholder="Description"
                onChange={e =>
                  setFormData({ ...formData, description: e.target.value })
                }
              />
            </section>

            {/* CATEGORY (FIXED FROM API) */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>Category</h3>

              <select
                style={inputStyle}
                value={formData.categoryTag}
                onChange={e =>
                  setFormData({ ...formData, categoryTag: e.target.value })
                }
              >
                <option value="">Select category</option>
                {categories.map(c => (
                  <option key={c.id} value={c.tag}>
                    {c.name}
                  </option>
                ))}
              </select>
            </section>

            {/* MAP */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>📍 Pin Location</h3>

              <div style={{ height: 300, borderRadius: 12, overflow: 'hidden' }}>
                <PinMap
                  onLocationSelect={(lat, lng) =>
                    setFormData({ ...formData, latitude: lat, longitude: lng })
                  }
                />
              </div>

              <div style={{ marginTop: 10, fontSize: 12 }}>
                {formData.latitude.toFixed(4)}, {formData.longitude.toFixed(4)}
              </div>
            </section>

            {/* PAYMENT (FIXED FROM API) */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>Payment Options</h3>

              <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                {paymentOptions.map(opt => (
                  <button
                    key={opt.id}
                    type="button"
                    onClick={() => togglePayment(opt.tag)}
                    style={{
                      padding: '10px 16px',
                      borderRadius: 8,
                      border: '1px solid #E8D5B7',
                      background: formData.paymentOptionTags.includes(opt.tag)
                        ? '#4A2C19'
                        : '#fff',
                      color: formData.paymentOptionTags.includes(opt.tag)
                        ? '#fff'
                        : '#2C1A0E'
                    }}
                  >
                    {opt.name}
                  </button>
                ))}
              </div>
            </section>

            {/* OPENING HOURS (ADDED BACK LOGIC) */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>Opening Hours</h3>

              {days.map(day => (
                <div key={day} style={{ display: 'grid', gridTemplateColumns: '120px 1fr 1fr 100px', gap: 10, marginBottom: 10 }}>
                  <span style={{ textTransform: 'capitalize' }}>{day}</span>

                  <input
                    type="time"
                    disabled={openingHours[day].isClosed}
                    value={openingHours[day].open}
                    onChange={e =>
                      setOpeningHours({
                        ...openingHours,
                        [day]: { ...openingHours[day], open: e.target.value }
                      })
                    }
                  />

                  <input
                    type="time"
                    disabled={openingHours[day].isClosed}
                    value={openingHours[day].close}
                    onChange={e =>
                      setOpeningHours({
                        ...openingHours,
                        [day]: { ...openingHours[day], close: e.target.value }
                      })
                    }
                  />

                  <label>
                    <input
                      type="checkbox"
                      checked={openingHours[day].isClosed}
                      onChange={e =>
                        setOpeningHours({
                          ...openingHours,
                          [day]: {
                            ...openingHours[day],
                            isClosed: e.target.checked
                          }
                        })
                      }
                    />
                    Closed
                  </label>
                </div>
              ))}
            </section>

            {/* ACTIONS */}
            <div style={{ display: 'flex', gap: 10 }}>
              <button type="button" onClick={() => router.back()} style={secondaryBtn}>
                Cancel
              </button>

              <button type="submit" disabled={loading} style={primaryBtn}>
                {loading ? 'Adding...' : '+ Add Location'}
              </button>
            </div>

          </form>
        </div>
      </div>
    </ProtectedRoute>
  )
}

// Styles to match your UI
const cardStyle = { background: '#FFF', padding: '2rem', borderRadius: '16px', border: '1px solid #E8D5B7' }
const sectionTitle = { fontSize: '1.1rem', fontWeight: '700', marginBottom: '1.5rem', color: '#2C1A0E' }
const labelStyle = { display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '0.9rem' }
const inputStyle = { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #E8D5B7', marginBottom: '1.5rem', outline: 'none' }
const primaryBtn = { flex: 2, padding: '16px', background: '#4A2C19', color: '#FFF', border: 'none', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer' }
const secondaryBtn = { flex: 1, padding: '16px', background: '#FFF', border: '1px solid #E8D5B7', borderRadius: '12px', cursor: 'pointer' }