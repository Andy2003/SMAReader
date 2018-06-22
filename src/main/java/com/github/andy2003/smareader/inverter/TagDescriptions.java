package com.github.andy2003.smareader.inverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagDescriptions {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagDescriptions.class);
	private static final String FILE_TAG_LIST = "/support_files/TagListEN-US.txt";
	private static final TagDescriptions INSTANCE = new TagDescriptions();

	private Map<Integer, String> tagDescriptions = new HashMap<>();

	private TagDescriptions() {
		read();
	}

	public static TagDescriptions getInstance() {
		return INSTANCE;
	}

	private void read() {
		try (InputStream in = getClass().getResourceAsStream(FILE_TAG_LIST);
				BufferedReader br = new BufferedReader(new InputStreamReader(in)))
		{
			String line;
			int lineCnt = 0;
			while ((line = br.readLine()) != null) {
				lineCnt++;

				// Get rid of comments and empty lines
				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}

				// Split line TagID=Tag\Lri\Descr
				String[] lineparts = line.split("[=\\\\]");
				if (lineparts.length != 4) {
					LOGGER.warn("Error: {} on line {} [{}]", "Wrong number of items", lineCnt, FILE_TAG_LIST);
					continue;
				}
				try {
					int tagID = Integer.parseInt(lineparts[0]);
					tagDescriptions.put(tagID, lineparts[3].trim());
				} catch (NumberFormatException e) {
					LOGGER.warn("Error: {} on line {} [{}]", "Invalid tagID", lineCnt, FILE_TAG_LIST);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("could not read TagList", e);
		}
	}

	public String getDescription(int tagID) {
		return tagDescriptions.get(tagID);
	}

}
