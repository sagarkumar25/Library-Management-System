package library;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LibraryService {
    private Connection conn;

    public LibraryService() {
        this.conn = DBConnection.getConnection();
    }

    public boolean authenticateAdmin(String username, String password) {
        if (conn == null) { System.out.println("No database connection!"); return false; }
        
        String query = "SELECT * FROM Admins WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if credentials match
        } catch (SQLException e) {
            System.out.println("Error accessing login credentials.");
            return false;
        }
    }

    public void addBook(String title, String author, String isbn) {
        if (conn == null) return;
        String query = "INSERT INTO Books (title, author, isbn) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            pstmt.executeUpdate();
            System.out.println("Book added successfully!");
        } catch (SQLException e) { System.out.println("Error: ISBN might already exist."); }
    }

    public void deleteBook(int bookId) {
        if (conn == null) return;
        String query = "DELETE FROM Books WHERE book_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book completely removed from the system.");
            } else {
                System.out.println("Error: No book found with ID " + bookId);
            }
        } catch (SQLException e) { 
            System.out.println("Error: Cannot delete a book that is currently issued to a student."); 
        }
    }

    public void addStudent(String name, String email) {
        if (conn == null) return;
        String query = "INSERT INTO Members (name, email) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            System.out.println("Student registered successfully!");
        } catch (SQLException e) { System.out.println("Error: Email already registered."); }
    }

    public void viewAllStudents() {
        if (conn == null) return;
        String query = "SELECT * FROM Members";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n================ REGISTERED STUDENTS ================");
            System.out.printf("%-5s | %-20s | %-25s\n", "ID", "Name", "Email");
            System.out.println("-----------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d | %-20s | %-25s\n", rs.getInt("member_id"), rs.getString("name"), rs.getString("email"));
            }
        } catch (SQLException e) { System.out.println("Error retrieving students."); }
    }

    public void viewAllBooks() {
        if (conn == null) return;
        String query = "SELECT * FROM Books";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            printBookResults(rs, "LIBRARY CATALOG");
        } catch (SQLException e) { System.out.println("Error retrieving books."); }
    }

    public void searchBook(String keyword) {
        if (conn == null) return;
        String query = "SELECT * FROM Books WHERE title LIKE ? OR author LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            printBookResults(rs, "SEARCH RESULTS FOR: '" + keyword + "'");
        } catch (SQLException e) { System.out.println("Search error."); }
    }
    
    private void printBookResults(ResultSet rs, String header) throws SQLException {
        System.out.println("\n=================== " + header + " ===================");
        System.out.printf("%-5s | %-25s | %-20s | %-12s\n", "ID", "Title", "Author", "Status");
        System.out.println("----------------------------------------------------------------------");
        boolean found = false;
        while (rs.next()) {
            found = true;
            String title = rs.getString("title");
            String author = rs.getString("author");
            String status = rs.getBoolean("is_available") ? "Available" : "Issued";
            
            if(title.length() > 24) title = title.substring(0, 21) + "...";
            if(author.length() > 19) author = author.substring(0, 16) + "...";
            
            System.out.printf("%-5d | %-25s | %-20s | %-12s\n", rs.getInt("book_id"), title, author, status);
        }
        if (!found) System.out.println("No records found.");
    }

    public void borrowBook(int bookId, int studentId) {
        if (conn == null) return;
        try {
            String checkQuery = "SELECT is_available FROM Books WHERE book_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getBoolean("is_available")) {
                String borrowQuery = "INSERT INTO BorrowedBooks (book_id, member_id, due_date) VALUES (?, ?, ?)";
                PreparedStatement borrowStmt = conn.prepareStatement(borrowQuery);
                borrowStmt.setInt(1, bookId);
                borrowStmt.setInt(2, studentId);
                borrowStmt.setDate(3, Date.valueOf(LocalDate.now().plusDays(14))); 
                borrowStmt.executeUpdate();

                String updateBook = "UPDATE Books SET is_available = FALSE WHERE book_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateBook);
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                System.out.println("Success! Book issued. Due in 14 days.");
            } else { System.out.println("Error: Book unavailable or doesn't exist."); }
        } catch (SQLException e) { System.out.println("Issue error."); }
    }

    public void returnBook(int borrowId) {
        if (conn == null) return;
        try {
            String getQuery = "SELECT book_id, due_date FROM BorrowedBooks WHERE borrow_id = ? AND return_date IS NULL";
            PreparedStatement getStmt = conn.prepareStatement(getQuery);
            getStmt.setInt(1, borrowId);
            ResultSet rs = getStmt.executeQuery();

            if (rs.next()) {
                int bookId = rs.getInt("book_id");
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                LocalDate today = LocalDate.now();
                
                double fine = 0.0;
                if (today.isAfter(dueDate)) {
                    long daysLate = ChronoUnit.DAYS.between(dueDate, today);
                    fine = daysLate * 1.50; 
                    System.out.println("LATE RETURN! Fine calculated: $" + fine);
                }

                String returnQuery = "UPDATE BorrowedBooks SET return_date = ?, fine_amount = ? WHERE borrow_id = ?";
                PreparedStatement returnStmt = conn.prepareStatement(returnQuery);
                returnStmt.setDate(1, Date.valueOf(today));
                returnStmt.setDouble(2, fine);
                returnStmt.setInt(3, borrowId);
                returnStmt.executeUpdate();

                String updateBook = "UPDATE Books SET is_available = TRUE WHERE book_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateBook);
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                System.out.println("Book returned successfully.");
            } else { System.out.println("Error: Invalid transaction ID."); }
        } catch (SQLException e) { System.out.println("Return error."); }
    }
}