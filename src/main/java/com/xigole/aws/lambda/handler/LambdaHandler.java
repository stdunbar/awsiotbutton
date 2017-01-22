package com.xigole.aws.lambda.handler;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.GetIdentityVerificationAttributesRequest;
import com.amazonaws.services.simpleemail.model.GetIdentityVerificationAttributesResult;
import com.amazonaws.services.simpleemail.model.IdentityVerificationAttributes;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityRequest;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xigole.aws.lambda.model.IOTButtonRequest;
import com.xigole.aws.lambda.model.IOTResponse;


@SuppressWarnings("unused")
public class LambdaHandler {
    private static final String FROM = "scott@somedomain.tld";
    private static final String TO = "scott@somedomain.tld";


    public IOTResponse handleRequest(IOTButtonRequest request, Context context) throws JsonProcessingException {
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        LambdaLogger lambdaLogger = context.getLogger();

        AmazonSimpleEmailServiceClient simpleEmailServiceClient = new AmazonSimpleEmailServiceClient(credentialsProvider);
        simpleEmailServiceClient.setRegion(Region.getRegion(Regions.fromName(System.getenv("AWS_DEFAULT_REGION"))));

        ObjectMapper objectMapper = new ObjectMapper();

        lambdaLogger.log("in handlerRequest:, input is " + objectMapper.writeValueAsString(request));
        lambdaLogger.log("Function name: " + context.getFunctionName());
        lambdaLogger.log("Max mem allocated: " + context.getMemoryLimitInMB());
        lambdaLogger.log("Time remaining in milliseconds: " + context.getRemainingTimeInMillis());

        if( !isEmailEnabled(TO, simpleEmailServiceClient )) {
            throw new IllegalStateException("Email to \"" + TO + "\" is not enabled yet");
        }

        String subject = "Java says hello from your IoT button " + request.getSerialNumber();
        String bodyText = "Java says hello from your IoT Button " + request.getSerialNumber() + ".\nHere is the full event: " +
                objectMapper.writeValueAsString(request);

        simpleEmailServiceClient.sendEmail( new SendEmailRequest()
                .withSource(TO)
                .withDestination(new Destination().withToAddresses(TO))
                .withMessage(new Message()
                        .withBody(new Body(new Content(bodyText)))
                        .withSubject(new Content(subject))));

        IOTResponse iotResponse = new IOTResponse();

        iotResponse.setResponse(objectMapper.writeValueAsString(request));

        return iotResponse;
    }

    private boolean isEmailEnabled(String emailAddress, AmazonSimpleEmailServiceClient simpleEmailServiceClient) {
        GetIdentityVerificationAttributesResult result = simpleEmailServiceClient.getIdentityVerificationAttributes(
                new GetIdentityVerificationAttributesRequest()
                        .withIdentities(emailAddress));

        if (result.getVerificationAttributes().containsKey(emailAddress)) {
            IdentityVerificationAttributes identityVerificationAttributes = result.getVerificationAttributes().get(emailAddress);

            if (identityVerificationAttributes.getVerificationStatus().equals("Success")) {
                System.out.println("the email \"" + emailAddress + "\" has been verified");
                return true;
            }
        } else {
            System.out.println("the email address \"" + emailAddress + "\" has not been verified - sending email");

            VerifyEmailIdentityRequest request = new VerifyEmailIdentityRequest()
                    .withEmailAddress(emailAddress);
            VerifyEmailIdentityResult response = simpleEmailServiceClient.verifyEmailIdentity(request);
            return false;
        }

        return false;
    }
}
