package YoutubeArchiverDatabase.Utility;

import java.lang.reflect.ParameterizedType;

public class ClassHelper<T>
{
    @SuppressWarnings("unchecked")
    public String GetClassName()
    {
        // Our Wrapper
        var superClass = (ParameterizedType)getClass().getGenericSuperclass();

        // Template Type
        var type = (Class<T>) superClass.getActualTypeArguments()[0];

        return type.getSimpleName();
    }

    public Class<T> GetClass()
    {
        // Our Wrapper
        var superClass = (ParameterizedType)getClass().getGenericSuperclass();

        // Template Type
        return (Class<T>) superClass.getActualTypeArguments()[0];
    }
}
