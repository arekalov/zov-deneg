import { Preferences } from "@capacitor/preferences";

const AUTH_CREDENTIALS_KEY = "zov_auth_credentials";

export type AuthCredentials = {
  phone: string;
  email: string;
  password: string;
  pinCode: string;
  firstName?: string;
  lastName?: string;
};

export async function saveAuthCredentials(payload: AuthCredentials): Promise<void> {
  await Preferences.set({
    key: AUTH_CREDENTIALS_KEY,
    value: JSON.stringify(payload),
  });
}

export async function getAuthCredentials(): Promise<AuthCredentials | null> {
  const { value } = await Preferences.get({ key: AUTH_CREDENTIALS_KEY });

  if (!value) {
    return null;
  }

  try {
    return JSON.parse(value) as AuthCredentials;
  } catch {
    return null;
  }
}
