package com.mic.libcore.utils;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Android运行linux命令，多麦克风算法才需要，单麦不需要
 */
public final class RootShell {
    private static final String TAG = "wp_app_root";

    //Cannot run program "/system/xbin": error=13, Permission denied
    public static boolean isRooted() {
        try {
            // Executes the 'id' command in shell
            Process process = Runtime.getRuntime().exec(new String[] { "/system/xbin", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable t) {
            KLog.d(TAG,t.getMessage());
            return false;
        }
    }

    /**
     * 执行命令但不关注结果输出
     */
    public  static int execCommand(String cmd) {
        KLog.d(TAG, "run " + cmd);
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            KLog.d(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
            KLog.d(TAG, "run " + cmd + " result: " + result);
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

    public static String execCommand2(String cmd) {
        KLog.d(TAG, "run " + cmd);
        DataOutputStream dos = null;
        BufferedReader input = null;
        String result = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            KLog.d(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();

            // Read the output
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
            }
            result = output.toString();
            KLog.d(TAG, "run " + cmd + " result: " + result);
        } catch (Exception e) {
            KLog.d(TAG,e.getMessage());
            KLog.d(TAG,"执行命令失败");
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    KLog.d(TAG,e.getMessage());
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    KLog.d(TAG,e.getMessage());
                }
            }
        }
        return result;
    }

}
