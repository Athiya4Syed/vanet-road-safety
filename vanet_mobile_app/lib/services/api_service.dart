import 'package:http/http.dart' as http;
import 'dart:convert';
import 'dart:typed_data';
import '../models/pothole_model.dart';

class ApiService {
  static const String backendUrl =
      'https://vanet-road-safety.onrender.com/api/vanet';
  static const String yoloUrl = 'http://localhost:8000';

  // Wake up server
  static Future<void> wakeUpServer() async {
    try {
      await http
          .get(Uri.parse('$backendUrl/health'))
          .timeout(const Duration(seconds: 60));
    } catch (e) {
      // ignore
    }
  }

  // Report pothole
  static Future<Map<String, dynamic>> reportPothole({
    required double latitude,
    required double longitude,
    required String severity,
    required String description,
    required String deviceId,
  }) async {
    try {
      final uri = Uri.parse(
        '$backendUrl/report?latitude=$latitude&longitude=$longitude'
        '&severity=$severity&description=${Uri.encodeComponent(description)}'
        '&deviceId=$deviceId',
      );
      final response =
          await http.post(uri).timeout(const Duration(seconds: 120));
      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw Exception('Failed: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Error: $e');
    }
  }

  // Get nearby potholes
  static Future<List<PotholeReport>> getNearbyPotholes({
    required double latitude,
    required double longitude,
  }) async {
    try {
      final response = await http
          .get(
            Uri.parse(
                '$backendUrl/nearby?latitude=$latitude&longitude=$longitude'),
          )
          .timeout(const Duration(seconds: 60));
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final List<dynamic> potholes = data['potholes'] ?? [];
        return potholes.map((p) => PotholeReport.fromJson(p)).toList();
      } else {
        throw Exception('Failed to get nearby potholes');
      }
    } catch (e) {
      throw Exception('Error: $e');
    }
  }

  // Get verified potholes
  static Future<List<PotholeReport>> getVerifiedPotholes() async {
    try {
      final response = await http
          .get(
            Uri.parse('$backendUrl/verified'),
          )
          .timeout(const Duration(seconds: 60));
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final List<dynamic> potholes = data['potholes'] ?? [];
        return potholes.map((p) => PotholeReport.fromJson(p)).toList();
      } else {
        throw Exception('Failed to get verified potholes');
      }
    } catch (e) {
      throw Exception('Error: $e');
    }
  }

  // Get health
  static Future<Map<String, dynamic>> getHealth() async {
    try {
      final response = await http
          .get(
            Uri.parse('$backendUrl/health'),
          )
          .timeout(const Duration(seconds: 60));
      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw Exception('Backend not responding');
      }
    } catch (e) {
      throw Exception('Error: $e');
    }
  }

  // Delete pothole
  static Future<void> deletePothole({required int id}) async {
    try {
      await http
          .delete(
            Uri.parse('$backendUrl/report/$id'),
          )
          .timeout(const Duration(seconds: 60));
    } catch (e) {
      throw Exception('Error: $e');
    }
  }

  // Detect pothole with YOLOv8
  static Future<Map<String, dynamic>> detectPothole({
    required Uint8List imageBytes,
    required double latitude,
    required double longitude,
  }) async {
    try {
      var request = http.MultipartRequest(
        'POST',
        Uri.parse(
          '$yoloUrl/detect-and-report'
          '?latitude=$latitude&longitude=$longitude&device_id=flutter-app',
        ),
      );
      request.files.add(
        http.MultipartFile.fromBytes(
          'file',
          imageBytes,
          filename: 'pothole.jpg',
        ),
      );
      var response = await request.send().timeout(const Duration(seconds: 120));
      var responseBody = await response.stream.bytesToString();
      return jsonDecode(responseBody);
    } catch (e) {
      throw Exception('Detection failed: $e');
    }
  }
}
