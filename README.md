# README #

A very simple [AWS Lambda](https://aws.amazon.com/lambda/) function to handle an
[AWS IoT Button](https://aws.amazon.com/iotbutton/).  The Lambda function is based on the default
Lambda methods when you setup the IOT Button but is in Java.  First, it logs the message that it
gets from the button to the Lambda log.  Then, if the "TO_ADDRESS" address is not already registered
with [AWS SES](https://aws.amazon.com/ses/) it sends a welcome email to let SES know that the
destination address is valid.  Note that the "FROM_ADDRESS" address also needs to be verified this
way too - you may need to set the TO_ADDRESS and FROM_ADDRESS addresses the same to allow you to send email
initially.


### Setup ###

This code is dependent on having an AWS account and a AWS IoT button.  I won't go through
the setup as it's well documented on the Amazon site.

However, on the build side this is a simple Maven project.  Simply do a:

`mvn clean package`

to get a Jar file that can be deployed to Lambda.  If you've cloned this repository and run
the build then you will need to deploy `target/lambda-1.0-SNAPSHOT.jar` to Lambda.
The handler that will be needed in Lambda is `com.hotjoe.aws.lambda.handler.LambdaHandler::handleRequest`
with the code I have here.

The setup also needs to have Lambda environment variables for FROM_ADDRESS and TO_ADDRESS.  These
can be set in the Lambda console under Lambda -> Functions ->  (lambda name) -> Code.  Again, note that these addresses
need to be validated with SES.  To simplify things, start by setting them to the same thing.  You'll
 get an email from Amazon that needs to have the link in it clicked on.  At that point your "FROM_ADDRESS"
 can send emails.  But you still can't send to arbitrary "TO_ADDRESS" emails - they still need
 to be validated.
