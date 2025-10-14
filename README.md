# Smart Trash Classification System

## I. Mục tiêu dự án

Dự án này phát triển một **hệ thống phân loại rác thông minh** sử dụng **YOLOv11n-cls** (đã được convert sang định dạng **ONNX**) để nhận diện **10 loại rác**, được chia thành **3 nhóm chính**:

- **Tái chế (Recyclable)** — ví dụ: cardboard, paper, plastic,...
- **Hữu cơ (Organic)** — ví dụ: clothes, shoes, foods,...
- **Không tái chế (Non-recyclable)** — ví dụ: pin, trash,...

Hệ thống bao gồm 3 phần chính:
- **Model (Python + FastAPI)**: sử dụng YOLOv11n-cls để phân loại ảnh và trả về kết quả (Loại rác, Độ tin cậy, Phân nhóm).
- **Web (Next.js + NestJS)**: giao diện web trực quan giúp tải ảnh, hiển thị kết quả nhận diện và quản lý dữ liệu.
- **ESP32-CAM**: chụp ảnh và gửi lên server để xử lý; sau khi nhận diện, **actuator** điều khiển **mở nắp đúng thùng rác** tương ứng với loại rác được nhận dạng.

---
## II. Cấu trúc thư mục

```
├── esp32/ # code firmware cho esp32
├── web/
│ ├── frontend/ # Next.js frontend
│ └── backend/ # NestJS backend API
| └── model/ # Python + FastAPI
└── README.md
```

---
## III. Tính năng chính

- Phân loại 10 loại rác thành 3 nhóm khác nhau
- Hỗ trợ inference nhanh với mô hình **YOLOv11n-cls (ONNX)**
- Web app hiện đại với **Next.js** (frontend) và **NestJS** (backend)
- FastAPI phục vụ model dưới dạng REST API cho backend gọi
- Hiển thị kết quả chi tiết: **Label**, **Confident**, **Group**
- ESP32-CAM gửi ảnh trực tiếp lên server để nhận diện
- **Actuator (servo motor)** tự động **mở nắp đúng thùng rác**
  - Ví dụ:
    - Rác tái chế → mở thùng 
    - Rác hữu cơ → mở thùng 
    - Rác không tái chế → mở thùng 

---
## IV. Cài đặt

### 1. Yêu cầu hệ thống

- Python 3.8+
- Node.js 20+
- ESP32-CAM (AI Thinker hoặc tương đương)
- Servo hoặc motor điều khiển nắp thùng rác

### 2. Clone project

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system
```

### 3. Cài đặt YOLOv11 (Model) 
- Có thể up ảnh và chạy đơn lẻ trực tiếp trên máy cá nhân

```bash
cd web/model
pip install -r requirements.txt
python server.py
# Model API sẽ chạy tại http://localhost:8000
```

### 4. Cài đặt Web frontend (Next.js)

```bash
cd web/frontend
npm install
npm run build
npm run dev
# Ứng dụng frontend sẽ chạy tại http://localhost:3001
```

### 5.  Cài đặt backend API (NestJS)

```bash
cd web/backend
npm install
npm run build
npm run start
# API sẽ chạy tại http://localhost:3000
```

### 6.  ESP32-CAM (Firmware)

```bash
idf.py set-target esp32
idf.py build
idf.py -p <COM port> flash monitor
```

---
## V. Kết quả và đánh giá
- Top-1 Accuracy: 0.9723440408706665
- Top-5 Accuracy: 0.9986509084701538
  <img width="2240" height="931" alt="image" src="https://github.com/user-attachments/assets/7b69ae24-3f71-44ea-af35-e34f48f27133" />

- Confusion Matrix:
  <img width="3000" height="2250" alt="confusion_matrix_normalized" src="https://github.com/user-attachments/assets/ca442157-73a1-4fa8-b041-277e4b19eba6" />

---
## VI. Demo
<img width="2877" height="1613" alt="image" src="https://github.com/user-attachments/assets/4d042356-82a6-4221-a834-6da6d32cedd2" />
