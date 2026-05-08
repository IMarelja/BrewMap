'use client'
import Link from 'next/link'
import Image from 'next/image'
import { useRouter } from 'next/navigation'
import { logout, getUser } from '@/lib/auth'

export default function Navbar() {
  const router = useRouter()
  const user = getUser()

  const handleLogout = () => {
    logout()
  }

  return (
    <nav style={{
      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
      padding: '0.75rem 2rem', background: '#fff', borderBottom: '1px solid #E8D5B7',
      position: 'sticky', top: 0, zIndex: 100
    }}>
      <Link href="/explore" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '8px' }}>
        <Image src="/logo.png" alt="BrewMap" width={32} height={32} />
        <span style={{ fontFamily: 'Playfair Display, serif', fontWeight: 700, fontSize: '18px', color: '#2C1A0E' }}>BrewMap</span>
      </Link>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
        <Link href="/explore" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px', fontWeight: 500 }}>Explore</Link>
        <Link href="/search" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px', fontWeight: 500 }}>Search</Link>
        <Link href="/admin" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px', fontWeight: 500 }}>Admin</Link>
        <Link href="/profile" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px', fontWeight: 500 }}>
          {user?.username || 'Profile'}
        </Link>
        <button onClick={handleLogout} style={{
          background: '#2C1A0E', color: '#F5EFE6', border: 'none', padding: '7px 16px',
          borderRadius: '8px', fontSize: '14px', cursor: 'pointer', fontWeight: 500
        }}>Logout</button>
      </div>
    </nav>
  )
}