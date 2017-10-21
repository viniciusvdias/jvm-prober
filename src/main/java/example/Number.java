package example;

public class Number {

  public int i;

  public Number(int i) {
     this.i = i;
  }

  public Number doubleIt() {
     return new Number(i * 2);
  }

  public String toString() {
     return "Number(" + i + ")";
  }
}
