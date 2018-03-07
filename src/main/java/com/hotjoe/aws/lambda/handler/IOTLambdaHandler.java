package com.hotjoe.aws.lambda.handler;

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
import com.hotjoe.aws.lambda.notifier.SESNotifier;


/**
 * A simple class to test the Amazon IOT Button with a Lambda backend.  See the README for usage
 * details.
 */
@SuppressWarnings("unused") // make the IDE happy even though we know this class is used.
public class IOTLambdaHandler {

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


        ObjectMapper objectMapper = new ObjectMapper();

        String objectAsString = objectMapper.writeValueAsString(request);

        lambdaLogger.log("in handlerRequest:, input is " + objectAsString);
        lambdaLogger.log("Function name: " + context.getFunctionName());
        lambdaLogger.log("Max mem allocated: " + context.getMemoryLimitInMB());
        lambdaLogger.log("Time remaining in milliseconds: " + context.getRemainingTimeInMillis());

        String notifierType = System.getenv("NOTIFIER");

        if( (notifierType == null) || (notifierType.toLowerCase().equals("email"))) {
            return SESNotifier.sendMessage(to, from, objectAsString, request, lambdaLogger );
        }
        else {

        }
    }
}
