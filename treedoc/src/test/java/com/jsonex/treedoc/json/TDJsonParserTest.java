package com.jsonex.treedoc.json;

import com.jsonex.treedoc.TDNode;
import com.jsonex.treedoc.TDPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

@Slf4j
public class TDJsonParserTest {
  @Test public void testSkipSpaceAndComments() {
    ArrayCharSource in = new ArrayCharSource("  //abcd \n // defghi \n abc");
    assertTrue(TDJSONParser.skipSpaceAndComments(in));
    assertEquals("abc", in.read(3));

    in = new ArrayCharSource("  #abcd \n # defghi \n abc");
    assertTrue(TDJSONParser.skipSpaceAndComments(in));
    assertEquals("abc", in.read(3));

    in = new ArrayCharSource("/* abcd*/ \n /* defghi*/ \n abc");
    assertTrue(TDJSONParser.skipSpaceAndComments(in));
    assertEquals("abc", in.read(3));
  }


  @Test public void testParse() {
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.json");
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(reader));

    log.info("testParse: Node=" + TestUtil.toJSON(node));
    assertEquals("valueWithoutKey", node.getChildValue("2"));
    assertEquals("lastValueWithoutKey", node.getChildValue("5"));
    assertEquals(10, node.getChildValue("limit"));
    assertEquals("100000000000000000000", node.getChildValue("total"));
    assertEquals("Some Name 1", node.getValueByPath("data/0/name"));
    assertEquals("2nd st", node.getValueByPath("data/1/address/streetLine"));
    assertEquals("1", node.getByPath("data/1").getKey());
    assertEquals("1", node.getByPath(new String[]{"data", "1"}).getKey());

    assertTrue(node.getChild("total").isLeaf());
    assertFalse(node.getChild("data").isLeaf());

    assertArrayEquals(new String[]{"data", "1"}, node.getByPath("data/1").getPath().toArray(new String[0]));

    String json = TDJSONWriter.get().writeAsString(node);
    log.info("json: " + json);

    TDNode node1 = TDJSONParser.get().parse(new TDJSONParserOption(json));
    assertEquals(node, node1);

    json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("formatted json: " + json);
  }
  
  @Test public void testParseValueWithoutKey() {
    String json = "{\n" +
        "  abc: 10\n" +
        "  aaa\n" +
        "}";
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(json));
    log.info("Node=" + TestUtil.toJSON(node));
    assertEquals("aaa", node.getChildValue("1"));
  }

  @Test public void testParseProto() {
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.textproto");
    TDNode node = TDJSONParser.get().parse(
        new TDJSONParserOption(reader).setDefaultRootType(TDNode.Type.MAP));
    log.info("testParseProto: Node=" + TestUtil.toJSON(node));
    String json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("testParseProto: formatted json: " + json);
    assertEquals(Boolean.FALSE, node.getValueByPath("n/n1/0/n11/1/n111"));
    assertEquals(4, node.getValueByPath("n/n1/1/[d.e.f]"));
    assertEquals(6, node.getValueByPath("n/n3/0"));
    assertEquals("0", node.getByPath("n/n1/0").getKey());
    assertEquals("1", node.getByPath("n/n1/1").getKey());
  }

  @Test public void testParseJson5() {
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.json5");
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(reader).setDefaultRootType(TDNode.Type.MAP));
    log.info("testParseJson5: Node=" + TestUtil.toJSON(node));
    String json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("testParseJson5: formatted json: " + json);
    assertEquals("and you can quote me on that", node.getValueByPath( "unquoted"));
    assertEquals(912559, node.getValueByPath( "hexadecimal"));
    assertEquals(0.8675309, node.getValueByPath( "leadingDecimalPoint"));
    assertEquals(1, node.getValueByPath( "positiveSign"));
  }

  @Test public void testRootMap() {
    TDNode node = TDJSONParser.get().parse(
        new TDJSONParserOption("'a':1\nb:2").setDefaultRootType(TDNode.Type.MAP));
    assertEquals(1, node.getValueByPath("a"));
    assertEquals(2, node.getValueByPath("b"));
  }

  @Test public void testRootArray() {
    Reader reader = TestUtil.loadResource(this.getClass(), "rootArray.json");
    TDNode node = TDJSONParser.get().parse(
        new TDJSONParserOption(reader).setDefaultRootType(TDNode.Type.ARRAY));
    log.info("testParseJson5: Node=" + TestUtil.toJSON(node));
    String json = TDJSONWriter.get().writeAsString(node, new TDJSONWriterOption().setIndentFactor(2));
    log.info("testParseJson5: formatted json: " + json);
    assertEquals(4, node.getChildrenSize());
    assertEquals(2, node.getValueByPath("1"));
    assertEquals(3, node.getValueByPath("2/v"));
  }

  @Test public void testInvalid() {
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption("}"));
    assertEquals("", node.getValue());

    node = TDJSONParser.get().parse(new TDJSONParserOption(""));
    assertNull(node.getValue());
  }

  @Test public void testTDPath() {
    JSONPointer jp = JSONPointer.get();
    Reader reader = TestUtil.loadResource(this.getClass(), "testdata.json");
    TDNode node = TDJSONParser.get().parse(new TDJSONParserOption(reader));

    TDNode node1 = jp.query(node, "#1");
    log.info("testTDPath: node1: " + TestUtil.toJSON(node1));

    assertEquals("Some Name 1", node1.getChildValue("name"));
    assertEquals(10, jp.query(node1, "2/limit").getValue());
  }
}