package com.github.blausql;

import com.github.blausql.ui.MainMenuWindow;
import com.github.blausql.util.TextUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.springframework.util.Assert;

import java.io.StringWriter;

public class Main {

    private static CommandLineArguments commandLineArguments;

    public static synchronized void setCommandLineArguments(CommandLineArguments commandLineArguments) {
        Main.commandLineArguments = commandLineArguments;
    }

    public static synchronized CommandLineArguments getCommandLineArguments() {
        Assert.notNull(Main.commandLineArguments, "commandLineArguments is null");

        return Main.commandLineArguments;
    }

	public static void exitApplication(int exitCode) {
		TerminalUI.close();
		System.exit(exitCode);
	}

    public static void main(String[] args) {

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

		CommandLineArguments commandLineArguments = new CommandLineArguments();
		CmdLineParser parser = new CmdLineParser(commandLineArguments);

		try {
			TerminalUI.init();

			parser.parseArgument(args);

            Main.setCommandLineArguments(commandLineArguments);
			

			TerminalUI.showWindowCenter(new MainMenuWindow());

		} catch( CmdLineException e ) {

            StringWriter stringWriter = new StringWriter();
            parser.printUsage(stringWriter, null, OptionHandlerFilter.ALL);
            String usageString = stringWriter.toString();

            String errorMessage = TextUtils.separateLines(
                            "The application cannot start due to invalid command line arguments!",
                            "",
                            "ERROR:",
                            e.getMessage(),
                            "java -jar <JAR FILE> [options...] arguments...",
                            usageString);

            TerminalUI.showErrorMessageFromString("Invalid command line argument(s)", errorMessage);

            exitApplication(1);
		}


	}


    private static final class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            TerminalUI.close();

            System.err.format("--- UNHANDLED EXCEPTION in Thread '%s': exiting the JVM! --- %n", t.getName());

            e.printStackTrace();

            exitApplication(1);
        }
    }
}
