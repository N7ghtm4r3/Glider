# Glider Backend Service
**v1.0.4**

This is the section of Glider where you can find how to run directly the Glider's backend without need to
implement it anywhere, but your machine must at least have installed and configured **Java18 JDK** as Java's version to 
run this service.

## First configuration

In the first configuration you need to customize this JSON snippet with your preferences, following the hints written as 
values of the "configuration" branch, then save it.

```json
{
  "glider": {
    "configuration": {
      "encryptConfigsFile" : "[BOOLEAN] -> whether the \"glider_configs.json\" file must be encrypted, NOTE you will no longer be able to edit this file directly",
      "backupPath" : "[STRING] -> insert your path here, also null or this key removed is valid",
      "backupInterval": "[STRING] -> insert the interval to schedule the database backup (null or remove this key to not set a scheduled backup) -> [FIVE_MINUTES, FIFTEEN_MINUTES, HALF_HOUR, ONE_HOUR, FOUR_HOURS, EIGHT_HOURS, TWELVE_HOURS, ONE_DAY, ONE_WEEK, ONE_MONTH]",
      "databasePath": "[STRING] -> path where create the database",
      "password": "[STRING] -> password to protect the Session",
      "singleUseMode": "[BOOLEAN] -> whether the session allows multiple connections, so multiple devices",
      "QRCodeLoginEnabled": "[BOOLEAN] -> whether the session allows login by QR-CODE method\n (if enabled will be shown on hostAddress:(hostPort + 1))",
      "hostAddress": "[STRING] -> host address of the session (null or remove this key to auto-fetch it)",
      "hostPort": "[INTEGER] -> host port of the session",
      "runInLocalhost": "[BOOLEAN] -> whether the session can accept requests outside localhost"
    }
  }
}
```

To correctly launch the backend service you must:
<ul>
    <li>
       download the jar file <b>(GliderBackendService.jar)</b> of the backend service from the <b>assets</b> section <a href="https://github.com/N7ghtm4r3/Glider/releases/tag/1.0.4">here</a>
    </li> 
    <li>
        create a JSON file named <b>"glider_configs.json"</b>
    </li>
    <li>
        customize that file with your preferences
    </li>
    <li>
        <b>save the configs file in the same place where the jar has been saved, 
        or the backend service will not work</b>
    </li>
</ul>
Now you can run the jar file, good use!

## Next launches 

After the first configuration that snippet will appear like this, and you can directly run the jar without 
change any details to make work the backend service

```json
{
  "glider": {
    "configuration": {
      "encryptConfigsFile" : "[BOOLEAN] -> whether the \"glider_configs.json\" file must be encrypted, NOTE you will no longer be able to edit this file directly",
      "backupPath" : "[STRING] -> insert your path here, also null or this key removed is valid",
      "backupInterval": "[STRING] -> insert the interval to schedule the database backup (null or remove this key to not set a scheduled backup) -> [FIVE_MINUTES, FIFTEEN_MINUTES, HALF_HOUR, ONE_HOUR, FOUR_HOURS, EIGHT_HOURS, TWELVE_HOURS, ONE_DAY, ONE_WEEK, ONE_MONTH]",
      "databasePath": "[STRING] -> path where create the database",
      "password": "[STRING] -> password to protect the Session",
      "singleUseMode": "[BOOLEAN] -> whether the session allows multiple connections, so multiple devices",
      "QRCodeLoginEnabled": "[BOOLEAN] -> whether the session allows login by QR-CODE method\n (if enabled will be shown on hostAddress:(hostPort + 1))",
      "hostAddress": "[STRING] -> host address of the session (null or remove this key to auto-fetch it)",
      "hostPort": "[INTEGER] -> host port of the session",
      "runInLocalhost": "[BOOLEAN] -> whether the session can accept requests outside localhost"
    },
    // this section will be created and filled after the first run of Glider with this system
    "session" : {
      "secretKey": "[STRING] -> secret key of current the session",
      "databasePath": "[STRING] -> path where the database has been created",
      "ivSpec": "[STRING] -> iv spec of current the session",
      "token": "[STRING] -> token of the current session"
    }
  }
}
``` 

### Change configuration

If you need to change some details you need to follow these options, but only if <b>"encryptConfigsFile" property is "false", 
otherwise you cannot change the past configuration directly, but you must fill again the configs template</b>: 
<ul>
    <li>
        If you need to change any configuration details you need to remove the <b>"session"</b> branch or the changes
        will not have any effects for the backend service 
    </li>
    <li>
        If you need to change the session, without change any configuration details, you can directly change the 
        <b>"session"</b> branch without remove the <b>"configuration"</b> branch
    </li>
</ul> 

<b>After the change of the configuration you will need to restart the jar file</b> to correctly configure 
the backend service 

## Authors

- [@N7ghtm4r3](https://www.github.com/N7ghtm4r3)

## Support

If you need help using the library or encounter any problems or bugs, please contact us via the following links:

- Support via <a href="mailto:infotecknobitcompany@gmail.com">email</a>
- Support via <a href="https://github.com/N7ghtm4r3/Glider/issues/new">GitHub</a>

Thank you for your help!

## Badges

[![](https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/developer?id=Tecknobit)
[![Twitter](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/tecknobit)

[![](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)

[![](https://jitpack.io/v/N7ghtm4r3/Glider.svg)](https://jitpack.io/#N7ghtm4r3/Glider)

## Donations

If you want support project and developer: **0x5f63cc6d13b16dcf39cd8083f21d50151efea60e**

![](https://img.shields.io/badge/Bitcoin-000000?style=for-the-badge&logo=bitcoin&logoColor=white)
![](https://img.shields.io/badge/Ethereum-3C3C3D?style=for-the-badge&logo=Ethereum&logoColor=white)

If you want support project and developer with <a href="https://www.paypal.com/donate/?hosted_button_id=5QMN5UQH7LDT4">PayPal</a>

Copyright © 2023 Tecknobit
