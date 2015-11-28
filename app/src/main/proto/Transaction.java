// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Proto.proto at 7:1
package com.bean_keeper;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import java.io.IOException;
import java.lang.Boolean;
import java.lang.Double;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class Transaction extends Message<Transaction, Transaction.Builder> {
  public static final ProtoAdapter<Transaction> ADAPTER = new ProtoAdapter<Transaction>(FieldEncoding.LENGTH_DELIMITED, Transaction.class) {
    @Override
    public int encodedSize(Transaction value) {
      return (value.guid != null ? ProtoAdapter.STRING.encodedSizeWithTag(1, value.guid) : 0)
          + (value.date != null ? ProtoAdapter.INT64.encodedSizeWithTag(2, value.date) : 0)
          + (value.value != null ? ProtoAdapter.DOUBLE.encodedSizeWithTag(3, value.value) : 0)
          + (value.kind != null ? ProtoAdapter.STRING.encodedSizeWithTag(4, value.kind) : 0)
          + (value.deleted != null ? ProtoAdapter.BOOL.encodedSizeWithTag(5, value.deleted) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, Transaction value) throws IOException {
      if (value.guid != null) ProtoAdapter.STRING.encodeWithTag(writer, 1, value.guid);
      if (value.date != null) ProtoAdapter.INT64.encodeWithTag(writer, 2, value.date);
      if (value.value != null) ProtoAdapter.DOUBLE.encodeWithTag(writer, 3, value.value);
      if (value.kind != null) ProtoAdapter.STRING.encodeWithTag(writer, 4, value.kind);
      if (value.deleted != null) ProtoAdapter.BOOL.encodeWithTag(writer, 5, value.deleted);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public Transaction decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.guid(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.date(ProtoAdapter.INT64.decode(reader)); break;
          case 3: builder.value(ProtoAdapter.DOUBLE.decode(reader)); break;
          case 4: builder.kind(ProtoAdapter.STRING.decode(reader)); break;
          case 5: builder.deleted(ProtoAdapter.BOOL.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public Transaction redact(Transaction value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  };

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_GUID = "";

  public static final Long DEFAULT_DATE = 0L;

  public static final Double DEFAULT_VALUE = 0.0d;

  public static final String DEFAULT_KIND = "";

  public static final Boolean DEFAULT_DELETED = false;

  /**
   * A string containing a transaction GUID: "6ea4f8dd-382f-44a6-bc0a-91f3c6b8216b"
   */
  public final String guid;

  /**
   * A date and time of the transaction defined as the number of milliseconds since 1970:
   * 1385045656447
   */
  public final Long date;

  /**
   * An amount of money transferred by the transaction
   */
  public final Double value;

  /**
   * A description of the transaction
   */
  public final String kind;

  /**
   * If TRUE the transaction is deleted. It mustn??t be neither counted into the account
   * balance nor displayed to the user
   */
  public final Boolean deleted;

  public Transaction(String guid, Long date, Double value, String kind, Boolean deleted) {
    this(guid, date, value, kind, deleted, ByteString.EMPTY);
  }

  public Transaction(String guid, Long date, Double value, String kind, Boolean deleted, ByteString unknownFields) {
    super(unknownFields);
    this.guid = guid;
    this.date = date;
    this.value = value;
    this.kind = kind;
    this.deleted = deleted;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.guid = guid;
    builder.date = date;
    builder.value = value;
    builder.kind = kind;
    builder.deleted = deleted;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Transaction)) return false;
    Transaction o = (Transaction) other;
    return equals(unknownFields(), o.unknownFields())
        && equals(guid, o.guid)
        && equals(date, o.date)
        && equals(value, o.value)
        && equals(kind, o.kind)
        && equals(deleted, o.deleted);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (guid != null ? guid.hashCode() : 0);
      result = result * 37 + (date != null ? date.hashCode() : 0);
      result = result * 37 + (value != null ? value.hashCode() : 0);
      result = result * 37 + (kind != null ? kind.hashCode() : 0);
      result = result * 37 + (deleted != null ? deleted.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (guid != null) builder.append(", guid=").append(guid);
    if (date != null) builder.append(", date=").append(date);
    if (value != null) builder.append(", value=").append(value);
    if (kind != null) builder.append(", kind=").append(kind);
    if (deleted != null) builder.append(", deleted=").append(deleted);
    return builder.replace(0, 2, "Transaction{").append('}').toString();
  }

  public static final class Builder extends com.squareup.wire.Message.Builder<Transaction, Builder> {
    public String guid;

    public Long date;

    public Double value;

    public String kind;

    public Boolean deleted;

    public Builder() {
    }

    /**
     * A string containing a transaction GUID: "6ea4f8dd-382f-44a6-bc0a-91f3c6b8216b"
     */
    public Builder guid(String guid) {
      this.guid = guid;
      return this;
    }

    /**
     * A date and time of the transaction defined as the number of milliseconds since 1970:
     * 1385045656447
     */
    public Builder date(Long date) {
      this.date = date;
      return this;
    }

    /**
     * An amount of money transferred by the transaction
     */
    public Builder value(Double value) {
      this.value = value;
      return this;
    }

    /**
     * A description of the transaction
     */
    public Builder kind(String kind) {
      this.kind = kind;
      return this;
    }

    /**
     * If TRUE the transaction is deleted. It mustn??t be neither counted into the account
     * balance nor displayed to the user
     */
    public Builder deleted(Boolean deleted) {
      this.deleted = deleted;
      return this;
    }

    @Override
    public Transaction build() {
      return new Transaction(guid, date, value, kind, deleted, buildUnknownFields());
    }
  }
}
