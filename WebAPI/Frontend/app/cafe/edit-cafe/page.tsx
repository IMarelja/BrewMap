'use client'

import { useEffect, useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import dynamic from 'next/dynamic'
import api from '@/lib/api'
import Navbar from '@/components/ui/navbar'
import ProtectedRoute from '@/components/auth/protected-route'
import { Category, PaymentOption } from '@/lib/types'

const PinMap = dynamic(
  () => import('../../../components/ui/PinMap'),
  { ssr: false }
)

const days = [
  'monday',
  'tuesday',
  'wednesday',
  'thursday',
  'friday',
  'saturday',
  'sunday'
]

export default function EditCafePage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  const locationId = searchParams.get('locationId')

  const [loading, setLoading] = useState(false)
  const [pageLoading, setPageLoading] = useState(true)

  const [categories, setCategories] = useState<Category[]>([])
  const [paymentOptions, setPaymentOptions] = useState<PaymentOption[]>([])
  const [error, setError] = useState('')

  const [formData, setFormData] = useState({
    name: '',
    description: '',

    street: '',
    city: '',
    country: '',
    postalCode: '',

    latitude: 45.815,
    longitude: 15.9819,

    categoryTag: '',
    paymentOptionTags: [] as string[],

    contact: {
      website: ''
    }
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
        console.error(err)
      }
    }

    load()
  }, [])

  useEffect(() => {
    if (!locationId) return

    const loadCafe = async () => {
      try {
        const res = await api.get(`/api/Locations/${locationId}`)
        const data = res.data

        setFormData({
          name: data.name || '',
          description: data.description || '',

          street: data.address?.street || '',
          city: data.address?.city || '',
          country: data.address?.country || '',
          postalCode: data.address?.postalCode || '',

          latitude: data.latitude || 45.815,
          longitude: data.longitude || 15.9819,

          categoryTag: data.categoryTag || '',
          paymentOptionTags: data.paymentOptionTags || [],

          contact: {
            website: data.contact?.website || ''
          }
        })

        if (data.openingHours) {
          setOpeningHours(data.openingHours)
        }
      } catch (err) {
        console.error(err)
        setError('Failed to load cafe')
      } finally {
        setPageLoading(false)
      }
    }

    loadCafe()
  }, [locationId])

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

    if (!locationId) return

    setLoading(true)
    setError('')

    try {
      const formattedOpeningHours = Object.fromEntries(
        Object.entries(openingHours).map(([day, value]: any) => [
          day,
          value.isClosed
            ? {
                open: null,
                close: null,
                isClosed: true
              }
            : value
        ])
      )

      await api.put(`/api/Locations/${locationId}`, {
        name: formData.name,
        description: formData.description,

        categoryTag: formData.categoryTag,
        paymentOptionTags: formData.paymentOptionTags,

        openingHours: formattedOpeningHours,

        contact: formData.contact.website
          ? {
              website: formData.contact.website
            }
          : null,

        address: {
          street: formData.street,
          city: formData.city,
          country: formData.country,
          postalCode: formData.postalCode
        }
      })

      router.push(`/cafe/${locationId}`)
    } catch (err: any) {
      console.error(err)

      setError(
        err?.response?.data?.message ||
          'Cafe not edited'
      )
    } finally {
      setLoading(false)
    }
  }

  if (pageLoading) {
    return (
      <ProtectedRoute>
        <div style={{ padding: '4rem' }}>
          Loading...
        </div>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <div
        style={{
          background: '#F9F3E9',
          minHeight: '100vh',
          paddingBottom: '4rem'
        }}
      >
        <Navbar />

        <div
          style={{
            maxWidth: '800px',
            margin: '0 auto',
            padding: '2rem'
          }}
        >
          <h1
            style={{
              fontSize: '2.5rem',
              color: '#2C1A0E'
            }}
          >
            Edit Cafe
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

          <form
            onSubmit={handleSubmit}
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '2rem'
            }}
          >
            {/* BASIC INFO */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>
                Basic Information
              </h3>

              <input
                style={inputStyle}
                placeholder="Cafe name"
                value={formData.name}
                onChange={e =>
                  setFormData({
                    ...formData,
                    name: e.target.value
                  })
                }
                required
              />

              <div className="grid grid-cols-2 gap-4">
                <input
                  placeholder="Street"
                  value={formData.street}
                  onChange={e =>
                    setFormData({
                      ...formData,
                      street: e.target.value
                    })
                  }
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />

                <input
                  placeholder="City"
                  value={formData.city}
                  onChange={e =>
                    setFormData({
                      ...formData,
                      city: e.target.value
                    })
                  }
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />

                <input
                  placeholder="Country"
                  value={formData.country}
                  onChange={e =>
                    setFormData({
                      ...formData,
                      country: e.target.value
                    })
                  }
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />

                <input
                  placeholder="Postal code"
                  value={formData.postalCode}
                  onChange={e =>
                    setFormData({
                      ...formData,
                      postalCode: e.target.value
                    })
                  }
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />
              </div>

              <textarea
                style={{
                  ...inputStyle,
                  height: 100,
                  marginTop: 20
                }}
                placeholder="Description"
                value={formData.description}
                onChange={e =>
                  setFormData({
                    ...formData,
                    description: e.target.value
                  })
                }
              />

              <input
                style={inputStyle}
                placeholder="Website"
                value={formData.contact.website}
                onChange={e =>
                  setFormData({
                    ...formData,
                    contact: {
                      website: e.target.value
                    }
                  })
                }
              />
            </section>

            {/* CATEGORY */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>Category</h3>

              <select
                required
                style={inputStyle}
                value={formData.categoryTag}
                onChange={e =>
                  setFormData({
                    ...formData,
                    categoryTag: e.target.value
                  })
                }
              >
                <option value="">
                  Select category
                </option>

                {categories.map(c => (
                  <option
                    key={c.id}
                    value={c.tag}
                  >
                    {c.name}
                  </option>
                ))}
              </select>
            </section>

            {/* MAP */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>
                📍 Pin Location
              </h3>

              <div
                style={{
                  height: 300,
                  borderRadius: 12,
                  overflow: 'hidden'
                }}
              >
                <PinMap
                  onLocationSelect={(lat, lng) =>
                    setFormData({
                      ...formData,
                      latitude: lat,
                      longitude: lng
                    })
                  }
                />
              </div>

              <div
                style={{
                  marginTop: 10,
                  fontSize: 12
                }}
              >
                {formData.latitude.toFixed(4)},
                {' '}
                {formData.longitude.toFixed(4)}
              </div>
            </section>

            {/* PAYMENT */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>
                Payment Options
              </h3>

              <div
                style={{
                  display: 'flex',
                  gap: 10,
                  flexWrap: 'wrap'
                }}
              >
                {paymentOptions.map(opt => (
                  <button
                    key={opt.id}
                    type="button"
                    onClick={() =>
                      togglePayment(opt.tag)
                    }
                    style={{
                      padding: '10px 16px',
                      borderRadius: 8,
                      border: '1px solid #E8D5B7',
                      background:
                        formData.paymentOptionTags.includes(
                          opt.tag
                        )
                          ? '#4A2C19'
                          : '#fff',
                      color:
                        formData.paymentOptionTags.includes(
                          opt.tag
                        )
                          ? '#fff'
                          : '#2C1A0E'
                    }}
                  >
                    {opt.name}
                  </button>
                ))}
              </div>
            </section>

            {/* OPENING HOURS */}
            <section style={cardStyle}>
              <h3 style={sectionTitle}>
                Opening Hours
              </h3>

              <div className="space-y-4">
                {days.map(day => (
                  <div
                    key={day}
                    className="grid grid-cols-4 gap-4 items-center"
                  >
                    <p className="capitalize font-medium text-[#2C1A0E]">
                      {day}
                    </p>

                    <input
                      type="time"
                      value={openingHours[day].open}
                      disabled={
                        openingHours[day].isClosed
                      }
                      onChange={e =>
                        setOpeningHours({
                          ...openingHours,
                          [day]: {
                            ...openingHours[day],
                            open: e.target.value
                          }
                        })
                      }
                      className="border border-[#E8D5B7] rounded-xl px-3 py-2"
                    />

                    <input
                      type="time"
                      value={openingHours[day].close}
                      disabled={
                        openingHours[day].isClosed
                      }
                      onChange={e =>
                        setOpeningHours({
                          ...openingHours,
                          [day]: {
                            ...openingHours[day],
                            close: e.target.value
                          }
                        })
                      }
                      className="border border-[#E8D5B7] rounded-xl px-3 py-2"
                    />

                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={
                          openingHours[day].isClosed
                        }
                        onChange={e =>
                          setOpeningHours({
                            ...openingHours,
                            [day]: {
                              ...openingHours[day],
                              isClosed:
                                e.target.checked
                            }
                          })
                        }
                      />

                      Closed
                    </label>
                  </div>
                ))}
              </div>
            </section>

            {/* ACTIONS */}
            <div
              style={{
                display: 'flex',
                gap: 10
              }}
            >
              <button
                type="button"
                onClick={() =>
                  router.push(`/cafe/${locationId}`)
                }
                style={secondaryBtn}
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={loading}
                style={primaryBtn}
              >
                {loading
                  ? 'Saving...'
                  : 'Save Changes'}
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