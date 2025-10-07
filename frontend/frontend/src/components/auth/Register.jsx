import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { UserPlus, LogIn, ArrowLeft } from 'lucide-react';
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

const Register = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        grade: '',
        goal: ''
    });
    const [loading, setLoading] = useState(false);
    const [fieldErrors, setFieldErrors] = useState({});
    const navigate = useNavigate();

    const pwdValidation = useMemo(() => validatePassword(formData.password), [formData.password]);

    const handleChange = (e) => {
        setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
        if (fieldErrors[e.target.name]) {
            setFieldErrors(prev => {
                const copy = { ...prev };
                delete copy[e.target.name];
                return copy;
            });
        }
    };

    const inputClass = (field) => {
        const base = 'appearance-none relative block w-full px-3 py-3 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:z-10 sm:text-sm';
        const normalBorder = 'border border-gray-300 focus:ring-green-500 focus:border-green-500';
        const errorBorder = 'border border-red-500 focus:ring-red-300 focus:border-red-500';
        return `${base} ${fieldErrors[field] ? errorBorder : normalBorder}`;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        setFieldErrors({});

        if (!pwdValidation.valid) {
            toast.error('Mật khẩu chưa đáp ứng đủ yêu cầu bảo mật.');
            return;
        }

        if (formData.password !== formData.confirmPassword) {
            toast.error('Mật khẩu xác nhận không khớp.');
            return;
        }

        if (!formData.email) {
            toast.error('Vui lòng nhập email.');
            return;
        }

        setLoading(true);
        try {
            await api.post('/api/auth/register', {
                email: formData.email.trim().toLowerCase(),
                password: formData.password,
                grade: formData.grade,
                goal: formData.goal,
            });

            toast.success(
                'Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.'
            );
            navigate('/login');
        } catch (error) {
            console.error('Register error:', error);

            const status = error.response?.status;
            const responseData = error.response?.data;

            if (status === 400 && responseData) {
                const { errors, error: singleError } = responseData;
                let errorsObj = {};

                if (errors) {
                    if (typeof errors === 'string') {
                        toast.error(errors);
                    } else if (Array.isArray(errors)) {
                        toast.error(errors.join(', '));
                    } else if (typeof errors === 'object') {
                        errorsObj = errors;
                        toast.error(Object.values(errorsObj).join(', '));
                    } else {
                        toast.error('Dữ liệu lỗi không hợp lệ');
                    }

                    if (Object.keys(errorsObj).length) {
                        setFieldErrors(errorsObj);
                    }
                } else if (singleError) {
                    toast.error(singleError);
                } else {
                    toast.error('Yêu cầu không hợp lệ (400). Vui lòng kiểm tra dữ liệu.');
                }
            } else {
                toast.error('Đăng ký thất bại. Vui lòng thử lại sau.');
            }
        }
        finally {
            setLoading(false);
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
                        <div className="w-5" />
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
                                className={inputClass('email')}
                                placeholder="Email của bạn"
                                aria-invalid={!!fieldErrors.email}
                            />
                            {fieldErrors.email && <div className="text-xs text-red-600 mt-1">{fieldErrors.email}</div>}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Mật khẩu</label>
                            <input
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                                className={inputClass('password')}
                                placeholder="Mật khẩu"
                                aria-invalid={!!fieldErrors.password}
                            />
                            {fieldErrors.password && <div className="text-xs text-red-600 mt-1">{fieldErrors.password}</div>}

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

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Xác nhận mật khẩu</label>
                            <input
                                type="password"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                required
                                className={inputClass('confirmPassword')}
                                placeholder="Xác nhận mật khẩu"
                                aria-invalid={!!fieldErrors.confirmPassword}
                            />
                            {formData.confirmPassword && formData.password !== formData.confirmPassword && (
                                <div className="text-xs text-red-500 mt-1">Mật khẩu xác nhận không khớp.</div>
                            )}
                            {fieldErrors.confirmPassword && <div className="text-xs text-red-600 mt-1">{fieldErrors.confirmPassword}</div>}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Lớp học</label>
                            <select
                                name="grade"
                                value={formData.grade}
                                onChange={handleChange}
                                className={inputClass('grade')}
                                aria-invalid={!!fieldErrors.grade}
                            >
                                <option value="">Chọn lớp</option>
                                <option value="10">Lớp 10</option>
                                <option value="11">Lớp 11</option>
                                <option value="12">Lớp 12</option>
                            </select>
                            {fieldErrors.grade && <div className="text-xs text-red-600 mt-1">{fieldErrors.grade}</div>}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Mục tiêu học tập</label>
                            <input
                                type="text"
                                name="goal"
                                value={formData.goal}
                                onChange={handleChange}
                                placeholder="Ví dụ: Thi đại học, Olympic, v.v."
                                className={inputClass('goal')}
                                aria-invalid={!!fieldErrors.goal}
                            />
                            {fieldErrors.goal && <div className="text-xs text-red-600 mt-1">{fieldErrors.goal}</div>}
                        </div>
                    </div>

                    <div className="mt-6">
                        <button
                            type="submit"
                            disabled={isSubmitDisabled}
                            className={`group relative w-full flex justify-center items-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white transition duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2
                ${isSubmitDisabled ? 'opacity-60 cursor-not-allowed bg-gradient-to-r from-emerald-400 to-emerald-500 shadow-none' : 'bg-gradient-to-r from-emerald-500 to-emerald-700 hover:from-emerald-600 hover:to-emerald-800 shadow-lg'}`}
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

                    <div className="mt-4 text-center">
            <span className="text-gray-600 text-sm">
              Đã có tài khoản?{' '}
                <Link to="/login" className="font-medium text-green-600 hover:text-green-500 flex items-center justify-center mt-2">
                <LogIn size={16} className="mr-1" />
                Đăng nhập
              </Link>
            </span>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Register;
