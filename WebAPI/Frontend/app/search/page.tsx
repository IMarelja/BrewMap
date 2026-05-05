'use client'
import { useState, useEffect } from 'react'
import Link from 'next/link'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { Cafe } from '@/lib/types'

export default function SearchPage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Cafe[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!query.trim()) { setResults([]); return }
    const timer = setTimeout(() => {
      setLoading(true)
      api.get(`/api/Locations?search=${encodeURIComponent(query)}`)
        .then(res => setResults(res.data))
        .catch(() => setResults([]))
        .finally(() => setLoading(false))
    }, 400)
    return () => clearTimeout(timer)
  }, [query])

  return (
    <ProtectedRoute>
      <div style={{ minHeight: '100vh', background: '#F5EFE6' }}>
        <Navbar />
        <div style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem' }}>
          <h1 style={{ fontFamily: 'Playfair Display, serif', fontSize: '2rem', color: '#2C1A0E', marginBottom: '1.5rem' }}>Search Cafes</h1>

          <div style={{ position: 'relative', marginBottom: '2rem' }}>
            <span style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px' }}>🔍</span>
            <input
              type="text" value={query} onChange={e => setQuery(e.target.value)}
              placeholder="Search by name, city, or vibe..."
              style={{ width: '100%', padding: '14px 14px 14px 42px', border: '1.5px solid #E8D5B7', borderRadius: '12px', fontSize: '16px', background: '#fff', outline: 'none', boxSizing: 'border-box' }}
            />
          </div>

          {loading && <div style={{ textAlign: 'center', color: '#6B3F1F', padding: '2rem' }}>Searching...</div>}

          {!loading && query && results.length === 0 && (
            <div style={{ textAlign: 'center', color: '#6B3F1F', padding: '2rem' }}>No cafes found for "{query}"</div>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {results.map(cafe => (
              <Link key={cafe.id} href={`/cafe/${cafe.id}`} style={{ textDecoration: 'none' }}>
                <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '12px', padding: '1.25rem', display: 'flex', gap: '1rem', alignItems: 'center' }}
                  onMouseEnter={e => (e.currentTarget.style.background = '#FDFAF7')}
                  onMouseLeave={e => (e.currentTarget.style.background = '#fff')}>
                  <div style={{ width: '56px', height: '56px', background: '#E8D5B7', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '28px', flexShrink: 0 }}>☕</div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '15px', color: '#2C1A0E' }}>{cafe.name}</div>
                    <div style={{ fontSize: '13px', color: '#6B3F1F', marginTop: '2px' }}>{cafe.address.street}, {cafe.address.city}</div>
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