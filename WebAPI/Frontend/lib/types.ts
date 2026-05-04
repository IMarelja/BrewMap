export interface User {
  id: string
  username: string
  email: string
  profilePicture?: string
}

export interface Cafe {
  id: string
  name: string
  description: string
  address: string
  city: string
  latitude: number
  longitude: number
  imageUrl?: string
  rating?: number
  categories?: Category[]
  drinks?: Drink[]
  paymentOptions?: PaymentOption[]
}

export interface Review {
  id: string
  userId: string
  cafeId: string
  rating: number
  comment: string
  createdAt: string
  username?: string
}

export interface Category {
  id: string
  name: string
}

export interface Drink {
  id: string
  name: string
  price?: number
}

export interface PaymentOption {
  id: string
  name: string
}

export interface AuthResponse {
  token: string
  user: User
}