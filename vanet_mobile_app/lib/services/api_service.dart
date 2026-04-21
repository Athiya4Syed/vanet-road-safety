import 'package:http/http.dart' as http;
import 'dart:convert';
import '../models/pothole_model.dart';

class ApiService {
  static const String backendUrl = 'http://localhost:8080/api/vanet';

  // Report a pothole
  static Future<PotholeReport> reportPothole({
    required double latitude,
    required double longitude,
    required String severity,
    required String description,
    required String deviceId,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$backendUrl/report?latitude=$latitude&longitude=$longitude&severity=$severity&description=$description&deviceId=$deviceId'),
      ).timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        return PotholeReport.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Failed to report pothole: ${response.statusCode}');
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
      final response = await http.get(
        Uri.parse('$backendUrl/nearby?latitude=$latitude&longitude=$longitude'),
      ).timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final List<dynamic> potholes = data['potholes'];
        return potholes
            .map((p) => PotholeReport.fromJson(p))
            .toList();
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
      final response = await http.get(
        Uri.parse('$backendUrl/verified'),
      ).timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final List<dynamic> potholes = data['potholes'];
        return potholes
            .map((p) => PotholeReport.fromJson(p))
            .toList();
      } else {
        throw Exception('Failed to get verified potholes');
      }
    } catch (e) {
      throw Exception('Error: $e');
    }
  }

  // Get health/status
  static Future<Map<String, dynamic>> getHealth() async {
    try {
      final response = await http.get(
        Uri.parse('$backendUrl/health'),
      ).timeout(const Duration(seconds: 5));

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw Exception('Backend not responding');
      }
    } catch (e) {
      throw Exception('Error: $e');
    }
  }
}