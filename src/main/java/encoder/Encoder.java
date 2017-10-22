package encoder;

public class Encoder {
   public Object encode(Object caller, Object[] args, Object originalReturn) {
      System.out.println("DefaultEncoder");
      return originalReturn;
   }
}
