# 🤖 Smart Bin AI Model (FastAPI)

API server cho việc phân loại rác thải sử dụng YOLOv11n-cls model với ONNX Runtime.

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ](#-công-nghệ)
- [Model Details](#-model-details)
- [Yêu cầu](#-yêu-cầu)
- [Cài đặt](#-cài-đặt)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [API Endpoints](#-api-endpoints)
- [Testing API](#-testing-api)
- [Model Training](#-model-training)
- [Performance](#-performance)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Giới thiệu

Smart Bin AI Model là một REST API server được xây dựng bằng FastAPI, sử dụng YOLOv11n-cls (ONNX format) để phân loại rác thải. Model được train để nhận diện 10 loại rác và phân loại vào 3 nhóm chính:

- **Recyclable (Tái chế)**: `cardboard`, `paper`, `plastic`, `metal`, `glass`
- **Compostable (Phân hủy)**: `biological`, `clothes`, `shoes`
- **Non-recyclable**: `battery`, `trash`

---

## ✨ Tính năng

### 🔍 Image Classification
- ✅ Nhận diện 10 loại rác khác nhau
- ✅ Trả về label, confidence score, và category
- ✅ Xử lý ảnh từ multipart/form-data upload
- ✅ Hỗ trợ nhiều định dạng ảnh (JPG, PNG, BMP, WebP)
- ✅ Fast inference với ONNX Runtime

### 🚀 API Features
- ✅ RESTful API endpoint
- ✅ CORS enabled (cross-origin requests)
- ✅ Error handling với HTTP status codes
- ✅ Input validation
- ✅ Fast response time (< 100ms)

### 📊 Model Optimization
- ✅ YOLOv11n (nano) - lightweight model
- ✅ ONNX format cho deployment tối ưu
- ✅ CPU inference support
- ✅ Batch processing ready (future)

---

## 🚀 Công nghệ

| Technology | Version | Purpose |
|------------|---------|---------|
| **Python** | 3.8+ | Programming language |
| **FastAPI** | Latest | Web framework |
| **Uvicorn** | Latest | ASGI server |
| **ONNX Runtime** | Latest | Model inference engine |
| **NumPy** | Latest | Array operations |
| **OpenCV** | Latest (headless) | Image processing |
| **Python Multipart** | Latest | File upload handling |

---

## 🧠 Model Details

### YOLOv11n-cls Specifications

| Attribute | Value |
|-----------|-------|
| **Model Type** | Classification |
| **Architecture** | YOLOv11n (nano variant) |
| **Input Size** | 640x640 (RGB) |
| **Output** | 10 classes |
| **Format** | ONNX |
| **Parameters** | ~2.5M |
| **Size** | ~6 MB |
| **Inference Time** | ~50-100ms (CPU) |

### Classes & Categories

```python
LABELS = [
    "battery", "biological", "cardboard", "clothes", "glass",
    "metal", "paper", "plastic", "shoes", "trash"
]

# Category mapping
recyclable = ["cardboard", "paper", "plastic", "metal", "glass"]
compostable = ["biological", "clothes", "shoes"]
non_recyclable = ["battery", "trash"]
```

### Model Files

- **best.onnx**: Production model (ONNX format)
- **best.pt**: PyTorch weights (for reference)
- **model.py**: ONNX inference wrapper class

---

## 📱 Yêu cầu

### System Requirements
- **Python**: 3.8 or higher
- **RAM**: Minimum 2GB (4GB recommended)
- **CPU**: Any modern CPU (no GPU required)
- **Disk**: ~100MB for dependencies + model

### Python Packages

See [requirements.txt](requirements.txt):

```txt
fastapi
uvicorn
onnxruntime
numpy
opencv-python-headless
python-multipart
```

---

## 🔨 Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system/model-fastapi
```

### 2. Tạo virtual environment (khuyến nghị)

**Windows:**
```powershell
python -m venv venv
.\venv\Scripts\activate
```

**Linux/macOS:**
```bash
python3 -m venv venv
source venv/bin/activate
```

### 3. Cài đặt dependencies

```bash
pip install -r requirements.txt
```

### 4. Verify model files

Đảm bảo các file sau tồn tại:

```
model-fastapi/
├── best.onnx          # ✅ Required
├── model.py           # ✅ Required
├── server.py          # ✅ Required
└── requirements.txt
```

---

## 📁 Cấu trúc dự án

```
model-fastapi/
├── best.onnx              # ONNX model file (production)
├── best.pt                # PyTorch weights (backup)
├── model.py               # Yolov11_Onnx inference class
├── server.py              # FastAPI application
├── requirements.txt       # Python dependencies
├── Dockerfile             # Docker container config
├── README.md              # This file
└── __pycache__/           # Python cache
    └── model.cpython-*.pyc
```

---

## 🌐 API Endpoints

### 1. Classify Image

**Endpoint:** `POST /classify`

**Request:**

```http
POST http://localhost:8000/classify
Content-Type: multipart/form-data

image: [binary file]
```

**cURL Example:**

```bash
curl -X POST "http://localhost:8000/classify" \
  -F "image=@waste_sample.jpg"
```

**Python Example:**

```python
import requests

url = "http://localhost:8000/classify"
files = {"image": open("waste_sample.jpg", "rb")}
response = requests.post(url, files=files)
print(response.json())
```

**Response (Success - 200):**

```json
{
  "Label": "plastic",
  "Confident": 0.9523,
  "Category": "recyclable"
}
```

**Response (Error - 400):**

```json
{
  "detail": "Invalid image: ..."
}
```

**Response (Error - 404):**

```json
{
  "detail": "File not found: ..."
}
```

**Response (Error - 500):**

```json
{
  "detail": "Unexpected error: ..."
}
```
---

## 🔧 Chạy ứng dụng

### Development Mode (with auto-reload)

```bash
# Method 1: Direct Python
python server.py

# Method 2: Uvicorn CLI
uvicorn server:app --reload --host 0.0.0.0 --port 8000
```

### Production Mode

```bash
uvicorn server:app --host 0.0.0.0 --port 8000 --workers 4
```

### Docker (Optional)

```bash
# Build image
docker build -t smart-bin-model .

# Run container
docker run -d -p 8000:8000 --name smart-bin-model smart-bin-model
```

### Verify Server is Running

```bash

# Test classification
curl -X POST "http://localhost:8000/classify" \
  -F "image=@test_image.jpg"
```

**Expected Output:**

```
INFO:     Started server process [12345]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

---

## 🧪 Testing API

### Postman

1. Create new request: `POST http://localhost:8000/classify`
2. Select Body → form-data
3. Add key `image` (type: File)
4. Upload image file
5. Send request

### Python Script

Create `test_api.py`:

```python
import requests
import sys

def test_classify(image_path):
    url = "http://localhost:8000/classify"
    
    try:
        with open(image_path, "rb") as f:
            files = {"image": f}
            response = requests.post(url, files=files)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Classification Result:")
            print(f"   Label: {result['Label']}")
            print(f"   Confidence: {result['Confident']:.2%}")
            print(f"   Category: {result['Category']}")
        else:
            print(f"❌ Error {response.status_code}: {response.json()}")
    
    except FileNotFoundError:
        print(f"❌ File not found: {image_path}")
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to server. Is it running?")
    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python test_api.py <image_path>")
    else:
        test_classify(sys.argv[1])
```

Run:

```bash
python test_api.py plastic_bottle.jpg
```

---

## 🎓 Model Training

### Dataset

Model được train trên dataset gồm 10 classes:

- **battery**: Pin, ắc quy
- **biological**: Rác hữu cơ, thực phẩm
- **cardboard**: Bìa carton
- **clothes**: Quần áo
- **glass**: Thủy tinh
- **metal**: Kim loại (lon, sắt)
- **paper**: Giấy
- **plastic**: Nhựa (chai, túi)
- **shoes**: Giày dép
- **trash**: Rác thải khác

### Training Configuration

- **Base Model**: YOLOv11n-cls
- **Input Size**: 640x640
- **Epochs**: 100+
- **Optimizer**: AdamW
- **Data Augmentation**: Yes (rotation, flip, crop, color jitter)

### Export to ONNX

```python
from ultralytics import YOLO

# Load trained model
model = YOLO("best.pt")

# Export to ONNX
model.export(format="onnx")
```

---

## 📊 Performance

### Model Accuracy

| Metric | Value |
|--------|-------|
| **Top-1 Accuracy** | ~92% |
| **Top-3 Accuracy** | ~98% |
| **Average Confidence** | 0.85+ |



---

## 🐛 Troubleshooting

### Model File Not Found

```
RuntimeError: ONNX model not found at path: .../best.onnx
```

**Solution:**
- Ensure `best.onnx` exists in `model-fastapi/` directory
- Check file permissions
- Re-download or re-export model

### Import Error

```
ModuleNotFoundError: No module named 'onnxruntime'
```

**Solution:**
```bash
pip install -r requirements.txt
```

### Port Already in Use

```
ERROR: [Errno 48] error while attempting to bind on address ('0.0.0.0', 8000)
```

**Solution:**
```bash
# Use different port
uvicorn server:app --port 8001

# Or kill process using port 8000
# Windows
netstat -ano | findstr :8000
taskkill /PID <PID> /F

# Linux/macOS
lsof -ti:8000 | xargs kill -9
```

### Low Confidence Scores

```json
{"Label": "trash", "Confident": 0.35, "Category": "nonRecyclable"}
```

**Possible causes:**
- Poor image quality (blur, low light)
- Object not in training dataset
- Image too small or too far
- Multiple objects in image

**Solutions:**
- Use better quality images
- Ensure good lighting
- Center object in frame
- Retrain model with more data

### ONNX Runtime Error

```
onnxruntime.capi.onnxruntime_pybind11_state.Fail: ...
```

**Solution:**
```bash
# Reinstall onnxruntime
pip uninstall onnxruntime
pip install onnxruntime

# Or try CPU-only version
pip install onnxruntime-cpu
```

---

## 🔗 Related Documentation

- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [ONNX Runtime](https://onnxruntime.ai/)
- [YOLOv11 by Ultralytics](https://docs.ultralytics.com/)
- [Backend API Documentation](../backend-springboot/README.md)
- [ESP32-CAM Firmware](../esp32-cam/README.md)

---

## 📄 License

MIT License - See [LICENSE](../LICENSE) file

---

## 👥 Contributors

- Development Team - Smart Bin System Project 3
