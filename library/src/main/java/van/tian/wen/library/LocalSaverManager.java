package van.tian.wen.library;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 缓存文件系统
 */
public class LocalSaverManager {

    public static final String DEFAULT_ROOT_NAME_PREFIX = "LOCAL_SAVE_MANAGER";
    private static LocalSaverManager mInstance;

    // todo 池的概念和最大连接数
    private ArrayMap<String, LocalKaMi> mLocalKamiPools = new ArrayMap<>();
    private Context mContext;

    // 文件的根目录
    private File mRootFile;

    // 文件过滤器，只会将含有这些文件名称的目录下的文件过滤出来，并一起进行load操作
    private String[] mKeyFilters;

    private String mKeyFile;

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
        this.mKeyFile = keyFileName;
        return this;
    }

    public LocalSaverManager rootPath(File rootPath) {
        this.mRootFile = rootPath;
        return this;
    }

    public LocalSaverManager context(Context context) {
        this.mContext = context.getApplicationContext();
        return this;
    }

    public LocalSaverManager filter(String... keyFilters) {
        this.mKeyFilters = keyFilters;
        return this;
    }

    private LinkedHashMap<String, HashMap<String, FileEditor>> mFiles;

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
                        // todo 携带类型解析
                        FileEditor<Object> fileEditor = new FileEditor<>();
                        fileEditor.setFile(keyFile);
                        innerFiles.put(keyFile.getName(), fileEditor);
                    }
                }
                mFiles.put(file.getName(), innerFiles);
            }
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        if (TextUtils.isEmpty(mKeyFile)) {
            throw new IllegalArgumentException("should set file name");
        }

        if (mFiles == null) {
            load();
        }

        HashMap<String, FileEditor> innerFiles = mFiles.get(mKeyFile);
        if (innerFiles == null) {
            throw new RuntimeException("files not found");
        }

        FileEditor fileEditor = innerFiles.get(key);

        Reader reader = fileEditor.getReader();
        if (reader == null) {
            fileEditor.connectReader();
        }

        return (T) fileEditor.readContent();
    }

    public <T> void set(String key, T t) {
        if (TextUtils.isEmpty(mKeyFile)) {
            throw new IllegalArgumentException("should set file name");
        }

        if (mFiles == null) {
            load();
        }

        HashMap<String, FileEditor> innerFiles = mFiles.get(mKeyFile);
        if (innerFiles == null) {
            innerFiles = new HashMap<>();
        }
        FileEditor fileEditor = innerFiles.get(key);
        if (fileEditor == null) {
            fileEditor = new FileEditor<>();
            File file = composeFile(key);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            fileEditor.setFile(file);

            innerFiles.put(key, fileEditor);
        }

        Writer writer = fileEditor.getWriter();
        if (writer == null) {
            fileEditor.connectWriter();
        }

        fileEditor.writeContent(t, FileEditor.FLAG_RE_WRITE);
    }

    private File composeFile(String key) {
        File file = Utils.parseRootPath(mContext);
        File file1 = new File(file, mKeyFile);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        return new File(file1, key);
    }

    public class FileEditor<T> {
        private FileWriter writer;
        private FileReader reader;
        private File file;
        private Class<T> clazz;

        public Reader getReader() {
            return reader;
        }

        public void setReader(FileReader reader) {
            this.reader = reader;
        }

        public Writer getWriter() {
            return writer;
        }

        public void setWriter(FileWriter writer) {
            this.writer = writer;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public void connectWriter() {
            try {
                if (!this.file.exists()) {
                    boolean newFile = this.file.createNewFile();
                }
                this.writer = new FileWriter(this.file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void connectReader() {
            try {
                this.reader = new FileReader(this.file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public T readContent() {
            long length = this.file.length();

            if (length <= 0) {
                return null;
            } else {
                char[] chars = new char[(int) length];
                try {
                    this.reader.read(chars);
                    T deserialize = (T) ObjectSerializer.deserialize(new String(chars));
                    return deserialize;
                } catch (IOException e) {
                    Utils.closeQuietly(reader);
                }
            }
            commitRead();

            return null;
        }

        public void writeContent(T t, int flag) {

            switch (flag) {
                case FLAG_RE_WRITE:
                    try {
                        this.writer.write(ObjectSerializer.serialize(String.valueOf(t)));
                    } catch (IOException e) {
                        Utils.closeQuietly(reader);
                    }
                    break;
                case FLAG_APPEND:

                    // TODO: 17/3/24 通过在特定的位置之后再追加

                    break;
                default:
                    break;
            }

            commitWrite();

        }

        public void commitRead() {
            if (this.reader != null) {
                try {
                    this.reader.close();
                } catch (IOException e) {
                    Utils.closeQuietly(reader);
                }
            }
        }

        public void commitWrite() {
            if (this.writer != null) {
                try {
                    this.writer.flush();
                    this.writer.close();
                } catch (IOException e) {
                    Utils.closeQuietly(writer);
                }
            }
        }

        // 覆盖写
        public static final int FLAG_RE_WRITE = 0x0001;
        // 追加写
        public static final int FLAG_APPEND = 0x0010;

    }

}
