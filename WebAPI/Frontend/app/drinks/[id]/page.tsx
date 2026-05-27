'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter, useSearchParams } from 'next/navigation'

import ProtectedRoute from '@/components/auth/protected-route'
import Navbar from '@/components/ui/navbar'
import api from '@/lib/api'
import { isAdmin } from '@/lib/auth'

export default function DrinkDetailPage() {
  const { id } = useParams()
  const router = useRouter()
  const searchParams = useSearchParams()

  const locationId = searchParams.get('locationId')

  const [drink, setDrink] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [reviews, setReviews] = useState<any[]>([])

const [comment, setComment] = useState('')
const [rating, setRating] = useState(5)
const [submitting, setSubmitting] = useState(false)

const [error, setError] = useState('')
const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  useEffect(() => {
  if (!id) return

  Promise.all([
    api.get(`/api/Drink/${id}`),
    api.get(`/api/Review/drink/${id}`)
  ])
    .then(([drinkRes, reviewRes]) => {
      setDrink(drinkRes.data)
      setReviews(reviewRes.data)
    })
    .catch(console.error)
    .finally(() => setLoading(false))
}, [id])

const submitReview = async (e: React.FormEvent) => {
  e.preventDefault()

  setSubmitting(true)

  try {
    await api.post(`/api/Review/drink/${id}`, {
      rating,
      comment
    })

    const res = await api.get(`/api/Review/drink/${id}`)

    setReviews(res.data)

    setComment('')
    setRating(5)
  } catch (err) {
    console.error(err)
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

const deleteDrink = async () => {
  const confirmed = window.confirm(
    'Are you sure you want to delete this drink?'
  )

  if (!confirmed) return

  try {
    await api.delete(`/api/Drink/${id}`)

    router.push(`/cafe/${locationId}`)
  } catch (err) {
    console.error(err)
    showToast('Failed to delete drink', 'error')
  }
}

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
          <div
            style={{
              textAlign: 'center',
              padding: '4rem',
              color: '#6B3F1F'
            }}
          >
            Loading...
          </div>
        ) : !drink ? (
          <div
            style={{
              textAlign: 'center',
              padding: '4rem',
              color: '#991B1B'
            }}
          >
            Drink not found.
          </div>
        ) : (
          <div
            style={{
              maxWidth: '800px',
              margin: '0 auto',
              padding: '2rem'
            }}
          >
            {/* BACK BUTTON */}
            <button
              onClick={() => router.push(`/cafe/${locationId}`)}
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
              ← Back to Cafe
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
              🥤
            </div>

            {/* TITLE */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                gap: '1rem',
                marginBottom: '1rem'
              }}
            >
              <h1
                style={{
                  fontSize: '2rem',
                  color: '#2C1A0E',
                  margin: 0
                }}
              >
                {drink.name}
              </h1>

                <div
                style={{
                    display: 'flex',
                    gap: '10px',
                    alignItems: 'center'
                }}
                >
                <button
                    onClick={() =>
                    router.push(
                        `/drinks/edit-drinks?drinkId=${drink.id}&locationId=${locationId}`
                    )
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
                    Edit Drink
                </button>
                    {isAdmin() && (
                <button
                    onClick={deleteDrink}
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
                    Delete Drink
                </button>
                    )}
                </div>
            </div>

            {/* DESCRIPTION */}
            {drink.description && (
              <div
                style={{
                  background: '#fff',
                  border: '1px solid #E8D5B7',
                  borderRadius: '12px',
                  padding: '1.5rem',
                  marginBottom: '2rem'
                }}
              >
                <h2
                  style={{
                    fontSize: '18px',
                    fontWeight: 600,
                    marginBottom: '1rem',
                    color: '#2C1A0E'
                  }}
                >
                  Description
                </h2>

                <p
                  style={{
                    color: '#2C1A0E',
                    lineHeight: 1.7,
                    margin: 0
                  }}
                >
                  {drink.description}
                </p>
              </div>
            )}

    {/* OPTIONAL TAGS */}
    {drink.tags?.length > 0 && (
      <div style={{ marginBottom: '2rem' }}>
        <h2
          style={{
            fontSize: '18px',
            fontWeight: 600,
            marginBottom: '1rem'
          }}
        >
          Tags
        </h2>

        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: '8px'
          }}
        >
          {drink.tags.map((tag: string, i: number) => (
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
              {tag}
            </span>
          ))}
        </div>
      </div>
    )}

    {/* REVIEW FORM */}
    <div style={{ marginBottom: '2rem' }}>
      <h2
        style={{
          fontSize: '18px',
          fontWeight: 600,
          color: '#2C1A0E',
          marginBottom: '1rem'
        }}
      >
        Leave a Review
      </h2>

      <div
        style={{
          background: '#fff',
          border: '1px solid #E8D5B7',
          borderRadius: '12px',
          padding: '1.5rem'
        }}
      >
        <form onSubmit={submitReview}>
          <div style={{ marginBottom: '1rem' }}>
            <label
              style={{
                fontSize: '14px',
                fontWeight: 500,
                color: '#2C1A0E',
                display: 'block',
                marginBottom: '6px'
              }}
            >
              Rating
            </label>

            <select
              value={rating}
              onChange={e => setRating(Number(e.target.value))}
              style={{
                padding: '8px 12px',
                border: '1px solid #E8D5B7',
                borderRadius: '8px',
                background: '#FDFAF7',
                fontSize: '14px'
              }}
            >
              {[5, 4, 3, 2, 1].map(n => (
                <option key={n} value={n}>
                  {n} ★
                </option>
              ))}
            </select>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label
              style={{
                fontSize: '14px',
                fontWeight: 500,
                color: '#2C1A0E',
                display: 'block',
                marginBottom: '6px'
              }}
            >
              Comment
            </label>

            <textarea
              value={comment}
              onChange={e => setComment(e.target.value)}
              required
              rows={3}
              placeholder="Share your thoughts..."
              style={{
                width: '100%',
                padding: '10px 14px',
                border: '1px solid #E8D5B7',
                borderRadius: '8px',
                fontSize: '14px',
                background: '#FDFAF7',
                resize: 'vertical',
                boxSizing: 'border-box'
              }}
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            style={{
              background: '#2C1A0E',
              color: '#F5EFE6',
              border: 'none',
              padding: '10px 24px',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 500,
              cursor: submitting ? 'not-allowed' : 'pointer'
            }}
          >
            {submitting ? 'Submitting...' : 'Submit Review'}
          </button>
        </form>
      </div>
    </div>

    {/* REVIEWS */}
    <div>
      <h2
        style={{
          fontSize: '18px',
          fontWeight: 600
        }}
      >
        Reviews ({reviews.length})
      </h2>

      {reviews.length === 0 ? (
        <p>No reviews yet. Be the first!</p>
      ) : (
        reviews.map((r: any) => (
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
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between'
              }}
            >
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