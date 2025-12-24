package org.dromara.dbswitch.product.postgresql.copy.pgsql.constants;

import java.util.HashMap;
import java.util.Map;

public class ObjectIdentifier {

  
    public static int Boolean = 16;

    public static int Bytea = 17;

    public static int Char = 18;

    public static int Name = 19;

    public static int Int8 = 20;

    public static int Int2 = 21;

    public static int Int4 = 23;

    public static int Text = 25;

    public static int Oid = 26;

    public static int Tid = 27;

    public static int Xid = 28;

    public static int Cid = 29;

  
  
    public static int Jsonb = 114;

    public static int Xml = 115;

  
  
    public static int Point = 600;

    public static int LineSegment = 601;

    public static int Path = 602;

    public static int Box = 603;

    public static int Polygon = 604;

    public static int Line = 628;

  
  
    public static int SinglePrecision = 700;

    public static int DoublePrecision = 701;

    public static int AbsTime = 702;

    public static int RelTime = 703;

    public static int TInterval = 704;

    public static int Unknown = 705;

    public static int Circle = 705;

    public static int Cash = 790;

    public static int Money = 791;

  
  
    public static int MacAddress = 829;

    public static int Inet = 869;

    public static int Cidr = 650;

    public static int MacAddress8 = 774;

  
  
    public static int CharLength = 1042;

    public static int VarCharLength = 1043;

    public static int Date = 1082;

    public static int Time = 1082;

  
  
    public static int Timestamp = 1114;

    public static int TimestampTz = 1184;

    public static int Interval = 1186;

  
  
    public static int TimeTz = 1266;

  
  
    public static int Bit = 1560;

    public static int VarBit = 1562;

  
  
  public static int Numeric = 1700;

  
  
  public static int Uuid = 2950;

  
  
  public static int Record = 2249;

  
  private static Map<DataType, Integer> mapping = buildLookupTable();

  private static Map<DataType, Integer> buildLookupTable() {

    final Map<DataType, Integer> mapping = new HashMap<>();

    mapping.put(DataType.Boolean, Boolean);
    mapping.put(DataType.Bytea, Bytea);
    mapping.put(DataType.Char, Char);
    mapping.put(DataType.Name, Name);
    mapping.put(DataType.Int8, Int8);
    mapping.put(DataType.Int2, Int2);
    mapping.put(DataType.Int4, Int4);
    mapping.put(DataType.Text, Text);
    mapping.put(DataType.Oid, Oid);
    mapping.put(DataType.Tid, Tid);
    mapping.put(DataType.Xid, Xid);
    mapping.put(DataType.Cid, Cid);
    mapping.put(DataType.Jsonb, Jsonb);
    mapping.put(DataType.Xml, Xml);
    mapping.put(DataType.Point, Point);
    mapping.put(DataType.LineSegment, LineSegment);
    mapping.put(DataType.Path, Path);
    mapping.put(DataType.Box, Box);
    mapping.put(DataType.Polygon, Polygon);
    mapping.put(DataType.Line, Line);
    mapping.put(DataType.SinglePrecision, SinglePrecision);
    mapping.put(DataType.DoublePrecision, DoublePrecision);
    mapping.put(DataType.AbsTime, AbsTime);
    mapping.put(DataType.RelTime, RelTime);
    mapping.put(DataType.TInterval, TInterval);
    mapping.put(DataType.Unknown, Unknown);
    mapping.put(DataType.Circle, Circle);
    mapping.put(DataType.Cash, Cash);
    mapping.put(DataType.Money, Money);
    mapping.put(DataType.MacAddress, MacAddress);
    mapping.put(DataType.Inet4, Inet);
    mapping.put(DataType.Inet6, Inet);
    mapping.put(DataType.Cidr, Cidr);
    mapping.put(DataType.MacAddress8, MacAddress8);
    mapping.put(DataType.CharLength, CharLength);
    mapping.put(DataType.VarChar, VarCharLength);
    mapping.put(DataType.Date, Date);
    mapping.put(DataType.Time, Time);
    mapping.put(DataType.Timestamp, Timestamp);
    mapping.put(DataType.TimestampTz, TimestampTz);
    mapping.put(DataType.Interval, Interval);
    mapping.put(DataType.TimeTz, TimeTz);
    mapping.put(DataType.Bit, Bit);
    mapping.put(DataType.VarBit, VarBit);
    mapping.put(DataType.Numeric, Numeric);
    mapping.put(DataType.Uuid, Uuid);
    mapping.put(DataType.Record, Record);

    return mapping;
  }

  public static int mapFrom(DataType type) {
    if (mapping.containsKey(type)) {
      return mapping.get(type);
    }
    return Unknown;
  }
}
