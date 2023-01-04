# attis
Amazon Cloud Training Registration Application &amp; Orchestration App use to create student accounts in AWS.
This is done simply by executing step functions created by Cybele.

Step functions: 
* Create IAM user
* Add IAM user to Developer user group
* Creates password
* Creates access and secret key
* Emails new user their credentials
* Creates VM for user

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
java -jar target/attis-2.0.0.jar
```
### Docker Image
```
./mvnw spring-boot:build-image
```
### Run Docker Container
```
docker run --rm -it -p80:8080 -e AWS_ACCESS_KEY_ID=<access key> -e AWS_SECRET_ACCESS_KEY=<secret key> javajudd/attis
```
### Push Docker Image to hub.docker.com
```
docker push javajudd/attis:<version>
docker tag javajudd/attis:<version> javajudd/attis:latest
docker push javajudd/attis:latest
```

### Example root user
```
export AWS_ACCESS_KEY_ID=<access key>
export AWS_SECRET_ACCESS_KEY="<secret key>"
java -Daws.access.key.id=<access key> -Daws.secret.access.key="<secret key>" -Dserver.port=80  -jar attis.jar
```