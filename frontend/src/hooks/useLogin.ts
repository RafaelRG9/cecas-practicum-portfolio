import { useState } from "react";
import { useNavigate } from "react-router-dom";
import authService from "../services/authService";

const delay = (ms: number) =>
  new Promise((resolve) => setTimeout(resolve, ms));

export function useLogin() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    setError("");
    setSuccess(false);

    if (!email.trim()) {
      setError("Email is required.");
      return;
    }

    if (!password.trim()) {
      setError("Password is required.");
      return;
    }

    setLoading(true);

    try {
      const response = await authService.login({
        email,
        password,
      });

      setSuccess(true);
      await delay(1000);

      switch (response.role) {
        case "STUDENT":
          navigate("/student");
          break;

        case "CHAIR":
          navigate("/chair");
          break;

        default:
          navigate("/");
      }
    } catch (err) {
      setSuccess(false);

      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("An unexpected error occurred.");
      }
    } finally {
      setLoading(false);
    }
  }

  return {
    email,
    password,
    loading,
    error,
    success,
    setEmail,
    setPassword,
    handleSubmit,
  };
}