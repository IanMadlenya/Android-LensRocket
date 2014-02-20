LensRocket for Android
================

LensRocket (formally PikShare) is a picture / video sharing application built on top of Windows Azure.

## Requirements
* Eclipse - This sample was built with Eclipse (Kepler version).
* Android SDK - You can download this from the [Android Developer portal](http://developer.android.com/sdk/index.html).
* Windows Azure Account - Needed to create a Mobile Service, Notification Hub, and Storage Account.  [Sign up for a free trial](https://www.windowsazure.com/en-us/pricing/free-trial/).

## Repository Content ##

The Android-LensRocket repo currently includes the following resources:

 - **source/client** - The android client source code.  This encompasses all client side code necessary to run LensRocket on Android excluding one third party library mentioned below.
 - **source/server** - The Mobile Services scripts which are tied to the endpoints the client application talks to. 

#Setting up your Azure Services
In order to run this application, you'll need to set up several pieces within Windows Azure.  Start by creating the following: a Mobile Service, a Storage account, and a Notification Hub.  You will need the Storage account name / key as well as the Notification Hub name / access signatures later on.

After creating your Mobile Service in the Windows Azure Portal, you'll need to create tables named **AccountData**, **Friends**, **Messages**, **RocketFile**, and **UserPreferences**.  You'll also need to create custom APIs named **AcceptFriendRequest**, **GetRocketForRecipient**, **Login**, **Register**, **RequestFriend**, **SaveUsername**, and **SendRocketToFriends**  You'll also want to enable Script Source Control on the **Dashboard** page.

After creating your Notification Hub, you'll want to set up Google Cloud Messaging by following the first two steps in [this walkthrough](http://www.windowsazure.com/en-us/manage/services/notification-hubs/get-started-notification-hubs-android/).  Note that you'll need your **Project Number** from the Google Console later for the Android client's **Sender ID**.

Return to your Mobile Service and go to the **Configure** tab.  Under the **app settings** area, add the following name-value pairs with the appropriate value from your Storage Account or Notification Hub:
* STORAGE_ACCOUNT_NAME
* STORAGE_ACCOUNT_KEY
* NOTIFICATION_HUB_NAME
* NOTIFICATION_HUB_FULL_ACCESS_SIGNATURE

These values will be used by the scripts you upload later in the instructions.

#Client Application Changes
In order to run the client applicaiton, you'll need to change a few settings in your application.  After importing the project into Eclipse, open **com.msted.lensrocket.Constants.java**.  Use the values from your Mobile Service / Notification Hub to set the following properties: **MOBILE_SERVICE_URL**, **MOBILE_SERVICE_APPLICATION_KEY**, **SENDER_ID**, **NOTIFICATION_HUB_CONNECTION_STRING**, and **NOTIFICATION_HUB_NAME**.

Finally, the LensRocket Android appliation relies on the [ActionBar Pull To Refresh library](https://github.com/ChrisRisner/ActionBar-PullToRefresh).  The easiest way to put this library in the client application is to clone the ActionBar-PullToRefresh repository and import it's **library** folder into Eclipse.  You can then right click on the project, go to **Properties**, select **Android**, and fix the library path at the bottom of the panel (it should be pointing to the library in an incorrect location).

#Script Changes
After cloning the Mobile Service script repository locally, you'll want to copy over all files from the **source/server** folder to replace what you cloned locally.  Once that is done, push your repository changes back up to your Mobile Service.

## Contact

For additional questions or feedback, please contact the [team](mailto:chrisner@microsoft.com).
