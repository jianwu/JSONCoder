package com.jsonex.treedoc.json;

import com.jsonex.core.util.StringUtil;
import com.jsonex.treedoc.TDNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.function.Function;

@Accessors(chain = true) @Data
public class TDJSONOption {
  String KEY_ID = "$id";
//  String KEY_REF = "$ref";
//  String ObJ_START = "{";
//  String ObJ_END = "}";
//  String ARRAY_START = "[";
//  String ARRAY_END = "]";
//  String VAL_SEP = ":";
//  String VAL_END = ",";

  /** The source */
  //final CharSource source;
  URI uri;

  // Used for JSONParser
  /** In case there's no enclosed '[' of '{' on the root level, the default type. */
  TDNode.Type defaultRootType = TDNode.Type.SIMPLE;

  // Used for JSONWriter
  int indentFactor;
  boolean alwaysQuoteName = true;
  char quoteChar = '"';
  String indentStr = "";
  /** Node mapper, if it returns null, node will be skipped */
  Function<TDNode, TDNode> nodeMapper = Function.identity();
  Function<TDNode, Object> valueMapper = null;

  public static TDJSONOption ofIndentFactor(int factor) { return new TDJSONOption().setIndentFactor(factor); }
  public static TDJSONOption ofDefaultRootType(TDNode.Type type) { return new TDJSONOption().setDefaultRootType(type); }

  public TDJSONOption setIndentFactor(int _indentFactor) {
    this.indentFactor = _indentFactor;
    indentStr = StringUtil.appendRepeatedly(new StringBuilder(), ' ', indentFactor).toString();
    return this;
  }

  public boolean hasIndent() { return !indentStr.isEmpty(); }
}