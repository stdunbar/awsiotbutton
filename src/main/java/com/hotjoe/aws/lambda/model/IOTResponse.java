package com.hotjoe.aws.lambda.model;


/**
 * Simple "return" for the Lambda - really only used to send email.
 */
public class IOTResponse {
    private String response;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
