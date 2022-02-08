package counters;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A {@link FileVisitor} that measures different metrics
 * on each class and package of a java project.
 * </br>
 * The measuring is done by {@link ClassCounter}.
 */
public class ClassCounterVisitor implements FileVisitor<Path>
{
    private final Path root;                    //Root of the project (generaly the src folder)
    private Path currentPackage;                //Path of the currently visited package (relative to the root)

    //Measures for each class
    private final HashMap<String, Integer> classLOC
            = new HashMap<>();
    private final HashMap<String, Integer> classCLOC
            = new HashMap<>();
    private final HashMap<String, Float> classDC
            = new HashMap<>();
    private final HashMap<String, Integer> classWMC
            = new HashMap<>();
    private final HashMap<String, Float> classBC
            = new HashMap<>();

    //Arrays for the csv
    private final ArrayList<String[]> classTable
            = new ArrayList<>();       // {path, name, loc, cloc, dc, wmc, bc}
    private final ArrayList<String[]> packageTable
            = new ArrayList<>();     // {path, name, loc, cloc, dc, wcp, bc}

    public ClassCounterVisitor(Path root)
    {
        this.root = root;

        //Add headers to the table
        classTable.add(new String[]{
                "chemin", "classe",
                "classe_LOC", "classe_CLOC",
                "classe_DC", "WMC",
                "classe_BC"
        });
        packageTable.add(new String[]{
                "chemin", "paquet",
                "paquet_LOC", "paquet_CLOC",
                "paquet_DC", "WCP",
                "paquet_BC"
        });
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
     * Create a counters.ClassCounter for the file and run it.
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
            String className = pathToClassName(filePath);
            String classPath = filePath.toString();
            System.out.print("\tVisiting " + className);

            try
            {
                countClass(className, classPath, file);
            }
            catch(Exception e)
            {
                System.out.println("\tERROR");
                throw e;
            }

            System.out.println("\tCount done!");
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
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
    {
        if(exc == null)
        {
            String pkgName = pathToPackageName(currentPackage);
            String pkgPath = currentPackage.toString();
            System.out.print("Exiting " + pathToPackageName(currentPackage));

            //Ignore the root package
            if(!pkgName.isBlank())
            {
                countPackage(pkgName, pkgPath);
            }

            //Return to the parent
            currentPackage = root.relativize(dir.getParent());
            System.out.println(" returning to " + pathToPackageName(currentPackage));
        }
        else
        {
            currentPackage = root.relativize(dir.getParent());
            System.err.println(" could not complete the visit of " + pathToPackageName(currentPackage));
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
     * Count the different value for a given class.
     *
     * @param className The name of the class
     * @param classPath The path of the class
     * @param file Absolute path of the class file
     * @throws IOException If I/O errors occur
     */
    private void countClass(String className, String classPath, Path file) throws IOException
    {
        String[] line = new String[7];

        //Count
        ClassCounter counter = new ClassCounter(file.toString());
        try
        {
            //Read
            counter.read();

            //Make sure no count is zero
            int loc = counter.getLOC();
            int cloc = counter.getCLOC();
            int wmc = counter.getWMC();
            float dc = loc==0? 0 : ((float) cloc / loc);
            float bc = loc==0 || wmc==0? 0 : ((float) cloc / (loc*wmc));

            //Put the counts in the maps
            classLOC.put(className, loc);
            classCLOC.put(className, cloc);
            classDC.put(className, dc);
            classWMC.put(className, wmc);
            classBC.put(className, bc);
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
    }

    /**
     * Count the different value for a given package. </br>
     * Folder without java classe inside are not considered
     * package.
     *
     * @param pkgName The name of the package
     * @param pkgPath The path of the package
     */
    private void countPackage(String pkgName, String pkgPath)
    {
        String[] line = new String[7];
        boolean isPackage = false;

        //Count
        int loc = 0;
        int cloc = 0;
        int wcp = 0;

        for(String[] classEntry : classTable)
        {
            String classPath = classEntry[0];
            String className = classEntry[1];

            //Not recursive
            if(isInPackage(pkgName, className))
            {
                loc += classLOC.get(className);
                cloc += classCLOC.get(className);

                isPackage = true;
            }

            //Recursive
            if(className.startsWith(pkgName))
            {
                wcp += classWMC.get(className);

                isPackage = true;
            }
        }

        //Empty package and other folder are ignored
        if(isPackage)
        {
            float dc = loc==0? 0 : ((float) cloc / loc);
            float bc = loc==0 || wcp==0? 0 : ((float) cloc / (loc*wcp));

            //Create the entry for the csv
            line[0] = pkgPath;
            line[1] = pkgName;
            line[2] = String.valueOf(loc);
            line[3] = String.valueOf(cloc);
            line[4] = String.valueOf(dc);
            line[5] = String.valueOf(wcp);
            line[6] = String.valueOf(bc);
            packageTable.add(line);
        }
    }

    /**
     * Check if a classe is in a package.
     *
     * @param pkg The name of the package
     * @param cl The name of the class
     * @return True if the class is in the package, false otherwise
     */
    private boolean isInPackage(String pkg, String cl)
    {
        if(cl.startsWith(pkg))
        {
            String[] pkgArray = pkg.split("\\.");
            String[] clArray = cl.split("\\.");

            return clArray.length == (pkgArray.length + 1);
        }

        return false;
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
}
