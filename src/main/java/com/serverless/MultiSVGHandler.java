package com.serverless;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class MultiSVGHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(MultiSVGHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input.get("body"));
		LOG.info("Info: {}", input.get("binaryType"));
		String type = input.get("binaryType") == null ? "jpg" : input.get("binaryType").toString().toLowerCase();
		
		ArrayList<ByteArrayOutputStream> outputs = new ArrayList<>();
		ArrayList<String> originals = new ArrayList<>();
		String concSvgs = input.get("body").toString();
		concSvgs= concSvgs.substring(1, concSvgs.length() -  1);
		String rawSvgs[] = concSvgs.split(",[ \n]*(?=<)");
		LOG.debug("Number of SVGs: {}", rawSvgs.length);
		//LOG.debug(rawSvgs[1]);
		
		try {
			if(!type.equals("svg")) {
				for(String svg : rawSvgs) {
					outputs.add(TranscoderService.svg2Binary(svg, type));
				}
			}
		} catch(Exception ex) {
			LOG.error("Error occured during SVG parsing or conversion");
			ex.printStackTrace();
			return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("Error occured during SVG parsing or conversion", Collections.singletonMap("message", ex.getMessage()))).setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")).build();
		}
		
		final String fileKeys[] = new String[rawSvgs.length];
		try {
			int idx = 0;
			if(!type.equals("svg")) {
				for(ByteArrayOutputStream stream : outputs) {
					fileKeys[idx] = S3Service.uploadS3(stream, idx, type);
					idx++;
				}
			} else {
				for(String origSvg : rawSvgs) {
					fileKeys[idx] = S3Service.uploadS3(origSvg, idx, type);
					idx++;
				}
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
