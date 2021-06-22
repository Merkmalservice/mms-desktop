# Merkmalservice Desktop App

## Requirements
- Java 11 or higher

## Architecture
- Spring Boot Application
- JavaFX UI (Version 13)
- Maven
- [JFoenix (JFX Component Library)](http://www.jfoenix.com/) 

## Setup Credentials for local Build with Maven
This repository uses SNAPSHOT dependencies within our github packages, to download the necessary dependencies you need a Github Account:
1. [create a PAT]( https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-token)
This Token must have the `read:packages` Scope
![read_pkgs](https://user-images.githubusercontent.com/8503486/122915213-ce350d00-d35b-11eb-98e4-2273a0e0a6ad.PNG)
2. Edit/Create your maven settings.xml accordingly:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>[GITHUB USERNAME]</username>
      <password>[TOKEN]</password>
    </server>
  </servers>
</settings>
```
This snippet already contains the correct server id, if you however (for any reason) change the server id within the pom.xml you need to make sure that the settings.xml is also set to the correct id.

## How to Start (for now)
1. Checkout Project
2. `mvn spring-boot:run`

## How to Package as executable jar (for now)
1. Checkout Project
3. `mvn clean install spring-boot:repackage`
4. execute `target/mms-desktop-spring-boot.jar` directly
