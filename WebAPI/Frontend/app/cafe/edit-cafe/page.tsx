'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import dynamic from 'next/dynamic'
import api from '@/lib/api'
import Navbar from '@/components/ui/navbar'
import ProtectedRoute from '@/components/auth/protected-route'
import { Category, PaymentOption } from '@/lib/types'

const PinMap = dynamic(() => import('../../../components/ui/PinMap'), { ssr: false })

const days = [
  'monday','tuesday','wednesday','thursday','friday','saturday','sunday'
]

export default function EditLocationPage() {
  const router = useRouter()
  const { id } = useParams()

  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<Category[]>([])
  const [paymentOptions, setPaymentOptions] = useState<PaymentOption[]>([])
const [error, setError] = useState('')
  const [formData, setFormData] = useState<any>({
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

  const [openingHours, setOpeningHours] = useState<any>(
    days.reduce((acc, day) => {
      acc[day] = { open: '08:00', close: '22:00', isClosed: false }
      return acc
    }, {})
  )

  // metadata
  useEffect(() => {
    const load = async () => {
      const [catRes, payRes] = await Promise.all([
        api.get('/api/Category'),
        api.get('/api/PaymentOption')
      ])

      setCategories(catRes.data)
      setPaymentOptions(payRes.data)
    }

    load()
  }, [])

  // load existing
  useEffect(() => {
    if (!id) return

    const loadLocation = async () => {
      try {
        const res = await api.get(`/api/Locations/${id}`)
        const data = res.data

        setFormData({
          name: data.name,
          description: data.description,
          address: data.address,
          latitude: data.latitude,
          longitude: data.longitude,
          categoryTag: data.categoryTag,
          paymentOptionTags: data.paymentOptionTags || [],
          contact: data.contact || { website: '' }
        })

        if (data.openingHours) {
          setOpeningHours(data.openingHours)
        }
      } catch (err) {
        console.error('Failed to load location', err)
      }
    }

    loadLocation()
  }, [id])

  const togglePayment = (tag: string) => {
    const exists = formData.paymentOptionTags.includes(tag)

    setFormData({
      ...formData,
      paymentOptionTags: exists
        ? formData.paymentOptionTags.filter((t: string) => t !== tag)
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

      await api.put(`/api/Locations/${id}`, {
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
    } catch (err) {
      console.error('Update failed', err)
      setError('Cafe not edited')
    } finally {
      setLoading(false)
    }
  }

  return (
    <ProtectedRoute>
      <div style={{ background: '#F9F3E9', minHeight: '100vh' }}>
        <Navbar />

        <div style={{ maxWidth: 800, margin: '0 auto', padding: 32 }}>
          <h1>Edit Location</h1>
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
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>

            {/* BASIC */}
            <section style={cardStyle}>
              <input
                style={inputStyle}
                value={formData.name}
                onChange={e => setFormData({ ...formData, name: e.target.value })}
              />

              <textarea
                style={inputStyle}
                value={formData.description}
                onChange={e => setFormData({ ...formData, description: e.target.value })}
              />
            </section>

            {/* CATEGORY */}
            <section style={cardStyle}>
              <select
                style={inputStyle}
                value={formData.categoryTag}
                onChange={e =>
                  setFormData({ ...formData, categoryTag: e.target.value })
                }
              >
                {categories.map(c => (
                  <option key={c.id} value={c.tag}>
                    {c.name}
                  </option>
                ))}
              </select>
            </section>

            {/* MAP */}
            <section style={cardStyle}>
              <PinMap
                onLocationSelect={(lat, lng) =>
                  setFormData({ ...formData, latitude: lat, longitude: lng })
                }
              />
            </section>

            {/* PAYMENT */}
            <section style={cardStyle}>
              {paymentOptions.map(opt => (
                <button
                  key={opt.id}
                  type="button"
                  onClick={() => togglePayment(opt.tag)}
                  style={{
                    margin: 5,
                    padding: 10,
                    background: formData.paymentOptionTags.includes(opt.tag)
                      ? '#4A2C19'
                      : '#fff',
                    color: formData.paymentOptionTags.includes(opt.tag)
                      ? '#fff'
                      : '#000'
                  }}
                >
                  {opt.name}
                </button>
              ))}
            </section>

            {/* ACTIONS */}
            <button disabled={loading} type="submit">
              {loading ? 'Saving...' : 'Save Changes'}
            </button>

          </form>
        </div>
      </div>
    </ProtectedRoute>
  )
}

// styles
const cardStyle = { background: '#fff', padding: 20, borderRadius: 12 }
const inputStyle = { width: '100%', padding: 10, marginBottom: 10 }