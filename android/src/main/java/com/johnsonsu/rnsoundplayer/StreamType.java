package com.johnsonsu.rnsoundplayer;

enum StreamType {
  RINGTONE("RINGTONE"),
  MEDIA("MEDIA");

   private String streamType;

   StreamType(String streamType) {
    this.streamType = streamType;
  }

   public static StreamType fromString(String text) {
    for (StreamType b : StreamType.values()) {
      if (b.streamType.equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }
}