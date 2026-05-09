import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:image_picker/image_picker.dart';
import '../services/api_service.dart';

class YoloDetectionScreen extends StatefulWidget {
  const YoloDetectionScreen({Key? key}) : super(key: key);

  @override
  State<YoloDetectionScreen> createState() => _YoloDetectionScreenState();
}

class _YoloDetectionScreenState extends State<YoloDetectionScreen> {
  XFile? _selectedImage;
  Uint8List? _imageBytes;
  bool _isDetecting = false;
  Map<String, dynamic>? _result;
  final ImagePicker _picker = ImagePicker();

  Future<void> _pickImage() async {
    final XFile? image = await _picker.pickImage(
      source: ImageSource.gallery,
      imageQuality: 80,
    );
    if (image != null) {
      final bytes = await image.readAsBytes();
      setState(() {
        _selectedImage = image;
        _imageBytes = bytes;
        _result = null;
      });
    }
  }

  Future<void> _detectPothole() async {
    if (_imageBytes == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select an image first')),
      );
      return;
    }

    setState(() => _isDetecting = true);

    try {
      final result = await ApiService.detectPothole(
        imageBytes: _imageBytes!,
        latitude: 15.2968,
        longitude: 75.6250,
      );
      setState(() => _result = result);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('❌ Error: $e'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() => _isDetecting = false);
    }
  }

  Color _getSeverityColor(String severity) {
    switch (severity.toUpperCase()) {
      case 'LOW':
        return Colors.green;
      case 'MEDIUM':
        return Colors.orange;
      case 'HIGH':
        return Colors.red;
      case 'CRITICAL':
        return Colors.purple;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0A0E21),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0A0E21),
        title: const Text(
          '🤖 AI Pothole Detection',
          style: TextStyle(color: Colors.white),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Info Card
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF1E88E5).withOpacity(0.1),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: const Color(0xFF1E88E5).withOpacity(0.3),
                ),
              ),
              child: const Row(
                children: [
                  Icon(Icons.info_outline, color: Color(0xFF1E88E5), size: 20),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Upload a road image and YOLOv8 AI will automatically detect potholes!',
                      style: TextStyle(
                        color: Colors.white70,
                        fontSize: 13,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Image Preview
            GestureDetector(
              onTap: _pickImage,
              child: Container(
                width: double.infinity,
                height: 250,
                decoration: BoxDecoration(
                  color: const Color(0xFF1D1E33),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: Colors.grey.shade800),
                ),
                child: _imageBytes != null
                    ? ClipRRect(
                        borderRadius: BorderRadius.circular(20),
                        child: Image.memory(
                          _imageBytes!,
                          fit: BoxFit.cover,
                        ),
                      )
                    : Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.add_photo_alternate_outlined,
                            color: Colors.grey.shade600,
                            size: 60,
                          ),
                          const SizedBox(height: 12),
                          Text(
                            'Tap to select image',
                            style: TextStyle(
                              color: Colors.grey.shade600,
                              fontSize: 14,
                            ),
                          ),
                        ],
                      ),
              ),
            ),
            const SizedBox(height: 16),

            // Pick Image Button
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton.icon(
                icon: const Icon(Icons.photo_library, color: Colors.white),
                label: const Text(
                  'Select Image from Gallery',
                  style: TextStyle(color: Colors.white),
                ),
                onPressed: _pickImage,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF1D1E33),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                    side: BorderSide(color: Colors.grey.shade700),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Detect Button
            SizedBox(
              width: double.infinity,
              height: 55,
              child: ElevatedButton.icon(
                icon: _isDetecting
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Icon(Icons.search, color: Colors.white),
                label: Text(
                  _isDetecting ? 'Detecting...' : '🤖 Detect Pothole',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                onPressed: _isDetecting ? null : _detectPothole,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF1E88E5),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Result
            if (_result != null) ...[
              const Text(
                'Detection Result',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: const Color(0xFF1D1E33),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: _result!['pothole_detected'] == true
                        ? Colors.red.withOpacity(0.3)
                        : Colors.green.withOpacity(0.3),
                  ),
                ),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Icon(
                          _result!['pothole_detected'] == true
                              ? Icons.warning_amber
                              : Icons.check_circle,
                          color: _result!['pothole_detected'] == true
                              ? Colors.red
                              : Colors.green,
                          size: 40,
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                _result!['pothole_detected'] == true
                                    ? '⚠️ Pothole Detected!'
                                    : '✅ No Pothole Found',
                                style: TextStyle(
                                  color: _result!['pothole_detected'] == true
                                      ? Colors.red
                                      : Colors.green,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                _result!['message'] ?? '',
                                style: const TextStyle(
                                  color: Colors.grey,
                                  fontSize: 13,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    if (_result!['pothole_detected'] == true) ...[
                      const Divider(color: Colors.grey, height: 24),
                      _ResultRow(
                        label: 'Confidence',
                        value:
                            '${((_result!['confidence'] ?? 0) * 100).toStringAsFixed(1)}%',
                        color: Colors.white,
                      ),
                      const SizedBox(height: 8),
                      _ResultRow(
                        label: 'Severity',
                        value: _result!['severity'] ?? 'UNKNOWN',
                        color: _getSeverityColor(_result!['severity'] ?? 'LOW'),
                      ),
                      const SizedBox(height: 8),
                      _ResultRow(
                        label: 'Auto Reported',
                        value: _result!['auto_reported'] == true
                            ? '✅ Yes'
                            : '❌ No',
                        color: _result!['auto_reported'] == true
                            ? Colors.green
                            : Colors.red,
                      ),
                    ],
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _ResultRow extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _ResultRow({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 14)),
        Text(value,
            style: TextStyle(
                color: color, fontSize: 14, fontWeight: FontWeight.bold)),
      ],
    );
  }
}
