

public class POC {
  public static void main(String [] args) {
    int sum = 0;
    int i = 1;
    while(i <= 10) {
      sum = sum + i;
      System.out. format("Sum 1..%d is %d%n", i, sum);
      i++;
    }
    System.out. format("Sum 1..10 is %d%n", sum);
  }
}
