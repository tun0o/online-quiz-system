import { useState } from "react";
import { Home, Star, Shield, ClipboardList, User, Notebook } from "lucide-react";

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
    { title: "Làm đúng 10 câu trong 1 đề", progress: 0, total: 10 },
    { title: "Học 10 phút", progress: 0, total: 10 },
    { title: "Hoàn thành 10 đề", progress: 0, total: 10 },
  ];

  return (
    <div className="h-screen flex bg-[#0f1b24] text-white">
      {/* Sidebar */}
      <aside className="w-64 bg-[#0d171f] flex flex-col p-6 space-y-6 border-r border-gray-700">
        <h1 className="text-2xl font-bold text-green-400">Practizz</h1>
        <nav className="flex flex-col gap-2">
          {menu.map((item) => (
            <button
              key={item.label}
              onClick={() => setActive(item.label)}
              className={`flex items-center gap-3 px-4 py-2 rounded-lg transition ${
                active === item.label
                  ? "bg-blue-900 text-white"
                  : "text-gray-300 hover:bg-gray-800"
              }`}
            >
              {item.icon}
              {item.label}
            </button>
          ))}
        </nav>
      </aside>

      {/* Main */}
      <main className="flex-1 flex flex-col p-6 overflow-y-auto">
        {/* Search */}
        <div className="mb-6">
          <input
            type="text"
            placeholder="Tìm kiếm..."
            className="w-full p-3 rounded-md bg-gray-800 text-white placeholder-gray-400 focus:outline-none"
          />
        </div>

        {/* Đề xuất */}
        <section className="mb-6">
          <h2 className="text-lg font-semibold mb-3">Đề xuất</h2>
          <div className="flex gap-4 overflow-x-auto">
            {["Ôn luyện đạo hàm", "Ôn luyện điện tử", "Hóa hữu cơ"].map(
              (title, i) => (
                <div
                  key={i}
                  className="min-w-[220px] border border-gray-600 rounded-md p-4 bg-gray-900"
                >
                  <h3 className="font-bold">{title}</h3>
                  <p>Thời gian: 50p</p>
                  <p>Số người tham gia: 273829</p>
                  <p>Bình luận: 283</p>
                  <p>50 câu hỏi</p>
                </div>
              )
            )}
          </div>
        </section>

        {/* Danh sách đề */}
        <section>
          <h2 className="text-lg font-semibold mb-3">Danh sách đề</h2>
          <div className="grid grid-cols-4 gap-4">
            {Array.from({ length: 12 }).map((_, i) => (
              <div
                key={i}
                className="border border-gray-600 rounded-md p-4 bg-gray-900"
              >
                <h3 className="font-bold">Ôn luyện đạo hàm</h3>
                <p>Thời gian: 50p</p>
                <p>Số người tham gia: 273829</p>
                <p>Bình luận: 283</p>
                <p>50 câu hỏi</p>
              </div>
            ))}
          </div>
          {/* Pagination */}
          <div className="flex justify-center items-center gap-2 mt-4">
            <button className="px-3 py-1 rounded-full bg-green-500">1</button>
            <button className="px-3 py-1 rounded-full bg-gray-700">2</button>
            <span>...</span>
            <button className="px-3 py-1 rounded-full bg-gray-700">10</button>
            <button className="px-3 py-1 rounded-full bg-gray-700">Next</button>
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
      <aside className="w-80 bg-[#0d171f] p-6 space-y-6 border-l border-gray-700">
        {/* Ranking */}
        <div className="bg-gray-900 rounded-lg p-4">
          <h3 className="font-semibold mb-3">Bảng xếp hạng!</h3>
          <ol className="space-y-2 text-sm">
            <li>1. Nguyễn Văn A — <span className="text-green-400">128247</span> điểm</li>
            <li>2. Hehe — <span className="text-green-400">982988</span> điểm</li>
            <li>3. Nguyễn Thị B — <span className="text-green-400">2323</span> điểm</li>
          </ol>
        </div>

        {/* Nhiệm vụ */}
        <div className="bg-gray-900 rounded-lg p-4">
          <div className="flex justify-between items-center mb-3">
            <h3 className="font-semibold">Nhiệm vụ hằng ngày</h3>
            <a href="#" className="text-blue-400 text-sm">XEM TẤT CẢ</a>
          </div>
          {tasks.map((task, i) => (
            <div key={i} className="mb-4">
              <p className="text-sm">{task.title}</p>
              <div className="w-full bg-gray-700 h-3 rounded-full overflow-hidden">
                <div
                  className="bg-green-400 h-3"
                  style={{ width: `${(task.progress / task.total) * 100}%` }}
                />
              </div>
              <p className="text-xs text-right">{task.progress}/{task.total}</p>
            </div>
          ))}
        </div>
      </aside>
    </div>
  );
}

export default App;
