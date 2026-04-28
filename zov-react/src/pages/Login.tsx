import { IonContent, IonLoading, IonNote, IonHeader, IonRouterLink, IonPage, IonTitle, IonToolbar } from '@ionic/react'
import { FC, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import useSignIn from 'react-auth-kit/hooks/useSignIn'
import { useHistory } from 'react-router-dom'
import { getAuthCredentials } from '../services/authCredentials'
import { UserAuthState } from '../auth'
import { loginWithPassword } from '../services/authApi'
import { PinKeyboard } from './auth/components/PinKeyboard'

export const Login: FC = () => {
  const signIn = useSignIn<UserAuthState>()
  const history = useHistory()
  const [pinCode, setPinCode] = useState('')

  const loginMutation = useMutation({
    mutationFn: async (enteredPinCode: string) => {
      const creds = await getAuthCredentials()

      if (!creds) {
        throw new Error('Пользователь не найден. Сначала пройдите регистрацию.')
      }

      if (creds.pinCode !== enteredPinCode) {
        throw new Error('Неверный PIN-код')
      }

      const loginResponse = await loginWithPassword({
        phone: creds.phone,
        password: creds.password
      })

      const isSignedIn = signIn({
        auth: { token: loginResponse.token, type: 'Bearer' },
        userState: {
          phone: creds.phone,
          email: creds.email,
          firstName: creds.firstName,
          lastName: creds.lastName
        }
      })

      if (!isSignedIn) {
        throw new Error('Не удалось создать сессию')
      }

      history.replace('/tab2')
      setPinCode('')
    }
  })

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar color="success">
          <IonTitle>Вход</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">
        <PinKeyboard
          value={pinCode}
          disabled={loginMutation.isPending}
          onChange={setPinCode}
          onComplete={(completedPinCode) => {
            loginMutation.mutate(completedPinCode)
          }}
        />

        {loginMutation.error && (
          <IonNote color="danger" className="ion-margin-top ion-display-block">
            {loginMutation.error.message}
          </IonNote>
        )}

        <IonRouterLink
          routerLink="/register"
          className="ion-margin-top ion-text-center ion-display-block"
          color="success"
        >
          Создать аккаунт
        </IonRouterLink>
        <IonLoading isOpen={loginMutation.isPending} message="Подождите..." />
      </IonContent>
    </IonPage>
  )
}
