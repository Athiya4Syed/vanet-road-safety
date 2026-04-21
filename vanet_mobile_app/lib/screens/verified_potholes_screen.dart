import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../models/pothole_model.dart';

class VerifiedPotholesScreen extends StatefulWidget {
  const VerifiedPotholesScreen({Key? key}) : super(key: key);

  @override
  State<VerifiedPotholesScreen> createState() => _VerifiedPotholesScreenState();
}

class _VerifiedPotholesScreenState extends State<VerifiedPotholesScreen> {
  List<PotholeReport> _potholes = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadVerifiedPotholes();
  }

  Future<void> _loadVerifiedPotholes() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final potholes = await ApiService.getVerifiedPotholes();
      setState(() => _potholes = potholes);
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _isLoading = false);
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
      appBar: AppBar(
        title: const Text('✅ Verified Potholes'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const VerifiedPotholesScreen(),
                ),
              );
            },
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.error_outline,
                          size: 60, color: Colors.red),
                      const SizedBox(height: 16),
                      Text('Error: $_error'),
                      ElevatedButton(
                        onPressed: _loadVerifiedPotholes,
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : _potholes.isEmpty
                  ? const Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.verified, size: 60, color: Colors.green),
                          SizedBox(height: 16),
                          Text(
                            'No verified potholes yet!',
                            style: TextStyle(fontSize: 18),
                          ),
                          SizedBox(height: 8),
                          Text('Report potholes to get them verified'),
                        ],
                      ),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: _potholes.length,
                      itemBuilder: (context, index) {
                        final pothole = _potholes[index];
                        return Card(
                          margin: const EdgeInsets.only(bottom: 12),
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text(
                                      'Pothole #${pothole.id}',
                                      style: const TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 12,
                                        vertical: 4,
                                      ),
                                      decoration: BoxDecoration(
                                        color:
                                            _getSeverityColor(pothole.severity),
                                        borderRadius: BorderRadius.circular(20),
                                      ),
                                      child: Text(
                                        pothole.severity,
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 8),
                                Text(
                                    '📍 ${pothole.latitude.toStringAsFixed(4)}, ${pothole.longitude.toStringAsFixed(4)}'),
                                const SizedBox(height: 4),
                                Text('📝 ${pothole.description}'),
                                const SizedBox(height: 8),
                                const Row(
                                  children: [
                                    Icon(Icons.verified,
                                        color: Colors.green, size: 16),
                                    SizedBox(width: 4),
                                    Text(
                                      'Verified by community ✅',
                                      style: TextStyle(color: Colors.green),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
    );
  }
}
