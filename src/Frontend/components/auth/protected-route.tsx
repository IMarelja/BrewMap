'use client'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { isLoggedIn } from '@/lib/auth'

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const [ready, setReady] = useState(false)

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace('/login')
    } else {
      setReady(true)
    }
  }, [])

  if (!ready) return (
    <div className="min-h-screen flex items-center justify-center" style={{ background: '#F5EFE6' }}>
      <div className="text-brew-brown text-lg">Loading...</div>
    </div>
  )

  return <>{children}</>
}