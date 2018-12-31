/*
Copyright 2018 Mateusz Findeisen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ZPWT_appinfo.proto

package com.example.mfind.zpwtzerotouchpersonalwifitimetracker;

public final class ApplicationInfo {
  private ApplicationInfo() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }
  public interface AppInfoOrBuilder extends
      // @@protoc_insertion_point(interface_extends:ZPWT_ZerotouchPersonalWifiTimetracker.AppInfo)
      com.google.protobuf.MessageLiteOrBuilder {

    /**
     * <code>optional string ssid = 1;</code>
     */
    boolean hasSsid();
    /**
     * <code>optional string ssid = 1;</code>
     */
    java.lang.String getSsid();
    /**
     * <code>optional string ssid = 1;</code>
     */
    com.google.protobuf.ByteString
        getSsidBytes();

    /**
     * <code>optional int32 max_break_time = 2;</code>
     */
    boolean hasMaxBreakTime();
    /**
     * <code>optional int32 max_break_time = 2;</code>
     */
    int getMaxBreakTime();
  }
  /**
   * Protobuf type {@code ZPWT_ZerotouchPersonalWifiTimetracker.AppInfo}
   */
  public  static final class AppInfo extends
      com.google.protobuf.GeneratedMessageLite<
          AppInfo, AppInfo.Builder> implements
      // @@protoc_insertion_point(message_implements:ZPWT_ZerotouchPersonalWifiTimetracker.AppInfo)
      AppInfoOrBuilder {
    private AppInfo() {
      ssid_ = "";
    }
    private int bitField0_;
    public static final int SSID_FIELD_NUMBER = 1;
    private java.lang.String ssid_;
    /**
     * <code>optional string ssid = 1;</code>
     */
    public boolean hasSsid() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional string ssid = 1;</code>
     */
    public java.lang.String getSsid() {
      return ssid_;
    }
    /**
     * <code>optional string ssid = 1;</code>
     */
    public com.google.protobuf.ByteString
        getSsidBytes() {
      return com.google.protobuf.ByteString.copyFromUtf8(ssid_);
    }
    /**
     * <code>optional string ssid = 1;</code>
     */
    private void setSsid(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
      ssid_ = value;
    }
    /**
     * <code>optional string ssid = 1;</code>
     */
    private void clearSsid() {
      bitField0_ = (bitField0_ & ~0x00000001);
      ssid_ = getDefaultInstance().getSsid();
    }
    /**
     * <code>optional string ssid = 1;</code>
     */
    private void setSsidBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
      ssid_ = value.toStringUtf8();
    }

    public static final int MAX_BREAK_TIME_FIELD_NUMBER = 2;
    private int maxBreakTime_;
    /**
     * <code>optional int32 max_break_time = 2;</code>
     */
    public boolean hasMaxBreakTime() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional int32 max_break_time = 2;</code>
     */
    public int getMaxBreakTime() {
      return maxBreakTime_;
    }
    /**
     * <code>optional int32 max_break_time = 2;</code>
     */
    private void setMaxBreakTime(int value) {
      bitField0_ |= 0x00000002;
      maxBreakTime_ = value;
    }
    /**
     * <code>optional int32 max_break_time = 2;</code>
     */
    private void clearMaxBreakTime() {
      bitField0_ = (bitField0_ & ~0x00000002);
      maxBreakTime_ = 0;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeString(1, getSsid());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, maxBreakTime_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(1, getSsid());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, maxBreakTime_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    public static ApplicationInfo.AppInfo parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static ApplicationInfo.AppInfo parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static ApplicationInfo.AppInfo parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static ApplicationInfo.AppInfo parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static ApplicationInfo.AppInfo parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input);
    }
    public static ApplicationInfo.AppInfo parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input, extensionRegistry);
    }
    public static ApplicationInfo.AppInfo parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return parseDelimitedFrom(DEFAULT_INSTANCE, input);
    }
    public static ApplicationInfo.AppInfo parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }
    public static ApplicationInfo.AppInfo parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input);
    }
    public static ApplicationInfo.AppInfo parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(ApplicationInfo.AppInfo prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    /**
     * Protobuf type {@code ZPWT_ZerotouchPersonalWifiTimetracker.AppInfo}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageLite.Builder<
          ApplicationInfo.AppInfo, Builder> implements
        // @@protoc_insertion_point(builder_implements:ZPWT_ZerotouchPersonalWifiTimetracker.AppInfo)
        ApplicationInfo.AppInfoOrBuilder {
      // Construct using ApplicationInfo.AppInfo.newBuilder()
      private Builder() {
        super(DEFAULT_INSTANCE);
      }


      /**
       * <code>optional string ssid = 1;</code>
       */
      public boolean hasSsid() {
        return instance.hasSsid();
      }
      /**
       * <code>optional string ssid = 1;</code>
       */
      public java.lang.String getSsid() {
        return instance.getSsid();
      }
      /**
       * <code>optional string ssid = 1;</code>
       */
      public com.google.protobuf.ByteString
          getSsidBytes() {
        return instance.getSsidBytes();
      }
      /**
       * <code>optional string ssid = 1;</code>
       */
      public Builder setSsid(
          java.lang.String value) {
        copyOnWrite();
        instance.setSsid(value);
        return this;
      }
      /**
       * <code>optional string ssid = 1;</code>
       */
      public Builder clearSsid() {
        copyOnWrite();
        instance.clearSsid();
        return this;
      }
      /**
       * <code>optional string ssid = 1;</code>
       */
      public Builder setSsidBytes(
          com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setSsidBytes(value);
        return this;
      }

      /**
       * <code>optional int32 max_break_time = 2;</code>
       */
      public boolean hasMaxBreakTime() {
        return instance.hasMaxBreakTime();
      }
      /**
       * <code>optional int32 max_break_time = 2;</code>
       */
      public int getMaxBreakTime() {
        return instance.getMaxBreakTime();
      }
      /**
       * <code>optional int32 max_break_time = 2;</code>
       */
      public Builder setMaxBreakTime(int value) {
        copyOnWrite();
        instance.setMaxBreakTime(value);
        return this;
      }
      /**
       * <code>optional int32 max_break_time = 2;</code>
       */
      public Builder clearMaxBreakTime() {
        copyOnWrite();
        instance.clearMaxBreakTime();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:ZPWT_ZerotouchPersonalWifiTimetracker.AppInfo)
    }
    protected final Object dynamicMethod(
        com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
        Object arg0, Object arg1) {
      switch (method) {
        case NEW_MUTABLE_INSTANCE: {
          return new ApplicationInfo.AppInfo();
        }
        case IS_INITIALIZED: {
          return DEFAULT_INSTANCE;
        }
        case MAKE_IMMUTABLE: {
          return null;
        }
        case NEW_BUILDER: {
          return new Builder();
        }
        case VISIT: {
          Visitor visitor = (Visitor) arg0;
          ApplicationInfo.AppInfo other = (ApplicationInfo.AppInfo) arg1;
          ssid_ = visitor.visitString(
              hasSsid(), ssid_,
              other.hasSsid(), other.ssid_);
          maxBreakTime_ = visitor.visitInt(
              hasMaxBreakTime(), maxBreakTime_,
              other.hasMaxBreakTime(), other.maxBreakTime_);
          if (visitor == com.google.protobuf.GeneratedMessageLite.MergeFromVisitor
              .INSTANCE) {
            bitField0_ |= other.bitField0_;
          }
          return this;
        }
        case MERGE_FROM_STREAM: {
          com.google.protobuf.CodedInputStream input =
              (com.google.protobuf.CodedInputStream) arg0;
          com.google.protobuf.ExtensionRegistryLite extensionRegistry =
              (com.google.protobuf.ExtensionRegistryLite) arg1;
          try {
            boolean done = false;
            while (!done) {
              int tag = input.readTag();
              switch (tag) {
                case 0:
                  done = true;
                  break;
                default: {
                  if (!parseUnknownField(tag, input)) {
                    done = true;
                  }
                  break;
                }
                case 10: {
                  String s = input.readString();
                  bitField0_ |= 0x00000001;
                  ssid_ = s;
                  break;
                }
                case 16: {
                  bitField0_ |= 0x00000002;
                  maxBreakTime_ = input.readInt32();
                  break;
                }
              }
            }
          } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw new RuntimeException(e.setUnfinishedMessage(this));
          } catch (java.io.IOException e) {
            throw new RuntimeException(
                new com.google.protobuf.InvalidProtocolBufferException(
                    e.getMessage()).setUnfinishedMessage(this));
          } finally {
          }
        }
        case GET_DEFAULT_INSTANCE: {
          return DEFAULT_INSTANCE;
        }
        case GET_PARSER: {
          if (PARSER == null) {    synchronized (ApplicationInfo.AppInfo.class) {
              if (PARSER == null) {
                PARSER = new DefaultInstanceBasedParser(DEFAULT_INSTANCE);
              }
            }
          }
          return PARSER;
        }
      }
      throw new UnsupportedOperationException();
    }


    // @@protoc_insertion_point(class_scope:ZPWT_ZerotouchPersonalWifiTimetracker.AppInfo)
    private static final ApplicationInfo.AppInfo DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new AppInfo();
      DEFAULT_INSTANCE.makeImmutable();
    }

    public static ApplicationInfo.AppInfo getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static volatile com.google.protobuf.Parser<AppInfo> PARSER;

    public static com.google.protobuf.Parser<AppInfo> parser() {
      return DEFAULT_INSTANCE.getParserForType();
    }
  }


  static {
  }

  // @@protoc_insertion_point(outer_class_scope)
}
