import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

public class ClassCounterVisitor implements FileVisitor<Path>
{
    private final Path root;                    //Root of the project (generaly the src folder)
    private Path currentPackage;                //Path of the currently visited package (relative to the root)

    private final HashMap<String, Integer> classLOC = new HashMap<>();
    private final HashMap<String, Integer> classCLOC = new HashMap<>();
    private final HashMap<String, Float> classDC = new HashMap<>();
    private final HashMap<String, Integer> classWMC = new HashMap<>();
    private final HashMap<String, Float> classBC = new HashMap<>();

    private final ArrayList<String[]> classTable = new ArrayList<>();
    private final ArrayList<String[]> packageTable = new ArrayList<>();

    public ClassCounterVisitor(Path root)
    {
        this.root = root;
    }

    /**
     * Before visiting a directory.
     * The directory will be treated as a package.
     *
     * @param dir The path to the directory
     * @param attrs the directory's basic attributes
     * @return Always Continue
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    {
        currentPackage = root.relativize(dir);
        System.out.println("Entering " + pathToPackageName(currentPackage));

        return FileVisitResult.CONTINUE;
    }

    /**
     * Visiting a file.
     * Create a ClassCounter for the file and run it.
     *
     * @param file The path to the file
     * @param attrs the file's basic attributes
     * @return Always Continue
     * @throws IOException If an I/O error occurs
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
        Path filePath = root.relativize(file);

        //Only read java files
        if(filePath.toString().endsWith(".java"))
        {
            String[] line = new String[7];          // {path, name, loc, cloc, dc, wmc, bc}

            String className = pathToClassName(filePath);
            String classPath = filePath.toString();
            System.out.print("\tVisiting " + className);

            //Count
            ClassCounter counter = new ClassCounter(file.toString());
            try
            {
                //Read
                counter.read();

                //Make sure no count is zero
                int loc = counter.getLOC() == 0 ? 0 : counter.getLOC();
                int cloc = counter.getCLOC() == 0 ? 0 : counter.getCLOC();
                int wmc = counter.getPredicat() == 0 ? 0 : counter.getPredicat();

                //Put the counts in the maps
                classLOC.put(className, loc);
                classCLOC.put(className, cloc);
                classDC.put(className, ((float) cloc/loc));
                classWMC.put(className, wmc);
                classBC.put(className, ((float) cloc/(loc*wmc)));
            }
            catch(Exception e)
            {
                throw new IOException("Can't read " + file.getFileName(), e);
            }

            //Create the entry for the csv
            line[0] = classPath;
            line[1] = className;
            line[2] = classLOC.get(className).toString();
            line[3] = classCLOC.get(className).toString();
            line[4] = classDC.get(className).toString();
            line[5] = classWMC.get(className).toString();
            line[6] = classBC.get(className).toString();
            classTable.add(line);

            System.out.println("\t Count done!");
        }
        else
        {
            System.out.println("\tIgnoring " + file.getFileName());
        }

        return FileVisitResult.CONTINUE;
    }

    /**
     * {@inheritDoc}
     * @param file
     * @param exc
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
    {
        Path filePath = root.relativize(file);
        String className = pathToClassName(filePath);
        String classPath = filePath.toString();

        String[] line = new String[7];
        line[0] = classPath;
        line[1] = className;
        line[2] = "N/A";
        line[3] = "N/A";
        line[4] = "N/A";
        line[5] = "N/A";
        line[6] = "N/A";
        classTable.add(line);

        return FileVisitResult.SKIP_SIBLINGS;
    }

    /**
     * After visiting a directory.
     *
     * @param dir The visited directory
     * @param exc null if the iteration of the directory completes without an error;
     *            otherwise the I/O exception that caused the iteration of the directory to complete prematurely
     * @return Always Continue
     * @throws IOException If an I/O error occurs
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
    {
        if(exc == null)
        {
            String[] line = new String[7];                  // {path, name, loc, cloc, dc, wcp, bc}

            String pkgName = pathToPackageName(currentPackage);
            String pkgPath = currentPackage.toString();
            System.out.print("Exiting " + pathToPackageName(currentPackage));

            //Count -> always recursive
            int loc = 0;
            int cloc = 0;
            int wcp = 0;

            for(String[] classEntry : classTable)
            {
                String classPath = classEntry[0];
                String className = classEntry[1];

                //Check if the class is in the current package
                if(className.startsWith(pkgName))
                {
                    loc += classLOC.get(className);
                    cloc += classCLOC.get(className);
                    wcp += classWMC.get(className);
                }
            }

            //Create the entry for the csv
            line[0] = pkgPath;
            line[1] = pkgName;
            line[2] = String.valueOf(loc);
            line[3] = String.valueOf(cloc);
            line[4] = String.valueOf((float) cloc / loc);
            line[5] = String.valueOf(wcp);
            line[6] = String.valueOf((float) cloc/(loc*wcp));
            packageTable.add(line);

            //Return to the parent
            currentPackage = root.relativize(dir.getParent());
            System.out.println(" returning to " + pathToPackageName(currentPackage));
        }
        else
        {
            currentPackage = root.relativize(dir.getParent());
            System.err.println("Could not complete the visit of " + pathToPackageName(currentPackage));
        }

        return FileVisitResult.CONTINUE;
    }

    public String[][] getClassTable()
    {
        String[][] classTableArray = new String[classTable.size()][7];

        for(int i = 0; i < classTable.size(); i++)
        {
            classTableArray[i] = classTable.get(i);
        }

        return classTableArray;
    }

    public String[][] getPackageTable()
    {
        String[][] packageTableArray = new String[packageTable.size()][7];

        for(int i = 0; i < packageTable.size(); i++)
        {
            packageTableArray[i] = packageTable.get(i);
        }

        return packageTableArray;
    }

    /**
     * Returned the package name of the given path.
     * This assumes the path points to a folder relative
     * to root.
     *
     * @param path Relative (to root) path to a folder
     * @return The package name of the path
     */
    private String pathToPackageName(Path path)
    {
        String[] segments = path.toString().split("\\\\");

        StringBuilder pkgName = new StringBuilder();
        for(String segment : segments)
        {
            pkgName.append(".").append(segment);
        }
        pkgName.deleteCharAt(0);            //delete the first character because it is '.'

        return pkgName.toString();
    }

    /**
     * Returned the class name of the given file.
     * This assumes the path points to a file relative
     * to root.
     *
     * @param file Relative (to root) path to a file
     * @return The class name of the file
     */
    private String pathToClassName(Path file)
    {
        String[] fileName = file.toString().split("\\.");
        return pathToPackageName(Paths.get(fileName[0]));
    }

    /**
     * Builds a path for a package. The path
     * will be relative to root.
     *
     * @param pkgName The name of the package
     * @return A Path
     */
    private Path packageNameToPath(String pkgName)
    {
        StringBuilder path = new StringBuilder();
        for(String pkg : pkgName.split("\\."))
        {
            path.append("\\").append(pkg);
        }

        return Paths.get(path.toString());      //fixme maybe throw exception?
    }
}
