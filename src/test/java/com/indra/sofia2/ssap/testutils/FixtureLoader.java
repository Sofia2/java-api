package com.indra.sofia2.ssap.testutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FixtureLoader {
	
	private final String basePath;
	
	public FixtureLoader() {
		this.basePath = "fixtures/";
	}
	
	public FixtureLoader(String basePath) {
		this.basePath = basePath;
	}

	public JsonNode load(String name) throws IOException  {
		String path = String.format("%s/%s%s", basePath,name, ".json");
		
		String contents = new String(Files.readAllBytes(Paths.get(path)));
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true);
		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		
		return  mapper.readTree(contents);
	}
}
