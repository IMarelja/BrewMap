'use client'
import Link from 'next/link'
import Image from 'next/image'
import { logout, getUserFromToken } from '@/lib/auth'

export default function Navbar() {
  const user = getUserFromToken()
  const role = user?.role?.toLowerCase()

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
        <span style={{ fontFamily: 'Playfair Display, serif', fontWeight: 700, fontSize: '18px', color: '#2C1A0E' }}>
          BrewMap
        </span>
      </Link>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
        {role === 'admin' && (
          <Link href="/admin/flag" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px' }}>
            Flags
          </Link>
        )}

        {role === 'admin' && (
          <Link href="/admin/users" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px' }}>
            Users
          </Link>
        )}
        <Link href="/explore" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px' }}>
          Explore
        </Link>

        <Link href="/search" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px' }}>
          Search
        </Link>

        <Link href="/profile" style={{ color: '#6B3F1F', textDecoration: 'none', fontSize: '14px' }}>
          Profile
        </Link>

        <button onClick={handleLogout} style={{
          background: '#2C1A0E', color: '#F5EFE6', border: 'none',
          padding: '7px 16px', borderRadius: '8px', fontSize: '14px',
          cursor: 'pointer'
        }}>
          Logout
        </button>
      </div>
    </nav>
  )
}