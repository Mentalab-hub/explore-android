Overview
==================

Explore Java API is Mentalab's open-source biosignal acquisition API for working with Mentalab Explore device. The code can also be adapted to use in Andrid echosystem. Amongst many things, it provides the following features:

* Real-time streaming of ExG, orientation and environmental data
* Explore device configuration

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
implementation 'com.github.salman2135:mentlabMobileApi:V__0.1'
```

![alt text](https://github.com/salman2135/mentlabMobileApi/blob/master/screenshots/app.png?raw=true)

* Add the following line in your anderoid manifest:
```
<uses-permission android:name="android.permission.BLUETOOTH" />
```
* Sync gradle and Mentlab API is ready to use!

A video demonstating above steps is also available:


[![SC2 Video](https://img.youtube.com/vi/nP57MqztEUI/0.jpg)](https://youtu.be/nP57MqztEUI)

Please check troubleshooting section of this document in case any problem occurs.


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
This project is licensed under the MIT license at <https://github.com/mentalab-hub/explore-mobile-api/blob/main/LICENSE>. You can reach us at contact@mentalab.com.
