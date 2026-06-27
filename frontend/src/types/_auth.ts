export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    authenticated: boolean;
    email: string;
    role: string | null;
}