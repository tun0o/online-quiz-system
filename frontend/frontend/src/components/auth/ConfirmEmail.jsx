import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import api from '@/services/api.js';
import { toast } from 'react-toastify';
import { CheckCircle, XCircle, ArrowLeft, Mail } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';

const ConfirmEmail = () => {
    const [loading] = useState(false);
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [status, setStatus] = useState('verifying');
    const { loginWithTokens } = useAuth(); // Lấy hàm để đăng nhập bằng token
    const [email, setEmail] = useState('');

    useEffect(() => {
        const token = searchParams.get('token');
        if (!token) {
            setStatus('invalid');
            toast.error('Token xác thực không hợp lệ');
            return;
        }

        let isMounted = true;

        const verifyEmail = async () => {
            try {
                // POST token và nhận lại JWTs
                const response = await api.post('/api/auth/verify', { token });

                if (isMounted) {
                    // Dùng hàm từ useAuth và chờ cho đến khi state được lưu xong
                    await loginWithTokens(response.data.accessToken, response.data.refreshToken, response.data.user);

                    setStatus('success');
                    toast.success('Xác thực thành công! Chào mừng bạn đến với Practizz.');

                    // Chuyển hướng về trang chủ sau khi đăng nhập
                    setTimeout(() => {
                        if (isMounted) navigate('/');
                    }, 3000);
                }
            } catch (err) {
                if (isMounted) {
                    setStatus('error');
                    toast.error(err.response?.data?.error || 'Xác thực thất bại. Token có thể đã hết hạn hoặc không hợp lệ.');
                }
            }
        };

        verifyEmail();

        return () => {
            isMounted = false;
        };
    }, [searchParams, navigate, loginWithTokens]);

    const handleResend = async () => {
        if (!email) {
            toast.error('Vui lòng nhập email');
            return;
        }

        try {
            await api.post('/api/auth/resend-verification', { email });
            toast.success('Đã gửi lại email xác thực');
        } catch (error) {
            toast.error(error.response?.data?.error || 'Gửi lại email thất bại');
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
                        <h2 className="text-center text-2xl font-extrabold text-white">
                            Xác thực email
                        </h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <div className="px-8 py-6">
                    {status === 'verifying' && (
                        <div className="text-center py-6">
                            <div className="flex justify-center">
                                <svg className="animate-spin h-12 w-12 text-green-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                            </div>
                            <p className="mt-4 text-gray-600">Đang xác thực email của bạn...</p>
                        </div>
                    )}

                    {status === 'success' && (
                        <div className="text-center py-6">
                            <CheckCircle className="mx-auto h-12 w-12 text-green-500" />
                            <h3 className="mt-4 text-lg font-medium text-gray-900">Đăng nhập thành công!</h3>
                            <p className="mt-2 text-gray-600">Bạn sẽ được chuyển đến trang chủ trong giây lát.</p>
                        </div>
                    )}

                    {status === 'error' && (
                        <div className="py-6">
                            <div className="text-center mb-6">
                                <XCircle className="mx-auto h-12 w-12 text-red-500" />
                                <h3 className="mt-4 text-lg font-medium text-gray-900">Xác thực thất bại</h3>
                                <p className="mt-2 text-gray-600">Link xác thực có thể đã hết hạn hoặc không hợp lệ. Vui lòng thử gửi lại.</p>
                            </div>
                            {/* <button
                                type="submit"
                                disabled={loading}
                                className="w-full flex justify-center items-center py-3 px-4 rounded-md
                   text-white font-medium shadow-md
                   bg-green-500 hover:bg-green-600 transition disabled:opacity-50"
                            >
                                {loading ? (
                                    'Đang xử lý...'
                                ) : (
                                    <>
                                        <LogIn size={16} className="mr-2" />
                                        Đăng nhập
                                    </>
                                )}
                            </button> */}

                            <div className="mt-6">
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Nhập email để gửi lại link xác thực
                                </label>
                                <input
                                    type="email"
                                    placeholder="Email của bạn"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 focus:z-10 sm:text-sm"
                                />
                                <button
                                    onClick={handleResend}
                                    className="mt-4 group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200"
                                >
                                    <Mail size={16} className="mr-2" />
                                    Gửi lại email xác thực
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ConfirmEmail;
