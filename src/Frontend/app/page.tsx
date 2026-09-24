'use client'
import Link from 'next/link'
import Image from 'next/image'
import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { isLoggedIn } from '@/lib/auth'

export default function LandingPage() {
  
  const router = useRouter()

  useEffect(() => {

    if (isLoggedIn()){
      router.replace('/explore')
    }
  }, [])

  return (
    <main style={{ minHeight: '100vh', background: '#F5EFE6' }}>
      <nav style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '1rem 2rem', borderBottom: '1px solid #E8D5B7', background: '#F5EFE6'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Image src="/logo.png" alt="BrewMap" width={36} height={36} />
          <span style={{ fontFamily: 'Playfair Display, serif', fontWeight: 700, fontSize: '20px', color: '#2C1A0E' }}>BrewMap</span>
        </div>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <Link href="/login" style={{ color: '#2C1A0E', textDecoration: 'none', fontSize: '15px' }}>Sign in</Link>
          <Link href="/register" style={{ background: '#2C1A0E', color: '#F5EFE6', padding: '8px 20px', borderRadius: '8px', textDecoration: 'none', fontSize: '15px', fontWeight: 500 }}>Get Started</Link>
        </div>
      </nav>

      <section style={{ textAlign: 'center', padding: '6rem 2rem 4rem' }}>
        <div style={{ display: 'inline-block', background: '#E8D5B7', color: '#6B3F1F', padding: '4px 14px', borderRadius: '20px', fontSize: '13px', marginBottom: '1.5rem' }}>
          ☕ For coffee lovers, by coffee lovers
        </div>
        <h1 style={{ fontFamily: 'Playfair Display, serif', fontSize: '3.5rem', color: '#2C1A0E', lineHeight: 1.2, marginBottom: '1.5rem' }}>
          Discover your<br />next favorite cafe
        </h1>
        <p style={{ fontSize: '18px', color: '#6B3F1F', maxWidth: '500px', margin: '0 auto 2.5rem' }}>
          Join our community of coffee enthusiasts. Find the perfect spot for your morning brew, remote work, or catching up with friends.
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link href="/register" style={{ background: '#2C1A0E', color: '#F5EFE6', padding: '14px 32px', borderRadius: '8px', textDecoration: 'none', fontSize: '16px', fontWeight: 500 }}>Start Exploring →</Link>
          <Link href="/login" style={{ background: 'transparent', color: '#2C1A0E', padding: '14px 32px', borderRadius: '8px', textDecoration: 'none', fontSize: '16px', border: '1.5px solid #2C1A0E' }}>Sign In</Link>
        </div>
      </section>

      <section style={{ display: 'flex', justifyContent: 'center', gap: '4rem', padding: '3rem 2rem', borderTop: '1px solid #E8D5B7', borderBottom: '1px solid #E8D5B7', flexWrap: 'wrap' }}>
        {[['500+', 'Cafes'], ['10,000+', 'Reviews'], ['2,000+', 'Members'], ['50+', 'Cities']].map(([num, label]) => (
          <div key={label} style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 600, color: '#2C1A0E' }}>{num}</div>
            <div style={{ fontSize: '14px', color: '#6B3F1F' }}>{label}</div>
          </div>
        ))}
      </section>

      <section style={{ padding: '5rem 2rem', textAlign: 'center' }}>
        <h2 style={{ fontFamily: 'Playfair Display, serif', fontSize: '2rem', color: '#2C1A0E', marginBottom: '0.5rem' }}>Everything you need to find great coffee</h2>
        <p style={{ color: '#6B3F1F', marginBottom: '3rem' }}>Simple tools for discovering and sharing amazing cafe experiences</p>
        <div style={{ display: 'flex', flexDirection:'row', flexWrap:'nowrap', gap:'20px',justifyContent: 'center', alignItems:'stretch',width:'100%', padding:'20px' }}>
          {[
            { icon: '🗺️', title: 'Interactive Map', desc: 'Find cafes near you with our live map view' },
            { icon: '⭐', title: 'Honest Reviews', desc: 'Read and write reviews from real coffee lovers' },
            { icon: '🔍', title: 'Smart Search', desc: 'Filter by vibe, drinks, WiFi, and more' },
            { icon: '📍', title: 'Add a Cafe', desc: 'Help the community by adding new spots' },
          ].map(f => (
            <div key={f.title} style={{ background: '#fff', border: '1px solid #E8D5B7', borderRadius: '12px', padding: '2rem 1.5rem', textAlign: 'left' }}>
              <div style={{ fontSize: '28px', marginBottom: '1rem' }}>{f.icon}</div>
              <div style={{ fontWeight: 600, fontSize: '20px', color: '#2C1A0E', marginBottom: '0.5rem' }}>{f.title}</div>
              <div style={{ fontSize: '17px', color: '#6B3F1F', lineHeight: 1.6 }}>{f.desc}</div>
            </div>
          ))}
        </div>
      </section>

      <footer style={{ textAlign: 'center', padding: '2rem', borderTop: '1px solid #E8D5B7', color: '#6B3F1F', fontSize: '14px' }}>
        © 2025 BrewMap. Made for coffee lovers.
      </footer>
    </main>
  )
}