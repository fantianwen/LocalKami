package van.tian.wen.library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

/**
 * Created by RadAsm on 17/3/28.
 */
public class FileEditor<T> {
    private FileWriter writer;
    private FileReader reader;
    private File file;

    // 覆盖写
    public static final int FLAG_RE_WRITE = 0x0001;
    // 追加写
    public static final int FLAG_APPEND = 0x0010;


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
                this.file.createNewFile();
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
            if (!this.file.exists()) {
                this.file.createNewFile();
            }
            this.reader = new FileReader(this.file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
                return (T) ObjectSerializer.deserialize(new String(chars));
            } catch (IOException e) {
                // 尝试重新连接
                // fantianwen stream关闭预发现
                connectReader();
                try {
                    this.reader.read(chars);
                    return (T) ObjectSerializer.deserialize(new String(chars));
                } catch (IOException e1) {
                    Utils.closeQuietly(reader);
                    return null;
                }
            }
        }
    }

    public void writeContent(T t, int flag) {

        switch (flag) {
            case FLAG_RE_WRITE:
                if (t == null) {
                    throw new IllegalArgumentException("can not put null object");
                } else {
                    try {
                        this.writer.write(ObjectSerializer.serialize((Serializable) t));
                    } catch (IOException e) {
                        // 尝试重连
                        // fantianwen stream关闭预发现
                        connectWriter();
                        try {
                            this.writer.write(ObjectSerializer.serialize((Serializable) t));
                        } catch (IOException e1) {
                            Utils.closeQuietly(writer);
                        }
                    }
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

    public void clear() {
        clearWriter();
        clearReader();
        clearFile();
    }

    private void clearFile() {
        if (this.file != null) {
            if (this.file.exists()) {
                this.file.delete();
            }
        }
    }

    private void clearReader() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                Utils.closeQuietly(this.reader);
            }
            this.reader = null;
        }
    }

    private void clearWriter() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                Utils.closeQuietly(this.writer);
            }
            this.writer = null;
        }
    }
}