import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { ArrowLeft, Mail } from 'lucide-react';
import authService from '@/services/authService.js';
import { mapApiError } from '@/utils/errorCodes.js';

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [fieldError, setFieldError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!email?.trim()) {
            toast.error('Vui lòng nhập email');
            setFieldError('Vui lòng nhập email');
            return;
        }
        setLoading(true);
        try {
            await authService.requestPasswordReset(email.trim().toLowerCase());
            toast.success('Nếu email tồn tại, liên kết đặt lại mật khẩu đã được gửi.');
            navigate('/login');
        } catch (e1) {
            const msg = mapApiError(e1) || 'Không thể gửi yêu cầu đặt lại mật khẩu';
            const details = e1?.response?.data?.details;
            if (details && typeof details === 'object' && details.email) {
                setFieldError(details.email);
            }
            toast.error(msg);
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
                        <h2 className="text-center text-2xl font-extrabold text-white">Quên mật khẩu</h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="px-8 py-6">
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                            <div className="relative">
                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    className="appearance-none block w-full px-3 py-3 pr-12 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                    placeholder="Email của bạn"
                                />
                                <Mail size={20} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500" />
                            </div>
                            {fieldError && <div className="text-xs text-red-600 mt-1">{fieldError}</div>}
                        </div>
                    </div>

                    <div className="mt-6">
                        <button
                            type="submit"
                            disabled={loading}
                            className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200 disabled:opacity-50"
                        >
                            {loading ? 'Đang gửi...' : 'Gửi liên kết đặt lại mật khẩu'}
                        </button>
                    </div>

                    <div className="mt-4 text-center">
                        <Link to="/login" className="text-sm text-green-600 hover:text-green-500">Quay lại đăng nhập</Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ForgotPassword;




