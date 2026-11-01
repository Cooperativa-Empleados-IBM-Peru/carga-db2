package ibm.coope;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CoopeUtils {

	private static String resourcefolder = "./resources";

	public static LocalDate convertTolocalDate(String input, String vformat) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(vformat);
		LocalDate ld = LocalDate.parse(input, formatter);

		return ld;
	}

	public static LocalDateTime convertTolocalDateTime(String input, String vformat) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(vformat);
		LocalDateTime ldt = LocalDateTime.parse(input, formatter);

		return ldt;
	}

	public static String cleanString(String input) {
			
		return input.replaceAll("[^a-z A-Z0-9]", "").trim();
	}

	/**
	 * Loads ./resources/coopeibm.properties.
	 *
	 * @throws IllegalStateException if the file does not exist or cannot be read
	 */
	public static Properties loadSettings() {

		Properties prop = new Properties();

		final String configFileprop = resourcefolder + "/coopeibm.properties";

		try (InputStream inputStream = new FileInputStream(configFileprop)) {
			prop.load(inputStream);
		} catch (IOException ex) {
			throw new IllegalStateException("Cannot read the configuration file "
					+ new File(configFileprop).getAbsolutePath() + " (" + ex.getMessage() + "). "
					+ "Create it from resources/coopeibm.properties.template and run the program "
					+ "from the folder that contains the 'resources' folder", ex);
		}

		return prop;

	}

	/**
	 * Returns the keys that are missing, blank, or still hold a template placeholder
	 * such as {@code <sftp-hostname>}. Values are never returned (they may be secrets).
	 */
	public static List<String> missingProperties(Properties prop, String... keys) {

		List<String> missing = new ArrayList<>();

		for (String key : keys) {
			String value = prop.getProperty(key);
			if (value == null || value.isBlank() || (value.trim().startsWith("<") && value.trim().endsWith(">"))) {
				missing.add(key);
			}
		}

		return missing;

	}

}
