package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Logging.LogMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class ShellRunner
{
    private String executableName;
    private Path executablePath;
    private Path executableTempDirectory;
    private boolean initialized;
    private boolean initializationError;
    private boolean runCalled;
    private String initializationErrorMessage;

    /// Initializes the shell runner with the name of the executable. This is used to prepare
    /// the environment for running - which includes a working tmp directory unique to the
    /// executable instance. The shell that executes will be the terminal shell for the current
    /// platform.
    /// @param executableName Name of the executable:  (e.g. yt-dlp, yt-dlp.exe, or any alias...)
    protected ShellRunner(String executableName)
    {
        if (executableName == null || executableName.isBlank())
            throw new IllegalArgumentException("Invalid process executable");

        this.executableName = executableName;
        this.executableTempDirectory = null;
        this.initialized = false;
        this.initializationError = false;
        this.initializationErrorMessage = "";
        this.runCalled = false;
    }

    /// Returns true if the Initialize() method has been run successfully. If this is the case the user may
    /// call Run, which will continue the run process.
    /// @return (success / failure) of the initialized state of the runner
    protected boolean IsInitialized()
    {
        return this.initialized;
    }

    /// Returns true if a Run() call has been made. Recalling Run() will cause an exception if the Reset()
    /// and Initialize() methods have not been executed.
    /// @return (true / false) showing that a call to Run() has been made.
    protected boolean HasRun()
    {
        return this.runCalled;
    }

    /// Returns state of initialization (error). Calling Reset() will clear this indicator.
    /// @return Error state of initialization
    protected boolean HasInitializationError()
    {
        return this.initializationError;
    }

    /// Returns the current initialization error message. Calling Reset() will clear this message.
    /// @return Returns the current initialization error message
    protected String GetInitializationErrorMessage()
    {
        return this.initializationErrorMessage;
    }

    /// Returns the temp directory created by the Initialize() method.
    /// @return Path object for the (absolute) path of the temp directory
    /// @throws IllegalStateException Initialize() has not yet been called
    protected Path GetExecutableTempDirectory() throws IllegalStateException
    {
        if (!this.initialized)
            throw new IllegalStateException("ShellRunner has already not been initialized");

        return this.executableTempDirectory;
    }

    /// Initializes the runner - setting initialized to true if the runner is in a state to execute
    /// @return (success / failure) (also, user may use IsInitialized())
    /// @throws IllegalStateException The initialized state has already been set. Must call Run() or Reset() to
    ///                               reset the runner state.
    protected boolean Initialize() throws IllegalStateException
    {
        if (this.initialized)
            throw new IllegalStateException("ShellRunner has already been initialized");

        if (this.runCalled)
            throw new IllegalStateException("ShellRunner has already run. Please use Reset() to start process again.");

        // Portability:  Shell usage varies per issue...
        //
        // 1) Windows (IN SOME CASES):  "cmd.exe /c ..." must be called with absolute paths to command executables
        // 2) Linux (TODO)
        //
        this.executablePath = ApplicationHelpers.GetExecutablePath(this.executableName);

        if (this.executablePath == null)
        {
            this.initializationErrorMessage = "Executable path not found:  " + this.executableName;
            this.initializationError = true;

            return false;
        }

        // Procedure:  It's easier to use the system shell to handle commands when Java is built to be portable.
        //             The issue (currently) is that the python startup scripts vary depending on the package.
        //
        // 0) Prepare Process Folder / Command Line[s]
        // 1) Start command shell (cmd.exe, bash.exe, ...) with resolved command line
        //

        try
        {
            // Create Application Temp Directory
            this.executableTempDirectory = ApplicationHelpers.ResolvePath(this.executableName);

            // Delete Existing Folder
            if (Files.exists(this.executableTempDirectory))
            {
                this.initializationError = !ApplicationHelpers.ClearDirectory(executableTempDirectory.toString(), true);
            }

            if (this.initializationError)
            {
                this.initializationErrorMessage = "Error creating temp directory:  " + this.executableTempDirectory.toString();
                return false;
            }

            // Create (temp) Folder
            var directory = Files.createDirectory(this.executableTempDirectory);

            if (!Files.exists(directory))
            {
                this.initializationError = true;
                this.initializationErrorMessage = "Error creating temp directory:  " + this.executableTempDirectory.toString();
                return false;
            }

            // Initialization Complete!
            this.initialized = true;
        }
        catch (Exception e)
        {
            this.initializationError = true;
            this.initializationErrorMessage = "Error initializing ShellRunner:  Could not complete setup for working directory";

            e.printStackTrace();
        }

        return !this.initializationError;
    }

    /// User code that will execute after initialization; and before execution.
    /// @param executableTempDirectory Working temp directory for the shell command to execute in.
    /// @return (success / failure) Execution of the Run sequence will halt if this method returns false.
    protected abstract boolean ExecutePrerun(Path executableTempDirectory);

    /// User code that will execute after the application has exited.
    /// @param executableTempDirectory Working temp directory for the shell command to execute in.
    /// @param exitCode Exit code of the shell application
    /// @return (success / failure) Execution of the Run sequence will halt if this method returns false - indicating
    ///         that the Run method (as a whole) has failed.
    protected abstract boolean ExecutePostrun(Path executableTempDirectory, int exitCode);

    /// Runs the user's command line
    /// @param arguments Variable number of arguments (excluding executable name or path)
    /// @return success / failure of the run
    /// @throws IllegalStateException
    protected boolean Run(String...arguments) throws IllegalStateException
    {
        return RunImpl(Arrays.stream(arguments));
    }

    /// Runs the user's command line
    /// @param arguments Variable number of arguments (excluding executable name or path)
    /// @return success / failure of the run
    /// @throws IllegalStateException
    protected boolean Run(List<String> arguments) throws IllegalStateException
    {
        return RunImpl(arguments.stream());
    }

    /// Resets state of runner. Throws exception if there is an unhandled exception related to the working
    /// directory.
    /// @throws Exception Error running ApplicationHelpers.ClearDirectory would indicate probably a permissions
    ///                   error or some other application is locking the directory (currently in use).
    protected void Reset() throws Exception
    {
        try
        {
            // Clean Directory
            ApplicationHelpers.ClearDirectory(this.executableTempDirectory.toString(), true);

            // De-initialize:  This allows for a re-run using the initialize method
            this.initialized = false;
            this.runCalled = false;
            this.initializationError = false;
            this.initializationErrorMessage = "";

        }
        catch (Exception e)
        {
            e.printStackTrace();

            throw e;
        }
    }


    private boolean RunImpl(Stream<String> arguments)
    {
        if (!this.initialized)
            throw new IllegalStateException("Trying to run un-initialized ShellRunner instance");

        if (this.runCalled)
            throw new IllegalStateException("Trying to recall Run() after it has been executed. Please call Reset().");

        // Run Called!
        this.runCalled = true;

        // Procedure
        //
        // 0) Check initialization / run state variables (done above)
        // 1) Execute Pre-run User Code (halt on failure)
        // 2) Execute Shell Command-Line
        // 3) Execute Post-run User Code (halt on failure)
        // 4) Return Result (exitCode == 0)
        //

        try
        {
            // Pre-run Callback:  Used for anything utilizing the working directory
            var prerunResult = ExecutePrerun(this.executableTempDirectory);

            // Pre-run Failure!
            if (!prerunResult)
            {
                ApplicationHelpers.Log(LogMessage.Error("Execution halted due to user code:  ShellRunner Pre-run failure"));
                return false;
            }

            // User's Command Line (with the shell command)
            var commandLine = arguments.reduce(this.executablePath.toString(), (seed, current) ->
            {
                return seed + " " + current;
            });

            // Typically this kind of constructor would throw some kind of exception.. but probably not
            // the way this class was built since Java's try-catch-with-resources was made for
            // that purpose.
            //
            var builder = ApplicationHelpers.CreateProcessBuilder(commandLine);

            // Options:  Working Directory, Inherit IO (console)
            builder.directory(this.executableTempDirectory.toFile());

            // -> Start
            var process = builder.start();

            // Output Stream:  Re-directing the stream bypasses the System.setOut / System.setErr we did in
            //                 MainApplication. (why set them at all?!)
            //
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    //System.out.println(line); // This will respect any System.setOut() modifications
                    System.out.println(this.executableName + ": \t\t" + line);
                }
            }

            int exitCode = process.waitFor();

            ApplicationHelpers.Log(LogMessage.Info("Exited with error code : " + exitCode));

            // Close Process
            process.destroyForcibly();

            // User Callback
            var postrunResult = ExecutePostrun(this.executableTempDirectory, exitCode);

            // Post-run Failure!
            if (!postrunResult)
            {
                ApplicationHelpers.Log(LogMessage.Error("Execution halted due to user code:  ShellRunner Post-run failure"));
                ApplicationHelpers.Log(LogMessage.Error("Execution halted due to user code:  The process otherwise exited with code " + exitCode));
                return false;
            }

            return exitCode == 0;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("ShellRunner has encountered an unexpected error", e));
        }

        return false;
    }
}
