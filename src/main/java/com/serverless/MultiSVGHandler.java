package com.serverless;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.MetadataEntry;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.AmazonServiceException;
import java.io.File;
import java.io.InputStream;

import com.serverless.TranscoderService;

public class MultiSVGHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(MultiSVGHandler.class);
	private static final String BUCKET = "prov-comic-storage";
	private static final String REGION = "eu-central-1";

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input.get("body"));
		ArrayList<ByteArrayOutputStream> outputs = new ArrayList<>();
		String concSvgs = input.get("body").toString();
		String rawSvgs[] = concSvgs.split(",[ \n]*(?=<)");
		LOG.debug("Number of SVGs: {}", rawSvgs.length);	
		
		try {
			for(String svg : rawSvgs) {
				outputs.add(TranscoderService.svg2Binary(svg));
			}
		} catch(Exception ex) {
			LOG.error("Error occured during SVG parsing or conversion");
			ex.printStackTrace();
			return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("Error occured during SVG parsing or conversion", Collections.singletonMap("message", ex.getMessage()))).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
		}
		
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		final String fileKeys[] = new String[outputs.size()];
		try {
		int idx = 0;
			for(ByteArrayOutputStream stream : outputs) {
				String fileKey = "provenance_image" + (System.currentTimeMillis() / 1000l) + "_" + idx + ".jpg";
				byte transcoderData[] = stream.toByteArray();
				InputStream transcoderRes = new ByteArrayInputStream(transcoderData);
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(transcoderData.length);
				s3.putObject(BUCKET, fileKey , transcoderRes, metadata);
				fileKeys[idx] = fileKey;
				idx++;
			}
		} catch (AmazonServiceException ex) {
			LOG.error("Error occured during image upload to s3");
		    ex.printStackTrace();
		    return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("Error occured during image upload to s3", Collections.singletonMap("message", ex.getMessage()))).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
		}
		
		Response responseBody = new Response("SVG Transcoding successful, has been uploaded to s3 bucket", Collections.singletonMap("key", fileKeys));
		return ApiGatewayResponse.builder().setStatusCode(200).setObjectBody(responseBody).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
	}
}
