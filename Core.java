import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Core {
	static void help(){
		System.out.println("install \t\t\t\tto install a .xpm package");
		System.out.println("uninstall or remove \t\t\tto remove a .xpm package");
		System.out.println("disable \t\t\t\tto disable a .xpm package");
		System.out.println("enable \t\t\t\t\tto enable a .xpm package");
		System.out.println("make \t\t\t\t\tto run `make` in package");
		System.out.println("run \t\t\t\t\tto run `make run` in package");
	}

	static String getVersion(String packageName) {
		try {
			return parseXPM(new Scanner(new File(getHomePath() + "enables" + getPathSeperator() + packageName + getPathSeperator() + "manifest")).useDelimiter("\\Z").next()).get("version_name");
		} catch (FileNotFoundException e) {
			return "<Version NOT available>";
		}
	}

	private static void delete(File file) {
		if(file.isDirectory()){
			if(file.list().length == 0){
				file.delete();
			} else {
				String files[] = file.list();
				for (String fileName : files) {
					File fileDelete = new File(file, fileName);
					delete(fileDelete);
				}
				if(file.list().length == 0){
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}

	private static HashMap<String, String> parseXPM(String content) {
		HashMap<String, String> map = new HashMap<>();
		StringBuilder data = new StringBuilder();
		String topic = "";
		for (String line : content.split("\n")) {
			if (line.startsWith("\t")) {
				data.append(line.substring(1)).append("\n");
			} else {
				if (!topic.equals("")) {
					map.put(topic, data.toString());
				}
				data = new StringBuilder();
				topic = line;
			}
		}
		if (!topic.equals("")) {
			map.put(topic, data.toString());
		}
		return map;
	}

	static ArrayList<String> find(String compatibleWith) {
		ArrayList<String> list = new ArrayList<>();
		try {
			for (File file : new File(getHomePath() + "enables" + getPathSeperator()).listFiles()) {
				for (File file2 : new File(getHomePath() + "enables" + getPathSeperator() + file.getName()).listFiles()) {
					if (file2.isDirectory() && file2.getName().equals(compatibleWith)){
						list.add(file.getName());
					}
				}
			}
		} catch (NullPointerException e) {
			return new ArrayList<>();
		}
		return list;
	}

	public static String executeScript(String script) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(script);
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line;
		StringBuilder result = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			result.append(line).append("\n");
		}
		while ((line = errorReader.readLine()) != null) {
			result.append(line).append("\n");
		}
		return result.toString();
	}

	static void make(String packageName) {
		try {
			System.out.println(executeScript("make -f " + getHomePath() + "enables" + getPathSeperator() + packageName + getPathSeperator() + "Makefile"));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	static void run(String packageName) {
		try {
			System.out.println(executeScript("make -f " + getHomePath() + "enables" + getPathSeperator() + packageName + getPathSeperator() + "Makefile run"));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	static ArrayList<String> list() {
		ArrayList<String> list = new ArrayList<>();
		try {
			for (File file : new File(getHomePath() + "enables" + getPathSeperator()).listFiles()) {
				list.add(file.getName());
			}
		} catch (NullPointerException e) {
			return new ArrayList<>();
		}
		return list;
	}

	static void checkHome() {
		File file1 = new File(getHomePath());
		if (!file1.exists()) {
			file1.mkdir();
		}
		File file2 = new File(getHomePath() + "enables" + getPathSeperator());
		if (!file2.exists()) {
			file2.mkdir();
		}
		File file3 = new File(getHomePath() + "disables" + getPathSeperator());
		if (!file3.exists()) {
			file3.mkdir();
		}
	}

	static void install(File file) {
		String data = "";
		if (file.exists()) {
			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			scanner.useDelimiter("\\Z");
			data = scanner.next();
		} else {
			System.err.println("Package file does not exist");
		}
		String packageName = data.split("\n")[0];
		String packageDirectory = packageName + getPathSeperator();
		delete(new File(getHomePath() + "enables" + getPathSeperator() + packageName));
		data = data.substring(packageDirectory.length());
		File file1 = new File(getHomePath() + "enables" + getPathSeperator() + packageDirectory);
		if (!file1.exists()) {
			file1.mkdir();
		}
		boolean canInstall = true;
		for (Map.Entry entry : parseXPM(data).entrySet()) {
			if (entry.getKey().equals("dependencies")) {
				ArrayList<String> list = list();
				for (String dependency : ((String)entry.getValue()).split("\n")) {
					boolean founded = false;
					for (String item : list) {
						if (dependency.split("\t")[0].equals(item)) {
							founded = true;
							try {
								String operation = dependency.split("\t")[1];
								File manifest = new File(getHomePath() + "enables" + getPathSeperator() + item + getPathSeperator() + "manifest");
								Double version = Double.parseDouble(parseXPM(new Scanner(manifest).useDelimiter("\\Z").next()).get("version"));
								Double wantedVersion = Double.parseDouble(dependency.split("\t")[2]);
								if (operation.equals("==")) {
									if (version != wantedVersion) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								} else if (operation.equals("=")) {
									if (version.intValue() != wantedVersion.intValue()) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								} else if (operation.equals("!=")) {
									if (version.intValue() == wantedVersion.intValue()) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								} else if (operation.equals("!==")) {
									if (version == wantedVersion) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								} else if (operation.equals(">")) {
									if (version <= wantedVersion) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								} else if (operation.equals("<")) {
									if (version >= wantedVersion) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								} else if (operation.equals(">=")) {
									if (version < wantedVersion) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								} else if (operation.equals("<=")) {
									if (version > wantedVersion) {
										System.out.println("dependency " + dependency.replace("\t", " ") + " not exist");
										canInstall = false;
									}
								}
							} catch (ArrayIndexOutOfBoundsException ignored) {

							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
					if (!founded) {
						canInstall = false;
					}
				}
				if (!canInstall) {
					System.out.println("Cannot install this package because dependencies not exist");
					delete(new File(getHomePath() + "enables" + getPathSeperator() + packageName));
					return;
				}
			} else {
				File file2 = new File(getHomePath() + "enables" + getPathSeperator() + packageDirectory + ((String) entry.getKey()).replace("/", getPathSeperator()));
				if (!file2.exists()) {
					file2.getParentFile().mkdir();
				}
				final Path path = Paths.get(getHomePath() + "enables" + getPathSeperator() + packageDirectory + ((String) entry.getKey()).replace("/", getPathSeperator()));
				try (
						final BufferedWriter writer = Files.newBufferedWriter(path,
								StandardCharsets.UTF_8, StandardOpenOption.CREATE);
				) {
					writer.write((String) entry.getValue());
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static void uninstall(String packageName) {
		File file = new File(getHomePath() + "enables" + getPathSeperator() + packageName);
		if (!file.exists()){
			System.out.println("Package NOT installed");
			return;
		}
		try {
			System.out.println(executeScript("make -f " + getHomePath() + "enables" + getPathSeperator() + packageName + getPathSeperator() + "Makefile uninstall"));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		delete(file);
	}

	static void disable(String packageName) {
		File file1 = new File(getHomePath() + "enables" + getPathSeperator() + packageName);
		File file2 = new File(getHomePath() + "disables" + getPathSeperator() + packageName);
		try {
			Files.move(file1.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ignored) {

		}
	}
	static void enable(String packageName) {
		File file1 = new File(getHomePath() + "disables" + getPathSeperator() + packageName);
		File file2 = new File(getHomePath() + "enables" + getPathSeperator() + packageName);
		try {
			Files.move(file1.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {

		}
	}

	static String getHomePath() {
		String pathSeperator = getPathSeperator();
		if (System.getenv("XPM_HOME") != null && (!System.getenv("XPM_HOME").equals(""))) {
			String path = System.getenv("XPM_HOME");
			if(path.endsWith(pathSeperator))
				return path;
			else
				path += pathSeperator;
			return path;
		} else if(System.getenv("HOME") != null && (!System.getenv("HOME").equals(""))) {
			String path = System.getenv("HOME");
			if(path.endsWith(pathSeperator))
				return path + "xpm" + pathSeperator;
			else
				path += pathSeperator;
			return path + "xpm" + pathSeperator;
		} else if (!pathSeperator.equals("\\")) {
			return "/etc/xpm/";
		} else {
			return "C:\\xpm\\";
		}
	}

	private static String getPathSeperator() {
		return System.getProperty("os.name").toLowerCase().contains("win")? "\\":"/";
	}
}
