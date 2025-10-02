import 'package:location/location.dart';

class Location_Coordinates {
  Location location = Location();
  LocationData? _locationData;
  bool _serviceEnabled = false;
  PermissionStatus _permissionGranted = PermissionStatus.denied;
  double? longitude;
  double? latitude;

  Future<void> _fetchLocation() async {
    // Verificar estado do serviço
    _serviceEnabled = await location.serviceEnabled();
    if (!_serviceEnabled) {
      _serviceEnabled = await location.requestService();
    }

    // Pede permissões em runtime
    _permissionGranted = await location.hasPermission();
    if (_permissionGranted == PermissionStatus.denied) {
      _permissionGranted = await location.requestPermission();
    }

    await getCurrentLocation();
    // Desafio de usar o onLocationChanged
    // location.onLocationChanged.listen(((locationData) {
    //   setState(() => _locationData = locationData);
    // }));
  }

  Future<void> getCurrentLocation() async {
    try {
      _locationData = await location.getLocation();
      latitude = _locationData?.latitude;
      longitude = _locationData?.longitude;
    } catch (e) {
      print(e);
    }
  }
}
