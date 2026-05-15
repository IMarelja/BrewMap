import type { Metadata } from 'next'
import './globals.css'


export const metadata: Metadata = {
  title: 'BrewMap',
  description: 'Discover your next favorite cafe',
  icons: {
    icon: '/logoo.png',
    apple: '/logoo.png',
  },
}

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        {children}
      </body>
    </html>
  )
}