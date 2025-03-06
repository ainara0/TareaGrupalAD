package Utils;

import java.util.Scanner;

public class Ask {
    static String regex = "^[A-Za-zÁÉÍÓÚáéíóúÑñ]+$";

    static Scanner scanner = new Scanner(System.in);
    public static int askForNumber() {
        String input = scanner.nextLine();
        if (!isNumeric(input)) {
            System.out.println("No has introducido un número. Prueba de nuevo");
        } else {
            int number = Integer.parseInt(input);
            return number;
        }
        return -1;
    }

    public static int askForNumber(int min, int max) {

        String input = scanner.nextLine();
        if (!isNumeric(input)) {
            System.out.println("\n No has introducido un número, Prueba de nuevo.");
        } else {
            int number = Integer.parseInt(input);
            if (!((number > (min - 1)) && (number < (max + 1)))) {
                System.out.println("\n No es un numero válido. Prueba de nuevo.");
            } else {
                return number;
            }
        }
        return -1;
    }

    private static boolean isNumeric(String num) {
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String askForString() {
        String texto;
        boolean isRight = false;
        do {
            Scanner in = new Scanner(System.in);
            texto = in.nextLine().trim();
            if (!texto.matches(regex)){
                System.out.println("El texto introducido no es válido. \n Inténtalo de nuevo(solamente letras): ");
                isRight = false;
            } else {
                isRight = true;

            }
        } while (isRight == false || texto.equalsIgnoreCase("exit"));
        if (texto.equalsIgnoreCase("exit")) {
            return null;
        }
        return texto;
    }
}
