 
//
//     return (
//         <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
//             <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
//                 <div className="bg-green-600 py-4 px-6">
//                     <div className="flex items-center justify-between">
//                         <Link to="/" className="text-white hover:text-green-100">
//                             <ArrowLeft size={20} />
//                         </Link>
//                         <h2 className="text-center text-2xl font-extrabold text-white">Đăng nhập</h2>
//                         <div className="w-5"></div>
//                     </div>
//                 </div>
//
//                 <form onSubmit={handleSubmit} className="px-8 py-6">
//                     <div className="space-y-4">
//                         <div>
//                             <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
//                             <input
//                                 type="email"
//                                 name="email"
//                                 value={formData.email}
//                                 onChange={handleChange}
//                                 required
//                                 className="appearance-none block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
//                                 placeholder="Email của bạn"
//                             />
//                         </div>
//
//                         <div>
//                             <label className="block text-sm font-medium text-gray-700 mb-1">Mật khẩu</label>
//                             <div className="relative">
//                                 <input
//                                     type={showPassword ? 'text' : 'password'}
//                                     name="password"
//                                     value={formData.password}
//                                     onChange={handleChange}
//                                     required
//                                     className="appearance-none block w-full px-3 py-3 pr-12 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
//                                     placeholder="Mật khẩu"
//                                 />
//                                 <button
//                                     type="button"
//                                     onClick={() => setShowPassword(!showPassword)}
//                                     className="absolute inset-y-0 right-0 pr-3 flex items-center z-10"
//                                 >
//                                     {showPassword ? <EyeOff size={20} className="text-gray-500" /> : <Eye size={20} className="text-gray-500" />}
//                                 </button>
//                             </div>
//                         </div>
//                     </div>
//
//                     <div className="mt-6">
//                         <button
//                             type="submit"
//                             disabled={loading}
//                             className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200 disabled:opacity-50"
//                         >
//                             {loading ? (
//                                 <span className="flex items-center">
//                                     <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
//                                         <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
//                                         <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
//                                     </svg>
//                                     Đang xử lý...
//                                 </span>
//                             ) : (
//                                 <span className="flex items-center">
//                                     <LogIn size={16} className="mr-2" />
//                                     Đăng nhập
//                                 </span>
//                             )}
//                         </button>
//                     </div>
//
//                     <div className="mt-4 text-center">
//                         <span className="text-gray-600 text-sm">
//                             Chưa có tài khoản?{' '}
//                             <Link to="/register" className="font-medium text-green-600 hover:text-green-500">
//                                 <UserPlus size={16} className="inline mr-1" /> Đăng ký ngay
//                             </Link>
//                         </span>
//                     </div>
//
//                     <div className="mt-4 text-center">
//                         <Link to="/forgot-password" className="text-sm text-green-600 hover:text-green-500">Quên mật khẩu?</Link>
//                     </div>
//                 </form>
//             </div>
//         </div>
//     );
// };
//
// export default Login;
// src/pages/Login.jsx
import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import api from '@/services/api.js';
import { toast } from 'react-toastify';
import { mapApiError } from '@/utils/errorCodes.js';
import { LogIn, UserPlus, ArrowLeft, Eye, EyeOff, Mail } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { ensureDeviceIdentity } from '@/utils/device.js';

const Login = () => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();
    const auth = useAuth?.() || {};
    const { login: authLogin } = auth || {};

    const from = location.state?.from?.pathname || null;

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            if (from) {
                navigate(from, { replace: true });
            }
        }
    }, [navigate, from]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const normalizeRoles = (rolesOrRole) => {
        if (!rolesOrRole) return [];
        if (Array.isArray(rolesOrRole)) {
            return rolesOrRole.map(r => {
                if (typeof r === 'string') return r.toUpperCase();
                if (r && typeof r === 'object') {
                    return (r.name || r.role || r.code || '').toString().toUpperCase();
                }
                return '';
            }).filter(Boolean);
        }
        if (typeof rolesOrRole === 'string') return [rolesOrRole.toUpperCase()];
        if (typeof rolesOrRole === 'object') {
            return [(rolesOrRole.name || rolesOrRole.role || '').toString().toUpperCase()].filter(Boolean);
        }
        return [];
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await ensureDeviceIdentity();
            const payload = { email: formData.email?.trim().toLowerCase(), password: formData.password };
            const { data } = await api.post('/api/auth/login', payload);

            const accessToken = data.accessToken || data.access_token || data.token || data.accessToken;
            const refreshToken = data.refreshToken || data.refresh_token || data.refreshToken || null;
            const rolesRaw = data.roles ?? data.role ?? [];

            const userObj = {
                id: data.id ?? data.userId ?? null,
                email: data.email ?? data.user?.email ?? payload.email,
                roles: rolesRaw,
                isVerified: data.verified ?? data.isVerified ?? data.user?.verified ?? false,
            };

            if (accessToken) localStorage.setItem('accessToken', accessToken);
            if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('user', JSON.stringify(userObj));

            if (accessToken) {
                api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
            }

            if (typeof authLogin === 'function') {
                try {
                    const maybe = authLogin(accessToken, { roles: rolesRaw });
                    if (maybe && typeof maybe.then === 'function') await maybe;
                } catch (errToken) {
                    try {
                        const maybe2 = authLogin(userObj);
                        if (maybe2 && typeof maybe2.then === 'function') await maybe2;
                    } catch (errUser) {
                        console.warn('auth.login failed with token and user object', errToken, errUser);
                    }
                }
            }

            toast.success('Đăng nhập thành công!');

            if (from) {
                navigate(from, { replace: true });
                return;
            }

            const roles = normalizeRoles(userObj.roles);
            const isAdmin = roles.some(r => r.includes('ADMIN'));

            if (isAdmin) {
                navigate('/admin/dashboard', { replace: true });
            } else {
                navigate('/user/dashboard', { replace: true });
            }
        } catch (error) {
            console.error('Login error:', error);
            const errMsg = mapApiError(error) || 'Đăng nhập thất bại. Vui lòng thử lại.';
            toast.error(errMsg);
        } finally {
            setLoading(false);
        }
    };



    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                <div className="bg-green-600 py-4 px-6">
                    <div className="flex items-center justify-between">
                        <Link to="/" className="text-white hover:text-green-100">
                            <ArrowLeft size={20} />
                        </Link>
                        <h2 className="text-center text-2xl font-extrabold text-white">Đăng nhập</h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="px-8 py-6">
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                            <input
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                required
                                className="appearance-none block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                placeholder="Email của bạn"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Mật khẩu</label>
                            <div className="relative">
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    required
                                    className="appearance-none block w-full px-3 py-3 pr-12 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                    placeholder="Mật khẩu"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute inset-y-0 right-0 pr-3 flex items-center z-10"
                                >
                                    {showPassword ? <EyeOff size={20} className="text-gray-500" /> : <Eye size={20} className="text-gray-500" />}
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="mt-6">
                        <button
                            type="submit"
                            disabled={loading}
                            className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200 disabled:opacity-50"
                        >
                            {loading ? (
                                <span className="flex items-center">
                                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Đang xử lý...
                                </span>
                            ) : (
                                <span className="flex items-center">
                                    <LogIn size={16} className="mr-2" />
                                    Đăng nhập
                                </span>
                            )}
                        </button>
                    </div>

                    

                    <div className="mt-4 text-center">
                        <span className="text-gray-600 text-sm">
                            Chưa có tài khoản?{' '}
                            <Link to="/register" className="font-medium text-green-600 hover:text-green-500">
                                <UserPlus size={16} className="inline mr-1" /> Đăng ký ngay
                            </Link>
                        </span>
                    </div>

                    <div className="mt-4 text-center">
                        <Link to="/forgot-password" className="text-sm text-green-600 hover:text-green-500">Quên mật khẩu?</Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Login;