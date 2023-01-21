# Glider
**v1.0.0**

This is a Java Based open source project useful to manage the creation and the storage of your passwords
with the **Glider** ecosystem

## Implementation

Add the JitPack repository to your build file

### Gradle

- Add it in your root build.gradle at the end of repositories

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
- Add the dependency

```gradle
dependencies {
    implementation 'com.github.N7ghtm4r3:Glider:1.0.0'
}
```

### Maven

- Add it in your root build.gradle at the end of repositories

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
- Add the dependency

```xml
<dependency>
    <groupId>com.github.N7ghtm4r3</groupId>
  <artifactId>Glider</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 🛠 Skills
- Java

## Roadmap

This project will be constantly developed to reach different platforms to work on, following the platforms releases steps:

- Mobile
  - <a href="https://github.com/N7ghtm4r3/Glider-Android#readme">Android</a> (**coming soon**)
  - iOS
- Desktop Glider version

## Usages

To start the **Glider** service on your own backend infrastructure you will need to following 
these steps

### First run

```java
public static void main(String[] args) {
        
    /**
     * if set on true this session will allow only one device connected, if set on false this session allow 
     * multiple devices connected
     * **/
    boolean isSingleUseMode = //flag value

    /**
     * if set on true this session will create a QRCode (hosted on the next port that you choose, e.g. 21 -> 22) 
     * with the credentials to connect at the session create, if set on false this option will be disabled
     * **/
    boolean QRCodeLoginEnabled = //flag value

    /**
     * the service port where the Glider' service will accept the requests
     * **/
    int hostPort = //port value

    /**
     * whether the session can accept requests outside localhost
     * **/
    boolean runInLocalhost = //flag value;

    //Creation of the launcher        
    GliderLauncher launcher = new GliderLauncher("your_database_path", "session_password", isSingleUseMode,
            QRCodeLoginEnabled, hostPort,  runInLocalhost);

    //Starting of the service
    launcher.startService();

    /**
     * This will make throw an Exception to make you save the session data:
     * {
     *   "databasePath": "your_database_path.db",
     *   "secretKey": "your_secret_key",
     *   "ivSpec": "your_iv_spec",
     *   "token": "your_token"
     * }
     **/
}
``` 

### Normal workflow run

```java
    public static void main(String[] args) throws Exception {
    
        //Pass the credentials created at the first run with a file in JSON format
        GliderLauncher launcher = new GliderLauncher(new File("path_to_credentials_file.json"));
    
        //Pass the credentials created at the first run in JSON format
        GliderLauncher launcher = new GliderLauncher(new JSONObject("{\n" +
                "    \"databasePath\": \"your_database_path.db\",\n" +
                "    \"secretKey\": \"your_secret_key\",\n" +
                "    \"ivSpec\": \"your_iv_spec\",\n" +
                "    \"token\": \"your_token\"\n" +
                "}"));
    
        //Pass the credentials created at the first run one by one
        GliderLauncher launcher = new GliderLauncher("your_database_path", "your_token", "your_iv_spec",
                "your_secret_key");
    
        //Starting of the service
        launcher.startService();
    }
``` 

## Authors

- [@N7ghtm4r3](https://www.github.com/N7ghtm4r3)

## Support

If you need help using the library or encounter any problems or bugs, please contact us via the following links:

- Support via <a href="mailto:infotecknobitcompany@gmail.com">email</a>
- Support via <a href="https://github.com/N7ghtm4r3/APIManager/issues/new">GitHub</a>

Thank you for your help!

## Badges

[![](https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/developer?id=Tecknobit)
[![Twitter](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/tecknobit)

[![](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)

[![](https://jitpack.io/v/N7ghtm4r3/APIManager.svg)](https://jitpack.io/#N7ghtm4r3/APIManager)

## Donations

If you want support project and developer: **0x5f63cc6d13b16dcf39cd8083f21d50151efea60e**

![](https://img.shields.io/badge/Bitcoin-000000?style=for-the-badge&logo=bitcoin&logoColor=white)
![](https://img.shields.io/badge/Ethereum-3C3C3D?style=for-the-badge&logo=Ethereum&logoColor=white)

If you want support project and developer with <a href="https://www.paypal.com/donate/?hosted_button_id=5QMN5UQH7LDT4">PayPal</a>

Copyright © 2023 Tecknobit
