import os
import model
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

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

class DetectReq(BaseModel):
    image_path: str

@app.post("/detect")
async def classify(req: DetectReq):
    try:
        label, conf = model_cls.detect_and_save(req.image_path)

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
    
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)