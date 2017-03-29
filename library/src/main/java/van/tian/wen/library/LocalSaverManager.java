package van.tian.wen.library;

import android.content.Context;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 缓存文件系统
 */
public class LocalSaverManager {

    public static final String DEFAULT_ROOT_NAME_PREFIX = "LOCAL_SAVE_MANAGER";
    private static LocalSaverManager mInstance;

    private Context mContext;

    // 文件的根目录
    private File mRootFile;

    // 文件过滤器，只会将含有这些文件名称的目录下的文件过滤出来，并一起进行load操作
    private String[] mKeyFilters;

    private ArrayList<String> mKeyFile = new ArrayList<>();

    private LinkedHashMap<String, HashMap<String, FileEditor>> mFiles;

    private LocalSaverManager() {

    }

    public static LocalSaverManager getInstance() {
        if (mInstance == null) {
            synchronized (LocalSaverManager.class) {
                mInstance = new LocalSaverManager();
            }
        }
        return mInstance;
    }

    public LocalSaverManager keyFile(String keyFileName) {
        if (!mKeyFile.contains(keyFileName)) {
            mKeyFile.add(keyFileName);
        }
        return this;
    }

    public LocalSaverManager rootPath(File rootPath) {
        this.mRootFile = rootPath;
        return this;
    }

    public LocalSaverManager install(Context context) {
        this.mContext = context.getApplicationContext();
        return this;
    }

    public LocalSaverManager filter(String... keyFilters) {
        this.mKeyFilters = keyFilters;
        return this;
    }

    /**
     * 装载目录下的文件
     */
    public void load() {
        if (mFiles == null) {
            mFiles = new LinkedHashMap<>(0, 0.75f, true);
        }

        if (mRootFile == null) {
            mRootFile = Utils.parseRootPath(mContext);
        }

        if (!mRootFile.exists()) {
            mRootFile.mkdirs();
        }

        File[] files = mRootFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                HashMap<String, FileEditor> innerFiles = new HashMap<>();
                File[] keyFiles = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (mKeyFilters != null && mKeyFilters.length > 0) {
                            boolean shouldBeIncluded = false;
                            for (String strFilter : mKeyFilters) {
                                shouldBeIncluded |= pathname.getName().contains(strFilter);
                                if (shouldBeIncluded) {
                                    break;
                                }
                            }
                            return shouldBeIncluded;
                        } else {
                            return true;
                        }
                    }
                });
                if (keyFiles.length > 0) {
                    for (File keyFile : keyFiles) {
                        FileEditor<Object> fileEditor = new FileEditor<>();
                        fileEditor.setFile(keyFile);
                        innerFiles.put(keyFile.getName(), fileEditor);
                    }
                }
                mFiles.put(file.getName(), innerFiles);
            }
        }
    }

    public File composeFile(String keyFile, String key) {
        File file = Utils.parseRootPath(mContext);
        File file1 = new File(file, keyFile);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        return new File(file1, key);
    }

    /*================================== API start ===============================*/

    public HashMap<String, FileEditor> getAllFileUnder(String keyFile) {
        if (mFiles == null) {
            load();
        }
        if (mFiles != null && mFiles.size() > 0) {
            HashMap<String, FileEditor> stringFileEditorHashMap = mFiles.get(keyFile);
            if (stringFileEditorHashMap != null) {
                return stringFileEditorHashMap;
            }
        }
        return null;
    }


    /*================================== API start ===============================*/
}
