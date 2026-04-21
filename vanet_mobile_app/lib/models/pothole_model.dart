class PotholeReport {
  final int? id;
  final double latitude;
  final double longitude;
  final String severity;
  final String description;
  final String deviceId;
  final bool verified;
  final int verificationCount;

  PotholeReport({
    this.id,
    required this.latitude,
    required this.longitude,
    required this.severity,
    required this.description,
    required this.deviceId,
    this.verified = false,
    this.verificationCount = 0,
  });

  factory PotholeReport.fromJson(Map<String, dynamic> json) {
    return PotholeReport(
      id: json['id'],
      latitude: (json['latitude'] ?? 0.0).toDouble(),
      longitude: (json['longitude'] ?? 0.0).toDouble(),
      severity: json['severity'] ?? 'UNKNOWN',
      description: json['description'] ?? 'No description',
      deviceId: json['deviceId'] ?? 'unknown',
      verified: json['verified'] ?? false,
      verificationCount: json['verificationCount'] ?? 0,
    );
  }
}
