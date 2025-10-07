// OAuth2Success component - ENABLED
import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { CheckCircle, User, Mail, Shield, ArrowRight } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { getUserFromToken } from '../../utils/jwtUtils';
import api from '@/services/api.js';

/**
 * Handles successful OAuth2 authentication redirects from backend.
 * Processes JWT token and user data, then redirects to appropriate dashboard.
 */
const OAuth2Success = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { login } = useAuth();
    const [isProcessing, setIsProcessing] = useState(true);
    const [userData, setUserData] = useState(null);

    useEffect(() => {
        const processOAuth2Success = async () => {
            try {
                // Extract parameters from URL
                const token = searchParams.get('token');
                const userId = searchParams.get('userId');
                const email = searchParams.get('email');
                const name = searchParams.get('name');
                const provider = searchParams.get('provider');

                // Validate required parameters
                if (!token) {
                    throw new Error('Token không được cung cấp');
                }

                if (!email) {
                    throw new Error('Email không được cung cấp');
                }

                // ✅ Get role from JWT token instead of hardcoding
                const userFromToken = getUserFromToken(token);
                const roles = userFromToken?.roles || ['ROLE_USER'];

                // Prepare user data
                const user = {
                    id: userId,
                    email: email,
                    name: name,
                    provider: provider,
                    roles: roles, // ✅ Dynamic role from JWT token
                    isVerified: true // OAuth2 users are pre-verified
                };

                setUserData(user);

                // Store authentication data
                localStorage.setItem('accessToken', token);
                localStorage.setItem('user', JSON.stringify(user));
                localStorage.setItem('authProvider', provider || 'oauth2');

                // Set default API authorization header
                api.defaults.headers.common['Authorization'] = `Bearer ${token}`;

                // Update auth context - OAuth2 users are already authenticated
                // No need to call login() since we already have the token
                console.log('OAuth2 authentication completed successfully');

                toast.success(`Đăng nhập thành công với ${provider || 'OAuth2'}!`);

                // ✅ Redirect based on actual role from JWT token
                const redirectPath = roles.includes('ROLE_ADMIN') ? '/admin/dashboard' : '/user/dashboard';
                setTimeout(() => {
                    navigate(redirectPath, { replace: true });
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
    }, [searchParams, navigate, login]);

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
                            <svg className="animate-spin h-12 w-12 text-green-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
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
                                    <span className="ml-2 font-medium">{userData.name || 'Chưa cập nhật'}</span>
                                </div>
                                <div className="flex items-center text-sm">
                                    <Mail className="h-4 w-4 text-gray-400 mr-2" />
                                    <span className="text-gray-600">Email:</span>
                                    <span className="ml-2 font-medium">{userData.email}</span>
                                </div>
                                <div className="flex items-center text-sm">
                                    <Shield className="h-4 w-4 text-gray-400 mr-2" />
                                    <span className="text-gray-600">Đăng nhập bằng:</span>
                                    <span className="ml-2 font-medium capitalize">{userData.provider || 'OAuth2'}</span>
                                </div>
                                <div className="flex items-center text-sm">
                                    <Shield className="h-4 w-4 text-gray-400 mr-2" />
                                    <span className="text-gray-600">Quyền hạn:</span>
                                    <span className="ml-2 font-medium">
                                        {userData.roles?.includes('ROLE_ADMIN') ? 'Quản trị viên' : 'Người dùng'}
                                    </span>
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

