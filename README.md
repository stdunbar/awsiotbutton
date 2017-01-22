# README #

A very simple <a href="https://aws.amazon.com/lambda/">AWS Lambda</a> function to handle an <a href="https://aws.amazon.com/iotbutton/">AWS IoT Button</a>.  The Lambda function does nothing
but log the information from the button at this time.  But there were no Java examples and I
wanted to try this in Java.


### Setup ###

This code is dependent on having an AWS account and a AWS IoT button.  I won't go through
the setup as it's well documented on the Amazon site.

However, on the build side this is a simple Maven project.  Simply do a:

'''mvn clean package'''

to get a Jar file that can be deployed to Lambda.  If you've cloned this repository and run
the build then you will need to deploy <pre>target/lambda-1.0-SNAPSHOT.jar</pre> to Lambda.
The handler that will be needed in Lambda is

'''com.xigole.aws.lambda.handler.LambdaHandler::handleRequest'''

again, with the code I have here.