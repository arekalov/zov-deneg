import React from 'react'
import { createRoot } from 'react-dom/client'
import AuthProvider from 'react-auth-kit'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App'
import { authStore } from './auth'


const queryClient = new QueryClient()

const container = document.getElementById('root')
const root = createRoot(container!)
root.render(
  // <React.StrictMode>
  <AuthProvider store={authStore} fallbackPath="/login">
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </AuthProvider>
  // </React.StrictMode>
)
