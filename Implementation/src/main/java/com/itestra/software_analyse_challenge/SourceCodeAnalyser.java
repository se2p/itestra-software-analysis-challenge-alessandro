package com.itestra.software_analyse_challenge;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SourceCodeAnalyser {

    /**
     * Your implementation
     *
     * @param input {@link Input} object.
     * @return mapping from filename -> {@link Output} object.
     */
    public static Map<String, Output> analyse(Input input) {
        // Create a map to store the output for each file
        Map<String, Output> result = new HashMap<>();

        List<File> childrenFiles = new ArrayList<>();

        // Traverse the input directory and collect all files
        traverseDirectory(input.getInputDirectory().listFiles(), childrenFiles);

        for(File file : childrenFiles) {
            countSourceCodeLines(file, result);
        }
        // For each file put one Output object to your result map.
        // You can extend the Output object using the functions lineNumberBonus(int), if you did
        // the bonus exercise.
        return result;
    }

    /**
     * @AI-GENERATED
     * Traverses a directory and collects all files in a list.
     *
     * @param directory the directory to traverse
     * @param result    the list to store the files
     */
    public static void traverseDirectory(File[] directory, List<File> result) {
        for (File file : directory) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children != null) {
                    traverseDirectory(children, result); // Recursive call
                }
            } else {
                result.add(file); // It's a file
            }
        }
    }

    public static void countSourceCodeLines(File file, Map<String, Output> result) {
        try (Scanner scanner = new Scanner(file)) {
            // AtomicInteger to update them in checkComment method
            AtomicInteger lineCountSourceCode = new AtomicInteger(0);
            AtomicInteger  lineCountWithouthGettersBlocks = new AtomicInteger(0);

            List<String> foundDependencies = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // check if the line is a comment, comment block or empty
                checkComment(scanner, line, lineCountSourceCode, lineCountWithouthGettersBlocks);

                // look for dependencies
                if (line.trim().startsWith("import")) {
                    findDependencies(foundDependencies, line);
                }

                /*
                 * @AI-GENERATED REGEX
                 */
                if (line.matches("\\s*public\\s+(?!void)\\w+(<.*>)?\\s+get[A-Z]\\w*\\s*\\(\\s*\\)\\s*\\{?.*")) {
                    System.out.println("Found getter: " + line);
                    lineCountSourceCode.incrementAndGet();
                    // Case 1: inline getter
                    if (line.contains("{") && line.contains("}")) {
                        break;
                    } else {
                        // Case 2: multi-line getter
                        while (scanner.hasNextLine()) {
                            line = scanner.nextLine();
                            lineCountSourceCode.incrementAndGet();
                            if (line.contains("}")) {
                                break; // end of method block
                            }
                        }
                    }
                }

            }
            Output output = new Output(lineCountSourceCode.get(), foundDependencies);
            output.lineNumberBonus(lineCountWithouthGettersBlocks.get());
            result.put(file.getPath(), output);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
    }

    /**
     * Checks if the line is a comment, comment block or empty.
     *
     * @param scanner                     the scanner to read the file
     * @param line                        the line to check
     * @param lineCountSourceCode         the number of source code lines
     * @param lineCountWithouthGettersBlocks the number of source code lines without getters and blocks
     */
    public static void checkComment(
            Scanner scanner,
            String line,
            AtomicInteger  lineCountSourceCode,
            AtomicInteger  lineCountWithouthGettersBlocks
    ) {
        if (!line.trim().startsWith("//") && !line.isEmpty()) {
            lineCountSourceCode.incrementAndGet(); // count as source code line as it is not a comment
            boolean isCommentBlock = line.trim().startsWith("/*") || line.trim().startsWith("/**");
            if (!isCommentBlock) {
                lineCountWithouthGettersBlocks.incrementAndGet(); // count as source code line without getters and blocks
            } else {
                while (isCommentBlock) {
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                        lineCountSourceCode.incrementAndGet(); // add even when we're in the comment block
                        if (line.trim().startsWith("*/")) {
                            isCommentBlock = false;
                        }
                    } else {
                        break; // end of file
                    }
                }
            }
        }
    }

    /**
     * Finds dependencies in the line. Returns only those dependencies that are not already in the list.
     *
     * @param foundDependencies the list of found dependencies
     * @param line             the line to check
     */
    public static void findDependencies(
            List<String> foundDependencies,
            // File file
            String line) {
        List<String> projectNames = Arrays.asList("cronutils", "fig", "spark");

        // get the dependency name
        String fileDependency = Arrays.stream(Arrays.stream(line.split(" ")).toList().get(1).split("\\."))
                .toList()
                .get(0);

        if (projectNames.contains(fileDependency) && !foundDependencies.contains(fileDependency)) {
            foundDependencies.add(fileDependency);
        }
        /* SERVED AS A WAY TO UNDERSTAND IF THE DEPENDENCY ARE ONLY PRESENT IN THE SAME PROJECT
           FOR INSTANCE, CRONUTILS DEPENDS ONLY ON CRONUTILS

           FOUND THAT   fig dependent on cronutils
                        fig dependent on cronutils
                        fig dependent on cronutils
                        fig dependent on cronutils
                        fig dependent on cronutils
                        spark dependent on fig
                        spark dependent on fig
                        spark dependent on fig
                        spark dependent on fig
                        spark dependent on cronutils
                        spark dependent on cronutils
                        spark dependent on cronutils
                        spark dependent on cronutils
                        spark dependent on cronutils
           IN SOME FILES
        String projectName = Arrays.stream(file.getPath().split(Pattern.quote(File.separator))).toList().get(4);

        if(dependencyNames.contains(dependencyNameProject) && !projectName.equals(dependencyNameProject)) {
            System.out.println(projectName + " dependent on " + dependencyNameProject);
        }
         */
    }


    /**
     * INPUT - OUTPUT
     *
     * No changes below here are necessary!
     */

    public static final Option INPUT_DIR = Option.builder("i")
            .longOpt("input-dir")
            .hasArg(true)
            .desc("input directory path")
            .required(false)
            .build();

    // Arrays.asList("..", "CodeExamples", "src", "main", "java")) doesn't work
    public static final String DEFAULT_INPUT_DIR = String.join(File.separator , Arrays.asList("CodeExamples", "src", "main", "java"));

    private static Input parseInput(String[] args) {
        Options options = new Options();
        Collections.singletonList(INPUT_DIR).forEach(options::addOption);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine commandLine = parser.parse(options, args);
            return new Input(commandLine);
        } catch (ParseException e) {
            formatter.printHelp("help", options);
            throw new IllegalStateException("Could not parse Command Line", e);
        }
    }

    private static void printOutput(Map<String, Output> outputMap) {
        System.out.println("Result: ");
        List<OutputLine> outputLines =
                outputMap.entrySet().stream()
                        .map(e -> new OutputLine(e.getKey(), e.getValue().getLineNumber(), e.getValue().getLineNumberBonus(), e.getValue().getDependencies()))
                        .sorted(Comparator.comparing(OutputLine::getFileName))
                        .collect(Collectors.toList());
        outputLines.add(0, new OutputLine("File", "Source Lines", "Source Lines without Getters and Block Comments", "Dependencies"));
        int maxDirectoryName = outputLines.stream().map(OutputLine::getFileName).mapToInt(String::length).max().orElse(100);
        int maxLineNumber = outputLines.stream().map(OutputLine::getLineNumber).mapToInt(String::length).max().orElse(100);
        int maxLineNumberWithoutGetterAndSetter = outputLines.stream().map(OutputLine::getLineNumberWithoutGetterSetter).mapToInt(String::length).max().orElse(100);
        int maxDependencies = outputLines.stream().map(OutputLine::getDependencies).mapToInt(String::length).max().orElse(100);
        String lineFormat = "| %"+ maxDirectoryName+"s | %"+maxLineNumber+"s | %"+maxLineNumberWithoutGetterAndSetter+"s | %"+ maxDependencies+"s |%n";
        outputLines.forEach(line -> System.out.printf(lineFormat, line.getFileName(), line.getLineNumber(), line.getLineNumberWithoutGetterSetter(), line.getDependencies()));
    }

    public static void main(String[] args) {
        Input input = parseInput(args);
        Map<String, Output> outputMap = analyse(input);
        printOutput(outputMap);
    }
}
