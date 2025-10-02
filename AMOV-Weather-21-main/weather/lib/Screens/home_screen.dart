import 'package:flutter/material.dart';
import 'package:location/location.dart';
import 'package:get/get.dart';
import 'package:intl/intl.dart';
import 'package:weather/Screens/loading_screen.dart';
import 'package:weather/Screens/location_error_screen.dart';
import 'package:weather/Screens/settings_screen.dart';
import 'package:weather/Services/get_weather.dart';
import 'package:weather/Widgtes/bottom_container.dart';
import 'package:weather/Widgtes/center_container.dart';
import 'package:weather/Widgtes/week_days_container.dart';
import 'package:weather/Widgtes/tertiary_container.dart';
import 'package:weather/getX/controller.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';


// ignore: must_be_immutable
class HomeScreen extends StatelessWidget {
  Location location = Location();
  HomeScreen({Key? key, this.weatherDataJson, this.lastUpdateDate}) : super(key: key);
  final weatherDataJson;
  final lastUpdateDate;

  final double _fixedHeight = 10;
  //var dateStringFormat = DateFormat.yMMMEd('en_US').format(DateTime.now());
  final _transition = Transition.rightToLeft;
  final _duration = const Duration(milliseconds: 300);
  final _locationDuration = const Duration(milliseconds: 50);
  final _locationTransition = Transition.native;
  final WeatherModel _weatherModel = WeatherModel();

  // *********************************************
  Future<dynamic> getLocationData() async {
    var weatherData = await _weatherModel.getLocationAndWeatherData();
    bool serviceEnabled = await location.serviceEnabled();
    if (weatherData != null && serviceEnabled) {
      Get.to(
            () => HomeScreen(weatherDataJson: weatherData, lastUpdateDate: lastUpdateDate),
        transition: _transition,
        duration: _duration,
      );
    } else if (!serviceEnabled) {
      serviceEnabled = await location.requestService();
    }
  }

  Future<void> refreshList(StateController controller) async {
    bool serviceEnabled = await Location.instance.serviceEnabled();
    await Future.delayed(const Duration(seconds: 2));
    if (controller.groupVal.value == 'metric') {
      var weatherData1 = await _weatherModel.getLocationAndWeatherData();
      controller.updateUI(weatherData1);
    } else if (controller.groupVal.value == 'imperial') {
      var weatherData2 =
          await _weatherModel.getUnitMeasure(controller.groupVal.value);
      controller.updateUI(weatherData2);
      WeekDaysContainer(weatherDataJson: weatherData2);
    }
    if (!serviceEnabled) {
      Get.off(
        LocationError(),
        transition: _locationTransition,
        duration: _locationDuration,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    double _screenHeight = MediaQuery.of(context).size.height;
    StateController stateController = Get.put(StateController());
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: false,
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
        actions: [
          IconButton(
            splashRadius: 20,
            onPressed: () {
              Get.to(
                () => SettingsScreen(),
                transition: _transition,
                duration: _duration,
              );
            },
            icon: Icon(
              Icons.more_vert,
              color: Theme.of(context).appBarTheme.foregroundColor,
            ),
          ),
        ],
        title: Text(
          lastUpdateDate,
          style: Theme.of(context).textTheme.bodyText2,
        ),
      ),
      body: RefreshIndicator(
        displacement: 20,
        color: (Get.isDarkMode) ? Colors.black : Colors.white,
        onRefresh: () => refreshList(stateController),
        child: GetBuilder<StateController>(
          init: StateController(),
          initState: (_) {
            stateController.updateUI(weatherDataJson);
          },
          builder: (_) {
            return SafeArea(
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Obx(
                      () => CenterContainer(
                        cityName: stateController.cityName.value,
                        temperatureDegree:
                            stateController.temperatureDegree.value,
                        weatherCondition:
                            stateController.weatherCondition.value,
                        centerIcon: stateController.weatherIconData.value,
                      ),
                    ),
                    SizedBox(height: _fixedHeight),
                    Obx(
                      () => TertiaryContainer(
                        minDegree: stateController.minDegree.value,
                        maxDegree: stateController.maxDegree.value,
                      ),
                    ),
                    SizedBox(height: _fixedHeight * 2),
                    const Divider(
                      thickness: 1,
                    ),
                    SizedBox(height: _fixedHeight),
                    WeekDaysContainer(
                      weatherDataJson: weatherDataJson,
                      lastupdate: lastUpdateDate,
                    ),
                    SizedBox(height: _fixedHeight),
                    const Divider(
                      thickness: 1,
                    ),
                    SizedBox(height: _fixedHeight),

                    Obx(
                      () => BottomContainer(
                        humidity: stateController.humidity.value,
                        windSpeed: stateController.windSpeed.value,
                        sunrise: stateController.sunrise.value,
                        sunset: stateController.sunset.value,
                      ),
                    ),
                    SizedBox(height: _fixedHeight * 4),
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                              primary: Colors.black,
                              side: const BorderSide(
                                color: Colors.white,
                              )),
                      onPressed: () {
                        Navigator.of(context).push(MaterialPageRoute(builder: (context) => LoadingScreen()));
                      },
                      child:  Text(
                          AppLocalizations.of(context)!.refresh_button
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
