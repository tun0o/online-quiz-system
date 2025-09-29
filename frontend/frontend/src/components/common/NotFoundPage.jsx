import { Link } from "react-router-dom";
import { Home, ServerCrash } from "lucide-react";

const NotFoundPage = () => {
    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
            <div className="max-w-md w-full text-center">
                <ServerCrash size={64} className="text-red-500 mx-auto mb-6" />
                <h1 className="text-4xl font-bold text-gray-900 mb-4">404 - Không tìm thấy trang</h1>
                <p className="text-gray-600 mb-8">
                    Rất tiếc, trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.
                </p>
                <Link
                    to="/"
                    className="inline-flex items-center px-4 py-2 bg-pink-600 text-white rounded-lg hover:bg-pink-700 transition-colors"
                >
                    <Home size={20} className="mr-2" />
                    Quay lại trang chủ
                </Link>
            </div>
        </div>
    );
};

export default NotFoundPage;