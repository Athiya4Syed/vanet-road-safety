import 'dart:math';

class DeviceService {
  static String generateDeviceId() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    Random random = Random();
    String deviceId = 'vehicle-';
    for (int i = 0; i < 6; i++) {
      deviceId += chars[random.nextInt(chars.length)];
    }
    return deviceId;
  }
  
  static String getStoredDeviceId() {
    // In production, use shared_preferences to persist
    return generateDeviceId();
  }
}