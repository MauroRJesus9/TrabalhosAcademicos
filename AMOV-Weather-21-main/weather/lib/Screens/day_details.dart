

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:get/get_core/src/get_main.dart';
import 'package:get/get_navigation/src/extension_navigation.dart';
import 'package:get/get_navigation/src/routes/transitions_type.dart';
import 'package:weather/Screens/home_screen.dart';
import 'package:weather/Widgtes/week_days_container.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class DayDetails extends StatelessWidget{
  DayDetails({Key? key, this.weatherIcon, this.day, this.tempDay, this.tempNight, this.weatherCondition, this.weatherDataJson,this.lastupdate}) : super(key: key);
  final weatherIcon;
  final day;
  final tempDay;
  final tempNight;
  final weatherCondition;
  final weatherDataJson;

  final double _fixedHeight = 10;
  final _transition = Transition.rightToLeft;
  final lastupdate;



  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(AppLocalizations.of(context)!.daydetails),
      ),
      body: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Column(
              children: [
                SizedBox(height: _fixedHeight * 2),
                Text(
                  weatherCondition.toString(),
                  style: Theme.of(context).textTheme.headline1,
                ),
                SizedBox(height: _fixedHeight * 2),
                Icon(
                  weatherIcon,
                  size: 70,
                ),
                SizedBox(height: _fixedHeight * 3),
                Builder(builder: (BuildContext context) {
                  if(day.toString() == 'Mon'){
                    return Text(AppLocalizations.of(context)!.monday,style: Theme.of(context).textTheme.headline2);
                  }else if(day.toString() == 'Tue'){
                    return Text(AppLocalizations.of(context)!.tuesday,style: Theme.of(context).textTheme.headline2);
                  }else if(day.toString() == 'Wed'){
                    return Text(AppLocalizations.of(context)!.wednesday,style: Theme.of(context).textTheme.headline2);
                  }else if(day.toString() == 'Thu'){
                    return Text(AppLocalizations.of(context)!.thursday,style: Theme.of(context).textTheme.headline2);
                  }else if(day.toString() == 'Fri'){
                    return Text(AppLocalizations.of(context)!.friday,style: Theme.of(context).textTheme.headline2);
                  }else if(day.toString() == 'Sat'){
                    return Text(AppLocalizations.of(context)!.saturday,style: Theme.of(context).textTheme.headline2);
                  }else if(day.toString() == 'Sun'){
                    return Text(AppLocalizations.of(context)!.sunday,style: Theme.of(context).textTheme.headline2);
                  }
                  return const Text('ERROR');
                }),
                SizedBox(height: _fixedHeight * 3),
                Text(
                  'Max: ${tempDay.toString()}° | Min: ${tempNight.toString()}º',
                  style: Theme.of(context).textTheme.headline2,
                ),
              ],
            ),
          ],
        ),
      )
    );
  }

  /*
  appBar: AppBar(
        automaticallyImplyLeading: false,
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
        actions: [

        ],
        title:
      ),
   */
  
}