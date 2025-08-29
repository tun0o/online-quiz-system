import { useState, useEffect } from 'react';
import { Clock, CheckCircle, XCircle, Eye } from 'lucide-react';
import { toast } from 'react-toastify';
import { quizService } from '../../services/quizService';
import ConfirmationModal from '../common/ConfirmationModal';

export default function ModerationPanel() {
  const [pendingSubmissions, setPendingSubmissions] = useState([]);
  const [selectedSubmission, setSelectedSubmission] = useState(null);
  const [loading, setLoading] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [modalContent, setModalContent] = useState({});

  const loadPendingSubmissions = async () => {
    setLoading(true);
    try {
      const response = await quizService.getPendingSubmissions(0, 20);
      setPendingSubmissions(response.content || []);
    } catch (error) {
      console.error('Error loading pending submissions:', error);
      toast.error("Không thể tải danh sách đề chờ duyệt.");
    } finally {
      setLoading(false);
    }
  };

  const loadSubmissionDetail = async (id) => {
    try {
      const submission = await quizService.getSubmissionDetail(id);
      setSelectedSubmission(submission);
    } catch (error) {
      console.error('Error loading submission detail:', error);
      toast.error("Không thể tải chi tiết đề thi.");
    }
  };

  useEffect(() => {
    loadPendingSubmissions();
  }, []);

  const handleApproveRequest = (id) => {
    setModalContent({
      title: 'Xác nhận duyệt',
      message: 'Bạn có chắc chắn muốn duyệt đề thi này không? Đề thi sẽ được công khai cho mọi người.',
      confirmText: 'Duyệt',
      variant: 'default',
      size: 'sm'
    });
    setConfirmAction(() => () => performApprove(id));
    setIsConfirmModalOpen(true);
  };

  const performApprove = async (id) => {
    setActionLoading(true);
    try {
      await quizService.approveSubmission(id);
      toast.success('Đã duyệt đề thi thành công!');
      loadPendingSubmissions();
      setSelectedSubmission(null);
    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setActionLoading(false);
      closeConfirmModal();
    }
  };

  const handleReject = async (id) => {
    if (!rejectReason.trim()) {
      toast.warn('Vui lòng nhập lý do từ chối');
      return;
    }

    setActionLoading(true);
    try {
      await quizService.rejectSubmission(id, rejectReason);
      toast.success('Đã từ chối đề thi!');
      loadPendingSubmissions();
      setSelectedSubmission(null);
      setShowRejectModal(false);
      setRejectReason('');
    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setActionLoading(false);
    }
  };

  const closeConfirmModal = () => {
    setIsConfirmModalOpen(false);
    setConfirmAction(null);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Kiểm duyệt đề thi</h1>
        <div className="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-medium">
          {pendingSubmissions.length} đề chờ duyệt
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pending List */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="p-4 border-b border-gray-200">
            <h2 className="font-semibold text-gray-800">Đề chờ duyệt</h2>
          </div>
          
          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <div className="p-4 text-center text-gray-500">Đang tải...</div>
            ) : pendingSubmissions.length === 0 ? (
              <div className="p-4 text-center text-gray-500">Không có đề nào chờ duyệt</div>
            ) : (
              pendingSubmissions.map((submission) => (
                <div
                  key={submission.id}
                  onClick={() => loadSubmissionDetail(submission.id)}
                  className={`p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50 transition ${
                    selectedSubmission?.id === submission.id ? 'bg-blue-50 border-l-4 border-blue-500' : ''
                  }`}
                >
                  <div className="flex justify-between items-start gap-4">
                    <div className="flex-1">
                      <h3 className="font-medium text-gray-800 mb-1">{submission.title}</h3>
                      <p className="text-sm text-gray-600 mb-2">{submission.description}</p>
                      <div className="flex items-center gap-4 text-xs text-gray-500">
                        <span>Môn: <strong>{submission.subject}</strong></span>
                        <span>Thời gian: <strong>{submission.durationMinutes} phút</strong></span>
                        <span>Câu hỏi: <strong>{submission.questionCount || submission.questions?.length || 0}</strong></span>
                      </div>
                    </div>
                    <div className="text-right flex-shrink-0">
                      <Clock size={16} className="text-yellow-600 ml-auto" />
                      <div className="text-xs text-gray-500 mt-1">{formatDate(submission.createdAt)}</div>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Detail Panel */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          {selectedSubmission ? (
            <div>
              <div className="p-4 border-b border-gray-200">
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="font-semibold text-gray-800 mb-1">{selectedSubmission.title}</h2>
                    <p className="text-sm text-gray-600">{selectedSubmission.description}</p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleApproveRequest(selectedSubmission.id)}
                      disabled={actionLoading}
                      className="flex items-center gap-1 px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 transition disabled:opacity-50"
                    >
                      <CheckCircle size={14} />
                      Duyệt
                    </button>
                    <button
                      onClick={() => setShowRejectModal(true)}
                      disabled={actionLoading}
                      className="flex items-center gap-1 px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 transition disabled:opacity-50"
                    >
                      <XCircle size={14} />
                      Từ chối
                    </button>
                  </div>
                </div>
              </div>

              <div className="p-4 max-h-[calc(100vh-250px)] overflow-y-auto">
                <div className="space-y-3 text-sm mb-6 border-b border-gray-200 pb-4">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Người đóng góp:</span>
                    <span className="font-medium text-gray-800">{selectedSubmission.contributor?.name || 'Không rõ'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Môn học:</span>
                    <span className="font-medium text-gray-800">{selectedSubmission.subject}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Thời gian làm bài:</span>
                    <span className="font-medium text-gray-800">{selectedSubmission.durationMinutes} phút</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Số câu hỏi:</span>
                    <span className="font-medium text-gray-800">{selectedSubmission.questions?.length || 0}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Ngày gửi:</span>
                    <span className="font-medium text-gray-800">{formatDate(selectedSubmission.createdAt)}</span>
                  </div>
                </div>

                {/* Questions Preview */}
                <div>
                  <h3 className="font-medium text-gray-800 mb-3">Nội dung câu hỏi:</h3>
                  {selectedSubmission.questions?.map((question, index) => (
                    <div key={question.id} className="mb-4 p-3 bg-gray-50 rounded">
                      <div className="font-medium text-gray-800 mb-2">
                        Câu {index + 1}: {question.questionText}
                      </div>
                      <div className="space-y-1">
                        {question.answerOptions?.map((option, optIndex) => (
                          <div
                            key={option.id}
                            className={`text-sm px-2 py-1 rounded ${
                              option.isCorrect
                                ? 'bg-green-100 text-green-800 font-medium'
                                : 'text-gray-600'
                            }`}
                          >
                            {String.fromCharCode(65 + optIndex)}. {option.optionText}
                            {option.isCorrect && ' ✓'}
                          </div>
                        ))}
                      </div>
                      {question.explanation && (
                        <div className="mt-2 text-xs text-gray-500 italic">
                          Giải thích: {question.explanation}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            <div className="p-8 h-full flex flex-col justify-center items-center text-center text-gray-500">
              <Eye size={48} className="mx-auto mb-4 text-gray-300" />
              <p>Chọn một đề thi để xem chi tiết</p>
            </div>
          )}
        </div>
      </div>

      {/* Reject Modal */}
      {showRejectModal && selectedSubmission && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96">
            <h3 className="text-lg font-semibold mb-4">Từ chối đề thi</h3>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Lý do từ chối *
              </label>
              <textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                rows="4"
                placeholder="Nhập lý do từ chối đề thi..."
              />
              <p className="text-xs text-gray-500 mt-2">
                Lý do này sẽ được hiển thị cho người đóng góp. Vui lòng viết rõ ràng, lịch sự.
              </p>
            </div>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => {
                  setShowRejectModal(false);
                  setRejectReason('');
                }}
                className="px-4 py-2 text-gray-600 border border-gray-200 rounded hover:bg-gray-50 transition"
              >
                Hủy
              </button>
              <button
                onClick={() => handleReject(selectedSubmission.id)}
                disabled={actionLoading || !rejectReason.trim()}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition disabled:opacity-50"
              >
                {actionLoading ? 'Đang xử lý...' : 'Từ chối'}
              </button>
            </div>
          </div>
        </div>
      )}

      <ConfirmationModal
        isOpen={isConfirmModalOpen}
        onClose={closeConfirmModal}
        onConfirm={confirmAction}
        title={modalContent.title}
        message={modalContent.message}
        confirmText={modalContent.confirmText}
        variant={modalContent.variant}
        size={modalContent.size}
        isLoading={actionLoading}
      />
    </div>
  );
}