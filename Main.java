import java.io.File;

public class Main {
	public static void main(String [] args) {
		Core.checkHome();
		if (args.length < 1){
			System.err.println("Please add args");
			Core.help();
		} else {
			ArgParser argParser = new ArgParser(args);
			if (argParser.getArgs().get(0).equals("uninstall") || argParser.getArgs().get(0).equals("remove")) {
				try {
					Core.uninstall(argParser.getArgs().get(1));
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter file name you like to uninstall");
				}
			} else if (argParser.getArgs().get(0).equals("install")) {
				try {
					File file = new File(argParser.getArgs().get(1));
					if (file.exists()) {
						Core.install(file);
					} else {
						System.err.println("Package file not exist");
					}
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					System.err.println("Please enter file name you like to install");
				}
			} else if (argParser.getArgs().get(0).equals("disable")) {
				try {
					Core.disable(argParser.getArgs().get(1));
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter package name you like to disable");
				}
			} else if (argParser.getArgs().get(0).equals("enable")) {
				try {
					Core.enable(argParser.getArgs().get(1));
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter package name you like to enable");
				}
			} else if (argParser.getArgs().get(0).equals("getHomePath")) {
				System.out.println(Core.getHomePath());
			} else if (argParser.getArgs().get(0).equals("getPath")) {
				try {
					File file = new File(Core.getHomePath() + "enables/" + argParser.getArgs().get(1));
					if (file.exists()) {
						System.out.println(file.getAbsolutePath());
					}
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter file name you like to return absolute path");
				}
			} else if (argParser.getArgs().get(0).equals("find")) {
				try {
					for (String string : Core.find(argParser.getArgs().get(1))) {
						System.out.print(string + "\t\t\t" + Core.getVersion(string));
					}
					System.out.println();
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter file name you like to return absolute path");
				}
			} else if (argParser.getArgs().get(0).equals("list")) {
				try {
					for (String string : Core.list()) {
						System.out.print(string + "\t\t\t" + Core.getVersion(string));
					}
					System.out.println();
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter file name you like to return absolute path");
				}
			} else if (argParser.getArgs().get(0).equals("make")) {
				try {
					Core.make(argParser.getArgs().get(1));
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter file name you like to return absolute path");
				}
			} else if (argParser.getArgs().get(0).equals("run")) {
				try {
					Core.run(argParser.getArgs().get(1));
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Please enter file name you like to return absolute path");
				}
			}
		}
	}
}
