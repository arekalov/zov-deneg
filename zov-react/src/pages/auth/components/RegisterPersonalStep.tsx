import { FC } from 'react'
import { IonInput, IonNote } from '@ionic/react'

type RegisterPersonalStepProps = {
  values: {
    firstName: string
    lastName: string
    phone: string
    email: string
    password: string
  }
  stepError?: string
  setFieldValue: (field: string, value: string) => void
  setFieldTouched: (field: string, touched?: boolean) => void
}

export const RegisterPersonalStep: FC<RegisterPersonalStepProps> = ({
  values,
  stepError,
  setFieldValue,
  setFieldTouched
}) => {
  return (
    <>
      <IonInput
        className="ion-margin-top"
        value={values.firstName}
        label="Имя"
        labelPlacement="floating"
        color="success"
        fill="outline"
        onIonInput={(event) => setFieldValue('firstName', event.detail.value ?? '')}
        onIonBlur={() => setFieldTouched('firstName', true)}
      />

      <IonInput
        className="ion-margin-top"
        label="Фамилия"
        labelPlacement="floating"
        color="success"
        fill="outline"
        value={values.lastName}
        onIonInput={(event) => setFieldValue('lastName', event.detail.value ?? '')}
        onIonBlur={() => setFieldTouched('lastName', true)}
      />

      <IonInput
        className="ion-margin-top"
        label="Телефон"
        labelPlacement="floating"
        color="success"
        fill="outline"
        type="tel"
        value={values.phone}
        onIonInput={(event) => setFieldValue('phone', event.detail.value ?? '')}
        onIonBlur={() => setFieldTouched('phone', true)}
      />

      <IonInput
        className="ion-margin-top"
        label="Электронная почта"
        labelPlacement="floating"
        color="success"
        fill="outline"
        type="email"
        value={values.email}
        onIonInput={(event) => setFieldValue('email', event.detail.value ?? '')}
        onIonBlur={() => setFieldTouched('email', true)}
      />

      <IonInput
        className="ion-margin-top ion-margin-bottom"
        label="Пароль"
        labelPlacement="floating"
        color="success"
        fill="outline"
        type="password"
        value={values.password}
        onIonInput={(event) => setFieldValue('password', event.detail.value ?? '')}
        onIonBlur={() => setFieldTouched('password', true)}
      />
      {stepError && (
        <IonNote color="danger" className="ion-margin-top">
          {stepError}
        </IonNote>
      )}
    </>
  )
}
