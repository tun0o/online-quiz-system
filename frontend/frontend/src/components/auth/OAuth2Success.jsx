import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { CheckCircle, User, Mail, Shield, ArrowRight, Loader2 } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import api from '@/services/api.js';

/**
 * Handles successful OAuth2 authentication redirects from backend.
 * Processes JWT token and user data, then redirects to appropriate dashboard.
 */
const OAuth2Success = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { loginWithTokens } = useAuth();
    const [isProcessing, setIsProcessing] = useState(true);
    const [userData, setUserData] = useState(null);

    useEffect(() => {
        const processOAuth2Success = async () => {
            try {
                // Extract parameters from URL
                const accessToken = searchParams.get('accessToken');
                const refreshToken = searchParams.get('refreshToken');
                const userId = searchParams.get('userId');
                const email = searchParams.get('email');
                const name = searchParams.get('name');
                const provider = searchParams.get('provider');
                const grade = searchParams.get('grade');
                const goal = searchParams.get('goal');
                const createdAt = searchParams.get('createdAt');
                const rolesParam = searchParams.get('roles');
                const verifiedParam = searchParams.get('verified');

                // Validate required parameters
                if (!accessToken || !refreshToken) {
                    throw new Error('Token xác thực không được cung cấp');
                }

                if (!email) {
                    throw new Error('Email không được cung cấp');
                }

                // Prepare user data
                const user = {
                    id: userId ? parseInt(userId, 10) : null,
                    email: email, // No need to decode, not encoded
                    provider: provider, // No need to decode, not encoded
                    grade: grade ? decodeURIComponent(grade) : null,
                    goal: goal ? decodeURIComponent(goal) : null,
                    createdAt: createdAt || null,
                    name: name ? decodeURIComponent(name) : email.split('@')[0], // Fallback name
                    roles: rolesParam ? JSON.parse(decodeURIComponent(rolesParam)) : ['ROLE_USER'],
                    verified: verifiedParam === 'true'
                };

                setUserData(user);

                // Update auth context
                loginWithTokens(accessToken, refreshToken, user);

                toast.success(`Đăng nhập thành công với ${searchParams.get('provider') || 'OAuth2'}!`);

                // Redirect based on role (for now, all OAuth2 users go to user dashboard)
                setTimeout(() => {
                    const isAdmin = user.roles.some(r => r.includes('ADMIN'));
                    navigate(isAdmin ? '/admin' : '/', { replace: true });
                }, 2000);

            } catch (error) {
                console.error('OAuth2 success processing error:', error);
                toast.error(error.message || 'Có lỗi xảy ra khi xử lý đăng nhập');
                setTimeout(() => {
                    navigate('/login', { replace: true });
                }, 3000);
            } finally {
                setIsProcessing(false);
            }
        };

        processOAuth2Success();
    }, [searchParams, navigate, loginWithTokens]);

    if (isProcessing) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
                <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                    <div className="bg-green-600 py-4 px-6">
                        <h2 className="text-center text-2xl font-extrabold text-white">
                            Đang xử lý đăng nhập
                        </h2>
                    </div>

                    <div className="px-8 py-6 text-center">
                        <div className="flex justify-center mb-4">
                            <Loader2 className="animate-spin h-12 w-12 text-green-600" />
                        </div>
                        <p className="text-gray-600">Đang xử lý thông tin đăng nhập...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                <div className="bg-green-600 py-4 px-6">
                    <h2 className="text-center text-2xl font-extrabold text-white">
                        Đăng nhập thành công
                    </h2>
                </div>

                <div className="px-8 py-6">
                    <div className="text-center mb-6">
                        <CheckCircle className="mx-auto h-12 w-12 text-green-500 mb-4" />
                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                            Chào mừng bạn đến với hệ thống!
                        </h3>
                        <p className="text-gray-600">
                            Bạn sẽ được chuyển đến trang chính trong giây lát
                        </p>
                    </div>

                    {userData && (
                        <div className="bg-gray-50 rounded-lg p-4 mb-6">
                            <h4 className="text-sm font-medium text-gray-900 mb-3">Thông tin tài khoản</h4>
                            <div className="space-y-2">
                                <div className="flex items-center text-sm">
                                    <User className="h-4 w-4 text-gray-400 mr-2" />
                                    <span className="text-gray-600">Tên:</span>
                                    <span className="ml-2 font-medium text-gray-900">{userData.name || 'Chưa cập nhật'}</span>
                                </div>
                                <div className="flex items-center text-sm">
                                    <Mail className="h-4 w-4 text-gray-400 mr-2" />
                                    <span className="text-gray-600">Email:</span>
                                    <span className="ml-2 font-medium text-gray-900">{userData.email}</span>
                                </div>
                                <div className="flex items-center text-sm">
                                    <Shield className="h-4 w-4 text-gray-400 mr-2" />
                                    <span className="text-gray-600">Đăng nhập bằng:</span>
                                    <span className="ml-2 font-medium capitalize text-gray-900">{searchParams.get('provider') || 'OAuth2'}</span>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="flex items-center justify-center text-sm text-gray-500">
                        <ArrowRight className="h-4 w-4 mr-1 animate-pulse" />
                        Đang chuyển hướng...
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OAuth2Success;
