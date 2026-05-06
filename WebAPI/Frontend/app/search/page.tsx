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


  const recentSearches = ['espresso', 'outdoor seating', 'cozy cafe', 'cold brew']
  const popularTags = ['WiFi', 'Pet Friendly', 'Good for Work', 'Outdoor Seating', 'Quiet']

useEffect(() => {

  if (!query.trim()) {
    setLoading(true)
    api.get(`/api/Locations/search`, {
      params: {
        query: "", 
        longitude: 15.8457503, 
        latitude: 45.7976803,
        radiusMeters: 10000 
      }
    })
    .then(res => {
      const data = Array.isArray(res.data) ? res.data : res.data.locations || []
      setResults(data.slice(0, 4)) // Show only the first 4 as "Featured"
    })
    .catch(() => setResults([]))
    .finally(() => setLoading(false))
    return
  }
  
  const timer = setTimeout(() => {
    setLoading(true)
    api.get(`/api/Locations/search`, {
      params: {
        query: query,
        longitude: 15.8457503, 
        latitude: 45.7976803,
        radiusMeters: 5000 
      }
    })
    .then(res => {
      const data = Array.isArray(res.data) ? res.data : res.data.locations || []
      setResults(data)
    })
    .catch(() => setResults([]))
    .finally(() => setLoading(false))
  }, 400)
  
  return () => clearTimeout(timer)
}, [query])

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
            <span style={{ position: 'absolute', left: '18px', top: '50%', transform: 'translateY(-50%)', fontSize: '18px', color: '#888' }}>🔍</span>
            <input
              type="text" value={query} onChange={e => setQuery(e.target.value)}
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

          {/* Recent Searches Section - Only shows when not searching */}
          {!query && (
            <>
              <div style={{ marginBottom: '2rem' }}>
                <h3 style={{ fontSize: '0.9rem', color: '#8C7861', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '1rem' }}>Recent Searches</h3>
                <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                  {recentSearches.map(item => (
                    <button key={item} onClick={() => setQuery(item)} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #E8D5B7', background: 'transparent', cursor: 'pointer', color: '#2C1A0E', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '12px' }}>🔍</span> {item}
                    </button>
                  ))}
                </div>
              </div>

              <div style={{ marginBottom: '3rem' }}>
                <h3 style={{ fontSize: '0.9rem', color: '#8C7861', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '1rem' }}>Popular Tags</h3>
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  {popularTags.map(tag => (
                    <span key={tag} style={{ padding: '6px 12px', background: '#EADBC8', borderRadius: '6px', fontSize: '0.85rem', color: '#5C4033', cursor: 'pointer' }}>
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            </>
          )}

          {/* Results Section */}
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
                }}
                onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-2px)'}
                onMouseLeave={e => e.currentTarget.style.transform = 'translateY(0)'}
                >
                  <div style={{ 
                    width: '80px', 
                    height: '80px', 
                    background: '#F0F0F0', 
                    borderRadius: '8px', 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center', 
                    fontSize: '32px', 
                    flexShrink: 0,
                    border: '1px solid #EEE'
                  }}>
                    {/* Using an emoji as a placeholder for the image circle in your ref */}
                    ☕
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: '700', fontSize: '1.1rem', color: '#2C1A0E', marginBottom: '4px' }}>{cafe.name}</div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                      <span style={{ color: '#D4A373' }}>★★★★☆</span>
                      <span style={{ fontSize: '0.85rem', color: '#888' }}>(128)</span>
                    </div>
                    <div style={{ fontSize: '0.9rem', color: '#8C7861' }}>{cafe.address.street}, {cafe.address.city}</div>
                  </div>
                </div>
              </Link>
            ))}
          </div>

          {!loading && query && results.length === 0 && (
            <div style={{ textAlign: 'center', color: '#6B3F1F', padding: '2rem' }}>No cafes found for "{query}"</div>
          )}
        </div>
      </div>
    </ProtectedRoute>
  )
}