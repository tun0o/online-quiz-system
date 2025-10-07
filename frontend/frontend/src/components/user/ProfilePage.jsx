import React, { useState, useEffect, useRef } from 'react';
import { User, Mail, Phone, MapPin, School, Calendar, Shield, Edit, Copy, Check, Upload, X, Camera } from 'lucide-react';
import { userProfileService } from '../../services/userProfileService';
import { fileUploadService } from '../../services/fileUploadService';
import { toast } from 'react-toastify';

const ProfilePage = () => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [copiedField, setCopiedField] = useState(null);
    
    // Avatar upload states
    const [avatarLoading, setAvatarLoading] = useState(false);
    const [avatarPreview, setAvatarPreview] = useState(null);
    const [showAvatarModal, setShowAvatarModal] = useState(false);
    const fileInputRef = useRef(null);
    
    // Edit modal states
    const [showEditModal, setShowEditModal] = useState(false);
    const [editField, setEditField] = useState('');
    const [editValue, setEditValue] = useState('');
    const [editLoading, setEditLoading] = useState(false);
    
    // Change password modal states
    const [showChangePasswordModal, setShowChangePasswordModal] = useState(false);
    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [passwordLoading, setPasswordLoading] = useState(false);

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            setLoading(true);
            console.log('Fetching profile...');
            const profileData = await userProfileService.getMyProfile();
            console.log('Profile data received:', profileData);
            console.log('Profile fields:', {
                userId: profileData?.userId,
                fullName: profileData?.fullName,
                email: profileData?.email,
                emergencyPhone: profileData?.emergencyPhone,
                oauth2Phone: profileData?.oauth2Phone,
                avatarUrl: profileData?.avatarUrl,
                dateOfBirth: profileData?.dateOfBirth,
                gender: profileData?.gender,
                province: profileData?.province,
                school: profileData?.school,
                grade: profileData?.grade,
                goal: profileData?.goal,
                bio: profileData?.bio
            });
            setProfile(profileData);
        } catch (error) {
            console.error('Error fetching profile:', error);
            console.error('Error response:', error.response?.data);
            toast.error('Không thể tải thông tin hồ sơ');
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (field, currentValue) => {
        setEditField(field);
        setEditValue(currentValue || '');
        setShowEditModal(true);
    };

    const handleSaveEdit = async () => {
        console.log('🔥 handleSaveEdit called!');
        console.log('🔥 editField:', editField);
        console.log('🔥 editValue:', editValue);
        console.log('🔥 editLoading:', editLoading);
        
        try {
            setEditLoading(true);
            console.log('🔥 setEditLoading(true) called');
            console.log('Saving field:', editField, 'Value:', editValue);
            
            // Validation cơ bản
            if (editField === 'fullName' && editValue && editValue.length < 2) {
                toast.error('Tên phải có ít nhất 2 ký tự');
                return;
            }
            
            if (editField === 'emergencyPhone' && editValue && !/^(\+84|0)[0-9]{9,10}$/.test(editValue)) {
                toast.error('Số điện thoại không đúng định dạng Việt Nam');
                return;
            }
            
            if (editField === 'email' && editValue && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editValue)) {
                toast.error('Email không đúng định dạng');
                return;
            }
            
            // Chuẩn bị dữ liệu theo format UserProfileUpdateRequest
            const updateData = {};
            let value = editValue;
            
            // Xử lý giá trị rỗng
            if (value === '' || value === null || value === undefined) {
                value = null;
            }
            
            // Map frontend field names to backend DTO field names
            const fieldMapping = {
                'fullName': 'fullName',
                'email': 'email', 
                'emergencyPhone': 'emergencyPhone',
                'dateOfBirth': 'dateOfBirth',
                'gender': 'gender',
                'province': 'province',
                'school': 'school',
                'grade': 'grade',
                'goal': 'goal',
                'bio': 'bio'
            };
            
            const backendField = fieldMapping[editField] || editField;
            updateData[backendField] = value;
            
            console.log('Sending update data:', updateData);
            
            const updatedProfile = await userProfileService.updateProfile(updateData);
            console.log('Updated profile received:', updatedProfile);
            
            setProfile(updatedProfile);
            setShowEditModal(false);
            setEditField('');
            setEditValue('');
            toast.success('Cập nhật thành công');
        } catch (error) {
            console.error('Error updating profile:', error);
            console.error('Error response:', error.response?.data);
            
            // Xử lý các loại lỗi khác nhau
            if (error.code === 'ERR_TOO_MANY_REDIRECTS' || error.code === 'ERR_NETWORK') {
                toast.error('Kết nối bị gián đoạn. Vui lòng đăng nhập lại.');
                // Clear tokens và redirect
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else if (error.response?.status === 401) {
                toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else if (error.response?.status === 403) {
                toast.error('Bạn không có quyền thực hiện thao tác này.');
            } else if (error.response?.status >= 500) {
                toast.error('Lỗi máy chủ. Vui lòng thử lại sau.');
            } else {
                toast.error(`Cập nhật thất bại: ${error.response?.data?.error || error.message}`);
            }
        } finally {
            setEditLoading(false);
        }
    };

    const handleCancelEdit = () => {
        setShowEditModal(false);
        setEditField('');
        setEditValue('');
    };

    // Change password handlers
    const handleChangePassword = () => {
        setShowChangePasswordModal(true);
        setPasswordData({
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
        });
    };

    const handlePasswordChange = (field, value) => {
        setPasswordData(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const handleSavePassword = async () => {
        // Validation
        if (!passwordData.currentPassword) {
            toast.error('Vui lòng nhập mật khẩu hiện tại');
            return;
        }
        if (!passwordData.newPassword) {
            toast.error('Vui lòng nhập mật khẩu mới');
            return;
        }
        if (passwordData.newPassword.length < 6) {
            toast.error('Mật khẩu mới phải có ít nhất 6 ký tự');
            return;
        }
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            toast.error('Mật khẩu xác nhận không khớp');
            return;
        }
        if (passwordData.currentPassword === passwordData.newPassword) {
            toast.error('Mật khẩu mới phải khác mật khẩu hiện tại');
            return;
        }

        setPasswordLoading(true);
        try {
            // Gọi API đổi mật khẩu
            const response = await fetch('http://localhost:8080/api/auth/change-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
                },
                body: JSON.stringify({
                    currentPassword: passwordData.currentPassword,
                    newPassword: passwordData.newPassword
                })
            });

            if (response.ok) {
                toast.success('Đổi mật khẩu thành công!');
                setShowChangePasswordModal(false);
                setPasswordData({
                    currentPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                });
            } else {
                const errorData = await response.json();
                toast.error(errorData.message || 'Đổi mật khẩu thất bại');
            }
        } catch (error) {
            console.error('Change password error:', error);
            toast.error('Có lỗi xảy ra khi đổi mật khẩu');
        } finally {
            setPasswordLoading(false);
        }
    };

    const handleCancelPassword = () => {
        setShowChangePasswordModal(false);
        setPasswordData({
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
        });
    };

    const handleCopy = async (text, field) => {
        try {
            await navigator.clipboard.writeText(text);
            setCopiedField(field);
            setTimeout(() => setCopiedField(null), 2000);
            toast.success('Đã sao chép');
        } catch (error) {
            toast.error('Không thể sao chép');
        }
    };

    // Avatar upload functions
    const handleAvatarClick = () => {
        setShowAvatarModal(true);
    };

    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        if (file) {
            // Validate file
            const errors = fileUploadService.validateImageFile(file);
            if (errors.length > 0) {
                toast.error(errors[0]);
                return;
            }
            
            // Create preview
            const previewUrl = fileUploadService.createPreviewUrl(file);
            setAvatarPreview({ file, previewUrl });
        }
    };

    const handleAvatarUpload = async () => {
        if (!avatarPreview) return;
        
        try {
            setAvatarLoading(true);
            console.log('Uploading avatar...');
            
            const result = await fileUploadService.uploadAvatar(avatarPreview.file);
            
            if (result.success) {
                // Update profile with new avatar URL
                setProfile(prev => ({
                    ...prev,
                    avatarUrl: result.fileUrl
                }));
                
                toast.success('Cập nhật ảnh đại diện thành công');
                setShowAvatarModal(false);
                setAvatarPreview(null);
            } else {
                toast.error(result.error || 'Upload thất bại');
            }
        } catch (error) {
            console.error('Avatar upload error:', error);
            toast.error(error.response?.data?.error || 'Upload thất bại');
        } finally {
            setAvatarLoading(false);
        }
    };

    const handleAvatarDelete = async () => {
        try {
            setAvatarLoading(true);
            console.log('Deleting avatar...');
            
            const result = await fileUploadService.deleteAvatar();
            
            if (result.success) {
                // Update profile to remove avatar
                setProfile(prev => ({
                    ...prev,
                    avatarUrl: null
                }));
                
                toast.success('Xóa ảnh đại diện thành công');
                setShowAvatarModal(false);
            } else {
                toast.error(result.error || 'Xóa thất bại');
            }
        } catch (error) {
            console.error('Avatar delete error:', error);
            toast.error(error.response?.data?.error || 'Xóa thất bại');
        } finally {
            setAvatarLoading(false);
        }
    };

    const handleCloseAvatarModal = () => {
        setShowAvatarModal(false);
        setAvatarPreview(null);
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Chưa cập nhật';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600">Đang tải thông tin...</p>
                </div>
            </div>
        );
    }

    if (!profile) {
        return (
            <div className="text-center py-10">
                <p className="text-gray-600">Không thể tải thông tin hồ sơ</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-green-50 to-white">
            {/* Header with Avatar */}
            <div className="bg-gradient-to-r from-green-50 to-green-100">
                <div className="max-w-6xl mx-auto px-6 py-8">
                    <div className="flex items-center gap-6">
                        {/* Profile Picture */}
                        <div className="relative">
                            <div className="w-20 h-20 bg-green-200 rounded-full flex items-center justify-center border-2 border-white shadow-sm">
                                {profile.avatarUrl ? (
                                    <img 
                                        src={profile.avatarUrl} 
                                        alt="Avatar" 
                                        className="w-20 h-20 rounded-full object-cover"
                                    />
                                ) : (
                                    <User size={32} className="text-green-700" />
                                )}
                            </div>
                        </div>
                        
                        {/* Change Avatar Button */}
                        <button 
                            onClick={handleAvatarClick}
                            className="px-4 py-2.5 bg-white text-gray-700 rounded-lg hover:bg-gray-50 transition-all duration-200 text-sm font-medium border border-gray-200 shadow-sm hover:shadow-md"
                        >
                            Thay đổi ảnh đại diện
                        </button>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-6xl mx-auto px-6 py-6">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* Left Column - Account Information */}
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                        <h2 className="text-xl font-bold text-gray-800 mb-6">Thông tin tài khoản</h2>
                        
                        {/* User ID */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <span className="text-gray-600 font-bold text-sm">#</span>
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">ID người dùng</p>
                                    <p className="font-bold text-gray-800">USR{profile?.userId || 'N/A'}</p>
                                </div>
                            </div>
                            <button 
                                onClick={() => handleCopy(profile.userId?.toString(), 'userId')}
                                className="px-3 py-1 text-xs text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded transition"
                            >
                                {copiedField === 'userId' ? 'Đã sao chép' : 'Sao chép'}
                            </button>
                        </div>

                        {/* Phone Number */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <Phone size={16} className="text-gray-600" />
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Số điện thoại</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.emergencyPhone || profile?.oauth2Phone || 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('emergencyPhone', profile.emergencyPhone)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Email */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <Mail size={16} className="text-gray-600" />
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Email</p>
                                    <p className="font-medium text-gray-800">{profile?.email || 'Chưa cập nhật'}</p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('email', profile.email)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Password */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <Shield size={16} className="text-gray-600" />
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Mật khẩu</p>
                                    <p className="font-medium text-gray-800">........</p>
                                </div>
                            </div>
                            <button 
                                onClick={handleChangePassword}
                                className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                            >
                                Chỉnh sửa
                            </button>
                        </div>

                        {/* Links */}
                        <div className="flex items-center justify-between py-3">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <span className="text-gray-600 text-sm">🔗</span>
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Liên kết</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.oauth2Provider === 'facebook' ? 'Đã liên kết' : 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <button className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition">
                                Chỉnh sửa
                            </button>
                        </div>

                        {/* Tip */}
                        <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                            <p className="text-sm text-gray-600">
                                <span className="font-medium">(*) Mẹo:</span> Cập nhật số điện thoại hoặc email để đăng nhập và khôi phục mật khẩu thuận tiện hơn.
                            </p>
                        </div>
                    </div>

                    {/* Right Column - Personal Information */}
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                        <h2 className="text-xl font-bold text-gray-800 mb-6">Thông tin cá nhân</h2>
                        
                        <div className="mb-4 p-3 bg-gray-50 rounded-lg">
                            <p className="text-sm text-gray-600">
                                Thông tin cá nhân chính xác giúp tránh nhầm lẫn trong lớp học hoặc kỳ thi.
                            </p>
                        </div>

                        {/* Full Name */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <User size={16} className="text-gray-600" />
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Tên</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.fullName || 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('fullName', profile.fullName)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Date of Birth */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <Calendar size={16} className="text-gray-600" />
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Ngày sinh</p>
                                    <p className="font-medium text-gray-800">
                                        {formatDate(profile?.dateOfBirth)}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('dateOfBirth', profile.dateOfBirth)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Province */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <MapPin size={16} className="text-gray-600" />
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Tỉnh</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.province || 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('province', profile.province)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* School */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <School size={16} className="text-gray-600" />
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Trường</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.school || 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('school', profile.school)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Grade */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <span className="text-gray-600 text-sm">🎓</span>
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Cấp độ học</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.grade || 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('grade', profile.grade)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Goal */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <span className="text-gray-600 text-sm">🎯</span>
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Mục tiêu</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.goal || 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('goal', profile.goal)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Gender */}
                        <div className="flex items-center justify-between py-3 border-b border-gray-200">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <span className="text-gray-600 text-sm">👤</span>
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Giới tính</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.gender === 'MALE' ? 'Nam' : 
                                         profile?.gender === 'FEMALE' ? 'Nữ' : 
                                         profile?.gender === 'OTHER' ? 'Khác' : 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('gender', profile.gender)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>

                        {/* Bio */}
                        <div className="flex items-center justify-between py-3">
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                    <span className="text-gray-600 text-sm">📝</span>
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-sm text-gray-600">Tiểu sử</p>
                                    <p className="font-medium text-gray-800">
                                        {profile?.bio || 'Chưa cập nhật'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2 flex-shrink-0">
                                <button 
                                    onClick={() => handleEdit('bio', profile.bio)}
                                    className="px-3 py-1 bg-green-100 text-green-600 text-xs rounded hover:bg-green-200 transition"
                                >
                                    Chỉnh sửa
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Avatar Upload Modal */}
            {showAvatarModal && (
                <div className="fixed inset-0 bg-black bg-opacity-30 backdrop-blur-sm flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 w-full max-w-md mx-4 shadow-2xl">
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="text-lg font-semibold text-gray-800">Thay đổi ảnh đại diện</h3>
                            <button 
                                onClick={handleCloseAvatarModal}
                                className="text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        {/* Current Avatar */}
                        <div className="text-center mb-4">
                            <div className="w-24 h-24 bg-gray-50 rounded-full mx-auto mb-3 flex items-center justify-center border-2 border-gray-200 shadow-sm">
                                {avatarPreview ? (
                                    <img 
                                        src={avatarPreview.previewUrl} 
                                        alt="Preview" 
                                        className="w-24 h-24 rounded-full object-cover shadow-md"
                                    />
                                ) : profile.avatarUrl ? (
                                    <img 
                                        src={profile.avatarUrl} 
                                        alt="Current Avatar" 
                                        className="w-24 h-24 rounded-full object-cover shadow-md"
                                    />
                                ) : (
                                    <User size={32} className="text-gray-400" />
                                )}
                            </div>
                            <p className="text-sm text-gray-600 font-medium">
                                {avatarPreview ? 'Ảnh mới' : 'Ảnh hiện tại'}
                            </p>
                        </div>

                        {/* File Input */}
                        <div className="mb-4">
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/*"
                                onChange={handleFileSelect}
                                className="hidden"
                            />
                            <button
                                onClick={() => fileInputRef.current?.click()}
                                className="w-full flex items-center justify-center gap-2 px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg hover:border-green-500 hover:bg-green-50 transition-all duration-200 hover:shadow-sm"
                            >
                                <Upload size={20} className="text-gray-500" />
                                <span className="text-gray-700 font-medium">Chọn ảnh mới</span>
                            </button>
                            <p className="text-xs text-gray-500 mt-2 text-center">
                                Hỗ trợ: JPEG, PNG, GIF, WebP (tối đa 5MB)
                            </p>
                        </div>

                        {/* Action Buttons */}
                        <div className="flex gap-3">
                            {profile.avatarUrl && (
                                <button
                                    onClick={handleAvatarDelete}
                                    disabled={avatarLoading}
                                    className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 disabled:opacity-50 transition-all duration-200 font-medium"
                                >
                                    <X size={16} />
                                    Xóa ảnh
                                </button>
                            )}
                            
                            {avatarPreview && (
                                <button
                                    onClick={handleAvatarUpload}
                                    disabled={avatarLoading}
                                    className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-all duration-200 font-medium shadow-sm"
                                >
                                    {avatarLoading ? (
                                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                    ) : (
                                        <Upload size={16} />
                                    )}
                                    {avatarLoading ? 'Đang tải...' : 'Cập nhật'}
                                </button>
                            )}
                        </div>

                        <button
                            onClick={handleCloseAvatarModal}
                            className="w-full mt-3 px-4 py-2.5 text-gray-600 hover:text-gray-800 hover:bg-gray-50 rounded-lg transition-all duration-200 font-medium"
                        >
                            Hủy
                        </button>
                    </div>
                </div>
            )}

            {/* Edit Field Modal */}
            {showEditModal && (
                <div className="fixed inset-0 bg-black bg-opacity-30 backdrop-blur-sm flex items-center justify-center z-50 transition-all duration-300">
                    <div className="bg-white rounded-xl p-6 w-full max-w-md mx-4 shadow-2xl border border-gray-200 transform transition-all duration-300 scale-100">
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="text-lg font-semibold text-gray-800">
                                Chỉnh sửa {editField === 'fullName' ? 'Tên' : 
                                         editField === 'emergencyPhone' ? 'Số điện thoại' :
                                         editField === 'email' ? 'Email' :
                                         editField === 'dateOfBirth' ? 'Ngày sinh' :
                                         editField === 'province' ? 'Tỉnh' :
                                         editField === 'school' ? 'Trường' :
                                         editField === 'grade' ? 'Cấp độ học' :
                                         editField === 'goal' ? 'Mục tiêu' :
                                         editField === 'gender' ? 'Giới tính' :
                                         editField === 'bio' ? 'Tiểu sử' : 'Thông tin'}
                            </h3>
                            <button 
                                onClick={handleCancelEdit}
                                className="text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        {/* Instruction */}
                        <p className="text-sm text-gray-600 mb-4">
                            Bạn sẽ cần xác thực thông tin sau khi cập nhật
                        </p>

                        {/* Input Field */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                {editField === 'fullName' ? 'Tên' : 
                                 editField === 'emergencyPhone' ? 'Số điện thoại' :
                                 editField === 'email' ? 'Email' :
                                 editField === 'dateOfBirth' ? 'Ngày sinh' :
                                 editField === 'province' ? 'Tỉnh' :
                                 editField === 'school' ? 'Trường' :
                                 editField === 'grade' ? 'Cấp độ học' :
                                 editField === 'goal' ? 'Mục tiêu' :
                                 editField === 'gender' ? 'Giới tính' :
                                 editField === 'bio' ? 'Tiểu sử' : 'Giá trị'}
                            </label>
                            
                            {editField === 'gender' ? (
                                <select
                                    value={editValue}
                                    onChange={(e) => setEditValue(e.target.value)}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 text-gray-800"
                                >
                                    <option value="">Chọn giới tính</option>
                                    <option value="MALE">Nam</option>
                                    <option value="FEMALE">Nữ</option>
                                    <option value="OTHER">Khác</option>
                                </select>
                            ) : editField === 'bio' ? (
                                <textarea
                                    value={editValue}
                                    onChange={(e) => setEditValue(e.target.value)}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 h-24 resize-none transition-all duration-200 text-gray-800 placeholder-gray-400"
                                    placeholder="Nhập tiểu sử của bạn..."
                                />
                            ) : (
                                <input
                                    type={editField === 'dateOfBirth' ? 'date' : 
                                          editField === 'email' ? 'email' : 'text'}
                                    value={editValue}
                                    onChange={(e) => setEditValue(e.target.value)}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 text-gray-800 placeholder-gray-400"
                                    placeholder={`Nhập ${editField === 'fullName' ? 'tên' : 
                                                      editField === 'emergencyPhone' ? 'số điện thoại' :
                                                      editField === 'email' ? 'email' :
                                                      editField === 'dateOfBirth' ? 'ngày sinh' :
                                                      editField === 'province' ? 'tỉnh' :
                                                      editField === 'school' ? 'trường' :
                                                      editField === 'grade' ? 'cấp độ học' :
                                                      editField === 'goal' ? 'mục tiêu' : 'thông tin'}...`}
                                />
                            )}
                        </div>

                        {/* Action Buttons */}
                        <div className="flex gap-3">
                            <button
                                onClick={handleCancelEdit}
                                className="flex-1 px-4 py-3 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-all duration-200 font-medium border border-gray-200"
                            >
                                Hủy
                            </button>
                            <button
                                onClick={() => {
                                    console.log('🔥 Save button clicked!');
                                    console.log('🔥 editLoading:', editLoading);
                                    console.log('🔥 editField:', editField);
                                    console.log('🔥 editValue:', editValue);
                                    handleSaveEdit();
                                }}
                                disabled={editLoading}
                                className="flex-1 px-4 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-all duration-200 font-medium shadow-sm hover:shadow-md"
                            >
                                {editLoading ? (
                                    <div className="flex items-center justify-center gap-2">
                                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                        Đang lưu...
                                    </div>
                                ) : (
                                    'Lưu'
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Change Password Modal */}
            {showChangePasswordModal && (
                <div className="fixed inset-0 bg-black bg-opacity-30 backdrop-blur-sm flex items-center justify-center z-50 transition-all duration-300">
                    <div className="bg-white rounded-xl p-6 w-full max-w-md mx-4 shadow-2xl border border-gray-200 transform transition-all duration-300 scale-100">
                        <div className="flex items-center justify-between mb-6">
                            <h3 className="text-xl font-bold text-gray-800">Đổi mật khẩu</h3>
                            <button
                                onClick={handleCancelPassword}
                                className="text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        <div className="space-y-4">
                            {/* Current Password */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Mật khẩu hiện tại
                                </label>
                                <input
                                    type="password"
                                    value={passwordData.currentPassword}
                                    onChange={(e) => handlePasswordChange('currentPassword', e.target.value)}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all duration-200"
                                    placeholder="Nhập mật khẩu hiện tại"
                                />
                            </div>

                            {/* New Password */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Mật khẩu mới
                                </label>
                                <input
                                    type="password"
                                    value={passwordData.newPassword}
                                    onChange={(e) => handlePasswordChange('newPassword', e.target.value)}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all duration-200"
                                    placeholder="Nhập mật khẩu mới"
                                />
                            </div>

                            {/* Confirm Password */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Xác nhận mật khẩu mới
                                </label>
                                <input
                                    type="password"
                                    value={passwordData.confirmPassword}
                                    onChange={(e) => handlePasswordChange('confirmPassword', e.target.value)}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all duration-200"
                                    placeholder="Nhập lại mật khẩu mới"
                                />
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div className="flex gap-3 mt-6">
                            <button
                                onClick={handleCancelPassword}
                                className="flex-1 px-4 py-3 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-all duration-200 font-medium border border-gray-200"
                            >
                                Hủy
                            </button>
                            <button
                                onClick={handleSavePassword}
                                disabled={passwordLoading}
                                className="flex-1 px-4 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-all duration-200 font-medium shadow-sm hover:shadow-md"
                            >
                                {passwordLoading ? (
                                    <div className="flex items-center justify-center gap-2">
                                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                        Đang đổi...
                                    </div>
                                ) : (
                                    'Đổi mật khẩu'
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProfilePage;