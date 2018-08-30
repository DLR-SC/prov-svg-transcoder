package com.serverless;

import java.io.*;
import org.w3c.dom.svg.*;
import org.apache.batik.anim.dom.*;
import org.apache.batik.util.*;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;

public class TranscoderService {
    
    public static ByteArrayOutputStream svg2Binary(String svgString, String type) throws TranscoderException, IOException, IllegalArgumentException {
    	if(!type.equals("jpg") && !type.equals("png"))
    		throw new IllegalArgumentException("Transcoding type has to be svg, png or jpg");
        // Create a JPEG transcoder and set hints
        JPEGTranscoder t = new JPEGTranscoder();
        PNGTranscoder p = new PNGTranscoder();
        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));

        // Create the transcoder input.
        //String svgString = "<svg  xmlns=\"http://www.w3.org/2000/svg\" height=\"150\" width=\"500\"><ellipse cx=\"240\" cy=\"100\" rx=\"220\" ry=\"30\" style=\"fill:purple\" /><ellipse cx=\"220\" cy=\"70\" rx=\"190\" ry=\"20\" style=\"fill:lime\" /><ellipse cx=\"210\" cy=\"45\" rx=\"170\" ry=\"15\" style=\"fill:yellow\" /></svg>";
        SVGDocument doc = loadXMLFromString(svgString);
        //System.out.println(doc);

        TranscoderInput input = new TranscoderInput(doc);
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		TranscoderOutput output = new TranscoderOutput(ostream);
		// Create the transcoder output.
		if(type.equals("jpg")) t.transcode(input, output);
		else p.transcode(input, output);

		return (ByteArrayOutputStream) output.getOutputStream();
    }

	public static SVGDocument loadXMLFromString(String xml) throws IOException {
		SVGDocument doc = null;
		StringReader reader = new StringReader(xml);
		String uri = "http://example.serverless.com";
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		doc = f.createSVGDocument(uri, reader);

		reader.close();

		return doc;
	}
}