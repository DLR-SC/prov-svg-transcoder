package com.serverless;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.AmazonServiceException;
import java.io.File;
import java.io.InputStream;

import com.serverless.TranscoderService;
import com.serverless.S3Service;

public class SingleSVGHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(SingleSVGHandler.class);
	private static final String BUCKET = "prov-comic-storage";
	private static final String REGION = "eu-central-1";

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("{}> received data", System.currentTimeMillis() / 1000l);
		LOG.info("Info: {}", input.get("binaryType"));
		
		String type = input.get("binaryType") == null ? "jpg" : input.get("binaryType").toString().toLowerCase();
		ByteArrayOutputStream output;
		String fileKey = "";
		try {
			if(!type.equals("svg")) {
				output = TranscoderService.svg2Binary(input.get("body").toString(), type);
				fileKey = S3Service.uploadS3(output, type);
			} else {
				fileKey = S3Service.uploadS3(input.get("body").toString(), type);
			}
			LOG.info("{}> transcoded data", System.currentTimeMillis() / 1000l);
			LOG.info("{}> uploaded data", System.currentTimeMillis() / 1000l);
		} catch(AmazonServiceException ex) {
			System.err.println("Error occured during image upload to s3");
		    ex.printStackTrace();
		    return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("Error occured during image upload to s3", Collections.singletonMap("message", ex.getMessage()))).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
		} catch(Exception ex) {
			System.err.println("Error occured during SVG parsing or conversion");
			ex.printStackTrace();
			return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("Error occured during SVG parsing or conversion", Collections.singletonMap("message", ex.getMessage()))).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
		}
		
		Response responseBody = new Response("SVG Transcoding successful, has been uploaded to s3 bucket", Collections.singletonMap("key", fileKey));
		return ApiGatewayResponse.builder().setStatusCode(200).setObjectBody(responseBody).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
	}
}

