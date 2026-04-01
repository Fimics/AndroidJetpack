package com.noetix.libcore.utils;

import android.media.Image;

import java.nio.ByteBuffer;

public class BitmapUtil {

  //Planar格式（P）的处理
  private static ByteBuffer getuvBufferWithoutPaddingP(ByteBuffer uBuffer,ByteBuffer vBuffer, int width, int height, int rowStride, int pixelStride){
    int pos = 0;
    byte []byteArray = new byte[height*width/2];
    for (int row=0; row<height/2; row++) {
      for (int col=0; col<width/2; col++) {
        int vuPos = col*pixelStride + row*rowStride;
        byteArray[pos++] = vBuffer.get(vuPos);
        byteArray[pos++] = uBuffer.get(vuPos);
      }
    }
    ByteBuffer bufferWithoutPaddings=ByteBuffer.allocate(byteArray.length);
    // 数组放到buffer中
    bufferWithoutPaddings.put(byteArray);
    //重置 limit 和postion 值否则 buffer 读取数据不对
    bufferWithoutPaddings.flip();
    return bufferWithoutPaddings;
  }
  //Semi-Planar格式（SP）的处理和y通道的数据
  private static ByteBuffer getBufferWithoutPadding(ByteBuffer buffer, int width, int rowStride, int times,boolean isVbuffer){
    if(width == rowStride) return buffer;  //没有buffer,不用处理。
    int bufferPos = buffer.position();
    int cap = buffer.capacity();
    byte []byteArray = new byte[times*width];
    int pos = 0;
    //对于y平面，要逐行赋值的次数就是height次。对于uv交替的平面，赋值的次数是height/2次
    for (int i=0;i<times;i++) {
      buffer.position(bufferPos);
      //part 1.1 对于u,v通道,会缺失最后一个像u值或者v值，因此需要特殊处理，否则会crash
      if(isVbuffer && i==times-1){
        width = width -1;
      }
      buffer.get(byteArray, pos, width);
      bufferPos+= rowStride;
      pos = pos+width;
    }

    //nv21数组转成buffer并返回
    ByteBuffer bufferWithoutPaddings=ByteBuffer.allocate(byteArray.length);
    // 数组放到buffer中
    bufferWithoutPaddings.put(byteArray);
    //重置 limit 和postion 值否则 buffer 读取数据不对
    bufferWithoutPaddings.flip();
    return bufferWithoutPaddings;
  }

  public static byte[] YUV_420_888toNV21(Image image) {
    int width =  image.getWidth();
    int height = image.getHeight();
    ByteBuffer yBuffer = getBufferWithoutPadding(image.getPlanes()[0].getBuffer(), image.getWidth(), image.getPlanes()[0].getRowStride(),image.getHeight(),false);
    ByteBuffer vBuffer;
    //part1 获得真正的消除padding的ybuffer和ubuffer。需要对P格式和SP格式做不同的处理。如果是P格式的话只能逐像素去做，性能会降低。
    if(image.getPlanes()[2].getPixelStride()==1){ //如果为true，说明是P格式。
      vBuffer = getuvBufferWithoutPaddingP(image.getPlanes()[1].getBuffer(), image.getPlanes()[2].getBuffer(),
              width,height,image.getPlanes()[1].getRowStride(),image.getPlanes()[1].getPixelStride());
    }else{
      vBuffer = getBufferWithoutPadding(image.getPlanes()[2].getBuffer(), image.getWidth(), image.getPlanes()[2].getRowStride(),image.getHeight()/2,true);
    }

    //part2 将y数据和uv的交替数据（除去最后一个v值）赋值给nv21
    int ySize = yBuffer.remaining();
    int vSize = vBuffer.remaining();
    byte[] nv21;
    int byteSize = width*height*3/2;
    nv21 = new byte[byteSize];
    yBuffer.get(nv21, 0, ySize);
    vBuffer.get(nv21, ySize, vSize);

    //part3 最后一个像素值的u值是缺失的，因此需要从u平面取一下。
    ByteBuffer uPlane = image.getPlanes()[1].getBuffer();
    byte lastValue = uPlane.get(uPlane.capacity() - 1);
    nv21[byteSize - 1] = lastValue;
    return nv21;
  }

  public static int imageToNV21(Image var0, byte[] var1) {
    int var2 = var0.getWidth();
    int var3 = var0.getHeight();
    Image.Plane[] var4 = var0.getPlanes();
    ByteBuffer var5 = var4[0].getBuffer();
    var4[1].getBuffer();
    ByteBuffer var6 = var4[2].getBuffer();
    var3 *= var2;
    var2 = Math.min(var3, var5.remaining());
    var3 = Math.min(var3 / 2, var6.remaining());
    var5.position(0);
    var5.limit(var2);
    var5.get(var1, 0, var2);
    var6.position(0);
    var6.limit(var3);
    var6.get(var1, var2, var3);
    return var2 + var3;
  }

  public static byte[] yuv420888ToBGR(Image image) {
    int width = image.getWidth();
    int height = image.getHeight();
    ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
    ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
    ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

    int ySize = yBuffer.remaining();
    int uSize = uBuffer.remaining();
    int vSize = vBuffer.remaining();

    byte[] yuvBytes = new byte[ySize + uSize + vSize];
    yBuffer.get(yuvBytes, 0, ySize);
    uBuffer.get(yuvBytes, ySize, uSize);
    vBuffer.get(yuvBytes, ySize + uSize, vSize);

    byte[] bgrBytes = new byte[width * height * 3];

    int yp = 0;
    for (int j = 0; j < height; j++) {
      int uvRow = (j >> 1) * (width >> 1);
      for (int i = 0; i < width; i++) {
        int y = yuvBytes[yp] & 0xFF;
        int u = yuvBytes[ySize + uvRow + (i >> 1)] & 0xFF;
        int v = yuvBytes[ySize + uSize + uvRow + (i >> 1)] & 0xFF;

        int c = y - 16;
        int d = u - 128;
        int e = v - 128;

        int b = (int) (1.164 * c + 2.018 * d);
        int g = (int) (1.164 * c - 0.813 * e - 0.391 * d);
        int r = (int) (1.164 * c + 1.596 * e);

        b = Math.max(0, Math.min(255, b));
        g = Math.max(0, Math.min(255, g));
        r = Math.max(0, Math.min(255, r));

        int index = yp * 3;
        bgrBytes[index] = (byte) b;
        bgrBytes[index + 1] = (byte) g;
        bgrBytes[index + 2] = (byte) r;

        yp++;
      }
    }

    return bgrBytes;
  }

}
