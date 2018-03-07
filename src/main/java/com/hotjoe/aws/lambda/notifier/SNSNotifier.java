package com.hotjoe.aws.lambda.notifier;

import com.amazonaws.regions.Regions;
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
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SetSMSAttributesRequest;
import com.hotjoe.aws.lambda.model.IOTButtonRequest;
import com.hotjoe.aws.lambda.model.IOTResponse;


/**
 * A simple class to test the Amazon IOT Button with a Lambda backend.  See the README for usage
 * details.
 */
@SuppressWarnings("unused") // make the IDE happy even though we know this class is used.
public class SNSNotifier {

    public static IOTResponse sendMessage( String to, String from, String objectAsJsonString, IOTButtonRequest request, LambdaLogger lambdaLogger ) {

        String topicName = System.getenv("SNS_TOPIC_NAME");

        AmazonSNS amazonSNS  = AmazonSNSClientBuilder.standard()
                        .withRegion(Regions.fromName(System.getenv("AWS_REGION"))).build();

        CreateTopicResult createTopicResult = amazonSNS.createTopic(topicName);

        SetSMSAttributesRequest setRequest = new SetSMSAttributesRequest()
                .addAttributesEntry("DefaultSenderID", "Xigole")
                .addAttributesEntry("MonthlySpendLimit", "1")
                .addAttributesEntry("DefaultSMSType", "Transactional")
                .addAttributesEntry("UsageReportSBucket", "xigole-sms-daily-usage");
        amazonSNS.setSMSAttributes(setRequest);

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
}
