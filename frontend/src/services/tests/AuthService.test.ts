import { describe, it, expect, beforeEach, vi } from 'vitest';
import authService from '../AuthService';
import csrfService from '../CsrfService';

vi.mock('../CsrfService', () => ({
    default: {
        init: vi.fn(),
        fetch: vi.fn(),
    },
}));

describe('AuthService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('registers user successfully', async () => {
        const user = {
            authenticated: true,
            email: 'newstudent@test.edu',
            role: 'STUDENT',
            mustChangePassword: false,
        };

        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response(JSON.stringify(user), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            })
        );

        const result = await authService.register({
            fullName: 'New Student',
            email: 'newstudent@test.edu',
            password: 'password123',
            program: 'Computer Science',
            studentId: 123456,
        });

        expect(result).toEqual(user);
    });

    it('logs in successfully', async () => {
        const user = {
            authenticated: true,
            email: 'student@test.edu',
            role: 'STUDENT',
            mustChangePassword: false,
        };

        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response(JSON.stringify(user), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            })
        );

        const result = await authService.login({
            email: 'student@test.edu',
            password: 'password123',
        });

        expect(result).toEqual(user);
    });

    it('throws on login failure', async () => {
        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response(null, { status: 401 })
        );

        await expect(
            authService.login({
                email: 'bad@test.edu',
                password: 'wrong',
            })
        ).rejects.toThrow('Invalid email or password.')
    });

    it('logs out successfully', async () => {
        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response(null, { status: 200 })
        );

        await authService.logout();

        expect(csrfService.fetch).toHaveBeenCalledWith(
            '/api/auth/logout',
            expect.objectContaining({
                method: 'POST',
            })
        );
    });

    it('throws on logout failure', async () => {
        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response(null, { status: 500 })
        );

        await expect(authService.logout()).rejects.toThrow(
            'Logout failed (500)'
        );
    });

    it('fetches current user', async () => {
        const user = {
            authenticated: true,
            email: 'student@test.edu',
            role: 'STUDENT',
            mustChangePassword: false,
        };

        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response(JSON.stringify(user), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            })
        );

        const result = await authService.fetchCurrentUser();

        expect(result.email).toBe('student@test.edu');
    });

    it('changes password successfully', async () => {
        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response('Password updated', {
                status: 200,
            })
        );

        const result = await authService.changePassword({
            currentPassword: 'oldpassword',
            newPassword: 'newpassword',
            confirmPassword: 'newpassword',
        });

        expect(result).toBe('Password updated');
    });

    it('throws on change password failure', async () => {
        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response('Invalid password', {
                status: 400,
            })
        );

        await expect(
            authService.changePassword({
                currentPassword: 'oldpassword',
                newPassword: 'bad',
                confirmPassword: 'bad',
            })
        ).rejects.toThrow('Change password failed (400): Invalid password');
    });

    it('force changes password successfully', async () => {
        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response('Password force updated', {
                status: 200,
            })
        );

        const result = await authService.forceChangePassword({
            currentPassword: 'oldpassword',
            newPassword: 'newpassword',
            confirmPassword: 'newpassword',
        });

        expect(result).toBe('Password force updated');
    });

    it('throws on force change password failure', async () => {
        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response('Forbidden', {
                status: 403,
            })
        );

        await expect(
            authService.forceChangePassword({
                currentPassword: 'oldpassword',
                newPassword: 'newpassword',
                confirmPassword: 'newpassword',
            })
        ).rejects.toThrow('Force change password failed (403): Forbidden');
    });

    it('preserves mustChangePassword when current user requires a password change', async () => {
        const user = {
            authenticated: true,
            email: 'chair@test.edu',
            role: 'CHAIR',
            mustChangePassword: true,
        }

        vi.mocked(csrfService.fetch).mockResolvedValue(
            new Response(JSON.stringify(user), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            })
        )

        const result = await authService.fetchCurrentUser()

        expect(result).toEqual(user)
        expect(result.mustChangePassword).toBe(true)
    });
});