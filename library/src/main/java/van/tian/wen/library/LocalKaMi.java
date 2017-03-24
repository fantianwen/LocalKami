package van.tian.wen.library;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * TODO fantianwen
 * 1、异步处理方法添加 线程池
 * 2、不需要重新写入数据，直接追加
 * 3、脏文件（写入失败的时候存储为.temp文件）
 * 4、文件被写入的日志系统（记录带看打卡的缓存数据的写入状况等）
 * 5、Util.closeQuietly(writer)
 */
public class LocalKaMi {

    private File mRootFile;

    public LocalKaMi(Context context, String name) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mRootFile = new File(context.getExternalFilesDir(null), name);
        } else {
            mRootFile = new File(context.getFilesDir(), name);
        }
        // todo connection的重用
        if (!mRootFile.exists()) {
            mRootFile.mkdirs();
        }
    }

    private File getFile(String fileName) {
        File file = new File(mRootFile, fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public String getString(String key) {

        File file = getFile(key);
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        String readString = "";
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                readString += currentLine;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readString;
    }

    public void saveString(String key, String data) {
        File file = getFile(key);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 线程安全
     *
     * @return
     */
    public HashMap<String, ArrayList> getAll() {
        HashMap<String, ArrayList> map = new HashMap<>();
        File[] files = mRootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String[] split = pathname.getName().split("_");

                String userCode = split[0];
                String signId = split[1];

                if (Utils.isEmpty(userCode) || Integer.parseInt(signId) <= 0) {
                    pathname.delete();
                    return false;
                }
                return true;
            }
        });

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.length() > 0) {
                    String name = file.getName();
                    String string = getString(name);

                    ArrayList list = new ArrayList();
                    try {
                        list = (ArrayList) ObjectSerializer.deserialize(string);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    map.put(name, list);
                } else {
                    file.delete();
                }
            }
        }
        return map;
    }

    public void clearAll() {
        File[] files = mRootFile.listFiles();
        for (File file : files) {
            file.delete();
        }
    }

    public void clear(String fileName) {
        if (fileName == null) {
            return;
        }
        File file = new File(mRootFile, fileName);
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public <T> ArrayList<T> getList(String key) {
        ArrayList<T> list = new ArrayList<>();
        try {
            list = (java.util.ArrayList<T>) ObjectSerializer.deserialize(getString(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public <T> void saveList(String key, ArrayList<T> list) {
        String serializedList = null;
        try {
            serializedList = ObjectSerializer.serialize(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (serializedList != null) {
            saveString(key, serializedList);
        }
    }

}
