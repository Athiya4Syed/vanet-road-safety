from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from ultralytics import YOLO
from PIL import Image
import io
import numpy as np
import uvicorn
import os

app = FastAPI(title="VANET YOLOv8 Pothole Detection")

# Allow all origins
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load YOLOv8 model
print("Loading YOLOv8 model...")
model = YOLO('yolov8n.pt')  # Downloads automatically
print("Model loaded!")

@app.get("/")
def root():
    return {
        "service": "VANET YOLOv8 Pothole Detection",
        "status": "running",
        "model": "YOLOv8n"
    }

@app.get("/health")
def health():
    return {
        "status": "✅ Running",
        "model": "YOLOv8n",
        "description": "Pothole detection service"
    }

@app.post("/detect")
async def detect_pothole(file: UploadFile = File(...)):
    try:
        # Read image
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
        
        # Run YOLOv8 detection
        results = model(image)
        
        detections = []
        pothole_detected = False
        confidence = 0.0
        
        for result in results:
            boxes = result.boxes
            if boxes is not None:
                for box in boxes:
                    class_id = int(box.cls[0])
                    class_name = result.names[class_id]
                    conf = float(box.conf[0])
                    
                    # Check if pothole detected
                    if 'pothole' in class_name.lower() or conf > 0.5:
                        pothole_detected = True
                        confidence = max(confidence, conf)
                    
                    detections.append({
                        "class": class_name,
                        "confidence": conf,
                        "bbox": box.xyxy[0].tolist()
                    })
        
        # Determine severity based on confidence
        severity = "LOW"
        if confidence > 0.8:
            severity = "CRITICAL"
        elif confidence > 0.6:
            severity = "HIGH"
        elif confidence > 0.4:
            severity = "MEDIUM"
        
        return {
            "pothole_detected": pothole_detected,
            "confidence": confidence,
            "severity": severity,
            "detections": detections,
            "total_objects": len(detections),
            "message": "Pothole detected!" if pothole_detected else "No pothole detected"
        }
        
    except Exception as e:
        return {
            "error": str(e),
            "pothole_detected": False
        }

@app.post("/detect-and-report")
async def detect_and_report(
    file: UploadFile = File(...),
    latitude: float = 15.2968,
    longitude: float = 75.6250,
    device_id: str = "yolo-detector"
):
    try:
        # Detect pothole
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
        results = model(image)
        
        pothole_detected = False
        confidence = 0.0
        
        for result in results:
            boxes = result.boxes
            if boxes is not None:
                for box in boxes:
                    conf = float(box.conf[0])
                    confidence = max(confidence, conf)
                    if conf > 0.3:
                        pothole_detected = True
        
        if pothole_detected:
            # Determine severity
            severity = "LOW"
            if confidence > 0.8:
                severity = "CRITICAL"
            elif confidence > 0.6:
                severity = "HIGH"
            elif confidence > 0.4:
                severity = "MEDIUM"
            
            # Report to Spring Boot backend
            import requests
            backend_url = os.getenv(
                "BACKEND_URL",
                "https://vanet-road-safety.onrender.com"
            )
            
            try:
                response = requests.post(
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
                "pothole_detected": True,
                "confidence": confidence,
                "severity": severity,
                "auto_reported": reported,
                "message": f"Pothole detected and {'reported!' if reported else 'report failed'}"
            }
        
        return {
            "pothole_detected": False,
            "confidence": confidence,
            "message": "No pothole detected in image"
        }
        
    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)