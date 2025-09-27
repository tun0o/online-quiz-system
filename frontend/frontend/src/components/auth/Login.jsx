// // src/pages/Login.jsx
// import { useState, useEffect } from 'react';
// import { Link, useNavigate, useLocation } from 'react-router-dom';
// import api from '@/services/api.js';
// import { toast } from 'react-toastify';
// import { LogIn, UserPlus, ArrowLeft, Eye, EyeOff } from 'lucide-react';
// import { useAuth } from '@/hooks/useAuth'; // nếu hook có vị trí khác, chỉnh đường dẫn tương ứng
//
// const Login = () => {
//     const [formData, setFormData] = useState({ email: '', password: '' });
//     const [showPassword, setShowPassword] = useState(false);
//     const [loading, setLoading] = useState(false);
//     const navigate = useNavigate();
//     const location = useLocation();
//     const auth = useAuth?.() || {}; // phòng trường hợp hook trả undefined
//     const { login: authLogin } = auth || {};
//
//     // nếu user tới từ 1 route bảo vệ thì lưu đường dẫn đó để redirect sau login
//     const from = location.state?.from?.pathname || null;
//
//     useEffect(() => {
//         const token = localStorage.getItem('accessToken');
//         if (token) {
//             if (from) {
//                 navigate(from, { replace: true });
//             }
//         }
//     }, [navigate, from]);
//
//     const handleChange = (e) => {
//         setFormData({ ...formData, [e.target.name]: e.target.value });
//     };
//
//     // Helper: chuẩn hóa roles thành mảng chuỗi UPPERCASE
//     const normalizeRoles = (rolesOrRole) => {
//         if (!rolesOrRole) return [];
//         if (Array.isArray(rolesOrRole)) {
//             return rolesOrRole.map(r => {
//                 if (typeof r === 'string') return r.toUpperCase();
//                 if (r && typeof r === 'object') {
//                     return (r.name || r.role || r.code || '').toString().toUpperCase();
//                 }
//                 return '';
//             }).filter(Boolean);
//         }
//         if (typeof rolesOrRole === 'string') return [rolesOrRole.toUpperCase()];
//         if (typeof rolesOrRole === 'object') {
//             return [(rolesOrRole.name || rolesOrRole.role || '').toString().toUpperCase()].filter(Boolean);
//         }
//         return [];
//     };
//
//     const handleSubmit = async (e) => {
//         e.preventDefault();
//         setLoading(true);
//         try {
//             const payload = { email: formData.email?.trim().toLowerCase(), password: formData.password };
//             const { data } = await api.post('/api/auth/login', payload);
//
//             // Tương thích nhiều tên trường token/roles mà backend có thể trả
//             const accessToken = data.accessToken || data.access_token || data.token || data.accessToken;
//             const refreshToken = data.refreshToken || data.refresh_token || data.refreshToken || null;
//             const rolesRaw = data.roles ?? data.role ?? [];
//
//             // Chuẩn bị đối tượng user lưu vào localStorage / context fallback
//             const userObj = {
//                 id: data.id ?? data.userId ?? null,
//                 email: data.email ?? data.user?.email ?? payload.email,
//                 roles: rolesRaw,
//                 isVerified: data.verified ?? data.isVerified ?? data.user?.verified ?? false,
//                 // thêm trường nếu backend trả
//             };
//
//             // Lưu token + user (dự phòng — nếu AuthContext xử lý token thì nó sẽ ghi đè / dùng lại)
//             if (accessToken) localStorage.setItem('accessToken', accessToken);
//             if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
//             localStorage.setItem('user', JSON.stringify(userObj));
//
//             // Set header mặc định để requests sau có auth
//             if (accessToken) {
//                 api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
//             }
//
//             // Nếu có auth context với hàm login thì gọi để cập nhật state toàn cục
//             if (typeof authLogin === 'function') {
//                 // Thử gọi theo nhiều cách để tương thích:
//                 // 1) authLogin(accessToken, { roles })
//                 // 2) authLogin(accessToken)
//                 // 3) authLogin(userObj)
//                 // 4) authLogin({ email, password })  <-- less likely here (we already logged in)
//                 try {
//                     // ưu tiên token (nhiều AuthContext mong token)
//                     const maybe = authLogin(accessToken, { roles: rolesRaw });
//                     if (maybe && typeof maybe.then === 'function') await maybe;
//                 } catch (errToken) {
//                     // nếu gọi bằng token thất bại, thử truyền userObj
//                     try {
//                         const maybe2 = authLogin(userObj);
//                         if (maybe2 && typeof maybe2.then === 'function') await maybe2;
//                     } catch (errUser) {
//                         // không block nếu auth context khác giao diện
//                         console.warn('auth.login failed with token and user object', errToken, errUser);
//                     }
//                 }
//             }
//
//             toast.success('Đăng nhập thành công!');
//
//             // Quy tắc điều hướng:
//             // 1) Nếu có from (người dùng bị redirect tới login từ route bảo vệ) -> về from
//             // 2) Nếu không, điều hướng theo role: ADMIN -> /admin, else -> /dashboard
//             if (from) {
//                 navigate(from, { replace: true });
//                 return;
//             }
//
//             const roles = normalizeRoles(userObj.roles);
//             const isAdmin = roles.some(r => r.includes('ADMIN'));
//
//             if (isAdmin) {
//                 navigate('/admin/dashboard', { replace: true });
//             } else {
//                 navigate('/user/dashboard', { replace: true });
//             }
//         } catch (error) {
//             console.error('Login error:', error);
//             const errMsg = error.response?.data?.error
//                 || (error.response?.data?.errors ? Object.values(error.response.data.errors || {}).join(', ') : null)
//                 || 'Đăng nhập thất bại. Vui lòng thử lại.';
//             toast.error(errMsg);
//         } finally {
//             setLoading(false);
//         }
//     };
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
import { LogIn, UserPlus, ArrowLeft, Eye, EyeOff, Mail } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';

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
            const errMsg = error.response?.data?.error
                || (error.response?.data?.errors ? Object.values(error.response.data.errors || {}).join(', ') : null)
                || 'Đăng nhập thất bại. Vui lòng thử lại.';
            toast.error(errMsg);
        } finally {
            setLoading(false);
        }
    };

    // Hàm xử lý đăng nhập bằng Google
    const handleGoogleLogin = () => {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    };

    // Hàm xử lý đăng nhập bằng Facebook
    const handleFacebookLogin = () => {
        window.location.href = 'http://localhost:8080/oauth2/authorization/facebook';
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

                    {/* Divider */}
                    <div className="mt-6">
                        <div className="relative">
                            <div className="absolute inset-0 flex items-center">
                                <div className="w-full border-t border-gray-300"></div>
                            </div>
                            <div className="relative flex justify-center text-sm">
                                <span className="px-2 bg-white text-gray-500">Hoặc đăng nhập với</span>
                            </div>
                        </div>
                    </div>

                    {/* Social Login Buttons */}
                    <div className="mt-4 space-y-3">
                        <button
                            type="button"
                            onClick={handleGoogleLogin}
                            className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200"
                        >
                            <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                            </svg>
                            Đăng nhập với Google
                        </button>

                        <button
                            type="button"
                            onClick={handleFacebookLogin}
                            className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200"
                        >
                            <svg className="w-5 h-5 mr-2 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                            </svg>
                            Đăng nhập với Facebook
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