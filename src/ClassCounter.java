public class ClassCounter
{
    private String filePath;

    public ClassCounter(String filePath)
    {
        this.filePath = filePath;
    }

    public int getLOC()
    {
        return 10;
    }

    public int getCLOC()
    {
        return 5;
    }

    public int getDC()
    {
        return getLOC() / getCLOC();
    }
}
