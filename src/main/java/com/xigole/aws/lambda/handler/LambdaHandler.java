package com.xigole.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.xigole.aws.lambda.model.IOTButtonRequest;
import com.xigole.aws.lambda.model.IOTResponse;



@SuppressWarnings("unused")
public class LambdaHandler {
    public IOTResponse handleRequest(IOTButtonRequest request, Context context) {
        LambdaLogger lambdaLogger = context.getLogger();
        Gson gson = new Gson();

        lambdaLogger.log("in handlerRequest:, input is " + gson.toJson(request));
        lambdaLogger.log("Function name: " + context.getFunctionName());
        lambdaLogger.log("Max mem allocated: " + context.getMemoryLimitInMB());
        lambdaLogger.log("Time remaining in milliseconds: " + context.getRemainingTimeInMillis());

        IOTResponse iotResponse = new IOTResponse();

        iotResponse.setResponse(gson.toJson(request));

        return iotResponse;
    }
}
