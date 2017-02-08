package com.hotjoe.aws.lambda.handler;

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
import com.hotjoe.aws.lambda.model.IOTButtonRequest;
import com.hotjoe.aws.lambda.model.IOTResponse;


/**
 * A simple class to test the Amazon IOT Button with a Lambda backend.  See the README for usage
 * details.
 */
@SuppressWarnings("unused") // make the IDE happy even though we know this class is used.
public class LambdaHandler {

    /**
     * This is the method called by the AWS Lambda service.
     *
     * @param request a IOTButtonRequest object that has been deserialized by Lambda.
     * @param context the Lambda context that this method was called with.
     *
     * @return an IOTResponse (which is basically just a String) that echos the input.
     *
     * @throws JsonProcessingException if there was a problem parsing the input
     */
    public IOTResponse handleRequest(IOTButtonRequest request, Context context) throws JsonProcessingException {

        String from = System.getenv("FROM_ADDRESS");
        String to = System.getenv("TO_ADDRESS");

        if( from == null )
            throw new IllegalArgumentException("FROM_ADDRESS needs to be set as a Lambda environment variable");

        if( to == null )
            throw new IllegalArgumentException("TO_ADDRESS needs to be set as a Lambda environment variable");

        //
        // this will get credentials in a variety of ways
        //
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        LambdaLogger lambdaLogger = context.getLogger();

        AmazonSimpleEmailServiceClient simpleEmailServiceClient = new AmazonSimpleEmailServiceClient(credentialsProvider);
        simpleEmailServiceClient.setRegion(Region.getRegion(Regions.fromName(System.getenv("AWS_DEFAULT_REGION"))));

        ObjectMapper objectMapper = new ObjectMapper();

        lambdaLogger.log("in handlerRequest:, input is " + objectMapper.writeValueAsString(request));
        lambdaLogger.log("Function name: " + context.getFunctionName());
        lambdaLogger.log("Max mem allocated: " + context.getMemoryLimitInMB());
        lambdaLogger.log("Time remaining in milliseconds: " + context.getRemainingTimeInMillis());

        if( !isEmailEnabled(to, simpleEmailServiceClient, lambdaLogger )) {
            throw new IllegalStateException("Email to \"" + to +
                    "\" is not enabled yet - have the user check for an email allowing them to enable it");
        }

        String subject = "Java says hello from your IoT button " + request.getSerialNumber();
        String bodyText = "Java says hello from your IoT Button " + request.getSerialNumber() + ".\nHere is the full event: " +
                objectMapper.writeValueAsString(request);

        simpleEmailServiceClient.sendEmail( new SendEmailRequest()
                .withSource(from)
                .withDestination(new Destination().withToAddresses(to))
                .withMessage(new Message()
                        .withBody(new Body(new Content(bodyText)))
                        .withSubject(new Content(subject))));

        IOTResponse iotResponse = new IOTResponse();

        iotResponse.setResponse(objectMapper.writeValueAsString(request));

        return iotResponse;
    }

    /**
     * Checks to see if the emailAddress has been validated with SES.  If the address has not been validated
     * then send the validation request to the email.
     *
     * @param emailAddress the email address to check
     * @param simpleEmailServiceClient the AmazonSimpleEmailServiceClient to use for validation/requesting validation
     * @param lambdaLogger where to log any messages to
     *
     * @return true if the address has been validate, false otherwise.
     */
    private boolean isEmailEnabled(String emailAddress,
                                   AmazonSimpleEmailServiceClient simpleEmailServiceClient,
                                   LambdaLogger lambdaLogger) {
        GetIdentityVerificationAttributesResult result = simpleEmailServiceClient.getIdentityVerificationAttributes(
                new GetIdentityVerificationAttributesRequest()
                        .withIdentities(emailAddress));

        if (result.getVerificationAttributes().containsKey(emailAddress)) {
            IdentityVerificationAttributes identityVerificationAttributes = result.getVerificationAttributes().get(emailAddress);

            if (identityVerificationAttributes.getVerificationStatus().equals("Success")) {
                lambdaLogger.log("the email \"" + emailAddress + "\" has been verified");
                return true;
            }
        } else {
            lambdaLogger.log("the email address \"" + emailAddress + "\" has not been verified - sending verification email");

            VerifyEmailIdentityRequest request = new VerifyEmailIdentityRequest()
                    .withEmailAddress(emailAddress);
            VerifyEmailIdentityResult response = simpleEmailServiceClient.verifyEmailIdentity(request);
            return false;
        }

        return false;
    }
}
