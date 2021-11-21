import java.util.Scanner;

public class Student { //Construct 2

    //Construct 1
    private String name;
    private int vergleichsnote;

    public Student(String name, int vergleichsnote) {
        this.name = name;
        this.vergleichsnote = vergleichsnote;
    }

    public static void main(String[] args) {
        Scanner scanner;
        String name;
        int vergleichsnote;
        Student student;
        int counter;
        int note;


        scanner = new Scanner(System.in);

        //Construct 6
        System.out.println("Geben Sie den Namen des Studenten");
        name = scanner.nextLine();
        System.out.println("Geben Sie die Vergleichsnote");
        vergleichsnote = scanner.nextInt();

        student = new Student(name, vergleichsnote);

        counter = 0;
        System.out.println("Fugen Sie Noten von 1 bis 10 fur den gegeben Student");
        System.out.println("Falls Sie fertig sind tasten Sie 0");

        note = 1;
        while (note != 0) {     //Construct 5
            //Einfugen der Noten anhand der Tastatur
            note = scanner.nextInt();
            if (note > vergleichsnote) // Construct 4
                counter = counter + 1;  // Construct 3
        }
        //Construct 7
        System.out.println(student.name);
        System.out.println("Anzahl Noten grosser als die Vergleichsnote");
        System.out.println(counter);

    }

}
