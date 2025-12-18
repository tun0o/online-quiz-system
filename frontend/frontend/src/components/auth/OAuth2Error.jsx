import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { XCircle, ArrowLeft, RefreshCw, Mail, AlertTriangle } from 'lucide-react';

/**
 * Handles OAuth2 authentication errors and provides user-friendly error messages.
 * Offers recovery options and guidance for users.
 */
const OAuth2Error = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [errorDetails, setErrorDetails] = useState(null);

    useEffect(() => {
        const error = searchParams.get('error');
        const message = searchParams.get('message');
        
        setErrorDetails({
            code: error,
            message: message || 'Có lỗi xảy ra trong quá trình đăng nhập'
        });

        // Show error toast
        if (error) {
            toast.error(message || 'Đăng nhập thất bại');
        }
    }, [searchParams]);

    const getErrorTitle = (errorCode) => {
        switch (errorCode) {
            case 'EMAIL_NOT_PROVIDED':
                return 'Email không được cung cấp';
            case 'AUTHENTICATION_FAILED':
                return 'Xác thực thất bại';
            case 'PROVIDER_NOT_SUPPORTED':
                return 'Nhà cung cấp không được hỗ trợ';
            default:
                return 'Lỗi đăng nhập';
        }
    };

    const getErrorDescription = (errorCode) => {
        switch (errorCode) {
            case 'EMAIL_NOT_PROVIDED':
                return 'Nhà cung cấp không cung cấp email. Vui lòng cấp quyền truy cập email hoặc thử đăng nhập bằng phương thức khác.';
            case 'AUTHENTICATION_FAILED':
                return 'Quá trình xác thực không thành công. Vui lòng thử lại hoặc liên hệ hỗ trợ nếu vấn đề vẫn tiếp tục.';
            case 'PROVIDER_NOT_SUPPORTED':
                return 'Nhà cung cấp đăng nhập này hiện chưa được hỗ trợ. Vui lòng sử dụng Google hoặc Facebook.';
            default:
                return 'Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.';
        }
    };

    const getRecoveryActions = (errorCode) => {
        switch (errorCode) {
            case 'EMAIL_NOT_PROVIDED':
                return [
                    { text: 'Thử lại với quyền email', action: 'retry' },
                    { text: 'Đăng nhập bằng email/mật khẩu', action: 'login' }
                ];
            case 'AUTHENTICATION_FAILED':
                return [
                    { text: 'Thử lại', action: 'retry' },
                    { text: 'Đăng nhập bằng email/mật khẩu', action: 'login' }
                ];
            case 'PROVIDER_NOT_SUPPORTED':
                return [
                    { text: 'Đăng nhập bằng Google', action: 'google' },
                    { text: 'Đăng nhập bằng Facebook', action: 'facebook' },
                    { text: 'Đăng nhập bằng email/mật khẩu', action: 'login' }
                ];
            default:
                return [
                    { text: 'Thử lại', action: 'retry' },
                    { text: 'Về trang chủ', action: 'home' }
                ];
        }
    };

    const handleAction = (action) => {
        switch (action) {
            case 'retry':
                // Go back to previous page or login
                window.history.back();
                break;
            case 'login':
                navigate('/login');
                break;
            case 'google':
                window.location.href = 'http://localhost:8080/oauth2/authorization/google';
                break;
            case 'facebook':
                window.location.href = 'http://localhost:8080/oauth2/authorization/facebook';
                break;
            case 'home':
                navigate('/');
                break;
            default:
                navigate('/login');
        }
    };

    const recoveryActions = errorDetails ? getRecoveryActions(errorDetails.code) : [];

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-200">
                <div className="bg-red-600 py-4 px-6">
                    <div className="flex items-center justify-between">
                        <Link to="/login" className="text-white hover:text-red-100">
                            <ArrowLeft size={20} />
                        </Link>
                        <h2 className="text-center text-2xl font-extrabold text-white">
                            Đăng nhập thất bại
                        </h2>
                        <div className="w-5"></div>
                    </div>
                </div>

                <div className="px-8 py-6">
                    <div className="text-center mb-6">
                        <XCircle className="mx-auto h-12 w-12 text-red-500 mb-4" />
                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                            {errorDetails ? getErrorTitle(errorDetails.code) : 'Lỗi đăng nhập'}
                        </h3>
                        <p className="text-gray-600 text-sm">
                            {errorDetails ? getErrorDescription(errorDetails.code) : 'Có lỗi xảy ra trong quá trình đăng nhập'}
                        </p>
                    </div>

                    {errorDetails && (
                        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
                            <div className="flex items-start">
                                <AlertTriangle className="h-5 w-5 text-red-400 mr-2 mt-0.5" />
                                <div className="text-sm">
                                    <p className="text-red-800 font-medium">Chi tiết lỗi:</p>
                                    <p className="text-red-700 mt-1">{errorDetails.message}</p>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="space-y-3">
                        <h4 className="text-sm font-medium text-gray-700">Bạn có thể:</h4>
                        {recoveryActions.map((action, index) => (
                            <button
                                key={index}
                                onClick={() => handleAction(action.action)}
                                className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition duration-200"
                            >
                                {action.action === 'retry' && <RefreshCw size={16} className="mr-2" />}
                                {action.action === 'login' && <Mail size={16} className="mr-2" />}
                                {action.text}
                            </button>
                        ))}
                    </div>

                    <div className="mt-6 pt-6 border-t border-gray-200">
                        <div className="text-center">
                            <Link 
                                to="/login" 
                                className="text-sm text-red-600 hover:text-red-500 font-medium"
                            >
                                Quay lại trang đăng nhập
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OAuth2Error;

