package com.mic.libcore.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Android运行linux命令
 */
public final class USBCardFiner {
    private static final String TAG = "USBCardFiner";
    private static boolean mHaveRoot = false;
    private static int cardNum = 0;

    /**
     * 判断机器Android是否已经root，即是否获取root权限
     */
    public static boolean haveRoot() {
        if (!mHaveRoot) {
            int ret = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
            if (ret != -1) {
                KLog.i(TAG, "have root!");
                mHaveRoot = true;
            } else {
                KLog.i(TAG, "not root!");
            }
        } else {
            KLog.i(TAG, "mHaveRoot = true, have root!");
        }
        return mHaveRoot;
    }

    public static int fetchCards() {
        cardNum = execRootCmd("cat /proc/asound/cards");
        return cardNum;
    }

    /**
     * 执行命令并且输出结果
     */
    public static int execRootCmd(String cmd) {
        int cardN = 0;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            KLog.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            //while ((line = dis.readUTF()) != null) {

            while ((line = dis.readLine()) != null) {
                KLog.d(TAG, "line->" + line);
                if (line != null && (line.contains("XFMDPV0018"))) {
                    KLog.d(TAG, "Find USB card:" + line);
                    line = line.replace('[', ',');
                    line = line.replace(']', ',');
                    KLog.d(TAG, "Find USB card parse:" + line);
                    String[] strs = line.split(",");
                    if (strs.length > 0) {
                        String numStr = strs[0].trim();
                        cardN = Integer.parseInt(numStr);
                    }
                    KLog.d(TAG, "USB card Number=" + cardN);
                    break;
                }
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cardN;
    }

    /**
     * 执行命令但不关注结果输出
     */
    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            KLog.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
