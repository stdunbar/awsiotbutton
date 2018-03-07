package com.hotjoe.aws.lambda.notifier;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
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
public class SESNotifier {

    public static IOTResponse sendMessage( String to, String from, String objectAsJsonString, IOTButtonRequest request, LambdaLogger lambdaLogger ) {

        AmazonSimpleEmailService simpleEmailService  = AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(Regions.fromName(System.getenv("AWS_REGION"))).build();

        if( !isEmailEnabled(to, simpleEmailService, lambdaLogger )) {
            throw new IllegalStateException("Email to \"" + to +
                    "\" is not enabled yet - have the user check for an email allowing them to enable it");
        }

        String subject = "Java says hello from your IoT button " + request.getSerialNumber();
        String bodyText = "Java says hello from your IoT Button " + request.getSerialNumber() + ".\nHere is the full event: " + objectAsJsonString;

        simpleEmailService.sendEmail( new SendEmailRequest()
                .withSource(from)
                .withDestination(new Destination().withToAddresses(to))
                .withMessage(new Message()
                        .withBody(new Body(new Content(bodyText)))
                        .withSubject(new Content(subject))));

        IOTResponse iotResponse = new IOTResponse();

        iotResponse.setResponse(objectAsJsonString);

        return iotResponse;
    }

    /**
     * Checks to see if the emailAddress has been validated with SES.  If the address has not been validated
     * then send the validation request to the email.
     *
     * @param emailAddress the email address to check
     * @param simpleEmailService the AmazonSimpleEmailService to use for validation/requesting validation
     * @param lambdaLogger where to log any messages to
     *
     * @return true if the address has been validate, false otherwise.
     */
    private static boolean isEmailEnabled(String emailAddress,
                                   AmazonSimpleEmailService simpleEmailService,
                                   LambdaLogger lambdaLogger) {
        GetIdentityVerificationAttributesResult result = simpleEmailService.getIdentityVerificationAttributes(
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
            VerifyEmailIdentityResult response = simpleEmailService.verifyEmailIdentity(request);
            return false;
        }

        return false;
    }
}
