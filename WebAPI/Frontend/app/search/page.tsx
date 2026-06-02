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
  const [distance, setDistance] = useState('20000')
  const [categories, setCategories] = useState<Category[]>([])
  const [paymentOptions, setPaymentOptions] = useState<PaymentOption[]>([])
  const [selectedCategory, setSelectedCategory] = useState('')
  const [selectedPayments, setSelectedPayments] = useState<string[]>([])
  const [results, setResults] = useState<Cafe[]>([])
  const [loading, setLoading] = useState(false)
  const [isPaymentsOpen, setIsPaymentsOpen] = useState(false)

  // Load payment options
  useEffect(() => {
    api.get('/api/PaymentOption')
      .then(res => setPaymentOptions(res.data))
      .catch(() => setPaymentOptions([]))

    api.get('/api/Category')
    .then(res => setCategories(res.data))
    .catch(() => setCategories([]))
  }, [])

  const fetchSearch = () => {
    setLoading(true)

    const params = new URLSearchParams()

    if (query) params.append('query', query)
    if (drinkQuery) params.append('drinkQuery', drinkQuery)
    if (rating) params.append('minRating', rating)

    if (selectedCategory) {
      params.append('categoryTags', selectedCategory)
    }

    selectedPayments.forEach(payment => {
      params.append('paymentOptionTags', payment)
    })

    params.append('longitude', '15.8457503')
    params.append('latitude', '45.7976803')
    params.append('radiusMeters', '200000')

    api.get(`/api/Locations/search?${params.toString()}`)
      .then(res => {
        const data = Array.isArray(res.data)
          ? res.data
          : res.data.locations || []

        setResults(data)
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchSearch()
    }, 400)

    return () => clearTimeout(timer)
  }, [query, drinkQuery, rating, distance, selectedCategory, selectedPayments])

  const togglePayment = (tag: string) => {
  setSelectedPayments(prev =>
    prev.includes(tag)
      ? prev.filter(t => t !== tag)
      : [...prev, tag]
  )
}

  return (
    <ProtectedRoute>
      <div style={{ minHeight: '100vh', background: '#F9F3E9' }}>
        <Navbar />
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '3rem 2rem' }}>
          
          {/* Header Section */}
          <h1 style={{ fontFamily: 'Playfair Display, serif', fontSize: '2.5rem', color: '#2C1A0E', marginBottom: '0.5rem', fontWeight: '800' }}>Search</h1>
          <p style={{ color: '#6B3F1F', marginBottom: '2rem', fontSize: '1.1rem' }}>Find cafes, drinks, and more</p>

          {/* Search Bar Section */}
          <div style={{ position: 'relative', marginBottom: '2rem' }}>
            <span style={{ position: 'absolute', left: '15px', top: '12%', transform: 'translateY(-50%)', fontSize: '20px', color: '#888' }}>🔍</span>

            <input
              type="text"
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Search for cafes, drinks, or amenities..."
              style={{ 
                width: '100%', 
                padding: '16px 16px 16px 50px', 
                border: '1px solid #E8D5B7', 
                borderRadius: '8px', 
                fontSize: '16px', 
                background: '#FFF', 
                outline: 'none', 
                boxShadow: '0 2px 4px rgba(0,0,0,0.02)' 
              }}
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

          {/* <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <input
              type="number"
              min="1"
              value={distance}
              onChange={e => setDistance(e.target.value)}
              placeholder="Distance"
              className="border border-[#E8D5B7] rounded-xl px-4 py-3 outline-none"
              style={{ width: '140px' }}
            />
          </div> */}

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

            <div style={{ position: 'relative', width: '260px' }}>
              <button
                type="button"
                onClick={() => setIsPaymentsOpen(prev => !prev)}
                className="border border-[#E8D5B7] rounded-xl px-4 py-3 bg-white w-full text-left"
                style={{
                  cursor: 'pointer',
                  color: '#2C1A0E'
                }}
              >
                {selectedPayments.length > 0
                  ? `${selectedPayments.length} payment option${selectedPayments.length > 1 ? 's' : ''} selected`
                  : 'Select payment methods'}
              </button>

              {isPaymentsOpen && (
                <div
                  style={{
                    position: 'absolute',
                    top: '110%',
                    left: 0,
                    width: '100%',
                    background: '#fff',
                    border: '1px solid #E8D5B7',
                    borderRadius: '12px',
                    padding: '12px',
                    zIndex: 1000,
                    boxShadow: '0 8px 20px rgba(0,0,0,0.08)',
                    maxHeight: '220px',
                    overflowY: 'auto'
                  }}
                >
                  {paymentOptions.map(option => (
                    <label
                      key={option.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '10px',
                        padding: '8px 4px',
                        cursor: 'pointer',
                        color: '#2C1A0E'
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={selectedPayments.includes(option.tag)}
                        onChange={() => togglePayment(option.tag)}
                      />

                      {option.name}
                    </label>
                  ))}

                  {selectedPayments.length > 0 && (
                    <button
                      type="button"
                      onClick={() => setSelectedPayments([])}
                      style={{
                        marginTop: '10px',
                        width: '100%',
                        border: 'none',
                        background: '#F5EFE6',
                        padding: '8px',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        color: '#6B3F1F',
                        fontWeight: 600
                      }}
                    >
                      Clear selection
                    </button>
                  )}
                </div>
              )}
            </div>
          </div>

          <h2 style={{ fontSize: '1.25rem', color: '#2C1A0E', marginBottom: '1.5rem' }}>
            {query ? `Results for "${query}"` : 'Featured Cafes'}
          </h2>

          {loading && <div style={{ textAlign: 'center', color: '#6B3F1F', padding: '2rem' }}>Searching...</div>}

          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', 
            gap: '1.5rem' 
          }}>
            {results.map(cafe => (
              <Link key={cafe.id} href={`/cafe/${cafe.id}`} style={{ textDecoration: 'none' }}>
                <div style={{ 
                  background: '#fff', 
                  border: '1px solid #E8D5B7', 
                  borderRadius: '12px', 
                  padding: '1.5rem', 
                  display: 'flex', 
                  gap: '1.25rem', 
                  alignItems: 'center',
                  transition: 'transform 0.2s',
                }}>
                  <div style={{ width: '80px', height: '80px' }}>
                    <img
                      src="/placeholder.png"
                      alt="Cafe"
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                  </div>

                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: '700', fontSize: '1.1rem', color: '#2C1A0E' }}>
                      {cafe.name}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: '#8C7861' }}>
                      {cafe.address.street}, {cafe.address.city}
                    </div>
                     {cafe.paymentOptionTags?.length > 0 && (
                      <div
                        style={{
                          display: 'flex',
                          flexWrap: 'wrap',
                          gap: '6px',
                          marginTop: '10px'
                        }}
                      >
                        {cafe.paymentOptionTags.map(payment => (
                          <span
                            key={payment}
                            style={{
                              background: '#F5EFE6',
                              color: '#6B3F1F',
                              padding: '4px 10px',
                              borderRadius: '999px',
                              fontSize: '12px',
                              fontWeight: 500,
                              border: '1px solid #E8D5B7'
                            }}
                          >
                            {payment}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>

          {!loading && query && results.length === 0 && (
            <div style={{ textAlign: 'center', color: '#6B3F1F', padding: '2rem' }}>
              No cafes found for "{query}"
            </div>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}