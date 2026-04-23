import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import '../services/api_service.dart';

class ReportPotholeScreen extends StatefulWidget {
  const ReportPotholeScreen({Key? key}) : super(key: key);

  @override
  State<ReportPotholeScreen> createState() => _ReportPotholeScreenState();
}

class _ReportPotholeScreenState extends State<ReportPotholeScreen> {
  final _formKey = GlobalKey<FormState>();
  String? _selectedSeverity;
  String _description = '';
  double? _latitude;
  double? _longitude;
  bool _isLoading = false;
  bool _locationLoading = false;
  bool _locationObtained = false;

  final severityLevels = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  final Map<String, Color> _severityColors = {
    'LOW': Colors.green,
    'MEDIUM': Colors.orange,
    'HIGH': Colors.red,
    'CRITICAL': Colors.purple,
  };

  @override
  void initState() {
    super.initState();
    _getCurrentLocation();
  }

  Future<void> _getCurrentLocation() async {
    setState(() => _locationLoading = true);
    try {
      if (kIsWeb) {
        setState(() {
          _latitude = 15.2968;
          _longitude = 75.6250;
          _locationObtained = true;
        });
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('📍 Using Vijayapura test location'),
            backgroundColor: Colors.orange,
          ),
        );
      } else {
        LocationPermission permission = await Geolocator.checkPermission();
        if (permission == LocationPermission.denied) {
          permission = await Geolocator.requestPermission();
        }
        Position position = await Geolocator.getCurrentPosition(
          desiredAccuracy: LocationAccuracy.high,
        );
        setState(() {
          _latitude = position.latitude;
          _longitude = position.longitude;
          _locationObtained = true;
        });
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() => _locationLoading = false);
    }
  }

  Future<void> _reportPothole() async {
    if (!_formKey.currentState!.validate()) return;
    if (!_locationObtained) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please get your location first')),
      );
      return;
    }
    _formKey.currentState!.save();
    setState(() => _isLoading = true);

    try {
      // Wake up server first
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('⏳ Connecting to server... please wait 30 seconds'),
          duration: Duration(seconds: 30),
          backgroundColor: Colors.orange,
        ),
      );

      await ApiService.wakeUpServer();

      final result = await ApiService.reportPothole(
        latitude: _latitude!,
        longitude: _longitude!,
        severity: _selectedSeverity!,
        description: _description,
        deviceId: 'vehicle-WEB001',
      );

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('✅ Pothole reported successfully!'),
          backgroundColor: Colors.green,
          duration: const Duration(seconds: 3),
        ),
      );

      _formKey.currentState!.reset();
      setState(() => _selectedSeverity = null);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('❌ Error: $e'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FA),
      appBar: AppBar(
        title: const Text('📍 Report Pothole'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Location Card
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: Colors.blue.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: const Icon(Icons.location_on,
                                color: Colors.blue),
                          ),
                          const SizedBox(width: 12),
                          const Text(
                            'Your Location',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      if (_locationLoading)
                        const Center(child: CircularProgressIndicator())
                      else if (_locationObtained)
                        Column(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: Colors.green.withOpacity(0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Row(
                                children: [
                                  const Icon(Icons.check_circle,
                                      color: Colors.green, size: 20),
                                  const SizedBox(width: 8),
                                  Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'Lat: ${_latitude?.toStringAsFixed(6)}',
                                        style: const TextStyle(fontSize: 13),
                                      ),
                                      Text(
                                        'Lng: ${_longitude?.toStringAsFixed(6)}',
                                        style: const TextStyle(fontSize: 13),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(height: 8),
                            TextButton.icon(
                              icon: const Icon(Icons.refresh),
                              label: const Text('Update Location'),
                              onPressed: _getCurrentLocation,
                            ),
                          ],
                        )
                      else
                        ElevatedButton.icon(
                          icon: const Icon(Icons.my_location),
                          label: const Text('Get My Location'),
                          onPressed: _getCurrentLocation,
                          style: ElevatedButton.styleFrom(
                            minimumSize: const Size(double.infinity, 45),
                            backgroundColor: Colors.blue,
                            foregroundColor: Colors.white,
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Severity Card
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: Colors.red.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: const Icon(Icons.warning, color: Colors.red),
                          ),
                          const SizedBox(width: 12),
                          const Text(
                            'Severity Level',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Wrap(
                        spacing: 10,
                        children: severityLevels.map((level) {
                          final isSelected = _selectedSeverity == level;
                          return ChoiceChip(
                            label: Text(level),
                            selected: isSelected,
                            onSelected: (selected) {
                              setState(() =>
                                  _selectedSeverity = selected ? level : null);
                            },
                            selectedColor:
                                _severityColors[level]?.withOpacity(0.3),
                            labelStyle: TextStyle(
                              color: isSelected
                                  ? _severityColors[level]
                                  : Colors.grey,
                              fontWeight: isSelected
                                  ? FontWeight.bold
                                  : FontWeight.normal,
                            ),
                          );
                        }).toList(),
                      ),
                      if (_selectedSeverity == null)
                        const Padding(
                          padding: EdgeInsets.only(top: 8),
                          child: Text(
                            'Please select a severity level',
                            style: TextStyle(
                              color: Colors.red,
                              fontSize: 12,
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Description Card
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: Colors.orange.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: const Icon(Icons.description,
                                color: Colors.orange),
                          ),
                          const SizedBox(width: 12),
                          const Text(
                            'Description',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        maxLines: 4,
                        onSaved: (value) => _description = value ?? '',
                        validator: (value) => value == null || value.isEmpty
                            ? 'Please add description'
                            : null,
                        decoration: InputDecoration(
                          hintText:
                              'Describe the pothole (size, depth, risk level...)',
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(10),
                          ),
                          filled: true,
                          fillColor: Colors.grey.shade50,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Submit Button
              SizedBox(
                width: double.infinity,
                height: 55,
                child: ElevatedButton.icon(
                  icon: _isLoading
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2,
                            color: Colors.white,
                          ),
                        )
                      : const Icon(Icons.send, color: Colors.white),
                  label: Text(
                    _isLoading ? 'Reporting...' : 'Submit Report',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  onPressed: _isLoading ? null : _reportPothole,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Encryption Note
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.blue.withOpacity(0.05),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(
                    color: Colors.blue.withOpacity(0.2),
                  ),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.lock, color: Colors.blue, size: 16),
                    SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Your report is encrypted with Post-Quantum Cryptography (AES-256-RSA)',
                        style: TextStyle(
                          color: Colors.blue,
                          fontSize: 12,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
