# countdown

A countdown application.

<img width="428" height="266" alt="Main Window" src="https://github.com/user-attachments/assets/59844908-c0c9-4fe3-a3a2-acd8e49c0f89" />

This application allows the user to set a specific date, time (in 24 hour format), message (optional) and message color (optional) by choosing the **Configure->Settings...** menu item which will display the below dialog.

![Settings Dialog](https://user-images.githubusercontent.com/32653184/204885540-e18392f3-17c2-4a8a-ae5d-52ec1e452b2e.png)

After the countdown data is entered and validated (date and time validation is triggered via clicking the dialog **OK** button), the application will persist the data in a user node preferences file.

To start the countdown, select the **Configure->Start** menu item which will display the configured message and countdown in the main window as below.

<img width="428" height="266" alt="Started Countdown" src="https://github.com/user-attachments/assets/63d4fbca-7095-4f80-89ee-fe4ce10ba78d" />

Once the countdown reaches the specified date and time the dialog below will display to signify the configured event has occurred.

![Event Occurrence Dialog](https://user-images.githubusercontent.com/32653184/204162648-0f51a29c-0e8a-434c-9bda-36d67ae49c43.png)

The event occurrence dialog message is my homage to one of the greatest crooners of all time. You are of course welcome to change the message as you see fit.

## Build

To build the application, in the project's root folder, execute the following from a terminal window

> mvnw package (use **./mvnw** for Unix/Linux based OSes)

The command will create the following executable JAR file in the project's root folder **target** directory.

- countdown-0.0.1-SNAPSHOT.jar

Run the following from the terminal window to execute the application.

```bash
java -jar countdown-0.0.1-SNAPSHOT.jar
```
