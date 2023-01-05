# attis
A utility for setting up AWS infrastructure for training or other purposes by starting 
the execution of pre-made step functions with basic user information as input.

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
docker push <user>/attis:<version>
docker tag <user>/attis:<version> <user>/attis:latest
docker push <user>/attis:latest
```

### Example root user
```
export AWS_ACCESS_KEY_ID=<access key>
export AWS_SECRET_ACCESS_KEY="<secret key>"
java -Daws.access.key.id=<access key> -Daws.secret.access.key="<secret key>" -Dserver.port=80  -jar attis.jar
```

### Making a step function for Attis
Start here if you are new to step functions: https://docs.aws.amazon.com/step-functions/latest/dg/welcome.html
Once you have your step function, ensure that it has a tag with the key "Attis". Casing is not important. This will allow Attis to find your step function. 
Do not expect Attis to interact with the infrastructure aside from starting the execution of your step function.
Attis is not synced with the execution of the step function and thus works as a fire and forget service. 
The format of the input JSON that will be passed to your step function is as follows: 
```
{
  "Participant": {
    "id": 1,
    "name": "first last",
    "initials": "abc",
    "email": "email@email.com",
    "company": "Company Inc."
  }
}
```
Be sure to keep this in mind when designing your step functions.

### Using Attis
After running Attis using one of the methods described above, the first thing 
you will have to do is get the generated admin password from the console output.
Once you try to access the website it will direct you to login. 
Use 'administrator' as the username and the password that you got from the output.
From here you'll be redirected to the initialization page. 
This is where you'll find a drop-down with all of your step functions that are tagged with "Attis".
This is the only time you will be able to get to this page so select carefully before submitting. 
You will then be redirected to the participants form page, and all subsequent visits to the site will be sent there.
When the form is filled out and submitted, it will execute the step function selected in the initialization using the form data as input (as shown above).
