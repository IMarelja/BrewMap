'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { getUser, isLoggedIn } from '@/lib/auth'

export default function AdminRoute({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const [ready, setReady] = useState(false)

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace('/login')
      return
    }

    const user = getUser()
    const role = user?.role?.toLowerCase()

    if (role !== 'admin') {
      router.replace('/explore')
      return
    }

    setReady(true)
  }, [])

  if (!ready) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#F5EFE6]">
        <p className="text-[#6B3F1F]">Loading...</p>
      </div>
    )
  }

  return <>{children}</>
}