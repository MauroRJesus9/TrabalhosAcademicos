import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:intl/intl.dart';
import 'package:weather/Screens/day_details.dart';
import 'package:weather/getX/controller.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../Screens/loading_screen.dart';

class WeekDaysContainer extends StatelessWidget {
  WeekDaysContainer({Key? key, @required this.weatherDataJson, this.lastupdate})
      : super(key: key);
  dynamic weatherDataJson;
  final double _fixedHeight = 5;
  final _transition = Transition.rightToLeft;
  final lastupdate;


  //
  StateController weekDaysController = Get.put(StateController());

  List<Map<String, dynamic>> weekDays(dynamic weatherDataJson) {
    return List.generate(6, (index) {
      final eachWeekDay = DateTime.now().subtract(Duration(days: index + 1));
      weekDaysController.updateTheWeekDay(weatherDataJson, index);
      return {
        'day': DateFormat.E().format(eachWeekDay).substring(0, 3),
        'tempDay': weekDaysController.groupVal.value == 'metric'
            ? weekDaysController.eachDayTemp.value
            : weekDaysController.eachDayFahrenheitTemp.value,
        'tempNight': weekDaysController.groupVal.value == 'metric'
            ? weekDaysController.eachNightTemp.value
            : weekDaysController.eachNightFahrenheitTemp.value,
        'weatherIcon': weekDaysController.weekDaysWeatherIconData.value,
        'weatherCond' : weekDaysController.cityName.value
      };
    }).reversed.toList();
  }



  //
  @override
  Widget build(BuildContext context) {
    StateController weekDaysController = Get.put(StateController());
    WidgetsBinding.instance!.addPostFrameCallback((timeStamp) {
      StateController();
      weekDays(weatherDataJson);
    });

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children:
      weekDays(weatherDataJson).map((data) {
        return Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget> [
            Builder(builder: (BuildContext context) {
              if(data['day'].toString() == 'Mon'){
                return Text(AppLocalizations.of(context)!.mon,style: Theme.of(context).textTheme.headline5);
              }else if(data['day'].toString() == 'Tue'){
                return Text(AppLocalizations.of(context)!.tue,style: Theme.of(context).textTheme.headline5);
              }else if(data['day'].toString() == 'Wed'){
                return Text(AppLocalizations.of(context)!.wed,style: Theme.of(context).textTheme.headline5);
              }else if(data['day'].toString() == 'Thu'){
                return Text(AppLocalizations.of(context)!.thu,style: Theme.of(context).textTheme.headline5);
              }else if(data['day'].toString() == 'Fri'){
                return Text(AppLocalizations.of(context)!.fri,style: Theme.of(context).textTheme.headline5);
              }else if(data['day'].toString() == 'Sat'){
                return Text(AppLocalizations.of(context)!.sat,style: Theme.of(context).textTheme.headline5);
              }else if(data['day'].toString() == 'Sun'){
                return Text(AppLocalizations.of(context)!.sun,style: Theme.of(context).textTheme.headline5);
              }
              return const Text('ERROR');
            }),
            SizedBox(height: _fixedHeight),
            IconButton(
              onPressed: () {
                Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (context) => DayDetails(weatherIcon:data["weatherIcon"],day: data['day'], tempDay: data['tempDay'], tempNight: data['tempNight'], weatherCondition: data['weatherCond'],weatherDataJson: weatherDataJson,lastupdate: lastupdate,)
                    )
                );
              } ,
              icon: Icon(data["weatherIcon"]),
              iconSize: 18,
            ),
          ],
        );
      }).toList(),
    );

  }
}
