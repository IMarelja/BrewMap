'use client'
import { useState, useEffect, useMemo, CSSProperties } from 'react'
import dynamic from 'next/dynamic'
import Navbar from '@/components/ui/navbar'
import Link from 'next/link'
import api from '@/lib/api'
import { Cafe } from '@/lib/types'
import { Map as MapIcon, List, LayoutGrid, Plus, Filter, X, Star, RotateCcw, Coffee } from 'lucide-react'

const MapComponent = dynamic(() => import('../../components/ui/ExploreMap'), { 
  ssr: false,
  loading: () => <div style={{ height: '70vh', background: '#eee', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '16px' }}>Loading Map...</div>
})

export default function ExplorePage() {
  const [view, setView] = useState<'map' | 'list' | 'grid'>('map')
  const [cafes, setCafes] = useState<Cafe[]>([])
  const [loading, setLoading] = useState(true)
  const [userLocation, setUserLocation] = useState<[number, number]>([45.8150, 15.9819]) 
  const [isFilterOpen, setIsFilterOpen] = useState(false)

  // --- Filter States ---
  const [minRating, setMinRating] = useState<number>(0)
  const [selectedPrices, setSelectedPrices] = useState<string[]>([]) // Array for multiple prices
  const [selectedCategories, setSelectedCategories] = useState<string[]>([])
  const [selectedDrinks, setSelectedDrinks] = useState<string[]>([]) 

  useEffect(() => {
    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition((pos) => {
        const lat = pos.coords.latitude
        const lng = pos.coords.longitude
        setUserLocation([lat, lng])
        fetchCafes(lat, lng)
      }, () => {
        fetchCafes(45.8150, 15.9819)
      })
    } else {
      fetchCafes(45.8150, 15.9819)
    }
  }, [])

  const fetchCafes = async (lat: number, lng: number) => {
    setLoading(true)
    try {
      const res = await api.get(`/api/Locations/search`, {
        params: { query: "", longitude: lng, latitude: lat, radiusMeters: 50000 }
      })
      const data = res.data.locations || (Array.isArray(res.data) ? res.data : [])
      setCafes(data)
    } catch (err) {
      console.error("Failed to fetch cafes", err)
      setCafes([])
    } finally {
      setLoading(false)
    }
  }

  // --- Flexible Filtering Logic ---
  const filteredCafes = useMemo(() => {
    return cafes.filter(cafe => {
      // 1. Rating (Min)
      if ((cafe.rating ?? 0) < minRating) return false

      // 2. Price Level (Multiple Selection - OR logic)
      if (selectedPrices.length > 0) {
        // Mapping string $ to numeric level if your API uses numbers, 
        // otherwise matching the string directly.
        const priceMap: Record<string, number> = { '$': 1, '$$': 2, '$$$': 3, '$$$$': 4 };
        const cafePrice = cafe.rating; // Adjust this if your property name is different
        const match = selectedPrices.some(p => priceMap[p] === cafePrice);
        if (!match) return false;
      }

      // 3. Categories/Amenities (Multiple Selection - AND logic: must have all selected)
      if (selectedCategories.length > 0) {
        const cafeCats = (cafe.categories || []).map(c => String(c).toLowerCase());
        const hasAll = selectedCategories.every(s => cafeCats.some(cat => cat.includes(s.toLowerCase())));
        if (!hasAll) return false
      }

      // 4. Drinks (Multiple Selection - OR logic: shows cafes that have ANY of the selected drinks)
      if (selectedDrinks.length > 0) {
        const cafeDrinks = (cafe.drinks || []).map(d => String(d).toLowerCase());
        const hasAny = selectedDrinks.some(s => cafeDrinks.some(drink => drink.includes(s.toLowerCase())));
        if (!hasAny) return false
      }

      return true
    })
  }, [cafes, minRating, selectedPrices, selectedCategories, selectedDrinks])

  const toggleArrayFilter = (item: string, state: string[], setState: (val: string[]) => void) => {
    setState(state.includes(item) ? state.filter(i => i !== item) : [...state, item])
  }

  const clearFilters = () => {
    setMinRating(0)
    setSelectedPrices([])
    setSelectedCategories([])
    setSelectedDrinks([])
  }

  return (
    <div style={{ background: '#F9F3E9', minHeight: '100vh', position: 'relative' }}>
      <Navbar />
      
      <div style={{ padding: '1.5rem 2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '800', color: '#2C1A0E', margin: 0 }}>Explore Cafes</h1>
          <p style={{ fontSize: '0.9rem', color: '#6B3F1F', marginTop: '4px' }}>
            📍 Zagreb • {loading ? '...' : filteredCafes.length} spots found
          </p>
        </div>
        
        <div style={{ display: 'flex', gap: '12px' }}>
          <Link href="/add-cafe" style={pinButtonStyle}>
            <Plus size={18} /> Pin a Cafe
          </Link>

          <button onClick={() => setIsFilterOpen(true)} style={filterButtonStyle}>
            <Filter size={18} /> Filters
            {(minRating > 0 || selectedPrices.length > 0 || selectedCategories.length > 0 || selectedDrinks.length > 0) && (
              <span style={badgeStyle}>
                {selectedPrices.length + selectedCategories.length + selectedDrinks.length + (minRating > 0 ? 1 : 0)}
              </span>
            )}
          </button>
          
          <div style={toggleContainerStyle}>
            <button onClick={() => setView('map')} style={{ ...viewBtnStyle, background: view === 'map' ? '#DCC8B0' : 'transparent' }}><MapIcon size={20} /></button>
            <button onClick={() => setView('list')} style={{ ...viewBtnStyle, background: view === 'list' ? '#DCC8B0' : 'transparent' }}><List size={20} /></button>
            <button onClick={() => setView('grid')} style={{ ...viewBtnStyle, background: view === 'grid' ? '#DCC8B0' : 'transparent' }}><LayoutGrid size={20} /></button>
          </div>
        </div>
      </div>

      <div style={{ padding: '0 2rem 2rem 2rem' }}>
        {loading ? (
           <div style={{ textAlign: 'center', padding: '3rem', color: '#6B3F1F' }}>Searching nearby...</div>
        ) : (
          <>
            {view === 'map' ? (
              <div style={{ borderRadius: '16px', overflow: 'hidden', border: '1px solid #EADBC8', height: '70vh' }}>
                <MapComponent center={userLocation} cafes={filteredCafes} />
              </div>
            ) : (
              <div style={view === 'list' ? { display: 'flex', flexDirection: 'column', gap: '1rem' } : { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
                {filteredCafes.map(cafe => (
                  <div key={cafe.id} style={view === 'list' ? listViewCardStyle : gridViewCardStyle}>
                    <img src={cafe.imageUrl || "/placeholder.png"} alt={cafe.name} style={view === 'list' ? { width: '120px', height: '120px', borderRadius: '12px', objectFit: 'cover' } : { width: '100%', height: '200px', objectFit: 'cover' }} />
                    <div style={{ padding: view === 'list' ? '0' : '1rem', flex: 1 }}>
                      <h3 style={{ margin: '0 0 4px 0', color: '#2C1A0E' }}>{cafe.name}</h3>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#B08968', marginBottom: '8px' }}>
                        <Star size={14} fill="#B08968" /> <span>{cafe.rating || 'N/A'}</span>
                      </div>
                      <p style={{ margin: 0, fontSize: '0.85rem', color: '#6B3F1F' }}>{cafe.address?.street || cafe.city}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>

      {/* Filter Sidebar */}
      {isFilterOpen && (
        <>
          <div onClick={() => setIsFilterOpen(false)} style={overlayStyle} />
          <div style={sidebarStyle}>
            <div style={sidebarHeaderStyle}>
              <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Filters</h2>
              <button onClick={clearFilters} style={{ background: 'none', border: 'none', color: '#6B3F1F', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <RotateCcw size={14} /> Clear all
              </button>
              <X cursor="pointer" onClick={() => setIsFilterOpen(false)} />
            </div>
            
            <div style={{ flex: 1, overflowY: 'auto', padding: '1.5rem' }}>
              {/* Rating */}
              <section style={filterSectionStyle}>
                <p style={filterLabelStyle}>Minimum Rating: {minRating > 0 ? `${minRating}+` : 'Any'}</p>
                <input type="range" min="0" max="5" step="0.5" value={minRating} onChange={(e) => setMinRating(Number(e.target.value))} style={{ width: '100%', accentColor: '#5C3A21' }} />
              </section>

              {/* Price Level (Multi-select) */}
              <section style={filterSectionStyle}>
                <p style={filterLabelStyle}>Price Range</p>
                <div style={{ display: 'flex', gap: '8px' }}>
                  {['$', '$$', '$$$', '$$$$'].map(p => (
                    <button 
                      key={p} 
                      onClick={() => toggleArrayFilter(p, selectedPrices, setSelectedPrices)}
                      style={priceBtnStyle(selectedPrices.includes(p))}
                    >
                      {p}
                    </button>
                  ))}
                </div>
              </section>

              {/* Drinks Selection (Multi-select) */}
              <section style={filterSectionStyle}>
                <p style={filterLabelStyle}><Coffee size={16} style={{display: 'inline', marginRight: '8px', verticalAlign: 'middle'}}/> Drinks</p>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                  {['Espresso', 'Matcha', 'Cold Brew', 'Specialty Tea', 'Juice', 'Chai'].map(d => (
                    <label key={d} style={checkboxLabelStyle}>
                      <input type="checkbox" checked={selectedDrinks.includes(d)} onChange={() => toggleArrayFilter(d, selectedDrinks, setSelectedDrinks)} /> {d}
                    </label>
                  ))}
                </div>
              </section>

              {/* Amenities */}
              <section style={filterSectionStyle}>
                <p style={filterLabelStyle}>Amenities</p>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                  {['WiFi', 'Outdoor', 'Parking', 'Pets', 'Power'].map(a => (
                    <label key={a} style={checkboxLabelStyle}>
                      <input type="checkbox" checked={selectedCategories.includes(a)} onChange={() => toggleArrayFilter(a, selectedCategories, setSelectedCategories)} /> {a}
                    </label>
                  ))}
                </div>
              </section>
            </div>

            <div style={{ padding: '1.5rem', borderTop: '1px solid #EADBC8' }}>
              <button onClick={() => setIsFilterOpen(false)} style={applyBtnStyle}>
                Show {filteredCafes.length} results
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

// Styles
const pinButtonStyle: CSSProperties = { background: '#5C3A21', color: '#F9F3E9', padding: '10px 20px', borderRadius: '12px', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '600' }
const filterButtonStyle: CSSProperties = { background: '#FFF', border: '1px solid #EADBC8', borderRadius: '12px', padding: '10px 16px', display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', color: '#2C1A0E', position: 'relative' }
const badgeStyle: CSSProperties = { position: 'absolute', top: '-5px', right: '-5px', background: '#B08968', color: 'white', borderRadius: '50%', width: '18px', height: '18px', fontSize: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center' }
const toggleContainerStyle: CSSProperties = { background: '#EADBC8', borderRadius: '14px', padding: '4px', display: 'flex', gap: '4px' }
const viewBtnStyle: CSSProperties = { border: 'none', padding: '8px 12px', borderRadius: '10px', cursor: 'pointer', display: 'flex', alignItems: 'center' }
const listViewCardStyle: CSSProperties = { display: 'flex', gap: '1.5rem', background: '#FFF', padding: '1rem', borderRadius: '16px', border: '1px solid #EADBC8', alignItems: 'center' }
const gridViewCardStyle: CSSProperties = { background: '#FFF', borderRadius: '16px', border: '1px solid #EADBC8', overflow: 'hidden', display: 'flex', flexDirection: 'column' }
const overlayStyle: CSSProperties = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.3)', zIndex: 2000 }
const sidebarStyle: CSSProperties = { position: 'fixed', right: 0, top: 0, bottom: 0, width: '350px', background: '#F9F3E9', zIndex: 2001, display: 'flex', flexDirection: 'column', boxShadow: '-5px 0 15px rgba(0,0,0,0.1)' }
const sidebarHeaderStyle: CSSProperties = { padding: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #EADBC8' }
const filterSectionStyle: CSSProperties = { marginBottom: '2rem' }
const filterLabelStyle: CSSProperties = { fontWeight: '700', marginBottom: '12px', display: 'block', color: '#2C1A0E' }
const checkboxLabelStyle: CSSProperties = { display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px', fontSize: '0.9rem', cursor: 'pointer', color: '#6B3F1F' }
const priceBtnStyle = (active: boolean): CSSProperties => ({ padding: '10px 18px', borderRadius: '8px', border: '1px solid #EADBC8', background: active ? '#5C3A21' : '#FFF', color: active ? '#FFF' : '#5C3A21', fontWeight: 'bold', cursor: 'pointer', transition: '0.2s' })
const applyBtnStyle: CSSProperties = { background: '#5C3A21', color: '#FFF', width: '100%', padding: '1.25rem', borderRadius: '12px', border: 'none', fontWeight: 'bold', cursor: 'pointer', fontSize: '1rem' }