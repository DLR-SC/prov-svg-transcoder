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
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.AmazonServiceException;
import java.io.File;
import java.io.InputStream;

import com.serverless.TranscoderService;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);
	private static final String BUCKET = "prov-comic-storage";
	private static final String REGION = "eu-central-1";

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input.get("body"));
		ByteArrayOutputStream output;
		
		try {
			output = TranscoderService.svg2Binary(input.get("body").toString());
		} catch(Exception ex) {
			System.err.println("Error occured during SVG parsing or conversion");
			ex.printStackTrace();
			return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("Error occured during SVG parsing or conversion", Collections.singletonMap("message", ex.getMessage()))).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
		}
		
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		final String fileKey = "provenance_image" + (System.currentTimeMillis() / 1000l) + ".jpg";
		InputStream transcoderRes = new ByteArrayInputStream(output.toByteArray());
		try {
		    s3.putObject(BUCKET, fileKey , transcoderRes, null);
		} catch (AmazonServiceException ex) {
			System.err.println("Error occured during image upload to s3");
		    ex.printStackTrace();
		    return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("Error occured during image upload to s3", Collections.singletonMap("message", ex.getMessage()))).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
		}
		
		Response responseBody = new Response("SVG Transcoding successful, has been uploaded to s3 bucket", Collections.singletonMap("key", fileKey));
		return ApiGatewayResponse.builder().setStatusCode(200).setObjectBody(responseBody).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
	}
}
