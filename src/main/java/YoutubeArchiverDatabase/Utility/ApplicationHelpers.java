package YoutubeArchiverDatabase.Utility;

import java.nio.file.Path;

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
}
