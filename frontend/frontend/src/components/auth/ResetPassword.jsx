import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { ArrowLeft, Lock, Eye, EyeOff, CheckCircle } from 'lucide-react';
import api from '@/services/api.js';

const validatePassword = (pwd = '') => {
    const checks = {
        length: pwd.length >= 8,
        lower: /[a-z]/.test(pwd),
        upper: /[A-Z]/.test(pwd),
        digit: /\d/.test(pwd),
        special: /[^A-Za-z0-9]/.test(pwd),
    };
    const passed = Object.values(checks).filter(Boolean).length;
    const strengthPercent = Math.round((passed / Object.keys(checks).length) * 100);
    return { checks, valid: passed === Object.keys(checks).length, passed, strengthPercent };
};

const getColorByPercent = (p) => {
    if (p >= 80) return '#16a34a';
    if (p >= 50) return '#f59e0b';
    return '#ef4444';
};

const requirementLabels = {
    length: 'Ít nhất 8 ký tự',
    lower: 'Ít nhất một chữ thường',
    upper: 'Ít nhất một chữ HOA',
    digit: 'Ít nhất một chữ số',
    special: 'Ít nhất một ký tự đặc biệt',
};

const ResetPassword = () => {
    const [searchParams] = useSearchParams();
    const [formData, setFormData] = useState({
        newPassword: '',
        confirmPassword: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const navigate = useNavigate();
    
    const token = searchParams.get('token');
    const pwdValidation = validatePassword(formData.newPassword);

    useEffect(() => {
        if (!token) {
            toast.error('Token không hợp lệ');
            navigate('/forgot-password');
        }
    }, [token, navigate]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!pwdValidation.valid) {
            toast.error('Mật khẩu chưa đáp ứng đủ yêu cầu bảo mật');
            return;
        }

        if (formData.newPassword !== formData.confirmPassword) {
            toast.error('Mật khẩu xác nhận không khớp');
            return;
        }

        setLoading(true);
        try {
            await api.post('/api/auth/reset-password', {
                token: token,
                newPassword: formData.newPassword
            });

            setSuccess(true);
            toast.success('Đặt lại mật khẩu thành công!');
            
            setTimeout(() => {
                navigate('/login');
            }, 3000);
        } catch (error) {
            console.error('Reset password error:', error);
            const errorMsg = error.response?.data?.error || 'Có lỗi xảy ra khi đặt lại mật khẩu';
            toast.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
                <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                    <div className="bg-green-600 py-4 px-6">
                        <div className="flex items-center justify-between">
                            <Link to="/login" className="text-white hover:text-green-100">
                                <ArrowLeft size={20} />
                            </Link>
                            <h2 className="text-center text-2xl font-extrabold text-white">
                                Thành công
                            </h2>
                            <div className="w-5"></div>
                        </div>
                    </div>

                    <div className="px-8 py-6 text-center">
                        <CheckCircle className="mx-auto h-12 w-12 text-green-500 mb-4" />
                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                            Mật khẩu đã được đặt lại
                        </h3>
                        <p className="text-gray-600 mb-6">
                            Bạn sẽ được chuyển đến trang đăng nhập trong giây lát
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    const isSubmitDisabled = loading || !pwdValidation.valid || formData.newPassword !== formData.confirmPassword;
    const barColor = getColorByPercent(pwdValidation.strengthPercent);

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                <div className="bg-green-600 py-4 px-6">
                    <div className="flex items-center justify-between">
                        <Link to="/login" className="text-white hover:text-green-100">
                            <ArrowLeft size={20} />
                        </Link>
                        <h2 className="text-center text-2xl font-extrabold text-white">
                            Đặt lại mật khẩu
                        </h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="px-8 py-6">
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Mật khẩu mới
                            </label>
                            <div className="relative">
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    name="newPassword"
                                    value={formData.newPassword}
                                    onChange={handleChange}
                                    required
                                    className="appearance-none block w-full px-3 py-3 pr-12 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                    placeholder="Mật khẩu mới"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute inset-y-0 right-0 pr-3 flex items-center z-10"
                                >
                                    {showPassword ? <EyeOff size={20} className="text-gray-500" /> : <Eye size={20} className="text-gray-500" />}
                                </button>
                            </div>
                            
                            <div className="mt-3">
                                <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                                    <div
                                        className="h-2 rounded-full transition-all"
                                        style={{
                                            width: `${pwdValidation.strengthPercent}%`,
                                            background: barColor,
                                            transition: 'width 250ms ease-in-out'
                                        }}
                                        aria-hidden="true"
                                    />
                                </div>
                                <div className="text-xs text-gray-500 mt-2">
                                    Độ mạnh mật khẩu: <span className="font-medium">{pwdValidation.strengthPercent}%</span>
                                </div>
                            </div>
                        </div>

                        {/* Chỉ dẫn mật khẩu */}
                        {formData.password && (
                            <div className="mt-3 p-3 bg-gray-50 rounded-lg border border-gray-200">
                                <div className="w-full bg-gray-200 rounded-full h-2 mb-3 overflow-hidden">
                                    <div
                                        className="h-2 rounded-full"
                                        style={{
                                            width: `${pwdValidation.strengthPercent}%`,
                                            background: barColor,
                                            transition: 'width 250ms ease-in-out, background-color 250ms ease-in-out'
                                        }}
                                    />
                                </div>
                                <ul className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1 text-xs">
                                    {Object.entries(requirementLabels).map(([key, label]) => (
                                        <li key={key} className={`flex items-center ${pwdValidation.checks[key] ? 'text-green-600' : 'text-gray-500'}`}>
                                            {pwdValidation.checks[key] ? <Check size={14} className="mr-1.5" /> : <X size={14} className="mr-1.5" />}
                                            {label}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Xác nhận mật khẩu
                            </label>
                            <div className="relative">
                                <input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    name="confirmPassword"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    required
                                    className="appearance-none block w-full px-3 py-3 pr-12 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                    placeholder="Xác nhận mật khẩu"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute inset-y-0 right-0 pr-3 flex items-center z-10"
                                >
                                    {showConfirmPassword ? <EyeOff size={20} className="text-gray-500" /> : <Eye size={20} className="text-gray-500" />}
                                </button>
                            </div>
                            {formData.confirmPassword && formData.newPassword !== formData.confirmPassword && (
                                <div className="text-xs text-red-500 mt-1">Mật khẩu xác nhận không khớp</div>
                            )}
                        </div>
                    </div>

                    <div className="mt-6">
                        <button
                            type="submit"
                            disabled={isSubmitDisabled}
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
                                    <Lock size={16} className="mr-2" />
                                    Đặt lại mật khẩu
                                </span>
                            )}
                        </button>
                    </div>

                    <div className="mt-4 text-center">
                        <Link to="/login" className="text-sm text-green-600 hover:text-green-500">
                            Quay lại đăng nhập
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ResetPassword;

