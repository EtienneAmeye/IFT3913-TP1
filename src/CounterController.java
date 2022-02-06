import io.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CounterController
{
    private final Path projectPath;
    private final Path outputPath;

    public CounterController(String projectPath, String outputPath)
    {
        this.projectPath = Paths.get(projectPath);
        this.outputPath = Paths.get(outputPath);
    }

    public void count() throws IOException
    {
        //Count
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
