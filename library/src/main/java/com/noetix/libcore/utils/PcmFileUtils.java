package com.noetix.libcore.utils;

import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PcmFileUtils {
    private String WRITE_PCM_DIR = "/data/PCM/";

    private final static String PCM_SURFFIX = ".pcm";

    private FileOutputStream mFos;

    private FileInputStream mFis;

    // 每个 messageId 一个输出流（流式追加）
    private final Map<String, FileOutputStream> mFosMap = new ConcurrentHashMap<>();
    // 每个 messageId 一个锁，避免不同 messageId 相互阻塞
    private final Map<String, Object> mIdLocks = new ConcurrentHashMap<>();

    public PcmFileUtils(String writeDir) {
        WRITE_PCM_DIR = writeDir;
    }

    public boolean openPcmFile(String filePath) {
        File file = new File(filePath);
        try {
            mFis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mFis = null;
            return false;
        }

        return true;
    }

    public int read(byte[] buffer) {
        if (null != mFis) {
            try {
                return mFis.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                closeReadFile();
                return 0;
            }
        }

        return -1;
    }

    public void closeReadFile() {
        if (null != mFis) {
            try {
                mFis.close();
                mFis = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void createPcmFile() {
        File dir = new File(WRITE_PCM_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (null != mFos) {
            return;
        }

        DateFormat df = new SimpleDateFormat("yy-MM-dd-hh-mm-ss", Locale.CHINA);
        String filename = df.format(new Date());
        String pcmPath = WRITE_PCM_DIR + filename + PCM_SURFFIX;

        File pcm = new File(pcmPath);
        try {
            if (pcm.createNewFile()) {
                mFos = new FileOutputStream(pcm);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] data) {
        synchronized (PcmFileUtils.this) {
            if (null != mFos) {
                try {
                    mFos.write(data);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void write(byte[] data, int offset, int len) {
        synchronized (PcmFileUtils.this) {
            if (null != mFos) {
                try {
                    mFos.write(data, offset, len);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
//                    e.printStackTrace();
                }
            }
        }
    }

    public void closeWriteFile() {
        synchronized (PcmFileUtils.this) {
            if (null != mFos) {
                try {
                    mFos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mFos = null;
            }
        }
    }

    /* ==================== 流式写入：按 messageId 追加 ==================== */

    public void writeByMessageId(String messageId, byte[] data, int offset, int len) {
        if (messageId == null || messageId.isEmpty() || data == null || len <= 0) return;

        ensureDir();

        String safeId = sanitizeFileName(messageId);
        Object lock = getLockForId(safeId);

        synchronized (lock) {
            FileOutputStream fos = mFosMap.get(safeId);
            if (fos == null) {
                fos = openOrCreateStream(safeId); // append=true
                if (fos == null) return;
                mFosMap.put(safeId, fos);
            }
            try {
                fos.write(data, offset, len);
                // 性能优先可不 flush；如果怕断电丢数据就保留
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                // 出错就关闭并移除，避免一直持有坏句柄
                closeWriteFile(messageId);
            }
        }
    }

    /** 关闭某个 messageId 的文件（建议在 message 完成时调用） */
    public void closeWriteFile(String messageId) {
        if (messageId == null || messageId.isEmpty()) return;

        String safeId = sanitizeFileName(messageId);
        Object lock = getLockForId(safeId);

        synchronized (lock) {
            FileOutputStream fos = mFosMap.remove(safeId);
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** 全部关闭（如 onDestroy / shutdown） */
    public void closeAllWriteFiles() {
        for (String id : mFosMap.keySet()) {
            closeWriteFile(id);
        }
        mFosMap.clear();
    }

    /* ==================== helpers ==================== */

    private void ensureDir() {
        File dir = new File(WRITE_PCM_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private FileOutputStream openOrCreateStream(String safeId) {
        String pcmPath = WRITE_PCM_DIR + safeId + PCM_SURFFIX;
        try {
            return new FileOutputStream(new File(pcmPath), true /* append */);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String sanitizeFileName(String name) {
        // 避免 / \ : 等非法字符导致路径错误
        return name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private Object getLockForId(String safeId) {
        Object lock = mIdLocks.get(safeId);
        if (lock == null) {
            Object newLock = new Object();
            Object existing = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                existing = mIdLocks.putIfAbsent(safeId, newLock);
            }
            lock = (existing != null) ? existing : newLock;
        }
        return lock;
    }

}
