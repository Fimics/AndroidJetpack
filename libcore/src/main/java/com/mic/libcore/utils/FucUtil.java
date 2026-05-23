package com.mic.libcore.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 功能性函数扩展类
 */
public class FucUtil {
	/**
	 * 读取文件字符串
	 * @param filePath
	 * @return
	 */
	public static String readFile(String filePath) {
		{
			int len = 0;
			byte []buf = null;
			String result = "";
			try {
				InputStream in = new FileInputStream(new File(filePath));
				len  = in.available();
				buf = new byte[len];
				in.read(buf, 0, len);

				result = new String(buf, Charset.defaultCharset());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	/**
	 * 读取asset目录下文件。
	 * @return content
	 */
	public static String readAssetFile(Context mContext, String file, String code)
	{
		int len = 0;
		byte []buf = null;
		String result = "";
		try {
			InputStream in = mContext.getAssets().open(file);
			len  = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);
			
			result = new String(buf,code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean copyAssetFolder(Context context, String srcName, String dstName) {
		try {
			boolean result = true;
			String[] fileList = context.getAssets().list(srcName);
			if (fileList == null) return false;

			if (fileList.length == 0) {
				result = copyAssetFile(context, srcName, dstName);
			} else {
				File file = new File(dstName);
				result = file.mkdirs();
				for (String filename : fileList) {
					result &= copyAssetFolder(context, srcName + File.separator + filename, dstName + File.separator + filename);
				}
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean copyAssetFile(Context context, String srcName, String dstName) {
		try {
			File outFile = new File(dstName);
			InputStream in = context.getAssets().open(srcName);
			if(!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();
			}
			OutputStream out = new FileOutputStream(outFile);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String[] listAssetFiles(Context context, String assetsFolder) {
		try {
			return context.getAssets().list(assetsFolder);
		} catch (IOException e) {
			e.printStackTrace();
			return new String[]{};
		}
	}
}
