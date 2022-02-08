import counters.CounterController;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        if(args.length != 2)
        {
            throw new IllegalArgumentException("Wrong number of argument. " +
                    "Should be: <project_path> <output_path>");
        }

        String projectPath = args[0];
        String outputPath = args[1];

        CounterController counter = new CounterController(projectPath, outputPath);
        counter.count();
    }
}
