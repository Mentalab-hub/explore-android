![Release](https://jitpack.io/v/Mentalab-hub/explore-android.svg)


Overview
==================

Explore Android API is Mentalab's open-source biosignal acquisition API for use with Mentalab Explore devices. The code can also be adapted to use in Android ecosystems. Among others, it provides the following key features: :


* Real-time streaming of ExG, orientation and environmental data
* Connect, pair and search Explore device via Bluetooth
* Record data in csv format
* Push data to Lab Streaming Layer(LSL)
* Change device settings

Requirements
==================

* Android Studio with SDK bundle from this link: <https://developer.android.com/studio>
* Android device with at least Android Lollipop(OS version 5.1)


Quick installation
==================

To add the library to your project:

* In your project’s build.gradle add the following line
```
maven { url ‘https://jitpack.io’ }
```

![alt text](https://github.com/Mentalab-hub/explore-android/blob/master/screenshots/maven.png?raw=true)

* Add the following dependency in your app level build.gradle file
```
implementation 'com.github.Mentalab-hub:explore-android:V_0.2'
```

* Add the following permisions in your android manifest:
```
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.INTERNET" />
```
* Sync gradle and Mentlab API is ready to use!

The following example shows how to set up the your project for explore-android. Please always follow the instructions in this page to integrate latest features in your app.


[![SC2 Video](https://img.youtube.com/vi/nP57MqztEUI/0.jpg)](https://youtu.be/nP57MqztEUI)

A demo Android application which used explore-java library is available [here](https://github.com/Mentalab-hub/explore-demo-app).
Please check troubleshooting section of this document in case of issues.

Usage Example
=============

The following code snippet shows how to scan, connect and get data stream from Explore device:

```java
Set<String> deviceList = MentalabCommands.scan();
/* Connect to Explore device */
MentalabCommands.connect("Explore_XXXX");

InputStream inputStream = MentalabCommands.getRawData();
/* Get data map with decoded data points */
Map<String, Queue<Float>> map = MentalabCodec.decode(inputStream);

/* Push data to LSL */
MentalabCommands.pushToLsl();

/* Set specific channels */
Map<String, Boolean> configMap = Map.of(DeviceConfigSwitches.Channels[7], false,
MentalabConstants.DeviceConfigSwitches.Channels[6], false);
 MentalabCommands.setEnabled(configMap);
```


Documentation
=============

For the full documentation of the API, please visit our [Javadoc page](https://javadoc.jitpack.io/com/github/Mentalab-hub/explore-android/V_0.2/javadoc/)

Troubleshooting
===============

* If your phone is not recognized by Android Studio, make sure that USB debugging is turned on on your Android device.

You can also create a new issue in the GitHub repository.

Authors
=======

* [Salman Rahman](https://github.com/salman2135)
* [Alex Platt](https://github.com/Nujanauss)
* [Florian Sesser](https://github.com/hacklschorsch)


License
=======
This project is licensed under the MIT license at <https://github.com/Mentalab-hub/explore-android/blob/master/LICENSE>. You can reach us at contact@mentalab.com.
