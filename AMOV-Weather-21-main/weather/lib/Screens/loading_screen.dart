import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:location/location.dart';
import 'package:get/get.dart';
import 'package:weather/Screens/error_screen.dart';
import 'package:weather/Screens/location_error_screen.dart';
import 'package:weather/Services/get_weather.dart';
import 'package:weather/Services/location.dart';
import 'home_screen.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:intl/intl.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class LoadingScreen extends StatefulWidget {
  const LoadingScreen({Key? key}) : super(key: key);

  @override
  _LoadingScreenState createState() => _LoadingScreenState();
}

class _LoadingScreenState extends State<LoadingScreen> {
  Location location = Location();
  int timestamp = DateTime.now().millisecondsSinceEpoch;

  String _lastupdate = '';
  final _locationDuration = const Duration(seconds: 1);
  final _locationTransition = Transition.native;
  final _transition = Transition.leftToRight;
  final _duration = const Duration(milliseconds: 300);
  final WeatherModel _weatherModel = WeatherModel();

  Future<int> _getDateFromSharedPref() async{
    final prefs = await SharedPreferences.getInstance();
    await prefs.reload();
    final value = prefs.getInt('myTimestampKey');
    if(value == null){
      return 0;
    }else{
      return value;
    }
  }

  Future<dynamic> _showDateFromSharedPref() async{
    //final prefs = await SharedPreferences.getInstance();
    int date = await _getDateFromSharedPref();
    var currentDataConv = DateTime.fromMicrosecondsSinceEpoch(date*1000);
    setState(() {
      _lastupdate = 'Last update: $currentDataConv';
    });
  }

  Future<void> _setDate() async{
    final prefs = await SharedPreferences.getInstance();
    int currentDate = DateTime.now().millisecondsSinceEpoch;
    await prefs.setInt('myTimestampKey', currentDate);
  }
  
  Future<dynamic> getLocationData() async {
    var weatherData = await _weatherModel.getLocationAndWeatherData();
    bool serviceEnabled = await location.serviceEnabled();
    if (weatherData != null && serviceEnabled) {
      Get.to(
        () => HomeScreen(weatherDataJson: weatherData, lastUpdateDate: _lastupdate),
        transition: _transition,
        duration: _duration,
      );
    } else if (!serviceEnabled) {
      serviceEnabled = await location.requestService();
    }
  }

  // Request Permission from the user.
  Future<dynamic> permissionRequest() async {
    _setDate();
    await location.requestPermission();
    var permissionStatus = await permissionCheck();
    if (permissionStatus) {
      getLocationData();
    } else {
      showDenialDialog();
    }
  }

  // Check the permission status.
  Future<bool> permissionCheck() async {
    var permissionResult = await location.hasPermission();
    if (permissionResult == PermissionStatus.denied) {
      permissionResult = await location.requestPermission();
      return false;
    } else {
      return true;
    }
  }

  // Show this dialog if permission access denied!.
  void showDenialDialog() {
    showDialog(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          backgroundColor: Colors.white,
          title: Text(
            AppLocalizations.of(context)!.location_access_denied,
            style: const TextStyle(
              fontSize: 16,
              color: Colors.black,
            ),
          ),
          actions: [
            TextButton(
              child: Text(
                AppLocalizations.of(context)!.exit,
                style: const TextStyle(
                  fontSize: 16,
                  color: Colors.red,
                  fontWeight: FontWeight.bold,
                ),
              ),
              onPressed: () {
                SystemChannels.platform.invokeMethod('SystemNavigator.pop');
              },
            ),
            TextButton(
              child: Text(
                AppLocalizations.of(context)!.request,
                style: const TextStyle(
                  fontSize: 16,
                  color: Colors.green,
                  fontWeight: FontWeight.bold,
                ),
              ),
              onPressed: () {
                permissionRequest();
                Get.back();
              },
            ),
          ],
        );
      },
    );
  }

  @override
  void initState() {
    super.initState();

    permissionRequest();
    _showDateFromSharedPref();
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: CircularProgressIndicator(),
      ),
    );
  }
}
