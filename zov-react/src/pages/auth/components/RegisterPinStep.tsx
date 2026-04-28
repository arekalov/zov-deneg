import { FC } from 'react'
import { IonNote } from '@ionic/react'
import { PinKeyboard } from './PinKeyboard'

type RegisterPinStepProps = {
  title: string
  value: string
  error?: string
  touched?: boolean
  onChange: (nextValue: string) => void
  disabled?: boolean
  onComplete?: () => void
}

export const RegisterPinStep: FC<RegisterPinStepProps> = ({
  title,
  value,
  error,
  touched,
  onChange,
  disabled = false,
  onComplete
}) => {
  return (
    <>
      <IonNote className="ion-display-block ion-text-center ion-margin-bottom">{title}</IonNote>
      <PinKeyboard value={value} onChange={onChange} disabled={disabled} onComplete={onComplete} />
      {touched && error && <IonNote color="danger">{error}</IonNote>}
    </>
  )
}
