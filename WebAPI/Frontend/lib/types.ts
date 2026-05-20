export interface User {
  id: string
  username: string
  email: string
  role?: string
  createdAt?: string
  profilePicture?: string
}

export interface Cafe {
  id: string
  name: string
  description: string
  address:{
    street: string;
    city:string;
    country:string;
    postalcode: string;
  };
  city: string
  latitude: number
  longitude: number
  categoryTag: string
  paymentOptionTags: string[]
  drinks: string[]
  openingHours: Record<string, OpeningHours>
  averageRating?: number
  totalReviews?: number
  createdAt?: string
  imageUrl?: string
}

export interface Address {
  street: string
  city: string
  country: string
  postalCode: string
}

export interface OpeningHours {
  open?: string
  close?: string
  isClosed: boolean

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

export interface Drink {
  id: string
  name: string
  price?: number
}

export interface Category {
  id: string
  tag: string
  name: string
}

export interface PaymentOption {
  id: string
  tag: string
  name: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface Flag {
  id: string
  reason: string
  description?: string
  status: string
  createdAt: string
  targetType: string
  targetId: string
}