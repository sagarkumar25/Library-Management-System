package library;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        LibraryService library = new LibraryService();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("===========================================");
        System.out.println("      LIBRARY MANAGEMENT SYSTEM v2.0       ");
        System.out.println("===========================================");

        
        boolean isAuthenticated = false;
        int loginAttempts = 0;
        
        while (!isAuthenticated && loginAttempts < 3) {
            System.out.print("\nAdmin Username: ");
            String user = scanner.nextLine();
            System.out.print("Admin Password: ");
            String pass = scanner.nextLine();
            
            if (library.authenticateAdmin(user, pass)) {
                isAuthenticated = true;
                System.out.println("Login Successful! Welcome, " + user + ".");
            } else {
                loginAttempts++;
                System.out.println("Invalid credentials. Attempts remaining: " + (3 - loginAttempts));
            }
        }

        if (!isAuthenticated) {
            System.out.println("Too many failed attempts. Terminating system.");
            scanner.close();
            return; 
        }

        
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\n----------- MAIN MENU -----------");
            System.out.println("1. Add a New Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search for a Book");
            System.out.println("4. Delete a Book");
            System.out.println("5. Register New Student");
            System.out.println("6. View All Students");
            System.out.println("7. Issue a Book");
            System.out.println("8. Return Book & Process Fines");
            System.out.println("9. Exit System");
            System.out.print("Select an operation: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    System.out.print("Book Title: "); String title = scanner.nextLine();
                    System.out.print("Author Name: "); String author = scanner.nextLine();
                    System.out.print("ISBN: "); String isbn = scanner.nextLine();
                    library.addBook(title, author, isbn);
                    break;
                case 2:
                    library.viewAllBooks();
                    break;
                case 3:
                    System.out.print("Enter search keyword (Title or Author): ");
                    String keyword = scanner.nextLine();
                    library.searchBook(keyword);
                    break;
                case 4:
                    System.out.print("Enter ID of book to delete: ");
                    int deleteId = scanner.nextInt();
                    library.deleteBook(deleteId);
                    break;
                case 5:
                    System.out.print("Student Name: "); String name = scanner.nextLine();
                    System.out.print("Student Email: "); String email = scanner.nextLine();
                    library.addStudent(name, email);
                    break;
                case 6:
                    library.viewAllStudents();
                    break;
                case 7:
                    System.out.print("Target Book ID: "); int bId = scanner.nextInt();
                    System.out.print("Issuing Student ID: "); int mId = scanner.nextInt();
                    library.borrowBook(bId, mId);
                    break;
                case 8:
                    System.out.print("Borrow Transaction ID: "); int tId = scanner.nextInt();
                    library.returnBook(tId);
                    break;
                case 9:
                    isRunning = false;
                    System.out.println("Logging out. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid selection. Try again.");
            }
        }
        scanner.close();
    }
}