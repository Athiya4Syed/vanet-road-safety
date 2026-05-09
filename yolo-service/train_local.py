from ultralytics import YOLO
import os

print("🚀 Training YOLOv8 on Pothole Dataset...")

current_dir = os.path.dirname(os.path.abspath(__file__))
data_yaml = os.path.join(current_dir, 'dataset', 'data.yaml')

print(f"Looking for: {data_yaml}")
print(f"File exists: {os.path.exists(data_yaml)}")

model = YOLO('yolov8n.pt')

results = model.train(
    data=data_yaml,
    epochs=50,
    imgsz=640,
    batch=8,
    name='pothole_custom',
    patience=10,
    save=True,
    device='cpu'
)

print("✅ Training Complete!")