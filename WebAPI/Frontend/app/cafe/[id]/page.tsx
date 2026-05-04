'use client'
import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { Cafe, Review } from '@/lib/types'

export default function CafeDetailPage() {
  const { id } = useParams()
  const router = useRouter()
  const [cafe, setCafe] = useState<Cafe | null>(null)
  const [reviews, setReviews] = useState<Review[]>([])
  const [loading, setLoading] = useState(true)
  const [comment, setComment] = useState('')
  const [rating, setRating] = useState(5)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    Promise.all([
      api.get(`/api/Locations/${id}`),
      api.get(`/api/Review?cafeId=${id}`)
    ]).then(([cafeRes, reviewRes]) => {
      setCafe(cafeRes.data)
      setReviews(reviewRes.data)
    }).catch(console.error)
      .finally(() => setLoading(false))
  }, [id])

  const submitReview = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await api.post('/api/Review', { cafeId: id, rating, comment })
      const res = await api.get(`/api/Review?cafeId=${id}`)
      setReviews(res.data)
      setComment('')
      setRating(5)
    } catch { } finally { setSubmitting(false) }
  }

  return (
    <ProtectedRoute>
      <div style={{ minHeight: '100vh', background: '#F5EFE6' }}>
        <Navbar />
        {loading ? (
          <div style={{ textAlign: 'center', padding: '4rem', color: '#6B3F1F' }}>Loading...</div>
        ) : !cafe ? (
          <div style={{ textAlign: 'center', padding: '4rem', color: '#991B1B' }}>Cafe not found.</div>
        ) : (
          <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
            <button onClick={() => router.back()} style={{ background: 'none', border: 'none', color: '#6B3F1F', cursor: 'pointer', fontSize: '14px', marginBottom: '1rem', padding: 0 }}>← Back</button>

            <div style={{ background: '#E8D5B7', height: '240px', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '64px', marginBottom: '1.5rem' }}>☕</div>

            <h1 style={{ fontFamily: 'Playfair Display, serif', fontSize: '2rem', color: '#2C1A0E', marginBottom: '0.5rem' }}>{cafe.name}</h1>
            <p style={{ color: '#6B3F1F', marginBottom: '1rem' }}>📍 {cafe.address}, {cafe.city}</p>
            {cafe.description && <p style={{ color: '#2C1A0E', lineHeight: 1.7, marginBottom: '2rem' }}>{cafe.description}</p>}

            {cafe.drinks && cafe.drinks.length > 0 && (
              <div style={{ marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1rem' }}>Menu</h2>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {cafe.drinks.map(d => (
                    <span key={d.id} style={{ background: '#E8D5B7', color: '#6B3F1F', padding: '6px 14px', borderRadius: '20px', fontSize: '13px' }}>{d.name}</span>
                  ))}
                </div>
              </div>
            )}

            {cafe.paymentOptions && cafe.paymentOptions.length > 0 && (
              <div style={{ marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1rem' }}>Payment Options</h2>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {cafe.paymentOptions.map(p => (
                    <span key={p.id} style={{ background: '#fff', border: '1px solid #E8D5B7', color: '#2C1A0E', padding: '6px 14px', borderRadius: '20px', fontSize: '13px' }}>{p.name}</span>
                  ))}
                </div>
              </div>
            )}

            <div style={{ marginBottom: '2rem' }}>
              <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1rem' }}>Leave a Review</h2>
              <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '12px', padding: '1.5rem' }}>
                <form onSubmit={submitReview}>
                  <div style={{ marginBottom: '1rem' }}>
                    <label style={{ fontSize: '14px', fontWeight: 500, color: '#2C1A0E', display: 'block', marginBottom: '6px' }}>Rating</label>
                    <select value={rating} onChange={e => setRating(Number(e.target.value))}
                      style={{ padding: '8px 12px', border: '1px solid #E8D5B7', borderRadius: '8px', background: '#FDFAF7', fontSize: '14px' }}>
                      {[5, 4, 3, 2, 1].map(n => <option key={n} value={n}>{n} ★</option>)}
                    </select>
                  </div>
                  <div style={{ marginBottom: '1rem' }}>
                    <label style={{ fontSize: '14px', fontWeight: 500, color: '#2C1A0E', display: 'block', marginBottom: '6px' }}>Comment</label>
                    <textarea value={comment} onChange={e => setComment(e.target.value)} required rows={3}
                      placeholder="Share your experience..."
                      style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '14px', background: '#FDFAF7', resize: 'vertical', boxSizing: 'border-box' }} />
                  </div>
                  <button type="submit" disabled={submitting} style={{
                    background: '#2C1A0E', color: '#F5EFE6', border: 'none', padding: '10px 24px',
                    borderRadius: '8px', fontSize: '14px', fontWeight: 500, cursor: submitting ? 'not-allowed' : 'pointer'
                  }}>{submitting ? 'Submitting...' : 'Submit Review'}</button>
                </form>
              </div>
            </div>

            <div>
              <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1rem' }}>Reviews ({reviews.length})</h2>
              {reviews.length === 0 ? (
                <p style={{ color: '#6B3F1F', fontSize: '14px' }}>No reviews yet. Be the first!</p>
              ) : (
                reviews.map(r => (
                  <div key={r.id} style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '12px', padding: '1rem 1.25rem', marginBottom: '1rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                      <span style={{ fontWeight: 500, fontSize: '14px', color: '#2C1A0E' }}>{r.username || 'Anonymous'}</span>
                      <span style={{ color: '#C8854A', fontSize: '14px' }}>{'★'.repeat(r.rating)}</span>
                    </div>
                    <p style={{ fontSize: '14px', color: '#6B3F1F', lineHeight: 1.6 }}>{r.comment}</p>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </ProtectedRoute>
  )
}