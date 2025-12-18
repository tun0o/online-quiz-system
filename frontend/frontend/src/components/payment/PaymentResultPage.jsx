import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle, XCircle } from 'lucide-react';

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();

  // Lấy các tham số từ URL do VNPay trả về
  const vnp_ResponseCode = searchParams.get('vnp_ResponseCode');
  const vnp_Amount = searchParams.get('vnp_Amount');
  const vnp_TxnRef = searchParams.get('vnp_TxnRef');

  const isSuccess = vnp_ResponseCode === '00';

  return (
    <div className="flex items-center justify-center min-h-full">
      <div className="text-center bg-white p-10 rounded-xl shadow-2xl max-w-lg w-full">
      {isSuccess ? (
        <>
            <CheckCircle size={80} className="mx-auto text-green-500 mb-4" />
            <h1 className="text-4xl font-extrabold text-gray-900 mt-4">Thanh toán thành công!</h1>
            <p className="text-lg text-gray-600 mt-3">Cảm ơn bạn đã giao dịch. Điểm đã được cộng vào tài khoản của bạn.</p>
            {vnp_Amount && (
              <div className="mt-6 bg-green-50 border border-green-200 rounded-lg p-4 text-left">
                <p className="text-sm text-gray-700">Số tiền: <span className="font-semibold text-green-700">{(parseInt(vnp_Amount) / 100).toLocaleString('vi-VN')} VNĐ</span></p>
                <p className="text-sm text-gray-700 mt-1">Mã giao dịch: <span className="font-semibold text-gray-800">{vnp_TxnRef}</span></p>
              </div>
            )}
        </>
      ) : (
        <>
            <XCircle size={80} className="mx-auto text-red-500 mb-4" />
            <h1 className="text-4xl font-extrabold text-gray-900 mt-4">Thanh toán thất bại</h1>
            <p className="text-lg text-gray-600 mt-3">
              Đã có lỗi xảy ra trong quá trình xử lý thanh toán. Vui lòng thử lại hoặc liên hệ bộ phận hỗ trợ.
            </p>
            {vnp_TxnRef && (
              <div className="mt-6 bg-red-50 border border-red-200 rounded-lg p-4 text-left">
                <p className="text-sm text-gray-700 mt-1">Mã giao dịch: <span className="font-semibold text-gray-800">{vnp_TxnRef}</span></p>
              </div>
            )}
        </>
      )}
        <Link to="/" className="mt-8 inline-block w-full sm:w-auto bg-indigo-600 text-white px-8 py-3 rounded-lg font-semibold text-lg hover:bg-indigo-700 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-50">
          Về trang chủ
        </Link>
      </div>
    </div>
  );
}
