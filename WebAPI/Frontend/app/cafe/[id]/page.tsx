'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { Cafe, Drink, Review } from '@/lib/types'
import { isAdmin } from '@/lib/auth'

export default function CafeDetailPage() {
  const { id } = useParams()
  const router = useRouter()

  const [cafe, setCafe] = useState<Cafe | null>(null)
  const [reviews, setReviews] = useState<Review[]>([])
  const [loading, setLoading] = useState(true)

  const [comment, setComment] = useState('')
  const [rating, setRating] = useState(5)
  const [submitting, setSubmitting] = useState(false)
  const [drinks, setDrinks] = useState<Drink[]>([])
const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  useEffect(() => {
    Promise.all([
      api.get(`/api/Locations/${id}`),
      api.get(`/api/Review/location/${id}`),
      api.get(`/api/Drink/location/${id}`)
    ])
      .then(([cafeRes, reviewRes, drinkRes]) => {
        setCafe(cafeRes.data)
        setReviews(reviewRes.data)
        setDrinks(drinkRes.data)
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [id])

  const submitReview = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)

    try {
      await api.post(`/api/Review/location/${id}`, {
        cafeId: id,
        rating,
        comment
      })

      const res = await api.get(`/api/Review/location/${id}`)
      setReviews(res.data)

      setComment('')
      setRating(5)
    } finally {
      setSubmitting(false)
    }
  }

  const showToast = (message: string, type: 'success' | 'error' = 'error') => {
    setToast({ message, type })

    setTimeout(() => {
      setToast(null)
    }, 3000)
  }

  const deleteReview = async (reviewId: string) => {
  const confirmed = window.confirm(
    'Are you sure you want to delete this review?'
  )

  if (!confirmed) return

  try {
    await api.delete(`/api/Review/${reviewId}`)

    setReviews(prev => prev.filter(r => r.id !== reviewId))
  } catch (err) {
    console.error(err)
    showToast('Failed to delete review', 'error')
  }
}

const deleteCafe = async () => {
  const confirmed = window.confirm(
    'Are you sure you want to delete this cafe?'
  )

  if (!confirmed) return

  try {
    await api.delete(`/api/Locations/${id}`)

    router.push('/search')
  } catch (err) {
    console.error(err)
    showToast('Failed to delete cafe.', 'error')
  }
}

  const formatDay = (day: string) =>
    day.charAt(0).toUpperCase() + day.slice(1)

  return (
    <ProtectedRoute>
      <div style={{ minHeight: '100vh', background: '#F5EFE6' }}>
        <Navbar />

        {toast && (
          <div
            style={{
              position: 'fixed',
              top: '20px',
              right: '20px',
              background: toast.type === 'success' ? '#166534' : '#991B1B',
              color: '#fff',
              padding: '10px 16px',
              borderRadius: '8px',
              zIndex: 9999,
              fontSize: '14px',
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
            }}
          >
            {toast.message}
          </div>
        )}

        {loading ? (
          <div style={{ textAlign: 'center', padding: '4rem', color: '#6B3F1F' }}>
            Loading...
          </div>
        ) : !cafe ? (
          <div style={{ textAlign: 'center', padding: '4rem', color: '#991B1B' }}>
            Cafe not found.
          </div>
        ) : (
          <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
            <button
              onClick={() => router.push(`/search`)}
              style={{
                background: 'none',
                border: 'none',
                color: '#6B3F1F',
                cursor: 'pointer',
                fontSize: '14px',
                marginBottom: '1rem',
                padding: 0
              }}
            >
              ← Back
            </button>

            {/* HEADER */}
            <div
              style={{
                background: '#E8D5B7',
                height: '240px',
                borderRadius: '12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '64px',
                marginBottom: '1.5rem'
              }}
            >
              ☕
            </div>

          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '1rem',
              gap: '1rem'
            }}
          >
            <h1
              style={{
                fontSize: '2rem',
                color: '#2C1A0E',
                margin: 0
              }}
            >
              {cafe.name}
            </h1>

            <div
              style={{
                display: 'flex',
                gap: '10px'
              }}
            >
              <button
                onClick={() =>
                  router.push(`/cafe/edit-cafe?locationId=${cafe.id}`)
                }
                style={{
                  background: '#4A2C19',
                  color: '#F5EFE6',
                  border: 'none',
                  padding: '8px 14px',
                  borderRadius: '8px',
                  fontSize: '13px',
                  cursor: 'pointer',
                  whiteSpace: 'nowrap'
                }}
              >
                Edit Cafe
              </button>

            {isAdmin() && (
              <button
                onClick={deleteCafe}
                style={{
                  background: '#991B1B',
                  color: '#fff',
                  border: 'none',
                  padding: '8px 14px',
                  borderRadius: '8px',
                  fontSize: '13px',
                  cursor: 'pointer',
                  whiteSpace: 'nowrap'
                }}
              >
                Delete Cafe
              </button>
            )}
            </div>
          </div>

            <p style={{ color: '#6B3F1F', marginBottom: '0.5rem' }}>
              📍 {cafe.address.street}, {cafe.address.city}
            </p>

            <p style={{ color: '#6B3F1F', marginBottom: '1rem' }}>
              🏷 Category: {cafe.categoryTag}
            </p>

            {cafe.contact?.website && (
              <div style={{ color: '#6B3F1F', marginBottom: '1rem' }}>
                <a
                  href={cafe.contact.website}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{
                    color: '#6B3F1F',
                    textDecoration: 'none',
                    fontSize: '14px',
                    fontWeight: 500
                  }}
                >
                  🔗 Visit Website
                </a>
              </div>
            )}

            {cafe.description && (
              <p style={{ color: '#2C1A0E', lineHeight: 1.7, marginBottom: '2rem' }}>
                {cafe.description}
              </p>
            )}

            <div style={{ marginBottom: '2rem' }}>
              <button
                onClick={() => router.push(`/drinks/create-drinks?locationId=${cafe.id}`)}
                style={{
                  background: '#4A2C19',
                  color: '#F5EFE6',
                  border: 'none',
                  padding: '10px 18px',
                  borderRadius: '8px',
                  fontSize: '14px',
                  cursor: 'pointer'
                }}
              >
                + Add Drink
              </button>
            </div>

{drinks.length > 0 && (
  <div style={{ marginBottom: '2rem' }}>
    <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '1rem' }}>
      Drinks
    </h2>

    <div
      style={{
        display: 'flex',
        overflowX: 'auto',
        gap: '12px',
        paddingBottom: '10px',
        scrollSnapType: 'x mandatory'
      }}
    >
      {drinks.map((drink, i) => (
        <div
          key={i}
          onClick={() => router.push(`/drinks/${drink.id}?locationId=${cafe.id}`)}
          style={{
            minWidth: '180px',
            background: '#fff',
            border: '1px solid #E8D5B7',
            borderRadius: '12px',
            padding: '12px',
            scrollSnapAlign: 'start',
            flexShrink: 0,
            cursor: 'pointer'
          }}
        >
          <div
            style={{
              fontWeight: 600,
              color: '#2C1A0E',
              marginBottom: '6px'
            }}
          >
            {drink.name}
          </div>

          <p style={{ fontSize: '12px', color: '#6B3F1F' }}>
            {drink.description?.slice(0, 60) || 'No description'}
          </p>

          <div style={{ marginTop: '10px' }}>
            <span
              style={{
                background: '#E8D5B7',
                padding: '4px 10px',
                borderRadius: '999px',
                fontSize: '11px',
                color: '#6B3F1F'
              }}
            >
              ☕ Drink
            </span>
          </div>
        </div>
      ))}
    </div>
  </div>
)}

            {cafe.drinks?.length > 0 && (
              <div style={{ marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 600 }}>Menu</h2>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {cafe.drinks.map((d, i) => (
                    <span
                      key={i}
                      style={{
                        background: '#E8D5B7',
                        color: '#6B3F1F',
                        padding: '6px 14px',
                        borderRadius: '20px',
                        fontSize: '13px'
                      }}
                    >
                      {d}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {cafe.paymentOptionTags?.length > 0 && (
              <div style={{ marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 600 }}>
                  Payment Options
                </h2>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {cafe.paymentOptionTags.map((p, i) => (
                    <span
                      key={i}
                      style={{
                        background: '#fff',
                        border: '1px solid #E8D5B7',
                        color: '#2C1A0E',
                        padding: '6px 14px',
                        borderRadius: '20px',
                        fontSize: '13px'
                      }}
                    >
                      {p}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {cafe.openingHours && (
              <div style={{ marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 600 }}>
                  Opening Hours
                </h2>

                <div
                  style={{
                    background: '#fff',
                    border: '1px solid #E8D5B7',
                    borderRadius: '12px',
                    padding: '1rem'
                  }}
                >
                  {Object.entries(cafe.openingHours).map(([day, h]) => (
                    <div
                      key={day}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        padding: '4px 0',
                        fontSize: '14px'
                      }}
                    >
                      <span style={{ color: '#2C1A0E' }}>
                        {formatDay(day)}
                      </span>
                      <span style={{ color: '#6B3F1F' }}>
                        {h.isClosed ? 'Closed' : `${h.open} - ${h.close}`}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div style={{ marginBottom: '2rem' }}> 
              <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#2C1A0E', marginBottom: '1rem' }}>
                Leave a Review
                </h2> 
                <div style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '12px', padding: '1.5rem' }}> 
                  <form onSubmit={submitReview}> 
                    <div style={{ marginBottom: '1rem' }}> 
                    <label style={{ fontSize: '14px', fontWeight: 500, color: '#2C1A0E', display: 'block', marginBottom: '6px' }}>
                    Rating
                    </label> 
                    <select value={rating} onChange={e => setRating(Number(e.target.value))} style={{ padding: '8px 12px', border: '1px solid #E8D5B7', borderRadius: '8px', background: '#FDFAF7', fontSize: '14px' }}>
                      {[5, 4, 3, 2, 1].map(n => <option key={n} value={n}>
                        {n} ★
                      </option>)} 
                      </select> 
                      </div> 
                      <div style={{ marginBottom: '1rem' }}> 
                        <label style={{ fontSize: '14px', fontWeight: 500, color: '#2C1A0E', display: 'block', marginBottom: '6px' }}>
                          Comment</label> 
                        <textarea value={comment} onChange={e => setComment(e.target.value)} required rows={3} placeholder="Share your experience..." style={{ width: '100%', padding: '10px 14px', border: '1px solid #E8D5B7', borderRadius: '8px', fontSize: '14px', background: '#FDFAF7', resize: 'vertical', boxSizing: 'border-box' }} /> 
                        </div>
                        <button type="submit" disabled={submitting} style={{ background: '#2C1A0E', color: '#F5EFE6', border: 'none', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: 500, cursor: submitting ? 'not-allowed' : 'pointer' }}>
                          {submitting ? 'Submitting...' : 'Submit Review'}
                          </button> 
                  </form> 
                </div> 
            </div>

            <div>
              <h2 style={{ fontSize: '18px', fontWeight: 600 }}>
                Reviews ({reviews.length})
              </h2>

              {reviews.length === 0 ? (
                <p>No reviews yet. Be the first!</p>
              ) : (
                reviews.map(r => (
                  <div
                    key={r.id}
                    style={{
                      background: '#fff',
                      border: '1px solid #E8D5B7',
                      borderRadius: '12px',
                      padding: '1rem',
                      marginBottom: '1rem'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>{r.username || 'Anonymous'}</span>
                      <span>{'★'.repeat(r.rating)}</span>
                    </div>
                    <p>{r.comment}</p>
                    {isAdmin() && (
                      <button
                        onClick={() => deleteReview(r.id)}
                        style={{
                          background: '#991B1B',
                          color: '#fff',
                          border: 'none',
                          padding: '6px 12px',
                          borderRadius: '8px',
                          fontSize: '12px',
                          cursor: 'pointer'
                        }}
                      >
                        Delete
                      </button>
                    )}
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