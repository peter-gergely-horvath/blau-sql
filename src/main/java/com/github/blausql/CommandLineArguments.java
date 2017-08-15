package com.github.blausql;

import org.kohsuke.args4j.Option;

public final class CommandLineArguments {

    public String getClasspath() {
        return classpath;
    }

    @Option(name="--classpath")
    private String classpath = null;


}
