'use client'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { Cafe } from '@/lib/types'

export default function ExplorePage() {
  const [cafes, setCafes] = useState<Cafe[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

 useEffect(() => {
    // We use a large radius (e.g., 50000 meters / 50km) to capture all local cafes
    api.get('/api/Locations/search', {
        params: {
            longitude: 15.8457503,
            latitude: 45.7976803,
            radiusMeters: 50000 // Increase this to find more locations
        }
    })
    .then(res => {
        // Based on the data you provided, the response is a direct array
        setCafes(res.data); 
    })
    .catch(() => setError('Failed to load cafes.'));
}, []);

  return (
    <ProtectedRoute>
      <div style={{ minHeight: '100vh', background: '#F5EFE6' }}>
        <Navbar />
        <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '2rem' }}>
          <div style={{ marginBottom: '2rem' }}>
            <h1 style={{ fontFamily: 'Playfair Display, serif', fontSize: '2rem', color: '#2C1A0E' }}>Explore Cafes</h1>
            <p style={{ color: '#6B3F1F', marginTop: '0.5rem' }}>Discover amazing coffee spots near you</p>
          </div>

          {loading && (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#6B3F1F' }}>Loading cafes...</div>
          )}

          {error && (
            <div style={{ background: '#FEE2E2', color: '#991B1B', padding: '1rem', borderRadius: '8px' }}>{error}</div>
          )}

          {!loading && !error && cafes.length === 0 && (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#6B3F1F' }}>No cafes found yet.</div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
            {cafes.map(cafe => (
              <Link key={cafe.id} href={`/cafe/${cafe.id}`} style={{ textDecoration: 'none' }}>
                <div style={{
                  background: '#fff', border: '1px solid #E8D5B7', borderRadius: '12px',
                  overflow: 'hidden', cursor: 'pointer', transition: 'transform 0.2s'
                }}
                  onMouseEnter={e => (e.currentTarget.style.transform = 'translateY(-4px)')}
                  onMouseLeave={e => (e.currentTarget.style.transform = 'translateY(0)')}
                >
                  <div style={{ height: '160px', background: '#E8D5B7', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '48px' }}>☕</div>
                  <div style={{ padding: '1.25rem' }}>
                    <h3 style={{ fontSize: '16px', fontWeight: 600, color: '#2C1A0E', marginBottom: '4px' }}>{cafe.name}</h3>
                    <p style={{ fontSize: '13px', color: '#6B3F1F', marginBottom: '8px' }}>{cafe.address.street}, {cafe.address.city}</p>
                    {cafe.description && (
                      <p style={{ fontSize: '13px', color: '#888', lineHeight: 1.5 }}>{cafe.description}</p>
                    )}
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