package installer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Client {
	private GUI gui;

	public Client(GUI gui) {
		this.gui = gui;
	}

	public void updateClientVersions() {
		String selectedMinecraftVersion = (String) gui.minecraftVersionDropdown.getSelectedItem();
		List<String> clientVersions = getClientVersions(selectedMinecraftVersion);
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(clientVersions.toArray(new String[0]));
		gui.clientVersionDropdown.setModel(model);
	}

	public List<String> getMinecraftVersions() {
		List<String> minecraftVersions = new ArrayList<>();

		try {
			URL jsonUrl = new URL("https://raw.githubusercontent.com/MorchClient/resources/json/installer.json");

			HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
			connection.setConnectTimeout(5000);

			JsonReader jsonReader = new JsonReader(new InputStreamReader(connection.getInputStream()));
			JsonObject json = new Gson().fromJson(jsonReader, JsonObject.class);
			JsonObject clientJson = json.getAsJsonObject("client");

			for (String minecraftVersion : clientJson.keySet()) {
				JsonArray versions = clientJson.getAsJsonObject(minecraftVersion).getAsJsonArray("versions");

				if (!versions.isJsonNull() && versions.size() > 0) {
					minecraftVersions.add(minecraftVersion);
				}
			}
		} catch (IOException e) {
			gui.statusLabel.setText(
					"Error: Unable to fetch Minecraft versions. Check your internet connection or try again later.");
			e.printStackTrace();
		}

		return minecraftVersions;
	}

	public List<String> getClientVersions(String minecraftVersion) {
		List<String> clientVersions = new ArrayList<>();

		try {
			URL jsonUrl = new URL("https://raw.githubusercontent.com/MorchClient/resources/json/installer.json");

			HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
			connection.setConnectTimeout(5000);

			JsonReader jsonReader = new JsonReader(new InputStreamReader(connection.getInputStream()));
			JsonObject json = new Gson().fromJson(jsonReader, JsonObject.class);
			JsonObject clientJson = json.getAsJsonObject("client");

			if (clientJson.has(minecraftVersion)) {
				JsonArray versions = clientJson.getAsJsonObject(minecraftVersion).getAsJsonArray("versions");

				for (int i = 0; i < versions.size(); i++) {
					String version = versions.get(i).getAsString();
					clientVersions.add(version);
				}
			}
		} catch (IOException e) {
			gui.statusLabel.setText(
					"Error: Unable to fetch client versions. Check your internet connection or try again later.");
			e.printStackTrace();
		}

		return clientVersions;
	}

	public void downloadAndPlaceJar(String instLink, String selectedClientVersion, String minecraftPath,
			String selectedMinecraftVersion) {
		try {
			URL jarUrl = new URL(instLink);

			// Set a timeout for the URL connection (e.g., 5000 milliseconds)
			HttpURLConnection connection = (HttpURLConnection) jarUrl.openConnection();
			connection.setConnectTimeout(5000);

			InputStream in = connection.getInputStream();

			// Create the .minecraft/versions folder if it doesn't exist
			Path versionsPath = Paths.get(minecraftPath, "versions");
			Files.createDirectories(versionsPath);

			// Define the destination directory for the JAR file
			String versionDirectoryName = "morch-" + selectedClientVersion + "-" + selectedMinecraftVersion;
			Path versionDirectoryPath = Paths.get(versionsPath.toString(), versionDirectoryName);

			// Create the version directory if it doesn't exist
			Files.createDirectories(versionDirectoryPath);

			// Define the destination path for the JAR file
			String jarFileName = "morch-" + selectedClientVersion + "-" + selectedMinecraftVersion + ".jar";
			Path destinationPath = Paths.get(versionDirectoryPath.toString(), jarFileName);

			// Copy the JAR file to the destination path
			Files.copy(in, destinationPath, StandardCopyOption.REPLACE_EXISTING);

			System.out.println("Downloaded and placed JAR file at: " + destinationPath.toString());
			SwingUtilities.invokeLater(() -> gui.statusLabel.setText("Installation completed successfully!"));
		} catch (IOException e) {
			// Handle the exception and display an error message in the status label
			SwingUtilities.invokeLater(() -> gui.statusLabel.setText(
					"Error: Unable to download and place JAR file. Check your internet connection or try again later."));
			e.printStackTrace();
		}
	}

	public void installClient(String selectedClientVersion, String selectedMinecraftVersion, String minecraftPath) {
		new Thread(() -> {
			try {
				// Fetch Mojang's version manifest
				URL mojangJsonUrl = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
				HttpURLConnection mojangConnection = (HttpURLConnection) mojangJsonUrl.openConnection();
				mojangConnection.setConnectTimeout(5000);
				JsonReader mojangJsonReader = new JsonReader(new InputStreamReader(mojangConnection.getInputStream()));
				JsonObject mojangJson = new Gson().fromJson(mojangJsonReader, JsonObject.class);

				// Find the version information for the selectedMinecraftVersion
				JsonArray versions = mojangJson.getAsJsonArray("versions");
				JsonObject selectedVersionInfo = null;
				for (JsonElement version : versions) {
					JsonObject versionObject = version.getAsJsonObject();
					if (versionObject.get("id").getAsString().equals(selectedMinecraftVersion)) {
						selectedVersionInfo = versionObject;
						break;
					}
				}

				if (selectedVersionInfo != null) {
					// Modify client JSON
					modifyClientJson(selectedClientVersion, selectedMinecraftVersion, minecraftPath);

					// Fetch modified MorchClient's JSON
					URL morchJsonUrl = new URL(
							"https://raw.githubusercontent.com/MorchClient/resources/json/installer.json");
					HttpURLConnection morchConnection = (HttpURLConnection) morchJsonUrl.openConnection();
					morchConnection.setConnectTimeout(5000);
					JsonReader morchJsonReader = new JsonReader(
							new InputStreamReader(morchConnection.getInputStream()));
					JsonObject morchJson = new Gson().fromJson(morchJsonReader, JsonObject.class);
					JsonObject clientJson = morchJson.getAsJsonObject("client");

					if (clientJson.has(selectedMinecraftVersion)) {
						JsonArray instLinks = clientJson.getAsJsonObject(selectedMinecraftVersion)
								.getAsJsonArray("instLink");

						SwingUtilities.invokeLater(() -> gui.progressBar.setMaximum(instLinks.size()));

						for (int i = 0; i < instLinks.size(); i++) {
							String instLink = instLinks.get(i).getAsString();
							downloadAndPlaceJar(instLink, selectedClientVersion, minecraftPath,
									selectedMinecraftVersion);

							if (i % 5 == 0) {
								int finalI = i;
								SwingUtilities.invokeLater(() -> gui.progressBar.setValue(finalI + 1));
							}
						}

						SwingUtilities.invokeLater(() -> gui.progressBar.setValue(instLinks.size()));
					}
				} else {
					SwingUtilities.invokeLater(() -> gui.statusLabel
							.setText("Error: Selected Minecraft version not found in Mojang's manifest."));
				}
			} catch (IOException e) {
				SwingUtilities.invokeLater(() -> gui.statusLabel.setText(
						"Error: Unable to fetch or modify Mojang's version manifest. Check your internet connection or try again later."));
				e.printStackTrace();
			}
		}).start();
	}

	private void modifyClientJson(String selectedClientVersion, String selectedMinecraftVersion, String minecraftPath) {
		try {
			// Fetch Mojang's version manifest
			URL mojangJsonUrl = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
			HttpURLConnection mojangConnection = (HttpURLConnection) mojangJsonUrl.openConnection();
			mojangConnection.setConnectTimeout(5000);
			JsonReader mojangJsonReader = new JsonReader(new InputStreamReader(mojangConnection.getInputStream()));
			JsonObject mojangJson = new Gson().fromJson(mojangJsonReader, JsonObject.class);

			// Find the version information for the selectedMinecraftVersion
			JsonArray versions = mojangJson.getAsJsonArray("versions");
			JsonObject selectedVersionInfo = null;
			for (JsonElement version : versions) {
				JsonObject versionObject = version.getAsJsonObject();
				if (versionObject.get("id").getAsString().equals(selectedMinecraftVersion)) {
					selectedVersionInfo = versionObject;
					break;
				}
			}

			if (selectedVersionInfo != null) {
				// Fetch the URL for the selected version from the manifest
				String versionUrl = selectedVersionInfo.get("url").getAsString();
				URL versionJsonUrl = new URL(versionUrl);
				HttpURLConnection versionConnection = (HttpURLConnection) versionJsonUrl.openConnection();
				versionConnection.setConnectTimeout(5000);
				JsonReader versionJsonReader = new JsonReader(
						new InputStreamReader(versionConnection.getInputStream()));
				JsonObject versionJson = new Gson().fromJson(versionJsonReader, JsonObject.class);

				// Modify version ID, remove downloads, and rename the JSON file
				String morchVersion = "morch-" + selectedClientVersion + "-" + selectedMinecraftVersion;
				versionJson.addProperty("id", morchVersion);
				versionJson.remove("downloads");

				// Create the directory if it doesn't exist
				Path versionDirPath = Paths.get(minecraftPath, "versions", morchVersion);
				Files.createDirectories(versionDirPath);

				// Save the modified JSON to a file with the new version ID
				String jsonFileName = morchVersion + ".json";
				Path jsonFilePath = versionDirPath.resolve(jsonFileName);
				Files.write(jsonFilePath, new Gson().toJson(versionJson).getBytes());

				System.out.println("Modified client JSON and saved as: " + jsonFilePath.toString());
			} else {
				SwingUtilities.invokeLater(() -> gui.statusLabel
						.setText("Error: Selected Minecraft version not found in Mojang's manifest."));
			}
		} catch (IOException e) {
			SwingUtilities.invokeLater(() -> gui.statusLabel.setText(
					"Error: Unable to fetch or modify Mojang's version manifest. Check your internet connection or try again later."));
			e.printStackTrace();
		}
	}
}