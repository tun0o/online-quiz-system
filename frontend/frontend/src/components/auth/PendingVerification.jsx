import { useState } from 'react';
import { useLocation, Link, Navigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Mail, Send, ArrowLeft, LogIn } from 'lucide-react';
import api from '@/services/api';

const PendingVerification = () => {
    const location = useLocation();
    const email = location.state?.email;
    const [loading, setLoading] = useState(false);

    // Nếu người dùng truy cập trực tiếp trang này mà không có email, chuyển hướng họ
    if (!email) {
        return <Navigate to="/register" replace />;
    }

    const handleResend = async () => {
        setLoading(true);
        try {
            await api.post('/api/auth/resend-verification', { email });
            toast.success('Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư của bạn.');
        } catch (error) {
            toast.error(error.response?.data?.error || 'Gửi lại email thất bại. Vui lòng thử lại sau.');
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
                        <h2 className="text-center text-2xl font-extrabold text-white">
                            Kiểm tra hộp thư
                        </h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <div className="px-8 py-10 text-center">
                    <Mail className="mx-auto h-12 w-12 text-green-500 mb-4" />
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                        Xác thực email của bạn
                    </h3>
                    <p className="text-gray-600 text-sm mb-6">
                        Chúng tôi đã gửi một liên kết xác thực đến địa chỉ email:
                        <br />
                        <strong className="text-gray-800">{email}</strong>
                        <br />
                        Vui lòng kiểm tra hộp thư (và cả thư mục spam) để hoàn tất đăng ký.
                    </p>

                    <div className="space-y-4">
                        <button
                            onClick={handleResend}
                            disabled={loading}
                            className="w-full flex items-center justify-center px-4 py-3 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200 disabled:opacity-50"
                        >
                            {loading ? (
                                <>
                                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Đang gửi...
                                </>
                            ) : (
                                <>
                                    <Send size={16} className="mr-2" />
                                    Gửi lại email xác thực
                                </>
                            )}
                        </button>

                        <Link
                            to="/login"
                            className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200"
                        >
                            <LogIn size={16} className="mr-2" />
                            Quay lại trang Đăng nhập
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PendingVerification;

