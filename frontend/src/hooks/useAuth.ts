import { useState } from "react";

export function useAuth() {
    const [authenticated] = useState(true);

    const [role] = useState<"STUDENT" | "CHAIR" | null>("STUDENT");

    return {
        authenticated,
        role,
        isStudent: role === "STUDENT",
        isChair: role === "CHAIR",
    };
}