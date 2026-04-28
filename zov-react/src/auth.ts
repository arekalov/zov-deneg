import createAuthStore from 'react-auth-kit/store/createAuthStore'

export type UserAuthState = {
  phone: string
  email: string
  firstName?: string
  lastName?: string
}

export const authStore = createAuthStore<UserAuthState>('localstorage', {
  authName: '_zov_auth'
})
