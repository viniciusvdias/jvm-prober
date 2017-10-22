package encoder;

import example.Number;

public class NumberSubtractToAddEncoder extends Encoder {
   public Object encode(Object caller, Object[] args, Object originalReturn) {
      Number number = (Number) caller;
      Integer n = (Integer) args[0];
      Number modifiedReturn = new Number(number.i + n);
      return modifiedReturn;
   }
}
