'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Navbar from '@/components/ui/navbar'
import ProtectedRoute from '@/components/auth/protected-route'
import api from '@/lib/api'
import { Category, PaymentOption } from '@/lib/types'

const days = [
  'monday',
  'tuesday',
  'wednesday',
  'thursday',
  'friday',
  'saturday',
  'sunday'
]

export default function CreateLocationPage() {
  const router = useRouter()

  const [categories, setCategories] = useState<Category[]>([])
  const [paymentOptions, setPaymentOptions] = useState<PaymentOption[]>([])

  const [form, setForm] = useState({
    name: '',
    description: '',
    street: '',
    city: '',
    country: '',
    postalCode: '',
    latitude: 45.8150, // Default center
    longitude: 15.9819,
    categoryTag: '',
    website: ''
  })

  const [selectedPayments, setSelectedPayments] = useState<string[]>([])

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
    loadData()
  }, [])

  const loadData = async () => {
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

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault()

  try {
    // transform opening hours before sending
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

    await api.post('/api/Locations', {
      name: form.name,
      description: form.description,

      // fixed coordinates
      latitude: 45.8150,
      longitude: 15.9819,

      categoryTag: form.categoryTag,
      paymentOptionTags: selectedPayments,
      openingHours: formattedOpeningHours,

      contact: {
        website: form.website
      },

      address: {
        street: form.street,
        city: form.city,
        country: form.country,
        postalCode: form.postalCode
      }
    })

    router.push('/explore')
  } catch (err) {
    console.error(err)
  }
}

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-[#F5EFE6]">
        <Navbar />

        <div className="max-w-4xl mx-auto p-8">
          <div className="bg-white rounded-2xl border border-[#E8D5B7] p-8">
            <h1 className="text-4xl font-bold text-[#2C1A0E] mb-8">
              Add Cafe Location
            </h1>

            <form onSubmit={handleSubmit} className="space-y-6">
              <input
                placeholder="Cafe name"
                value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })}
                className="w-full border border-[#E8D5B7] rounded-xl px-4 py-3"
              />

              <textarea
                placeholder="Description"
                rows={4}
                value={form.description}
                onChange={e => setForm({ ...form, description: e.target.value })}
                className="w-full border border-[#E8D5B7] rounded-xl px-4 py-3"
              />

              <div className="grid grid-cols-2 gap-4">
                <input
                  placeholder="Street"
                  value={form.street}
                  onChange={e => setForm({ ...form, street: e.target.value })}
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />

                <input
                  placeholder="City"
                  value={form.city}
                  onChange={e => setForm({ ...form, city: e.target.value })}
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />

                <input
                  placeholder="Country"
                  value={form.country}
                  onChange={e => setForm({ ...form, country: e.target.value })}
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />

                <input
                  placeholder="Postal code"
                  value={form.postalCode}
                  onChange={e => setForm({ ...form, postalCode: e.target.value })}
                  className="border border-[#E8D5B7] rounded-xl px-4 py-3"
                />
              </div>

              <select
                value={form.categoryTag}
                onChange={e => setForm({ ...form, categoryTag: e.target.value })}
                className="w-full border border-[#E8D5B7] rounded-xl px-4 py-3"
              >
                <option value="">Select category</option>
                {categories.map(cat => (
                  <option key={cat.id} value={cat.tag}>
                    {cat.name}
                  </option>
                ))}
              </select>

              <div>
                <p className="font-medium mb-3 text-[#2C1A0E]">
                  Payment Methods
                </p>

                <div className="flex flex-wrap gap-3">
                  {paymentOptions.map(option => (
                    <label
                      key={option.id}
                      className="flex items-center gap-2 bg-[#F5EFE6] px-4 py-2 rounded-xl"
                    >
                      <input
                        type="checkbox"
                        checked={selectedPayments.includes(option.tag)}
                        onChange={e => {
                          if (e.target.checked) {
                            setSelectedPayments([...selectedPayments, option.tag])
                          } else {
                            setSelectedPayments(
                              selectedPayments.filter(p => p !== option.tag)
                            )
                          }
                        }}
                      />
                      {option.name}
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <h2 className="text-2xl font-semibold text-[#2C1A0E] mb-4">
                  Opening Hours
                </h2>

                <div className="space-y-4">
                  {days.map(day => (
                    <div key={day} className="grid grid-cols-4 gap-4 items-center">
                      <p className="capitalize font-medium text-[#2C1A0E]">
                        {day}
                      </p>

                      <input
                        type="time"
                        value={openingHours[day].open}
                        disabled={openingHours[day].isClosed}
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
                        disabled={openingHours[day].isClosed}
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
                </div>
              </div>

              <button
                type="submit"
                className="bg-[#2C1A0E] text-[#F5EFE6] px-8 py-3 rounded-xl font-medium"
              >
                Create Location
              </button>
            </form>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}