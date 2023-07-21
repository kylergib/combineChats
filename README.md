# CombineChats User Guide

### Set Up in IntelliJ
-   The following steps will work on Windows 10+ and MacOS 12+.
#### Prerequisites
-	IntelliJ IDEA
-	MacOS 12+ or Windows 10+
-	Download and install Liberica Java JDK 17.0.7
-	Install Guide: https://bell-sw.com/pages/liberica_install_guide-17.0.7/
-	Liberica JDK was used because it includes JavaFX, which simplifies the process of building the application. The base Java JDK no longer includes JavaFX.
-	An internet connection
#### Installation for development
1)	Unzip the file provided for the source code.
2)	Open IntelliJ IDEA and open the root folder of the source code.
3)	Once loaded, reload or load the Gradle project. This will download all dependencies outlined in the “build.gradle” file.
4)	Create a configuration to start the application. Ensure that the liberica-17 JDK is selected and set the main class to be the CombineChats class. Below is a screenshot depicting what the settings should look like.  
 <img width="468" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/d6684143-b63c-4c78-a1b3-cf24a2d5e4ec">
5)	Now you should be able to run the application from IntelliJ by starting the configuration you just created.

### Guide for user.  
###### The remainder of the User Guide will provide a walkthrough of how to use the application, offering a brief description of every window in the application.
-   Once the application is launched, it will open to the following screen:  
<img width="197" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/6b0af7c6-756e-4540-aa98-0453709a3f39">

#### Add a Twitch Channel:
-	Click the Twitch menu item and then click add.  
  <img width="243" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/dc568cd6-a539-4bd0-809f-9e34434032b3">

-	A new window will pop up asking for the Twitch channel name. 
-	Enter the name, and if desired, you can select the auto-connect checkbox. 
-	If selected, the application will attempt to connect to the channel both upon start-up and when you add the channel.  
<img width="167" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/e1bdeb2f-d12c-4602-aef8-a579ddb4527c">
-	Click 'Add', and the application will attempt to add the channel.
-	Once added, the channel will appear below the Twitch menu item.    
<img width="223" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/91bc923e-55dc-48fd-bb40-5dbc68db1c92">

#### Add a Kick Channel:
-	Click the Kick menu item and then click add.   
  <img width="242" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/e68a9134-b872-44b1-b7c5-b4e4bc37c27c">
-	A new window will pop up asking for the Kick channel name.   
<img width="155" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/874c7afe-6a58-4975-a6a0-268bc1ef598d">
-	Enter the name of the channel. Once entered, the 'Open Browser' button will become clickable.    
 <img width="185" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/899c2c1e-a0e5-4a5a-a2c0-1a16041ed250">

-	Click 'Open Browser', and it will direct you to a page from which we need to obtain the ID.   
	The ID is highlighted in the screenshot below. You need to copy and paste that into the 'Channel ID' input box.   
 <img width="488" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/563e8044-9405-4620-9e47-462f60d776a0">

-	If you want, you can select the auto connect check box and if selected the application will attempt to connect to the channel on startup and when you add the channel. 
-	Click 'Add', and the application will attempt to add the channel.
-	Once added, the channel will appear below the Kick menu item.   
 <img width="200" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/129bba3b-7504-4f53-a297-72d8a4489429">

#### Add a YouTube Channel:
-	Click the YouTube menu item and then click add.   
  <img width="235" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/a59c9e93-1c8e-484e-8c08-6a3072787c55">

-	A new window will pop up asking for the Twitch video URL. 
-	You must look up the YouTube live URL because it changes for every stream.
-	Copy and paste the URL into the input box and then click add.   
  <img width="162" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/4fa8ebf9-fc5b-4121-abfa-36f1502eafee">
 
-	For YouTube auto connect is not allowed, because the video URL will change every stream, so it would cause more harm than good if added. 
-	Once added the channel will appear below the YouTube menu item.
	When added the name under the YouTube menu item will appear as null until you connect to the channel.   
<img width="234" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/11c8d795-f071-4db8-a253-0f27dab275f3">



#### Connect to a channel’s chat:
-	Click the platform where the channel is located in the menu bar, for example, YouTube.  
  - If you click on YouTube, it will display 'null'. However, if you click on Kick or Twitch, it will show the channel name.
-	Click on 'null' for YouTube, or click the channel name for Kick or Twitch, and then click 'Connect'.   
 <img width="191" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/6d21e466-d376-41b9-91d6-9d272cd910b1">

-	Once connected the chat area will display a message.   
  <img width="215" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/6e87fde6-be1b-435a-b617-39f8bca935e7">

-	To disconnect from the chat, you can navigate back to the channel under the platform, and it should now display 'Disconnect'.      
<img width="217" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/d6b6cd35-d817-4a8e-b919-93626d1b3edd">

#### Change settings:
-	Click 'File' in the menu bar and then click 'Settings'. A new window will pop up.   
  <img width="200" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/2fd50f09-d688-49fe-bc38-18124996791f">
 <img width="180" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/4cde3c6d-aa0e-45e3-b0fe-2a4326642a48">

-	The setting for each platform's message color determines the color in which the chat messages appear in the main window.   
 <img width="211" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/2e10f70f-f278-4a7f-bc83-91ec546847e3">

-	The options "Show platform in messages" and "Show channel name in messages" determine whether the platform and/or channel names will appear before the chat message.
 <img width="345" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/c782dd37-2b98-409d-9579-d9a68d750f37"> 
-	The "Dark Mode" setting enables/disables the dark mode after the settings are saved.
 <img width="181" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/d044ef08-6c0d-496b-add6-23c5150bb143">

-	Once you enable dark mode, the color of the platform's messages remains the same. It is recommended to change the color for better readability.
	Please note that these color changes will only affect incoming messages and will not be applied to previously received messages.
<img width="149" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/9fb84ba6-c442-4d88-8033-24f63d2f6bdb">
<img width="149" alt="image" src="https://github.com/kylergib/combineChats/assets/48994502/1ea08d28-8d8d-4ec5-a042-8ea7bf9fb093">


#### Handle reports:
-	Click 'File' and then select 'Reports'.  
-	A new window will open.
-	Enter the desired criteria in the input boxes.  
-   The criteria is not case-sensitive. 
-	If the "Exact Match for Message" option is selected, it will only return matches for the entire content of the message input box.
-	To generate a report, click the "Save As..." button located in the bottom right corner. This will prompt a window to appear, asking you to choose the location to save the report.
-	After clicking "Save," you will return to the reports screen. If the report was successfully saved, a message will appear at the bottom indicating the location where it was saved.
-	If the report saving process was unsuccessful, an error message will appear at the bottom instead. 
-	Click the menu button to see the other report page.
-	Clicking on "Chat Count Reports" will open a new window. 
-	By default, the current month will be selected. This page displays the number of messages you received for each day of the selected month.
-	To change the month, click the "Month:" drop-down menu and select a different month.
 
-	To export this report, click "Save As..." in the bottom right-hand corner of the window. This will prompt you to choose the location where you want to save the report. 
-	If the report is saved successfully, a success message will be displayed. If there is a failure in saving the report, an error message will be shown. 

#### Auto scroll:
-	When a new message arrives, if auto scroll is enabled, the window will automatically scroll to the bottom, allowing you to read the latest message.
-	Auto scroll is enabled by default on the main window.

-	If you manually scroll up, the auto scroll feature will automatically be disabled. You can click it again to re-enable auto scroll.
