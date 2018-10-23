package org.erachain.log4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collections;
import java.util.stream.Collectors;

public class Logging extends DispatcherServlet {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("Logging!");
        //to be able to read request body multiple times and use "request" instance in lambda below
        MultiReadRequestUtility multiReadRequest = new MultiReadRequestUtility(request);

        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(multiReadRequest);
        }

        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        try {
            super.doDispatch(request, response);
        }

        finally {


            logger.info(request.getMethod() + " Request to " + request.getRequestURL());
            Collections.list(multiReadRequest.getHeaderNames())
                    .forEach(s -> logger.info(s + ": " + multiReadRequest.getHeader(s)));
            logger.info("Request body:");
            logger.info(multiReadRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            logger.info("Status Code: " + response.getStatus());

            String responseBody = new String(((ContentCachingResponseWrapper) response).getContentAsByteArray());
            logger.info(responseBody);
            updateResponse(response);
        }
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            responseWrapper.copyBodyToResponse();
        }
    }
}
