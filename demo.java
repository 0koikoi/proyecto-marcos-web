import java.util.Scanner;

public class demo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el primer numero: ");
        int a = scanner.nextInt();
        System.out.print("Ingrese el segundo numero: ");
        int b = scanner.nextInt();
        int suma = a + b;
        System.out.println("La suma es: " + suma);
        scanner.close();
    }
}
