package van.tian.wen.library;

import android.content.Context;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;

/**
 * Created by RadAsm on 17/3/23.
 */
public class Utils {
    public static boolean isEmpty(String string) {
        return TextUtils.isEmpty(string);
    }

    public static File parseRootPath(Context context) {
        assert context != null;
        File filesDir = context.getFilesDir();
        return new File(filesDir, getRootPathName(context));
    }

    private static String getRootPathName(Context context) {
        return LocalSaverManager.DEFAULT_ROOT_NAME_PREFIX + "_" + context.getPackageName().replace(".", "_");
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
