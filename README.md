![Release](https://jitpack.io/v/Mentalab-hub/explore-java.svg)


Overview
==================

Explore Java API is Mentalab's open-source biosignal acquisition API for working with Mentalab Explore device. The code can also be adapted to use in Andrid echosystem. Amongst many things, it provides the following features:

* Real-time streaming of ExG, orientation and environmental data
* Connect, pair and search with Explore device via Bluetooth 

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

![alt text](https://github.com/salman2135/mentlabMobileApi/blob/master/screenshots/maven.png?raw=true)

* In your app’s build.gradle add the dependency
```
implementation 'com.github.Mentalab-hub:explore-java:v_0.1'
```

![alt text](https://github.com/Mentalab-hub/explore-java/blob/master/screenshots/app.png?raw=true)

* Add the following line in your anderoid manifest:
```
<uses-permission android:name="android.permission.BLUETOOTH" />
```
* Sync gradle and Mentlab API is ready to use!

A demo Android application is availble here:


[![SC2 Video](https://img.youtube.com/vi/nP57MqztEUI/0.jpg)](https://youtu.be/nP57MqztEUI)

A demo Android application which used explore-java library is available [here](https://github.com/Mentalab-hub/explore-demo-app)
Please check troubleshooting section of this document in case any problem occurs.

Usage Example
=============

The following code sniipet shows how to scan, connect and get data stream from Explore device:

```java
Set<String> deviceList = MentalabCommands.scan();
/* Connect to Explore device */
MentalabCommands.connect("Explore_XXXX");

InputStream inputStream = MentalabCommands.getRawData();
/* Get data map with decoded data points */
Map<String, Queue<Float>> map = MentalabCodec.decode(inputStream);
```


Documentation
=============

For the full documentation of the API, please visit <https://github.com/Mentalab-hub/explore-java/tree/master/docs>

Troubleshooting
===============

* If your phone is not recognized by Andoid Studio, make sure that USB debugging is turned on from your Android device.

You can also create a new issue in the GitHub repository.

Authors
=======

* [Salman Rahman](https://github.com/salman2135)
* [Florian Sesser](https://github.com/hacklschorsch)


License
=======
This project is licensed under the MIT license at <https://github.com/Mentalab-hub/explore-java/blob/master/LICENSE>. You can reach us at contact@mentalab.com.
