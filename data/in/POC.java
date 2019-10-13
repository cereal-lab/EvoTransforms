public class POC {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1; i <= 10; i++) {            
            sum = sum + i;
            System.out.format("Sum 1..%d is %d%n", i, sum);
        }
        System.out.format("Sum 1..10 is %d%n", sum);
    }
}