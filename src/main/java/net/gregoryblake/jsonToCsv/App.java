package net.gregoryblake.jsonToCsv;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class App {

	public static void main(String[] args) {
		/* 
		 * Uses javax.json rather than jackson or GSON because this problem seems more amenable to a map-based parsing.
		 */
		Optional<JsonArray> jsonArray = readFile();

		String csvRepresentation = jsonArray.isPresent() ? transform(jsonArray.get()) : "";
		
		writeFile(csvRepresentation);
	}
	
	private static Optional<JsonArray> readFile() {
		try {
			JsonArray jsonArray = Json.createReader(new FileReader("json.txt")).readArray();
			return Optional.of(jsonArray);
		} catch (FileNotFoundException e) {
			System.out.println("File does not exist.");
			return Optional.empty();
		}
	}
	
	private static String transform(JsonArray jsonArray){
		Set<String> csvKeys = new HashSet<>();
		List<HashMap<String,String>> csvRowsByKey = new ArrayList<>();
		
		// build the set of csv keys.
		jsonArray.stream().forEach(jsonValue -> {
			JsonObject jsonObject = (JsonObject) jsonValue; // It IS an Object! I swear!
			csvKeys.addAll(jsonObject.keySet());
		});
		
		jsonArray.stream().forEach(jsonValue -> {
			HashMap<String,String> csvRow = new HashMap<>();
			JsonObject jsonObject = (JsonObject) jsonValue;
			
			csvKeys.forEach(key -> {
				// if there's no value for the key, insert "", else convert the JsonValue to a String.
				String s = jsonObject.get(key) == null ? "" : jsonObject.get(key).toString(); 
			
				csvRow.put(key, s);
			});
			csvRowsByKey.add(csvRow);
		});
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < csvKeys.size(); i++) {
			sb.append(csvKeys.toArray()[i]);
			sb.append(i != csvKeys.size() - 1 ? "," : ""); // if it's the last key, append nothing, otherwise append a comma.	
		}
		sb.append("\n");
		
		for(int i = 0; i < csvRowsByKey.size(); i++) {
			HashMap<String,String> csvRow = csvRowsByKey.get(i);
			for(int j = 0; j < csvRow.size(); j++) {
				sb.append(csvRow.get(csvKeys.toArray()[j]));
				sb.append(j != csvKeys.size() - 1 ? "," : ""); // same here, if it's the last key, append nothing, etc.
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	private static void writeFile(String s) {
		Writer writer = null;
		
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("csv.txt"), "utf-8"));
			writer.write(s);
		} catch (IOException e) {
			System.out.println("Failed to write to file.");
		} finally {
			try { writer.close(); } catch (Exception e) { } // ignore close failure 
		}
	}
	
}