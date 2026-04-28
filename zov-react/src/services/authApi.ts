type LoginRequest = {
  phone: string
  password: string
}

type LoginResponse = {
  token: string
}

export async function loginWithPassword(payload: LoginRequest): Promise<LoginResponse> {
  await new Promise((resolve) => {
    setTimeout(resolve, 350)
  })

  return {
    token: `token-${payload.phone}-${Date.now()}`
  }
}
