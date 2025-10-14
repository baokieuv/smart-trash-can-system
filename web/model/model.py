import os
import cv2
import numpy as np
import onnxruntime as ort
from typing import List
import argparse
class Yolov11_Onnx:
    def __init__(self, onnx_model_path: str, input_shape: tuple[int, int] = (320, 320), 
                 confidence_threshold: float = 0.3, label_list: List[str] = None):
        """Khởi tạo model ONNX"""
        self.onnx_model_path = onnx_model_path
        self.input_shape = input_shape
        self.confidence_threshold = confidence_threshold
        self.label_list = label_list if label_list else ["Object"]

        # Load mô hình ONNX
        if not os.path.exists(onnx_model_path):
            raise FileNotFoundError(f"Model file not found: {onnx_model_path}")
        self.session = ort.InferenceSession(self.onnx_model_path)
        
    def _preprocessing(self, frame):
        """Tiền xử lý ảnh"""
        if frame is None:
            raise ValueError("Invalid input image")
        original_height, original_width = frame.shape[:2]
        self.original_size = (original_width, original_height)

        # Resize ảnh
        input_img = cv2.resize(frame, self.input_shape)
        input_img = cv2.cvtColor(input_img, cv2.COLOR_BGR2RGB)  # Chuyển sang RGB
        input_img = input_img.transpose(2, 0, 1)  # Đổi từ HWC -> CHW
        input_img = np.ascontiguousarray(input_img) / 255.0
        input_tensor = input_img[np.newaxis, :, :, :].astype(np.float32)

        return input_tensor
    
    def _postprocessing(self, output):
        """Hậu xử lý kết quả mô hình"""
        # output = np.array(output)
        probs = output[0].squeeze()  # (num_classes,)
        idx = int(np.argmax(probs))
        conf = float(probs[idx] * 100)

        label = self.label_list[idx] if self.label_list and idx < len(self.label_list) else f"class_{idx}"
        return label, round(conf, 2)
        
    
    def drawbox(self, frame, label, conf):
        cv2.putText(frame, f"{label} {conf}%", (10, 30), 
                    cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

        return frame
    
    def detect_and_save(self, image_path):
        image_path = image_path.replace("\\", "/")
        print(image_path)
        # image_path = "D:/counting_fasteners_project-3/counting_fasteners_project-3/web/backend/uploads/Screenshot 2025-10-08 161908.png"
        if not os.path.exists(image_path):
            raise FileNotFoundError(f"Image not found: {image_path}")
        
        frame = cv2.imread(image_path)
        if frame is None:
            raise ValueError(f"Failed to load image: {image_path}")


        input_tensor = self._preprocessing(frame)
        
        input_name = self.session.get_inputs()[0].name
        output = self.session.run(None, {input_name: input_tensor})

        label, conf = self._postprocessing(output)
        print(f"Label: {label}, Conf: {conf}")
        
        return label, conf
        
# def main():
#     model: Yolov11_Onnx = Yolov11_Onnx("D:/HUST/soict/project3/best.onnx", label_list=["battery", "biological", "cardboard", "clothes", "glass", "metal", "paper", "plastic", "shoes", "trash"])
    
#     parser = argparse.ArgumentParser(description="Yolov11 Classification")
#     parser.add_argument("--input", type=str, required=True, help="image path")
#     args = parser.parse_args()
    
#     frame = model.detect_and_save(args.input)
    
#     cv2.imshow("Classification result", frame)
#     cv2.waitKey(0)
#     cv2.destroyAllWindows()
# if __name__ == "__main__":
#     main()