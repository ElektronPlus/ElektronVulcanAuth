W `/src/main/resources/` zrób plik **application-local.properties** i wklej tam swoje propertiesy

### Build:
`./gradlew build -x test`

### Do uruchomienia wymagana java 17
`java -jar -Dspring.profiles.active=local *.jar`


