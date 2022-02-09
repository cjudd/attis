# attis
Amazon Cloud Training Registration Application &amp; Orchestration App use to create student accounts in AWS.

* Create IAM user
* Add IAM user to Developer user group
* Creates password
* Creates access and secret key
* Emails new user their credentials

## Build
### Package
To create an executable jar.
```
./mvnw package
```
### Test
To run unit tests.
```
./mvnw test
```
### Run
```
java -Daws.account.url=<account url> -Dspring.mail.username=<mail username> -Dspring.mail.password=<mail passowrd> -Dmessage.send.from=<sent form> -jar target/attis-0.0.1-SNAPSHOT.jar
```
### Docker Image
```
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=javajudd/attis
```
### Run Docker Container
```
docker run --rm -it -p80:8080 -e AWS_ACCESS_KEY_ID=<access key> -e AWS_SECRET_ACCESS_KEY=<secret key> -e AWS_ACCOUNT_URL=<account url> -e SPRING_MAIL_USERNAME=<mail user> -e SPRING_MAIL_PASSWORD=<mail password> -e MESSAGE.SEND.FROM=<efrom email>  javajudd/attis
```