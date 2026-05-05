@PostMapping("/api/vanet/report")
public ResponseEntity<?> reportPothole(
        @RequestParam Double latitude,
        @RequestParam Double longitude,
        @RequestParam String severity,
        @RequestParam String description,
        @RequestParam String deviceId) {
    try {
        var pothole = potholeService.reportPothole(
            latitude, longitude, severity, description, deviceId
        );
        return ResponseEntity.ok(Map.of(
            "id", pothole.getId(),
            "latitude", pothole.getLatitude(),
            "longitude", pothole.getLongitude(),
            "severity", pothole.getSeverity(),
            "encrypted", true,
            "message", "Pothole reported successfully!"
        ));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of(
            "error", e.getMessage(),
            "cause", e.getCause() != null ? e.getCause().getMessage() : "unknown"
        ));
    }
}