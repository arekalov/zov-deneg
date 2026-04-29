import {
  IonButton,
  IonCard,
  IonCardContent,
  IonContent,
  IonHeader,
  IonIcon,
  IonList,
  IonLoading,
  IonNote,
  IonPage,
  IonProgressBar,
  IonRouterLink,
  IonText,
  IonTitle,
  IonToolbar
} from '@ionic/react'
import { FC, useMemo, useState } from 'react'
import { useFormik } from 'formik'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { useHistory } from 'react-router-dom'
import { saveAuthCredentials } from '../services/authCredentials'
import { RegisterPersonalStep } from './auth/components/RegisterPersonalStep'
import { RegisterPinStep } from './auth/components/RegisterPinStep'
import { arrowBack } from 'ionicons/icons'

export const Register: FC = () => {
  const history = useHistory()
  const [step, setStep] = useState(0)

  const steps = useMemo(() => ['Личные данные', 'Введите PIN-код', 'Повторите PIN-код'], [])

  const registerSchema = z
    .object({
      firstName: z.string().min(2, 'Введите имя'),
      lastName: z.string().min(2, 'Введите фамилию'),
      phone: z.string().min(6, 'Введите корректный номер телефона'),
      email: z.string().email('Введите корректный email'),
      password: z.string().min(8, 'Пароль должен содержать минимум 8 символов'),
      pinCode: z.string().length(4, 'PIN-код должен содержать 4 цифры'),
      pinCodeConfirmation: z.string().length(4, 'PIN-код должен содержать 4 цифры')
    })
    .refine((values) => values.pinCode === values.pinCodeConfirmation, {
      message: 'PIN-коды не совпадают',
      path: ['pinCodeConfirmation']
    })

  const registerMutation = useMutation({
    mutationFn: async (values: {
      firstName: string
      lastName: string
      phone: string
      email: string
      password: string
      pinCode: string
    }) => {
      await saveAuthCredentials(values)
    },
    onSuccess: () => {
      history.replace('/login')
    }
  })

  const formik = useFormik({
    initialValues: {
      firstName: '',
      lastName: '',
      phone: '',
      email: '',
      password: '',
      pinCode: '',
      pinCodeConfirmation: ''
    },
    validate: (values) => {
      const result = registerSchema.safeParse(values)
      if (result.success) {
        return {}
      }

      return result.error.issues.reduce<Record<string, string>>((acc, issue) => {
        const field = issue.path[0]
        if (typeof field === 'string') {
          acc[field] = issue.message
        }
        return acc
      }, {})
    },
    onSubmit: async (values) => {
      await registerMutation.mutateAsync(values)
    }
  })

  const firstStepFields: Array<keyof typeof formik.values> = ['firstName', 'lastName', 'phone', 'email', 'password']
  const hasFirstStepError = firstStepFields.some((field) => formik.touched[field] && Boolean(formik.errors[field]))

  const goNext = async () => {
    const stepFields: Record<number, Array<keyof typeof formik.values>> = {
      0: ['firstName', 'lastName', 'phone', 'email', 'password'],
      1: ['pinCode'],
      2: ['pinCodeConfirmation']
    }

    const currentFields = stepFields[step]

    for (const field of currentFields) {
      await formik.setFieldTouched(field, true)
    }

    const errors = await formik.validateForm()
    const hasCurrentStepErrors = currentFields.some((field) => errors[field])
    if (hasCurrentStepErrors) {
      return
    }

    setStep((current) => Math.min(current + 1, steps.length - 1))
  }

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar className="ion-display-flex ion-align-items-center">
          {step > 0 ? (
            <IonButton fill="clear" onClick={() => setStep(step - 1)}>
              <IonIcon icon={arrowBack} />
            </IonButton>
          ) : null}
          <IonTitle>Регистрация</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="ion-padding">
        <IonCard>
          <IonCardContent>
            <IonNote className="ion-display-block">
              <h2 className="ion-text-bold">{steps[step]}</h2>
              Шаг {step + 1} из {steps.length}
            </IonNote>
            <IonProgressBar color="success" className="ion-margin-top" value={(step + 1) / steps.length} />

            <form onSubmit={formik.handleSubmit}>
              <IonList lines="none" className="ion-margin-top">
                {step === 0 ? (
                  <RegisterPersonalStep
                    values={{
                      firstName: formik.values.firstName,
                      lastName: formik.values.lastName,
                      phone: formik.values.phone,
                      email: formik.values.email,
                      password: formik.values.password
                    }}
                    stepError={hasFirstStepError ? 'Проверьте корректность полей первого шага' : undefined}
                    setFieldValue={(field, value) => {
                      formik.setFieldValue(field, value)
                    }}
                    setFieldTouched={(field, touched) => {
                      formik.setFieldTouched(field, touched)
                    }}
                  />
                ) : null}

                {step === 1 ? (
                  <RegisterPinStep
                    title="Введите PIN-код"
                    value={formik.values.pinCode}
                    touched={formik.touched.pinCode}
                    error={formik.errors.pinCode}
                    disabled={registerMutation.isPending}
                    onChange={(nextValue) => {
                      formik.setFieldValue('pinCode', nextValue.replace(/\D/g, '').slice(0, 4))
                    }}
                    onComplete={goNext}
                  />
                ) : null}

                {step === 2 ? (
                  <RegisterPinStep
                    title="Повторите PIN-код"
                    value={formik.values.pinCodeConfirmation}
                    touched={formik.touched.pinCodeConfirmation}
                    error={formik.errors.pinCodeConfirmation}
                    disabled={registerMutation.isPending}
                    onChange={(nextValue) => {
                      formik.setFieldValue('pinCodeConfirmation', nextValue.replace(/\D/g, '').slice(0, 4))
                    }}
                    onComplete={goNext}
                  />
                ) : null}
              </IonList>

              <div className="ion-margin-top ion-align-items-center ion-display-flex ion-gap-x-2">
                {step < steps.length - 1 ? (
                  <IonButton color="success" shape="round" onClick={goNext}>
                    Далее
                  </IonButton>
                ) : (
                  <IonButton type="submit" disabled={registerMutation.isPending}>
                    Завершить регистрацию
                  </IonButton>
                )}

                {step === 0 && (
                  <IonText>
                    Уже есть аккаунт?{' '}
                    <IonRouterLink routerLink="/login" color="success">
                      Войти
                    </IonRouterLink>
                  </IonText>
                )}
              </div>
            </form>
          </IonCardContent>
        </IonCard>
        <IonLoading isOpen={registerMutation.isPending} message="Сохраняем..." />
      </IonContent>
    </IonPage>
  )
}
