# Stage 1: Base image
FROM python:3.13-slim AS base

WORKDIR /app

# Copy requirements trước để tận dụng cache
COPY requirements.txt .

# Cài dependencies (và onnxruntime nếu cần)
RUN pip install --no-cache-dir -r requirements.txt

# Copy toàn bộ code vào container
COPY . .

# Expose port 8000 cho FastAPI
EXPOSE 8000

# Lệnh chạy app (dùng uvicorn)
CMD ["uvicorn", "server:app", "--host", "0.0.0.0", "--port", "8000"]
