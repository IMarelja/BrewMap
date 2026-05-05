'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import dynamic from 'next/dynamic'
import api from '@/lib/api'
import Navbar from '@/components/ui/navbar'

// We'll build this component next - it needs to handle the click-to-pin logic
const PinMap = dynamic(() => import('../../components/ui/PinMap'), { ssr: false })

export default function AddCafePage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    address: {
      street: '',
      city: 'Zagreb',
      country: 'Croatia',
      postalCode: ''
    },
    latitude: 45.8150, // Default center
    longitude: 15.9819,
    categoryTag: 'Cafe',
    paymentOptionTags: [] as string[],
    contact: { website: '' },
    openingHours: {} // We can start simple and expand this
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      await api.post('/api/Locations', formData)
      alert('Cafe added successfully!')
      router.push('/explore') // Go back to map
    } catch (error) {
      console.error("Submission failed", error)
      alert('Error adding cafe. Check console.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ background: '#F9F3E9', minHeight: '100vh', paddingBottom: '4rem' }}>
      <Navbar />
      <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
        
        <h1 style={{ fontFamily: 'Playfair Display, serif', fontSize: '2.5rem', color: '#2C1A0E' }}>Add a New Location</h1>
        <p style={{ color: '#6B3F1F', marginBottom: '2rem' }}>Help the community by adding a cafe you love</p>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          
          {/* Basic Info Box */}
          <section style={cardStyle}>
            <h3 style={sectionTitle}>Basic Information</h3>
            <label style={labelStyle}>Cafe Name</label>
            <input 
              style={inputStyle} 
              placeholder="e.g., The Roasted Bean"
              onChange={e => setFormData({...formData, name: e.target.value})}
              required
            />
            
            <label style={labelStyle}>Street Address</label>
            <input 
              style={inputStyle} 
              placeholder="e.g., Ilica 10"
              onChange={e => setFormData({
                ...formData, 
                address: {...formData.address, street: e.target.value}
              })}
              required
            />

            <label style={labelStyle}>Description</label>
            <textarea 
              style={{...inputStyle, height: '100px'}} 
              placeholder="Describe the atmosphere..."
              onChange={e => setFormData({...formData, description: e.target.value})}
            />
          </section>

          {/* Map Pinning Box */}
          <section style={cardStyle}>
            <h3 style={sectionTitle}>📍 Pin Location</h3>
            <p style={{ fontSize: '0.9rem', color: '#6B3F1F', marginBottom: '1rem' }}>
              Click on the map to pin exactly where this cafe is located
            </p>
            <div style={{ height: '300px', borderRadius: '12px', overflow: 'hidden', border: '1px solid #E8D5B7' }}>
              <PinMap 
                onLocationSelect={(lat, lng) => setFormData({...formData, latitude: lat, longitude: lng})} 
              />
            </div>
            <div style={{ marginTop: '10px', fontSize: '0.8rem', color: '#8C7861' }}>
              Selected: {formData.latitude.toFixed(4)}, {formData.longitude.toFixed(4)}
            </div>
          </section>

          {/* Payment Options (referencing image_035139.png) */}
          <section style={cardStyle}>
            <h3 style={sectionTitle}>Payment Options</h3>
            <div style={{ display: 'flex', gap: '10px' }}>
              {['Cash', 'Card', 'Mobile Pay'].map(opt => (
                <button
                  key={opt}
                  type="button"
                  onClick={() => {
                    const tags = formData.paymentOptionTags.includes(opt)
                      ? formData.paymentOptionTags.filter(t => t !== opt)
                      : [...formData.paymentOptionTags, opt]
                    setFormData({...formData, paymentOptionTags: tags})
                  }}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '8px',
                    border: '1px solid #E8D5B7',
                    background: formData.paymentOptionTags.includes(opt) ? '#4A2C19' : '#FFF',
                    color: formData.paymentOptionTags.includes(opt) ? '#FFF' : '#2C1A0E',
                    cursor: 'pointer'
                  }}
                >
                  {opt}
                </button>
              ))}
            </div>
          </section>

          <div style={{ display: 'flex', gap: '1rem' }}>
             <button type="button" onClick={() => router.back()} style={secondaryBtn}>Cancel</button>
             <button type="submit" disabled={loading} style={primaryBtn}>
               {loading ? 'Adding...' : '+ Add Location'}
             </button>
          </div>
        </form>
      </div>
    </div>
  )
}

// Styles to match your UI
const cardStyle = { background: '#FFF', padding: '2rem', borderRadius: '16px', border: '1px solid #E8D5B7' }
const sectionTitle = { fontSize: '1.1rem', fontWeight: '700', marginBottom: '1.5rem', color: '#2C1A0E' }
const labelStyle = { display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '0.9rem' }
const inputStyle = { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #E8D5B7', marginBottom: '1.5rem', outline: 'none' }
const primaryBtn = { flex: 2, padding: '16px', background: '#4A2C19', color: '#FFF', border: 'none', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer' }
const secondaryBtn = { flex: 1, padding: '16px', background: '#FFF', border: '1px solid #E8D5B7', borderRadius: '12px', cursor: 'pointer' }