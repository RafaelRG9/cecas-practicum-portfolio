import type { LoginRequest, LoginResponse } from "../types/auth";

export async function login(
    request: LoginRequest
): Promise<LoginResponse> {
    console.log("Login request:", request);

    // simulate
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 50% success
    if (Math.random() > 0.5) {
        return {
            authenticated: true,
            email: request.email,
            role: "STUDENT",
        };
    }
    throw new Error("Invalid email or password.");
}


// import type { LoginRequest, LoginResponse } from "../types/auth";

// export async function login(
//   request: LoginRequest
// ): Promise<LoginResponse> {
//   const response = await fetch("/api/auth/login", {
//     method: "POST",
//     headers: {
//       "Content-Type": "application/json",
//     },
//     body: JSON.stringify(request),
//   });

//   if (!response.ok) {
//     throw new Error("Invalid email or password.");
//   }

//   return response.json();
// }