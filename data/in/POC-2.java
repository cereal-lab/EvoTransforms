/**
@author EvoParsons Team
@language java 
@title Prime Number
@description {
This program displays the first 50 prime numbers.
It displays 10 prime numbers in each line.
So, it will print 5 lines each containing 10 prime numbers  separated by space.
}
*/
public class POC {
    public static void main(String[] args){
        int max=50;
        int width=10;
        int count=0;
        int number=2;
        while (count < max) {
            boolean isPrime = true;
            for (int divisor = 2; divisor <= number / 2; divisor = divisor + 1) {
                if (number % divisor == 0){
                    isPrime=false;
                    break;
                }
            }
            if (isPrime){ 
                count = count + 1;
                System.out.print(number + " ");
                if (count % width == 0){
                    System.out.println();
                } 
            }
            number = number + 1;
        }
    }
}