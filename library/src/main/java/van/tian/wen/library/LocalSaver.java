package van.tian.wen.library;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;

public class LocalSaver {

    private LocalSaverManager mInstance;
    private String mKeyFile;

    public LocalSaver(String fileName) {
        this.mKeyFile = fileName;
        this.mInstance = LocalSaverManager.getInstance();
    }


    public <T> T get(String key) {
        if (TextUtils.isEmpty(mKeyFile)) {
            throw new IllegalArgumentException("should set file name");
        }

        if (mInstance == null) {
            mInstance.load();
        }

        HashMap<String, FileEditor> innerFiles = getAllFiles();
        if (innerFiles == null) {
            return null;
        } else {
            FileEditor fileEditor = innerFiles.get(key);
            if (fileEditor == null) {
                return null;
            } else {
                Reader reader = fileEditor.getReader();
                if (reader == null) {
                    // 流已经关闭的标识
                    fileEditor.connectReader();
                }
                T t = (T) fileEditor.readContent();
                fileEditor.commitRead();
                return t;
            }
        }
    }

    public <T> void set(String key, T t) {
        if (TextUtils.isEmpty(mKeyFile)) {
            throw new IllegalArgumentException("should set file name");
        }

        if (mInstance == null) {
            mInstance.load();
        }

        HashMap<String, FileEditor> innerFiles = getAllFiles();
        if (innerFiles == null) {
            innerFiles = new HashMap<>();
        }
        FileEditor fileEditor = innerFiles.get(key);
        if (fileEditor == null) {
            fileEditor = new FileEditor<>();
            File file = mInstance.composeFile(mKeyFile, key);

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

    public HashMap<String, FileEditor> getAllFiles() {
        if (mInstance == null) {
            mInstance.load();
        }
        return mInstance.getAllFileUnder(mKeyFile);
    }

    public void clear(String key) {
        if (TextUtils.isEmpty(mKeyFile)) {
            throw new IllegalArgumentException("should set file name");
        }

        if (mInstance == null) {
            mInstance.load();
        }

        HashMap<String, FileEditor> innerFiles = getAllFiles();
        if (innerFiles != null) {
            FileEditor fileEditor = innerFiles.get(key);
            if (fileEditor != null) {
                fileEditor.clear();
            }
        }
    }
}
