package fish.yukiemeralis.flock;

public abstract class DownloadFinishedThread implements Runnable 
{
    private Exception failureException;
    private boolean success;

    /**
     * Runs a thread after a download has finished.
     * @param downloadSuccessful Whether or not the download was successful
     */
    public void run(boolean downloadSuccessful)
    {
        this.success = downloadSuccessful;
        this.run();
    }

    public void setFailure(Exception e)
    {
        this.failureException = e;
    }

    public Exception getFailureException()
    {
        return this.failureException;
    }

    public boolean failed()
    {
        return !this.success;
    }
}
