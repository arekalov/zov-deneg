import { FC } from 'react'
import { IonButton, IonCol, IonGrid, IonIcon, IonRow } from '@ionic/react'
import { ellipse, ellipseOutline, backspaceOutline } from 'ionicons/icons'

type PinKeyboardProps = {
  value: string
  onChange: (nextValue: string) => void
  maxLength?: number
  disabled?: boolean
  onComplete?: (pin: string) => void
}

const DIGITS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', 'backspace', '0', 'OK'] as const

export const PinKeyboard: FC<PinKeyboardProps> = ({ value, onChange, maxLength = 4, disabled = false, onComplete }) => {
  const handleDigit = (digit: string) => {
    if (disabled || value.length >= maxLength) {
      return
    }

    const nextValue = `${value}${digit}`
    onChange(nextValue)
  }

  const handleBackspace = () => {
    if (disabled || value.length === 0) {
      return
    }

    onChange(value.slice(0, -1))
  }

  return (
    <div>
      <div className="ion-text-center ioc-space-2 ion-margin-bottom">
        {Array.from({ length: maxLength }).map((_, index) => (
          <IonIcon color="success" size="small" icon={index < value.length ? ellipse : ellipseOutline} />
        ))}
      </div>
      <IonGrid>
        {Array.from({ length: 4 }).map((_, rowIndex) => (
          <IonRow key={`row-${rowIndex}`}>
            {DIGITS.slice(rowIndex * 3, rowIndex * 3 + 3).map((key) => (
              <IonCol key={key || `empty-${rowIndex}`} size="4">
                <IonButton
                  expand="block"
                  fill="solid"
                  color={key === 'OK' ? 'success' : 'light'}
                  disabled={disabled}
                  onClick={() => {
                    if (key === 'backspace') {
                      handleBackspace()
                      return
                    }
                    if (key === 'OK') {
                      onComplete?.(value)
                      return
                    }
                    handleDigit(key)
                  }}
                >
                  {key === 'backspace' ? <IonIcon icon={backspaceOutline} /> : key}
                </IonButton>
              </IonCol>
            ))}
          </IonRow>
        ))}
      </IonGrid>
    </div>
  )
}
