import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { LogIn, UserPlus, ArrowLeft, Eye, EyeOff, Check, X } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';

// --- Tiện ích xác thực mật khẩu ---
const validatePassword = (pwd = '') => {
    const checks = {
        length: pwd.length >= 8,
        lower: /[a-z]/.test(pwd),
        upper: /[A-Z]/.test(pwd),
        digit: /\d/.test(pwd),
        special: /[^A-Za-z0-9]/.test(pwd),
    };
    const passedCount = Object.values(checks).filter(Boolean).length;
    const strengthPercent = Math.round((passedCount / Object.keys(checks).length) * 100);
    return { checks, valid: passedCount === Object.keys(checks).length, strengthPercent };
};

const getColorByPercent = (p) => {
    if (p >= 80) return '#16a34a'; // green-600
    if (p >= 50) return '#f59e0b'; // amber-500
    return '#ef4444'; // red-500
};

const requirementLabels = {
    length: 'Ít nhất 8 ký tự',
    lower: 'Ít nhất một chữ thường',
    upper: 'Ít nhất một chữ HOA',
    digit: 'Ít nhất một chữ số',
    special: 'Ít nhất một ký tự đặc biệt',
};

// --- Component ---
const Register = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        grade: '',
        goal: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { register: authRegister } = useAuth();

    const pwdValidation = useMemo(() => validatePassword(formData.password), [formData.password]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!pwdValidation.valid) {
            toast.error('Mật khẩu chưa đáp ứng đủ yêu cầu bảo mật.');
            return;
        }

        if (formData.password !== formData.confirmPassword) {
            toast.error('Mật khẩu xác nhận không khớp.');
            return;
        }

        setLoading(true);
        const result = await authRegister(formData);
        setLoading(false);

        if (result.success) {
            toast.success(result.message || 'Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.');
            navigate('/pending-verification', { state: { email: formData.email } });
        } else {
            toast.error(result.error || 'Đăng ký thất bại. Vui lòng thử lại.');
        }
    };

    const isSubmitDisabled = loading || !pwdValidation.valid || formData.password !== formData.confirmPassword;
    const barColor = getColorByPercent(pwdValidation.strengthPercent);

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                <div className="bg-green-600 py-4 px-6">
                    <div className="flex items-center justify-between">
                        <Link to="/" className="text-white hover:text-green-100">
                            <ArrowLeft size={20} />
                        </Link>
                        <h2 className="text-center text-2xl font-extrabold text-white">Đăng ký tài khoản</h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="px-8 py-6">
                    <div className="space-y-4">
                        {/* Email */}
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

                        {/* Mật khẩu */}
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

                        {/* Xác nhận mật khẩu */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Xác nhận mật khẩu</label>
                            <div className="relative">
                                <input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    name="confirmPassword"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    required
                                    className="appearance-none block w-full px-3 py-3 pr-12 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                    placeholder="Nhập lại mật khẩu"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute inset-y-0 right-0 pr-3 flex items-center z-10"
                                >
                                    {showConfirmPassword ? <EyeOff size={20} className="text-gray-500" /> : <Eye size={20} className="text-gray-500" />}
                                </button>
                            </div>
                            {formData.confirmPassword && formData.password !== formData.confirmPassword && (
                                <p className="text-xs text-red-600 mt-1">Mật khẩu xác nhận không khớp.</p>
                            )}
                        </div>

                        {/* Lớp và Mục tiêu */}
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Lớp</label>
                                <select
                                    name="grade"
                                    value={formData.grade}
                                    onChange={handleChange}
                                    className="appearance-none block w-full px-3 py-3 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                >
                                    <option value="">Chọn lớp</option>
                                    <option value="10">Lớp 10</option>
                                    <option value="11">Lớp 11</option>
                                    <option value="12">Lớp 12</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Mục tiêu</label>
                                <select
                                    name="goal"
                                    value={formData.goal}
                                    onChange={handleChange}
                                    className="appearance-none block w-full px-3 py-3 border border-gray-300 text-gray-900 rounded-lg focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                                >
                                    <option value="">Chọn mục tiêu</option>
                                    <option value="EXAM_PREP">Ôn thi</option>
                                    <option value="KNOWLEDGE_IMPROVEMENT">Nâng cao kiến thức</option>
                                    <option value="FUN">Học cho vui</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    {/* Nút Submit */}
                    <div className="mt-6">
                        <button
                            type="submit"
                            disabled={isSubmitDisabled}
                            className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
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
                                    <UserPlus size={16} className="mr-2" />
                                    Đăng ký
                                </span>
                            )}
                        </button>
                    </div>

                    {/* Link đến trang đăng nhập */}
                    <div className="mt-4 text-center">
                        <span className="text-gray-600 text-sm">
                            Đã có tài khoản?{' '}
                            <Link to="/login" className="font-medium text-green-600 hover:text-green-500">
                                <LogIn size={16} className="inline mr-1" /> Đăng nhập ngay
                            </Link>
                        </span>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Register;