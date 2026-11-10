package YoutubeArchiverDatabase.Utility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class ApplicationHelpers
{
    /// Returns working directory for the application as an absolute path
    /// @return Working directory as absolute path
    public static Path GetWorkingDirectory()
    {
        return Path.of("").toAbsolutePath();
    }

    /// Resolves path as either absolute; or relative. If it is relative the result path will be
    /// absolute with respect to the working directory.
    /// @param anyPath The path to resolve
    /// @return The resolved path (absolute)
    /// @throws IllegalArgumentException Argument path is not valid
    public static Path ResolvePath(String anyPath) throws IllegalArgumentException
    {
        if (anyPath == null || anyPath.isBlank())
            throw new IllegalArgumentException("Empty or invalid path");

        var path = Path.of(anyPath);

        // Absolute Path
        if (path.isAbsolute())
            return path;

        // Relative Path:  Resolve to working directory
        else
        {
            var workingDirectory = GetWorkingDirectory();

            return Path.of(workingDirectory.toString(), path.getFileName().toString());
        }
    }

    /// Deletes all files in a directory + (optionally) deletes the directory
    /// @param directory Directory to clear
    /// @param deleteDirectory Option to delete the actual directory
    /// @return true / false for success or failure
    public static boolean ClearDirectory(String directory, boolean deleteDirectory)
    {
        try
        {
            var directoryPath = Path.of(directory);
            var result = new AtomicReference<Boolean>();

            // Delete Existing Folder
            if (Files.exists(directoryPath))
            {
                // Delete Files
                Files.walk(directoryPath)
                        .forEach(path ->
                        {
                            try
                            {
                                if (!Files.isDirectory(path))
                                    Files.delete(path);
                            }
                            catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        });

                // Delete Directory
                Files.deleteIfExists(directoryPath);
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /// Searches $PATH for executable full path
    /// @param executable Name of executable
    /// @return Resolved path to executable
    public static Path GetExecutablePath(String executable)
    {
        try
        {
            var paths = System.getenv("PATH").split(";");

            for (var path : paths)
            {
                // Executable Path
                var absolutePath = Path.of(path, executable + ".exe");

                if (Files.exists(absolutePath))
                {
                    return absolutePath;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /// Creates a ProcessBuilder instance based on user's command line input. This will be
    /// a simple platform-specific shell in which to execute the command line:
    ///
    /// 1) Windows:  ping {url} -> cmd.exe /c ping {url}
    /// 2) Linux:    ls -a -> sh -c ls -a
    /// 3) Mac OS:   (same commands) (this is also assumed - unit tests should be made for each application runner)
    /// @param commandLine Command line of the actual command (not the shell)
    /// @return ProcessBuilder for the appropriate platform
    public static ProcessBuilder CreateProcessBuilder(String commandLine) throws Exception
    {
        // Typically this kind of constructor would throw some kind of exception.. but probably not
        // the way this class was built since Java's try-catch-with-resources was made for
        // that purpose.
        //
        var os = System.getProperty("os.name").toLowerCase();

        return switch (os)
        {
            // Windows
            case String s when s.contains("win") -> new ProcessBuilder("cmd.exe", "/c", commandLine);

            // Mac OS, Linux
            case String s when s.contains("mac") ||
                               s.contains("nix") ||
                               s.contains("nux") ||
                               s.contains("aix") -> new ProcessBuilder("sh", "-c", commandLine);
            default ->
                throw new Exception("Unknown operating system: " + os);
        };
    }
}
