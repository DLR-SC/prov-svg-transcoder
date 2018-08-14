package com.serverless;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class S3Service {
	private static final String BUCKET = "prov-comic-storage";
	private static final String REGION = "eu-central-1";
	private static final String ROOT_KEY = "provenance_image";
	
	public static String uploadS3(ByteArrayOutputStream payload, int idx, String type) throws AmazonServiceException {
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		final String fileKey = ROOT_KEY + (System.currentTimeMillis() / 1000l) + "_" + idx + "." + type;
		byte transcoderData[] = payload.toByteArray();
		InputStream transcoderRes = new ByteArrayInputStream(transcoderData);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(transcoderData.length);
		s3.putObject(BUCKET, fileKey , transcoderRes, metadata);
		
		return fileKey;
	}
	
	public static String uploadS3(String payload, int idx, String type) throws AmazonServiceException {
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		final String fileKey = ROOT_KEY + (System.currentTimeMillis() / 1000l) + "_" + idx + "." + type;
		s3.putObject(BUCKET, fileKey, payload);
		
		return fileKey;
	}
	
	
	public static String uploadS3(ByteArrayOutputStream payload, String type) throws AmazonServiceException {
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		final String fileKey = ROOT_KEY + (System.currentTimeMillis() / 1000l) + "." + type;
		byte transcoderData[] = payload.toByteArray();
		InputStream transcoderRes = new ByteArrayInputStream(transcoderData);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(transcoderData.length);
		s3.putObject(BUCKET, fileKey , transcoderRes, metadata);
		
		return fileKey;
	}
	
	public static String uploadS3(String payload, String type) throws AmazonServiceException {
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		final String fileKey = ROOT_KEY + (System.currentTimeMillis() / 1000l) + "." + type;
		s3.putObject(BUCKET, fileKey, payload);
		
		return fileKey;
	}

}
