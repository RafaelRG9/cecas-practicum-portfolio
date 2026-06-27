export type RegisterRequest = {
  fullName: string;
  email: string;
  password: string;
  program: string;
  studentId: number;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type CurrentUserResponse = {
  authenticated: boolean;
  email: string | null;
  role: string | null;
};

export type ChangePasswordRequest = {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
};