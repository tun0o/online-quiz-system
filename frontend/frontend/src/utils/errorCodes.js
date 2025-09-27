// Simplified single-locale (vi) error messages for now
const VI_MESSAGES = {
  'AUTH.INVALID_CREDENTIALS': 'Email hoặc mật khẩu không đúng.',
  'AUTH.UNVERIFIED': 'Tài khoản chưa được xác thực. Vui lòng kiểm tra email.',
  'VALIDATION_ERROR': 'Dữ liệu không hợp lệ. Vui lòng kiểm tra các trường thông tin.',
  'BUSINESS_ERROR': 'Yêu cầu không hợp lệ. Vui lòng thử lại.',
  'BAD_REQUEST': 'Yêu cầu không hợp lệ.',
  'INTERNAL_ERROR': 'Lỗi hệ thống. Vui lòng thử lại sau.',
  'RATE_LIMIT': 'Quá nhiều yêu cầu. Vui lòng thử lại sau.',
  DEFAULT: 'Đã xảy ra lỗi.'
};

function tError(code) {
  return (code && VI_MESSAGES[code]) || VI_MESSAGES.DEFAULT;
}

export function mapApiError(error) {
  const data = error?.response?.data;
  const status = error?.response?.status;
  if (!data) return null;

  // New standardized shape
  if (data.code) {
    return tError(data.code) || data.message;
  }

  // Legacy shapes
  if (data.error && typeof data.error === 'string') return data.error;
  if (data.errors && typeof data.errors === 'object') {
    try {
      const merged = Object.values(data.errors).filter(Boolean).join(', ');
      if (merged) return merged;
    } catch {}
  }
  if (status === 429) return tError('RATE_LIMIT');
  return tError();
}


