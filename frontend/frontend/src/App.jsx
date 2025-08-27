import { useState } from "react";
import { Home, Star, Shield, ClipboardList, User, Notebook, LogIn, UserPlus, Trophy, Target } from "lucide-react";

function App() {
  const [active, setActive] = useState("Học");

  const menu = [
    { icon: <Home size={20} />, label: "HỌC" },
    { icon: <Star size={20} />, label: "ĐÓNG GÓP" },
    { icon: <Shield size={20} />, label: "BẢNG XẾP HẠNG" },
    { icon: <ClipboardList size={20} />, label: "NHIỆM VỤ" },
    { icon: <User size={20} />, label: "HỒ SƠ" },
  ];

  const tasks = [
    { title: "Làm đúng 10 câu trong 1 đề", progress: 7, total: 10 },
    { title: "Học 10 phút", progress: 3, total: 10 },
    { title: "Hoàn thành 10 đề", progress: 2, total: 10 },
  ];

  const rankings = [
    { rank: 1, name: "Nguyễn Văn A", score: 128247, medal: "🥇" },
    { rank: 2, name: "Hehe", score: 982988, medal: "🥈" },
    { rank: 3, name: "Nguyễn Thị B", score: 2323, medal: "🥉" },
  ];

  return (
    <div className="min-h-screen flex bg-gray-50 text-gray-800">
      {/* Sidebar */}
      <aside className="w-64 bg-white flex flex-col p-6 space-y-6 border-r border-gray-200 shadow-sm">
        <h1 className="text-2xl font-bold text-green-600">Practizz</h1>
        <nav className="flex flex-col gap-2">
          {menu.map((item) => (
            <button
              key={item.label}
              onClick={() => setActive(item.label)}
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                active === item.label
                  ? "bg-green-50 text-green-600 border border-green-200 shadow-sm"
                  : "text-gray-700 hover:bg-green-50 text-white hover:text-green-600"
              }`}
            >
              {item.icon}
              {item.label}
            </button>
          ))}
        </nav>
      </aside>

      {/* Main */}
      <main className="flex-1 flex flex-col p-6 overflow-y-auto bg-gray-50 min-h-screen">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Tìm kiếm..."
              className="w-full p-3 rounded-lg bg-white border border-gray-200 text-gray-800 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm"
            />
          </div>
        </div>

        {/* Đề xuất */}
        <section className="mb-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Đề xuất</h2>
          <div className="flex gap-4 overflow-x-auto">
            {["Ôn luyện đạo hàm", "Ôn luyện điện tử", "Hóa hữu cơ"].map(
              (title, i) => (
                <div
                  key={i}
                  className="min-w-[220px] border border-gray-200 rounded-lg p-4 bg-white hover:shadow-md transition-shadow"
                >
                  <h3 className="font-bold text-gray-800">{title}</h3>
                  <p className="text-gray-600">Thời gian: 50p</p>
                  <p className="text-gray-600">Số người tham gia: 273829</p>
                  <p className="text-gray-600">Bình luận: 283</p>
                  <p className="text-gray-600">50 câu hỏi</p>
                </div>
              )
            )}
          </div>
        </section>

        {/* Danh sách đề */}
        <section>
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Danh sách đề</h2>
          <div className="grid grid-cols-4 gap-4">
            {Array.from({ length: 12 }).map((_, i) => (
              <div
                key={i}
                className="border border-gray-200 rounded-lg p-4 bg-white hover:shadow-md transition-shadow cursor-pointer"
              >
                <h3 className="font-bold text-gray-800">Ôn luyện đạo hàm</h3>
                <p className="text-gray-600">Thời gian: 50p</p>
                <p className="text-gray-600">Số người tham gia: 273829</p>
                <p className="text-gray-600">Bình luận: 283</p>
                <p className="text-gray-600">50 câu hỏi</p>
              </div>
            ))}
          </div>
          {/* Pagination */}
          <div className="flex justify-center items-center gap-2 mt-6">
            <button className="px-4 py-2 rounded-lg bg-blue-600 text-white shadow-sm">1</button>
            <button className="px-4 py-2 rounded-lg bg-white border border-gray-200 text-gray-600 hover:bg-gray-50">2</button>
            <span className="text-gray-500">...</span>
            <button className="px-4 py-2 rounded-lg bg-white border border-gray-200 text-gray-600 hover:bg-gray-50">10</button>
            <button className="px-4 py-2 rounded-lg bg-white border border-gray-200 text-gray-600 hover:bg-gray-50">Next</button>
          </div>
        </section>

        {/* Footer */}
        <footer className="mt-10 text-center text-sm text-gray-500">
          <span className="mx-2">GIỚI THIỆU</span>|
          <span className="mx-2">QUYỀN RIÊNG TƯ</span>|
          <span className="mx-2">ĐIỀU KHOẢN</span>
        </footer>
      </main>

      {/* Right sidebar */}
      <aside className="w-80 bg-white p-6 space-y-6 border-l border-gray-200 shadow-sm min-h-screen">
        <div className="flex gap-3">
          <button className="flex items-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition shadow-sm">
            <UserPlus size={18} />
            Đăng ký
          </button>
          <button className="flex items-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition shadow-sm">
            <LogIn size={18} />
            Đăng nhập
          </button>
        </div>

        {/* Ranking */}
        <div className="bg-gradient-to-br from-yellow-50 to-orange-50 rounded-xl p-5 border border-yellow-200">
          <div className="flex items-center gap-2 mb-4">
            <Trophy className="text-yellow-600" size={20} />
            <h3 className="font-bold text-gray-800">Bảng xếp hạng</h3>
          </div>
          <div className="space-y-3">
            {rankings.map((user) => (
              <div key={user.rank} className="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm border border-yellow-100">
                <div className="flex items-center gap-3">
                  <span className="text-lg">{user.medal || `${user.rank}.`}</span>
                  <div>
                    <p className="font-medium text-gray-800">{user.name}</p>
                    <p className="text-sm text-gray-600">{user.score.toLocaleString()} điểm</p>
                  </div>
                </div>
                {user.rank <= 3 && (
                  <div className="w-2 h-8 bg-gradient-to-b from-yellow-400 to-yellow-600 rounded-full"></div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Nhiệm vụ */}
        <div className="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl p-5 border border-green-200">
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center gap-2">
              <Target className="text-green-600" size={20} />
              <h3 className="font-bold text-gray-800">Nhiệm vụ hằng ngày</h3>
            </div>
            <a href="#" className="text-blue-600 text-sm font-medium hover:text-blue-700">XEM TẤT CẢ</a>
          </div>
          <div className="space-y-4">
            {tasks.map((task, i) => (
              <div key={i} className="p-3 bg-white rounded-lg shadow-sm border border-green-100">
                <div className="flex justify-between items-center mb-2">
                  <p className="text-sm font-medium text-gray-800">{task.title}</p>
                  <span className="text-xs text-gray-500">{task.progress}/{task.total}</span>
                </div>
                <div className="w-full bg-gray-200 h-2 rounded-full overflow-hidden">
                  <div
                    className="bg-gradient-to-r from-green-400 to-green-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${(task.progress / task.total) * 100}%` }}
                  />
                </div>
                <p className="text-xs text-green-600 mt-1 font-medium">
                  {Math.round((task.progress / task.total) * 100)}% hoàn thành
                </p>
              </div>
            ))}
          </div>
        </div>
      </aside>
    </div>
  );
}

export default App;









