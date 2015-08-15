package com.helpers;

import java.io.StringWriter;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonWriter;

public class Helper {

	
	public static void printJsonDocument(Document document){
		StringWriter writer = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(writer);
		new DocumentCodec().encode(jsonWriter, document, EncoderContext
				.builder().isEncodingCollectibleDocument(true).build());
		
		System.out.println(jsonWriter.getWriter());
	}
}
