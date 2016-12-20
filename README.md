# Push Android Heartbeat Monitor

The Heartbeat Monitor was designed to help PCF operators ensure their Push Notification service is running correctly end-to-end and provide historical data. The PCF Push tile deploys Heartbeat App in the push-service-instance in the `push-notifications` space of the `system` org. The app sends a "heartbeat" push every minute to every device subscribed to the `pcf.push.heartbeat` topic. The Heartbeat Monitor subscribes to the heartbeat topic and responds to the server every time it receives a heartbeat.

## Pre-requisites

This app requires release v1.7.0+ of the [PCF Push Notification Tile](https://network.pivotal.io/products/push-notification-service#/releases/) to work. Heartbeat App will automatically be installed in your push-service-instance.

__Note:__ Make sure you have added a valid Firebase Cloud Messaging Server Key to your Android FCM platform.

## Setup

Begin by cloning the repo and using the closest release that matches your PCF installation.

`git clone git@github.com:cfmobile/push-android-heartbeatmonitor.git`

__IMPORTANT__: Before building this project you need to build [Push Android SDK](https://github.com/cfmobile/push-android/tree/dev)
by

`./gradlew --info printVersion clean uploadArchives`

It will build and publish .aar and .pom to your local maven repository.

Then create an application inside your Firebase Project through the Google Firebase Console.
__Note:__ Ensure your app SHA1 certificate fingerprint of the app in the Firebase console matches the one in the keystore used to sign the heartbeat monitor.``

Once created, download the config file `google-service.json` and move it to the project under the `app` directory.


There is a file that you will need to modify:

1. Open the project in Android Studio

1. In the Project Navigator, navigate to res/raw/pivotal.properties

1. Change the pivotal.push.serviceUrl field by replacing `push-api.your.env.com` with your push-api url
    - __Note:__ Ensure the platform UUID and platform secret match the Android FCM platform found in your Configuration tab on your Push Dashboard _(They should by default)_

1. Build the Heartbeat Monitor on an Android Jellybean or later device

1. Accept Push Notifications on the device

1. Ensure the device shows up in the Devices tab of your Android Platform on your Push Dashboard
