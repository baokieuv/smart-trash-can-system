import os
import model
from fastapi import FastAPI, HTTPException, File, UploadFile
from datetime import datetime
from pydantic import BaseModel
import numpy as np
import cv2

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "best.onnx")

LABELS = [
    "battery", "biological", "cardboard", "clothes", "glass",
    "metal", "paper", "plastic", "shoes", "trash"
]

recyclable = ["cardboard", "paper", "plastic", "metal", "glass"]
compostable = ["biological", "clothes", "shoes"]
non_recyclable = ["battery", "trash"]

try:
    model_cls = model.Yolov11_Onnx(MODEL_PATH, label_list=LABELS)
except FileNotFoundError:
    raise RuntimeError(f"ONNX model not found at path: {MODEL_PATH}")

app = FastAPI()


@app.post("/classify")
async def classify(image: UploadFile = File(...)):
    try:
        contents = await image.read()
        
        # result_dir = os.path.join(BASE_DIR, "result")
        # os.makedirs(result_dir, exist_ok=True)
        
        # timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        # temp_path = os.path.join(result_dir, f"{timestamp}_{image.filename}")
        # with open(temp_path, "wb") as f:
        #     f.write(contents)
        nparr = np.frombuffer(contents, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        # label, conf = model_cls.detect_and_save(temp_path)

        label, conf = model_cls.detect_from_frame(frame)
        # os.remove(temp_path)
        
        if label in recyclable:
            category = "recyclable"
        elif label in compostable:
            category = "compostable"
        else:
            category = "nonRecyclable"
            
        return {"Label" : str(label), "Confident": float(conf), "Category": category}
    
    except FileNotFoundError as e:
        # 404 - Resource not found
        print("File not found")
        raise HTTPException(status_code=404, detail=f"File not found: {e}")

    except ValueError as e:
        # 400 - Bad request
        print("Invalid image")
        raise HTTPException(status_code=400, detail=f"Invalid image: {e}")

    except Exception as e:
        # 500 - Internal server error
        raise HTTPException(status_code=500, detail=f"Unexpected error: {type(e).__name__}: {e}")
    # finally:
    #     os.remove(temp_path)
    
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)