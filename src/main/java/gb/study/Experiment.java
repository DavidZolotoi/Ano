package gb.study;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class Experiment implements Convert {

    public static void main(String[] args) {
        Convert convert = new Convert() {};

        Integer myInt = 5;
        Object myObj1 = (Object) myInt;
        System.out.println(convert.objectToInteger(myObj1) + 2);

        String myStr = "Привет";
        Object myObj2 = (Object) myStr;
        System.out.println(convert.objectToString(myObj2) + " привет");
    }

    /*********************************************************************************/
    private static Boolean isEven = null;
    private static Boolean isWork = null;

//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//
//        isWork = true;          //работать
//        while (isWork) {
//            try {
//                System.out.print("Введите число (для выхода введите 'exit'): ");
//                int number = Integer.parseInt(scanner.nextLine());
//
//                //запуск параллельных асинхМетодов
//                CompletableFuture<Void> future1 = checkEvenOddAsync1(number);
//                CompletableFuture<Void> future2 = checkEvenOddAsync2(number);
//
//                //код для выполнения параллельно асинхронному методу в основном потоке
//                try {Thread.sleep(100);}    //чтоб асинхМетод успел поменять значение переменной
//                catch (InterruptedException e) {throw new RuntimeException(e);}
//                System.out.println("Число " + number + " является " + (isEven ? "четным." : "нечетным."));
//
//                future1.join();
//                future2.join();
//                //код для выполнения после работы асинхронного метода в основном потоке
//            } catch (NumberFormatException e) {
//                System.out.println("Некорректный ввод. Необходимое целое число.");
//                isWork = false; //прекратить
//                break;  // это на всякий
//            }
//        }
//
//        scanner.close();
//    }

    private static CompletableFuture<Void> checkEvenOddAsync1(int number) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("проверка в методе 1");
            isEven = number % 2 == 0;
        });
    }

    private static CompletableFuture<Void> checkEvenOddAsync2(int number) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("проверка в методе 2");
            isEven = number % 2 == 0;
        });
    }
}


/*
* List<CompletableFuture<Void>> futures = new ArrayList<>();
* ...
* CompletableFuture<Void> evenCheckFuture = checkEvenAsync(number);
* CompletableFuture<Void> oddCheckFuture = checkOddAsync(number);
* // Добавление CompletableFuture в коллекцию
* futures.add(evenCheckFuture);
* futures.add(oddCheckFuture);
* ...
* // Ожидание завершения всех асинхронных методов
* CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
* allOf.join();
* ///////////////////
* если словарь, то
* CompletableFuture<Void> allOf = CompletableFuture.allOf(futuresMap.values().toArray(new CompletableFuture[0]));
* */