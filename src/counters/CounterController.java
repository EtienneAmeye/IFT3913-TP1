package counters;

import io.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to mesure different metrics for
 * each class and package of a java project.
 * </br>
 * The measuring is done by {@link ClassCounterVisitor}.
 *
 * </p>
 *
 * The measures for each class and package are written
 * into csv files.
 */
public class CounterController
{
    private final Path projectPath;                 //Path of the project
    private final Path outputPath;                  //Path to output the csv

    public CounterController(String projectPath, String outputPath)
    {
        this.projectPath = Paths.get(projectPath);
        this.outputPath = Paths.get(outputPath);

        //Check if the path exist
        if(!Files.exists(this.projectPath))
        {
            throw new IllegalArgumentException("The project " + projectPath + " does not exist");
        }
        else if(!Files.exists(this.outputPath))
        {
            throw new IllegalArgumentException("The folder " + outputPath + " does not exist");
        }
    }

    /**
     * Measure the metrics.
     *
     * @throws IOException If I/O errors occur
     */
    public void count () throws IOException
    {
        //Read through the entire project
        ClassCounterVisitor visitor = new ClassCounterVisitor(projectPath);
        Files.walkFileTree(projectPath, visitor);

        //Create the csv file for classes
        Path classCSV = outputPath.resolve("classes.csv");
        if(!Files.exists(classCSV))
        {
            Files.createFile(classCSV);
        }

        //Write the csv for the classes
        CSVWriter writer = new CSVWriter(classCSV.toString());
        writer.setContent(visitor.getClassTable());
        writer.write();

        //Create the csv file for package
        Path pkgCSV = outputPath.resolve("paquets.csv");
        if(!Files.exists(pkgCSV))
        {
            Files.createFile(pkgCSV);
        }

        //Write the csv for the packages
        writer = new CSVWriter(pkgCSV.toString());
        writer.setContent(visitor.getPackageTable());
        writer.write();
    }

    public String getProjectPath()
    {
        return projectPath.toString();
    }

    public String getOutputPath()
    {
        return outputPath.toString();
    }
}
