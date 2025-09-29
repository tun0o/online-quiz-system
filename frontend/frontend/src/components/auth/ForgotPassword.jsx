import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { ArrowLeft, Mail, UserPlus, LogIn } from 'lucide-react';
import api from '@/services/api.js';

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [emailSent, setEmailSent] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!email.trim()) {
            toast.error('Vui lòng nhập email');
            return;
        }

        setLoading(true);
        try {
            await api.post('/api/auth/forgot-password', {
                email: email.trim().toLowerCase()
            });

            setEmailSent(true);
            toast.success('Email đặt lại mật khẩu đã được gửi!');
        } catch (error) {
            console.error('Forgot password error:', error);
            const errorMsg = error.response?.data?.error || 'Có lỗi xảy ra khi gửi email';
            toast.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    if (emailSent) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
                <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                    <div className="bg-green-600 py-4 px-6">
                        <div className="flex items-center justify-between">
                            <Link to="/login" className="text-white hover:text-green-100">
                                <ArrowLeft size={20} />
                            </Link>
                            <h2 className="text-center text-2xl font-extrabold text-white">
                                Email đã được gửi
                            </h2>
                            <div className="w-5"></div>
                        </div>
                    </div>

                    <div className="px-8 py-6 text-center">
                        <Mail className="mx-auto h-12 w-12 text-green-500 mb-4" />
                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                            Kiểm tra email của bạn
                        </h3>
                        <p className="text-gray-600 mb-6">
                            Chúng tôi đã gửi liên kết đặt lại mật khẩu đến <strong>{email}</strong>
                        </p>
                        <p className="text-sm text-gray-500 mb-6">
                            Liên kết có hiệu lực trong 1 giờ. Vui lòng kiểm tra hộp thư spam nếu không thấy email.
                        </p>
                        
                        <div className="space-y-3">
                            <button
                                onClick={() => setEmailSent(false)}
                                className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                            >
                                <Mail size={16} className="mr-2" />
                                Gửi lại email
                            </button>
                            
                            <Link
                                to="/login"
                                className="w-full flex items-center justify-center px-4 py-3 border border-transparent rounded-md shadow-sm bg-green-600 text-sm font-medium text-white hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                            >
                                <LogIn size={16} className="mr-2" />
                                Quay lại đăng nhập
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                <div className="bg-green-600 py-4 px-6">
                    <div className="flex items-center justify-between">
                        <Link to="/login" className="text-white hover:text-green-100">
                            <ArrowLeft size={20} />
                        </Link>
                        <h2 className="text-center text-2xl font-extrabold text-white">
                            Quên mật khẩu
                        </h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="px-8 py-6">
                    <div className="mb-6">
                        <p className="text-gray-600 text-sm">
                            Nhập email của bạn và chúng tôi sẽ gửi liên kết đặt lại mật khẩu.
                        </p>
                    </div>

                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Email
                            </label>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                className="appearance-none block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                placeholder="Email của bạn"
                            />
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
                                    Đang gửi...
                                </span>
                            ) : (
                                <span className="flex items-center">
                                    <Mail size={16} className="mr-2" />
                                    Gửi liên kết đặt lại
                                </span>
                            )}
                        </button>
                    </div>

                    <div className="mt-4 text-center">
                        <span className="text-gray-600 text-sm">
                            Nhớ mật khẩu?{' '}
                            <Link to="/login" className="font-medium text-green-600 hover:text-green-500">
                                <LogIn size={16} className="inline mr-1" />
                                Đăng nhập
                            </Link>
                        </span>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ForgotPassword;

