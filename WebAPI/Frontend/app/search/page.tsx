'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import Navbar from '@/components/ui/navbar'
import ProtectedRoute from '@/components/auth/protected-route'
import api from '@/lib/api'
import { Cafe, Category, PaymentOption } from '@/lib/types'

export default function SearchPage() {
  const [query, setQuery] = useState('')
  const [drinkQuery, setDrinkQuery] = useState('')
  const [rating, setRating] = useState('')
  const [distance, setDistance] = useState('5000')
  const [categories, setCategories] = useState<Category[]>([])
  const [paymentOptions, setPaymentOptions] = useState<PaymentOption[]>([])
  const [selectedCategory, setSelectedCategory] = useState('')
  const [selectedPayment, setSelectedPayment] = useState('')
  const [results, setResults] = useState<Cafe[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadFilters()
  }, [])

  const loadFilters = async () => {
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

  const handleSearch = async () => {
    setLoading(true)

    navigator.geolocation.getCurrentPosition(async position => {
      try {
        const params = new URLSearchParams({
          longitude: String(position.coords.longitude),
          latitude: String(position.coords.latitude),
          radiusMeters: distance
        })

        if (query) params.append('query', query)
        if (drinkQuery) params.append('drinkQuery', drinkQuery)
        if (rating) params.append('minRating', rating)
        if (selectedCategory) params.append('categoryTags', selectedCategory)
        if (selectedPayment) params.append('paymentOptionTags', selectedPayment)

        const res = await api.get(`/api/Locations/search?${params.toString()}`)
        setResults(res.data)
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    })
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-[#F5EFE6]">
        <Navbar />

        <div className="max-w-7xl mx-auto p-8">
          <h1 className="text-4xl font-bold text-[#2C1A0E] mb-8">
            Search Cafes
          </h1>

          <div className="bg-white rounded-2xl border border-[#E8D5B7] p-6 mb-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <input
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Search cafes..."
              className="border border-[#E8D5B7] rounded-xl px-4 py-3 outline-none"
            />

            <input
              value={drinkQuery}
              onChange={e => setDrinkQuery(e.target.value)}
              placeholder="Search beverages..."
              className="border border-[#E8D5B7] rounded-xl px-4 py-3 outline-none"
            />

            <select
              value={rating}
              onChange={e => setRating(e.target.value)}
              className="border border-[#E8D5B7] rounded-xl px-4 py-3"
            >
              <option value="">Any rating</option>
              <option value="1">1+ stars</option>
              <option value="2">2+ stars</option>
              <option value="3">3+ stars</option>
              <option value="4">4+ stars</option>
              <option value="5">5 stars</option>
            </select>

            <select
              value={distance}
              onChange={e => setDistance(e.target.value)}
              className="border border-[#E8D5B7] rounded-xl px-4 py-3"
            >
              <option value="1000">1 km</option>
              <option value="5000">5 km</option>
              <option value="10000">10 km</option>
              <option value="20000">20 km</option>
            </select>

            <select
              value={selectedCategory}
              onChange={e => setSelectedCategory(e.target.value)}
              className="border border-[#E8D5B7] rounded-xl px-4 py-3"
            >
              <option value="">All categories</option>
              {categories.map(cat => (
                <option key={cat.id} value={cat.tag}>
                  {cat.name}
                </option>
              ))}
            </select>

            <select
              value={selectedPayment}
              onChange={e => setSelectedPayment(e.target.value)}
              className="border border-[#E8D5B7] rounded-xl px-4 py-3"
            >
              <option value="">All payment methods</option>
              {paymentOptions.map(p => (
                <option key={p.id} value={p.tag}>
                  {p.name}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={handleSearch}
            className="bg-[#2C1A0E] text-[#F5EFE6] px-8 py-3 rounded-xl font-medium mb-8"
          >
            Search
          </button>

          {loading && <p>Loading...</p>}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {results.map(cafe => (
              <Link key={cafe.id} href={`/cafe/${cafe.id}`}>
                <div className="bg-white rounded-2xl border border-[#E8D5B7] overflow-hidden hover:-translate-y-1 transition-all">
                  <div className="h-40 bg-[#E8D5B7] flex items-center justify-center text-5xl">
                    ☕
                  </div>

                  <div className="p-5">
                    <h3 className="font-semibold text-lg text-[#2C1A0E] mb-2">
                      {cafe.name}
                    </h3>

                    <p className="text-sm text-[#6B3F1F] mb-3">
                      {cafe.address.street}, {cafe.address.city}
                    </p>

                    <div className="flex items-center justify-between">
                      <span className="text-sm bg-[#E8D5B7] px-3 py-1 rounded-full text-[#6B3F1F]">
                        {cafe.categoryTag}
                      </span>

                      <span className="text-sm text-[#2C1A0E] font-medium">
                        ⭐ {cafe.averageRating || 0}
                      </span>
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}