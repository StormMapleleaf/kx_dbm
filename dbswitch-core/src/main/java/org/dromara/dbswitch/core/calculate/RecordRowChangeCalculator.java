package org.dromara.dbswitch.core.calculate;


public interface RecordRowChangeCalculator {


  boolean isRecordIdentical();


  void setRecordIdentical(boolean recordOrNot);


  boolean isCheckJdbcType();


  void setCheckJdbcType(boolean checkOrNot);

  int getFetchSize();


  void setFetchSize(int size);


  void setInterruptCheck(Runnable r);

  void executeCalculate(TaskParamEntity task, RecordRowHandler handler);
}
