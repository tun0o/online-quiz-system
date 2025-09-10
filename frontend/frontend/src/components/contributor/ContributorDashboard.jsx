import { useState, useEffect, useMemo } from 'react';
import { Plus, Clock, CheckCircle, XCircle, Eye, Edit, Trash2, X } from 'lucide-react';
import { toast } from 'react-toastify';
import QuizSubmissionForm from '@/components/contributor/QuizSubmissionForm';
import ConfirmationModal from '@/components/common/ConfirmationModal';
import SubmissionDetailView from '@/components/contributor/SubmissionDetailView';
import { subjectDisplayMap } from '@/utils/displayMaps';
import { quizService } from '@/services/quizService';

const getStatusBadge = (status) => {
  const styles = {
    PENDING: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    APPROVED: 'bg-green-100 text-green-800 border-green-200',
    REJECTED: 'bg-red-100 text-red-800 border-red-200'
  };

  const icons = {
    PENDING: <Clock size={14} />,
    APPROVED: <CheckCircle size={14} />,
    REJECTED: <XCircle size={14} />
  };

  const labels = {
    PENDING: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối'
  };

  return (
    <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium border ${styles[status]}`}>
      {icons[status]}
      {labels[status]}
    </span>
  );
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

const getStatusColor = (status) => {
  const styles = {
    PENDING: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    APPROVED: 'bg-green-100 text-green-800 border-green-200',
    REJECTED: 'bg-red-100 text-red-800 border-red-200'
  };
  return styles[status] || 'bg-gray-100 text-gray-800 border-gray-200';
};

const getStatusText = (status) => {
  const labels = {
    PENDING: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối'
  };
  return labels[status] || 'Không xác định';
};

const SubmissionItem = ({ submission, onViewDetails, onEdit, onDeleteRequest }) => (
  <div className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition">
    <div className="flex justify-between items-start mb-3">
      <div className="flex-1">
        <h3 className="font-semibold text-gray-800 mb-1">{submission.title}</h3>
        <p className="text-gray-600 text-sm mb-2">{submission.description}</p>
        <div className="flex items-center gap-4 text-sm text-gray-500">
          <span>Môn: {subjectDisplayMap[submission.subject] || submission.subject}</span>
          <span>Thời gian: {submission.durationMinutes}p</span>
          <span>Câu hỏi: {submission.questions?.length || 0}</span>
        </div>
      </div>
      <div className="text-right">
        {getStatusBadge(submission.status)}
        <div className="text-xs text-gray-500 mt-1">{formatDate(submission.createdAt)}</div>
      </div>
    </div>

    {submission.status === 'REJECTED' && submission.adminFeedback && (
      <div className="bg-red-50 border border-red-200 rounded p-3 mt-3">
        <div className="text-sm font-medium text-red-800 mb-1">Lý do từ chối:</div>
        <div className="text-sm text-red-700">{submission.adminFeedback}</div>
      </div>
    )}

    <div className="flex gap-2 mt-3">
      <button onClick={() => onViewDetails(submission.id)} className="flex items-center gap-1 px-3 py-1 bg-white text-white border border-gray-300 rounded hover:bg-gray-50 transition text-sm"><Eye size={14} /> Xem chi tiết</button>
      {submission.status === 'PENDING' && (<>
        <button onClick={() => onEdit(submission)} className="flex items-center gap-1 px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 transition text-sm"><Edit size={14} /> Sửa</button>
        <button onClick={() => onDeleteRequest(submission.id)} className="flex items-center gap-1 px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 transition text-sm"><Trash2 size={14} /> Xóa</button>
      </>)}
    </div>
  </div>
);

export default function ContributorDashboard() {
  const [activeTab, setActiveTab] = useState('list'); // 'list', 'create', or 'edit'
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });
  const [editingSubmission, setEditingSubmission] = useState(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [modalContent, setModalContent] = useState({});

  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedSubmission, setSelectedSubmission] = useState(null);

  const loadSubmissions = async (page = 0) => {
    setLoading(true);
    try {
      const response = await quizService.getMySubmissions(1, page, 10);
      setSubmissions(response.content || []);
      setPagination({
        page: response.number || 0,
        size: response.size || 10,
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0
      });
    } catch (error) {
      console.error('Error loading submissions:', error);
      toast.error("Không thể tải danh sách đề thi.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'list') {
      loadSubmissions();
    } else if (activeTab === 'create') {
      setEditingSubmission(null); // Ensure form is clean for creation
    }
  }, [activeTab]);

  const handleEdit = (submission) => {
    setEditingSubmission(submission);
    setActiveTab('edit');
  };

  const handleViewDetails = async (submissionId) => {
    // Hiển thị modal ngay lập tức với trạng thái loading
    setIsDetailModalOpen(true);
    setSelectedSubmission(null); // Xóa dữ liệu cũ
    try {
      const submissionDetails = await quizService.getSubmissionDetail(submissionId);
      setSelectedSubmission(submissionDetails);
    } catch (error) {
      toast.error("Không thể tải chi tiết đề thi.");
      setIsDetailModalOpen(false); // Đóng modal nếu có lỗi
    }
  };

  const handleDeleteRequest = (id) => {
    setModalContent({
      title: 'Xác nhận xóa',
      message: 'Bạn có chắc chắn muốn xóa đề thi này không? Hành động này không thể hoàn tác.',
      confirmText: 'Xóa',
      variant: 'danger'
    });
    // We need to pass a function that returns the action function.
    // Otherwise, React treats `() => performDelete(id)` as an updater function and executes it immediately.
    setConfirmAction(() => () => performDelete(id));
    setIsConfirmModalOpen(true);
  };

  const performDelete = async (id) => {
    try {
      await quizService.deleteSubmission(id);
      toast.success('Đã xóa đề thi thành công!');
      loadSubmissions();
    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      closeConfirmModal();
    }
  };

  const closeConfirmModal = () => {
    setIsConfirmModalOpen(false);
    setConfirmAction(null);
  };

  const stats = useMemo(() => {
    const pending = submissions.filter(s => s.status === 'PENDING').length;
    const approved = submissions.filter(s => s.status === 'APPROVED').length;
    const rejected = submissions.filter(s => s.status === 'REJECTED').length;
    return { pending, approved, rejected };
  }, [submissions]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Đóng góp đề thi</h1>
        <div className="flex gap-2">
          <button
            onClick={() => setActiveTab('list')}
            className={`px-4 py-2 rounded-lg transition ${
              activeTab === 'list'
                ? 'bg-green-600 text-white'
                : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
            }`}
          >
            <Eye size={16} className="inline mr-2" />
            Đề của tôi
          </button>
          <button
            onClick={() => {
              // Luôn reset trạng thái sửa khi người dùng chủ động bấm "Tạo đề mới"
              setEditingSubmission(null);
              setActiveTab('create');
            }}
            className={`px-4 py-2 rounded-lg transition ${
              activeTab === 'create'
                ? 'bg-green-600 text-white'
                : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
            }`}
          >
            <Plus size={16} className="inline mr-2" />
            Tạo đề mới
          </button>
        </div>
      </div>

      {/* Content */}
      {activeTab === 'create' || activeTab === 'edit' ? (
        <QuizSubmissionForm 
          submission={editingSubmission}
          onSuccess={() => {
            setEditingSubmission(null);
            setActiveTab('list');
            loadSubmissions();
          }} 
        />
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          {/* Stats */}
          <div className="p-6 border-b border-gray-200">
            <div className="grid grid-cols-4 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-800">{pagination.totalElements}</div>
                <div className="text-sm text-gray-600">Tổng đề</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-yellow-600">{stats.pending}</div>
                <div className="text-sm text-gray-600">Chờ duyệt</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">{stats.approved}</div>
                <div className="text-sm text-gray-600">Đã duyệt</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-red-600">{stats.rejected}</div>
                <div className="text-sm text-gray-600">Từ chối</div>
              </div>
            </div>
          </div>

          {/* Submissions List */}
          <div className="p-6">
            {loading ? (
              <div className="text-center py-8">
                <div className="text-gray-500">Đang tải...</div>
              </div>
            ) : submissions.length === 0 ? (
              <div className="text-center py-8">
                <div className="text-gray-500 mb-4">Bạn chưa có đề thi nào</div>
                <button
                  onClick={() => setActiveTab('create')}
                  className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
                >
                  Tạo đề đầu tiên
                </button>
              </div>
            ) : (
              <div className="space-y-4">
                {submissions.map((submission) => (
                  <SubmissionItem
                    key={submission.id}
                    submission={submission}
                    onViewDetails={handleViewDetails}
                    onEdit={handleEdit}
                    onDeleteRequest={handleDeleteRequest}
                  />
                ))}

                {/* Pagination */}
                {pagination.totalPages > 1 && (
                  <div className="flex justify-center items-center gap-2 mt-6">
                    {Array.from({ length: pagination.totalPages }, (_, i) => (
                      <button
                        key={i}
                        onClick={() => loadSubmissions(i)}
                        className={`px-3 py-1 rounded ${
                          i === pagination.page
                            ? 'bg-green-600 text-white'
                            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        {i + 1}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
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
      />

      {/* Detail Modal */}
      {isDetailModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-start mb-4">
              <h3 className="text-lg font-semibold">Chi tiết đề thi</h3>
              <button
                onClick={() => setIsDetailModalOpen(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X size={20} />
              </button>
            </div>

            {selectedSubmission ? (
              <div className="space-y-4">
                {/* Basic Info */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <span className="text-gray-500">Tiêu đề:</span>
                    <p className="font-medium">{selectedSubmission.title}</p>
                  </div>
                  <div>
                    <span className="text-gray-500">Môn học:</span>
                    <p className="font-medium">{selectedSubmission.subject}</p>
                  </div>
                  <div>
                    <span className="text-gray-500">Thời gian:</span>
                    <p className="font-medium">{selectedSubmission.durationMinutes} phút</p>
                  </div>
                  <div>
                    <span className="text-gray-500">Số câu hỏi:</span>
                    <p className="font-medium">{selectedSubmission.questions?.length || 0}</p>
                  </div>
                </div>

                {/* Description */}
                {selectedSubmission.description && (
                  <div>
                    <span className="text-gray-500">Mô tả:</span>
                    <p className="mt-1">{selectedSubmission.description}</p>
                  </div>
                )}

                {/* Questions */}
                <div>
                  <h4 className="font-medium mb-3">Danh sách câu hỏi:</h4>
                  <div className="space-y-4">
                    {selectedSubmission.questions?.map((question, index) => (
                      <div key={question.id} className="border border-gray-200 rounded-lg p-4">
                        <div className="flex justify-between items-start mb-3">
                          <h5 className="font-medium">
                            Câu {index + 1}: {question.questionText}
                          </h5>
                          <div className="flex gap-2 text-xs">
                            <span className={`px-2 py-1 rounded ${
                              question.questionType === 'MULTIPLE_CHOICE' ? 'bg-blue-100 text-blue-800' :
                              question.questionType === 'TRUE_FALSE' ? 'bg-green-100 text-green-800' :
                              'bg-purple-100 text-purple-800'
                            }`}>
                              {question.questionType === 'MULTIPLE_CHOICE' ? 'Trắc nghiệm' :
                               question.questionType === 'TRUE_FALSE' ? 'Đúng/Sai' : 'Tự luận'}
                            </span>
                            {question.questionType === 'ESSAY' && (
                              <span className="px-2 py-1 bg-orange-100 text-orange-800 rounded">
                                {question.maxScore} điểm
                              </span>
                            )}
                          </div>
                        </div>

                        {/* Hiển thị đáp án cho câu trắc nghiệm và đúng/sai */}
                        {question.questionType !== 'ESSAY' && (
                          <div className="space-y-2">
                            <p className="text-sm font-medium text-gray-700">Các đáp án:</p>
                            {question.answerOptions?.map((option, optIndex) => (
                              <div
                                key={option.id}
                                className={`text-sm px-3 py-2 rounded ${
                                  option.isCorrect
                                    ? 'bg-green-100 text-green-800 font-medium border border-green-200'
                                    : 'bg-gray-50 text-gray-600'
                                }`}
                              >
                                {String.fromCharCode(65 + optIndex)}. {option.optionText}
                                {option.isCorrect && ' ✓ (Đáp án đúng)'}
                              </div>
                            ))}
                          </div>
                        )}

                        {/* Hiển thị thông tin câu tự luận */}
                        {question.questionType === 'ESSAY' && (
                          <div className="space-y-3">
                            <div className="text-sm">
                              <span className="font-medium text-gray-700">Điểm tối đa:</span>
                              <span className="ml-2 px-2 py-1 bg-orange-100 text-orange-800 rounded">
                                {question.maxScore} điểm
                              </span>
                            </div>
                            {question.essayGuidelines && (
                              <div className="text-sm">
                                <p className="font-medium text-gray-700 mb-2">Hướng dẫn trả lời:</p>
                                <div className="p-3 bg-blue-50 rounded border-l-4 border-blue-200 text-gray-700">
                                  {question.essayGuidelines}
                                </div>
                              </div>
                            )}
                          </div>
                        )}

                        {/* Hiển thị giải thích nếu có */}
                        {question.explanation && (
                          <div className="mt-3 text-sm">
                            <p className="font-medium text-gray-700">Giải thích:</p>
                            <p className="mt-1 text-gray-600">{question.explanation}</p>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                {/* Status and Feedback */}
                <div className="border-t pt-4">
                  <div className="flex justify-between items-center">
                    <div>
                      <span className="text-gray-500">Trạng thái:</span>
                      <span className={`ml-2 px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedSubmission.status)}`}>
                        {getStatusText(selectedSubmission.status)}
                      </span>
                    </div>
                    <div className="text-sm text-gray-500">
                      Ngày tạo: {formatDate(selectedSubmission.createdAt)}
                    </div>
                  </div>
                  
                  {selectedSubmission.adminFeedback && (
                    <div className="mt-3">
                      <span className="text-gray-500">Phản hồi từ admin:</span>
                      <p className="mt-1 p-3 bg-yellow-50 border border-yellow-200 rounded text-gray-700">
                        {selectedSubmission.adminFeedback}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="flex justify-center items-center h-32">
                <div className="text-gray-500">Đang tải...</div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}


