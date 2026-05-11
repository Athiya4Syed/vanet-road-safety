from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image
import io
import uvicorn
import os
import requests as req
import base64
from ultralytics import YOLO

app = FastAPI(title="VANET YOLOv8 Pothole Detection")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load custom trained model
def load_model():
    paths = [
        'runs/detect/pothole_custom/weights/best.pt',
        'runs/detect/pothole_custom-1/weights/best.pt',
        'runs/detect/pothole_custom-2/weights/best.pt',
        'runs/detect/pothole_custom-3/weights/best.pt',
        'runs/detect/pothole_custom-4/weights/best.pt',
        'runs/detect/pothole_custom-5/weights/best.pt',
        'runs/detect/pothole_custom-6/weights/best.pt',
        'runs/detect/pothole_custom-7/weights/best.pt',
    ]
    for path in paths:
        if os.path.exists(path):
            print(f"✅ Loading trained model: {path}")
            return YOLO(path)
    print("⚠️ No trained model found, using default")
    return YOLO('yolov8n.pt')

model = load_model()

@app.get("/")
def root():
    return {
        "service": "VANET Pothole Detection",
        "status": "running",
        "model": "Custom Trained YOLOv8"
    }

@app.get("/health")
def health():
    return {
        "status": "✅ Running",
        "model": "Custom Trained Pothole Model"
    }

@app.post("/detect")
async def detect_pothole(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))

        results = model(image, conf=0.25)

        pothole_detected = False
        confidence = 0.0

        for result in results:
            boxes = result.boxes
            if boxes is not None and len(boxes) > 0:
                pothole_detected = True
                confidence = float(boxes.conf.max())

        severity = "LOW"
        if confidence > 0.8: severity = "CRITICAL"
        elif confidence > 0.6: severity = "HIGH"
        elif confidence > 0.4: severity = "MEDIUM"

        return {
            "pothole_detected": pothole_detected,
            "confidence": confidence,
            "severity": severity,
            "message": "Pothole detected!" if pothole_detected else "No pothole detected"
        }

    except Exception as e:
        return {"error": str(e), "pothole_detected": False}

@app.post("/detect-and-report")
async def detect_and_report(
    file: UploadFile = File(...),
    latitude: float = 15.2968,
    longitude: float = 75.6250,
    device_id: str = "yolo-detector"
):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))

        results = model(image, conf=0.01)  # Very low confidence

pothole_detected = False
confidence = 0.0

for result in results:
    # Check both boxes and masks
    if result.boxes is not None and len(result.boxes) > 0:
        pothole_detected = True
        confidence = float(result.boxes.conf.max())
    elif hasattr(result, 'masks') and result.masks is not None:
        pothole_detected = True
        confidence = 0.5

        
        severity = "LOW"
        if confidence > 0.8: severity = "CRITICAL"
        elif confidence > 0.6: severity = "HIGH"
        elif confidence > 0.4: severity = "MEDIUM"

        reported = False
        if pothole_detected:
            try:
                backend_url = os.getenv(
                    "BACKEND_URL",
                    "https://vanet-road-safety.onrender.com"
                )
                response = req.post(
                    f"{backend_url}/api/vanet/report",
                    params={
                        "latitude": latitude,
                        "longitude": longitude,
                        "severity": severity,
                        "description": f"Auto-detected by YOLOv8 (confidence: {confidence:.2f})",
                        "deviceId": device_id
                    },
                    timeout=30
                )
                reported = response.status_code == 200
            except:
                reported = False

        return {
            "pothole_detected": pothole_detected,
            "confidence": confidence,
            "severity": severity,
            "auto_reported": reported,
            "message": f"Pothole detected!" if pothole_detected else "No pothole detected"
        }

    except Exception as e:
        return {"error": str(e), "pothole_detected": False}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)