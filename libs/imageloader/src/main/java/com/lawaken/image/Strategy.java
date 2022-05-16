package com.lawaken.image;

/**
 * 底层图片加载策略枚举
 * @author lipnegju
 */
public enum Strategy {

     GLIDE("glide"),
     FRESCO("fresco"),
     PICASSO("picasso");

     private String action;

     Strategy(String action) {
          this.action = action;
     }

     public String getAction() {
          return action;
     }
}
