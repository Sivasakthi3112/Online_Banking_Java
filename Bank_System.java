import java.util.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.mail.*;
import javax.mail.internet.*;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

public class Main1 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int acc_no = 0;
        int pin = 0;
        String name = null;
        
        String email = null;
        int balance = 0;
        int amt = 0;
        boolean flag1 = false;
        Connection con = null;
        boolean aut = false;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_db", "root", "root");
            if (con != null) {
                System.out.println("DB is Connected!");
            }
            Statement stm = con.createStatement();
            System.out.println("Welcome to ATM Console Application");
            System.out.println("Are you an Admin or a User?");
            System.out.println("1. Admin");
            System.out.println("2. User");
            int roleSelection = sc.nextInt();

            if (roleSelection == 1) {
                System.out.print("Enter Admin ID: ");
                int adminID = sc.nextInt();
                System.out.print("Enter Admin Password: ");
                int adminPassword = sc.nextInt();

                if (adminID == 4321 && adminPassword == 9150) {
                    System.out.println("Admin login successful!");

                    while (true) {
                        System.out.println("1. Show All User Data");
                        System.out.println("2. Show All Transactions");
                        System.out.println("3. Filter Transactions by Date");
                        System.out.println("4. Export Transactions to PDF");
                        System.out.println("5. Exit");
                        int adminChoice = sc.nextInt();

                        switch (adminChoice) {
                            case 1:
                                String queryUsers = "SELECT * FROM banking";
                                PreparedStatement pstmtUsers = con.prepareStatement(queryUsers);
                                ResultSet rsUsers = pstmtUsers.executeQuery();

                                System.out.println("---------------------------------------------------");
                                System.out.printf("%-10s %-10s %-15s %-10s %-10s%n", "Acc_No", "Name", "Email", "PIN", "Balance");
                                System.out.println("---------------------------------------------------");

                                while (rsUsers.next()) {
                                    int userAccNo = rsUsers.getInt("acc_no");
                                    String userName = rsUsers.getString("name");
                                    String userEmail = rsUsers.getString("email");
                                    int userPin = rsUsers.getInt("pin");
                                    int userBalance = rsUsers.getInt("balance");

                                    System.out.printf("%-10d %-10s %-15s %-10d %-10d%n", userAccNo, userName, userEmail, userPin, userBalance);
                                }
                                System.out.println("---------------------------------------------------");

                                rsUsers.close();
                                pstmtUsers.close();
                                break;

                            case 2:
                                String queryTrans = "SELECT * FROM transaction";
                                PreparedStatement pstmtTrans = con.prepareStatement(queryTrans);
                                ResultSet rsTrans = pstmtTrans.executeQuery();

                                System.out.println("-------------------------------------------------------------------");
                                System.out.printf("%-10s %-10s %-10s %-10s %-20s%n", "Acc_No", "Type", "Amount", "Balance", "Time");
                                System.out.println("-------------------------------------------------------------------");

                                while (rsTrans.next()) {
                                    int transAccNo = rsTrans.getInt("acc_no");
                                    String transType = rsTrans.getString("type");
                                    int transAmt = rsTrans.getInt("amt");
                                    int transBalance = rsTrans.getInt("balance");
                                    Timestamp transTime = rsTrans.getTimestamp("time");

                                    System.out.printf("%-10d %-10s %-10d %-10d %-20s%n", transAccNo, transType, transAmt, transBalance, transTime);
                                }
                                System.out.println("-------------------------------------------------------------------");

                                rsTrans.close();
                                pstmtTrans.close();
                                break;

                            case 3:
                                System.out.print("Enter start date (dd-MM-yyyy): ");
                                String startDateStr = sc.next();
                                System.out.print("Enter end date (dd-MM-yyyy): ");
                                String endDateStr = sc.next();

                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                java.util.Date parsedStartDate;
                                java.util.Date parsedEndDate;

                                try {
                                    parsedStartDate = dateFormat.parse(startDateStr);
                                    parsedEndDate = dateFormat.parse(endDateStr);
                                } catch (ParseException e) {
                                    System.out.println("Invalid date format. Please enter the date in dd-MM-yyyy format.");
                                    break;
                                }

                                java.sql.Timestamp startTimestamp = new java.sql.Timestamp(parsedStartDate.getTime());
                                java.sql.Timestamp endTimestamp = new java.sql.Timestamp(parsedEndDate.getTime());

                                String queryFilter = "SELECT * FROM transaction WHERE time BETWEEN ? AND ?";
                                PreparedStatement pstmtFilter = con.prepareStatement(queryFilter);
                                pstmtFilter.setTimestamp(1, startTimestamp);
                                pstmtFilter.setTimestamp(2, endTimestamp);
                                ResultSet rsFilter = pstmtFilter.executeQuery();

                                System.out.println("-------------------------------------------------------------------");
                                System.out.printf("%-10s %-10s %-10s %-10s %-20s%n", "Acc_No", "Type", "Amount", "Balance", "Time");
                                System.out.println("-------------------------------------------------------------------");

                                while (rsFilter.next()) {
                                    int filterAccNo = rsFilter.getInt("acc_no");
                                    String filterType = rsFilter.getString("type");
                                    int filterAmt = rsFilter.getInt("amt");
                                    int filterBalance = rsFilter.getInt("balance");
                                    Timestamp filterTime = rsFilter.getTimestamp("time");

                                    String formattedTime = new SimpleDateFormat("dd-MM-yyyy").format(filterTime);

                                    System.out.printf("%-10d %-10s %-10d %-10d %-20s%n", filterAccNo, filterType, filterAmt, filterBalance, formattedTime);
                                }
                                System.out.println("-------------------------------------------------------------------");

                                rsFilter.close();
                                pstmtFilter.close();
                                break;

                            case 4:
                                exportTransactionsToPDF(con);
                                System.out.println("Transactions exported to PDF successfully.");
                                break;

                            case 5:
                                System.out.println("Exiting Admin mode...");
                                return;

                            default:
                                System.out.println("Please enter a valid option.");
                                break;
                        }
                    }
                } else {
                    System.out.println("Invalid Admin ID or Password. Exiting...");
                    return;
                }
            } else if (roleSelection == 2) {
                while (!aut) {
                    System.out.println("1. LOGIN");
                    System.out.println("2. SIGNUP");
                    int opt1 = sc.nextInt();
                    switch (opt1) {
                        case 1:
                            System.out.print("Enter Account Number: ");
                            acc_no = sc.nextInt();
                            System.out.print("Enter PIN: ");
                            pin = sc.nextInt();
                            String q1 = "SELECT name, balance, email FROM banking WHERE acc_no = ? AND pin = ?";
                            PreparedStatement pstmt1 = con.prepareStatement(q1);
                            pstmt1.setInt(1, acc_no);
                            pstmt1.setInt(2, pin);
                            ResultSet rs = pstmt1.executeQuery();
                            if (rs.next()) {
                                name = rs.getString("name");
                                balance = rs.getInt("balance");
                                email = rs.getString("email");
                                aut = true;
                                flag1 = true;
                                System.out.println("Welcome back, " + name);
                            } else {
                                System.out.println("Invalid account number or PIN. Please try again.");
                            }
                            rs.close();
                            pstmt1.close();
                            break;
                        case 2:
                            System.out.print("Enter Account Number: ");
                            acc_no = sc.nextInt();
                            System.out.print("Enter Name: ");
                            name = sc.next();
                            System.out.print("Enter Email: ");
                            email = sc.next();
                            System.out.print("Enter PIN: ");
                            pin = sc.nextInt();
                            String insertQuery = "INSERT INTO banking (acc_no, pin, name, email, balance, time) VALUES (?, ?, ?, ?, ?, ?)";
                            PreparedStatement pstmt2 = con.prepareStatement(insertQuery);
                            pstmt2.setInt(1, acc_no);
                            pstmt2.setInt(2, pin);
                            pstmt2.setString(3, name);
                            pstmt2.setString(4, email);
                            pstmt2.setInt(5, balance);
                            pstmt2.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                            int rowsInserted = pstmt2.executeUpdate();
                            if (rowsInserted > 0) {
                                aut = true;
                                flag1 = true;
                                System.out.println("A new record was inserted successfully! \nWelcome, " + name);
                            }
                            pstmt2.close();
                            break;
                        default:
                            System.out.println("Please enter a valid option.");
                            break;
                    }
                }
                if (flag1) {
                    while (true) {
                        System.out.println("--------------------Select an option--------------------------");
                        System.out.println("1. Check Balance");
                        System.out.println("2. Credit Amount");
                        System.out.println("3. Debit Amount");
                        System.out.println("4. Transaction Report");
                        System.out.println("5. Export Transactions to PDF");
                        System.out.println("6. Change PIN Number");
                        System.out.println("7. Exit");
                        int opt2 = sc.nextInt();
                        switch (opt2) {
                            case 1:
                                System.out.println("Your current balance is: " + balance);
                                break;
                            case 2:
                                System.out.println("Enter amount to credit: ");
                                amt = sc.nextInt();
                                balance += amt;
                                System.out.println("Amount credited successfully. Your new balance is: " + balance);
                                String creditQuery = "UPDATE banking SET balance = ? WHERE acc_no = ?";
                                PreparedStatement creditStmt = con.prepareStatement(creditQuery);
                                creditStmt.setInt(1, balance);
                                creditStmt.setInt(2, acc_no);
                                creditStmt.executeUpdate();
                                creditStmt.close();

                                String insertCreditTrans = "INSERT INTO transaction (acc_no, type, amt, balance, time) VALUES (?, ?, ?, ?, ?)";
                                PreparedStatement pstmtCreditTrans = con.prepareStatement(insertCreditTrans);
                                pstmtCreditTrans.setInt(1, acc_no);
                                pstmtCreditTrans.setString(2, "Credit");
                                pstmtCreditTrans.setInt(3, amt);
                                pstmtCreditTrans.setInt(4, balance);
                                pstmtCreditTrans.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                                pstmtCreditTrans.executeUpdate();
                                pstmtCreditTrans.close();

                                sendEmail(email, "Credit Alert", "Your account has been credited with Rs. " + amt + ". Your current balance is Rs. " + balance);
                                break;
                            case 3:
                                System.out.println("Enter amount to debit: ");
                                amt = sc.nextInt();
                                if (balance >= amt) {
                                    balance -= amt;
                                    System.out.println("Amount debited successfully. Your new balance is: " + balance);
                                    String debitQuery = "UPDATE banking SET balance = ? WHERE acc_no = ?";
                                    PreparedStatement debitStmt = con.prepareStatement(debitQuery);
                                    debitStmt.setInt(1, balance);
                                    debitStmt.setInt(2, acc_no);
                                    debitStmt.executeUpdate();
                                    debitStmt.close();

                                    String insertDebitTrans = "INSERT INTO transaction (acc_no, type, amt, balance, time) VALUES (?, ?, ?, ?, ?)";
                                    PreparedStatement pstmtDebitTrans = con.prepareStatement(insertDebitTrans);
                                    pstmtDebitTrans.setInt(1, acc_no);
                                    pstmtDebitTrans.setString(2, "Debit");
                                    pstmtDebitTrans.setInt(3, amt);
                                    pstmtDebitTrans.setInt(4, balance);
                                    pstmtDebitTrans.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                                    pstmtDebitTrans.executeUpdate();
                                    pstmtDebitTrans.close();

                                    sendEmail(email, "Debit Alert", "Your account has been debited with Rs. " + amt + ". Your current balance is Rs. " + balance);
                                } else {
                                    System.out.println("Insufficient balance.");
                                }
                                break;
                            case 4:
                                String queryTransactions = "SELECT * FROM transaction WHERE acc_no = ?";
                                PreparedStatement pstmtTrans = con.prepareStatement(queryTransactions);
                                pstmtTrans.setInt(1, acc_no);
                                ResultSet rsTrans = pstmtTrans.executeQuery();

                                System.out.println("------------------------------------------------------------");
                                System.out.printf("%-10s %-10s %-10s %-10s %-20s%n", "Acc_No", "Type", "Amount", "Balance", "Time");
                                System.out.println("------------------------------------------------------------");

                                while (rsTrans.next()) {
                                    int transAccNo = rsTrans.getInt("acc_no");
                                    String transType = rsTrans.getString("type");
                                    int transAmt = rsTrans.getInt("amt");
                                    int transBalance = rsTrans.getInt("balance");
                                    Timestamp transTime = rsTrans.getTimestamp("time");

                                    System.out.printf("%-10d %-10s %-10d %-10d %-20s%n", transAccNo, transType, transAmt, transBalance, transTime);
                                }
                                System.out.println("------------------------------------------------------------");

                                rsTrans.close();
                                pstmtTrans.close();
                                break;
                            case 5:
                                generateUserTransactionsPDF(con, acc_no);
                                System.out.println("Transactions exported to PDF successfully.");
                                break;
                            case 6:
                                System.out.println("Enter new PIN: ");
                                int newPin = sc.nextInt();
                                String updatePinQuery = "UPDATE banking SET pin = ? WHERE acc_no = ?";
                                PreparedStatement updatePinStmt = con.prepareStatement(updatePinQuery);
                                updatePinStmt.setInt(1, newPin);
                                updatePinStmt.setInt(2, acc_no);
                                updatePinStmt.executeUpdate();
                                updatePinStmt.close();
                                System.out.println("PIN changed successfully.");
                                break;
                            case 7:
                                System.out.println("Thank you for using the ATM Console Application. Goodbye!");
                                return;
                            default:
                                System.out.println("Please enter a valid option.");
                                break;
                        }
                    }
                }
            } else {
                System.out.println("Invalid selection. Exiting...");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
                sc.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateUserTransactionsPDF(Connection con, int accNo) throws Exception {
        String query = "SELECT * FROM transaction WHERE acc_no = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, accNo);
        ResultSet rs = pstmt.executeQuery();

        PdfWriter writer = new PdfWriter("User_Transaction_Report.pdf");
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        Paragraph title = new Paragraph("Transaction Report")
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20);

        document.add(title);

        Paragraph subtitle = new Paragraph("Account No: " + accNo)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(15);

        document.add(subtitle);

        document.add(new Paragraph("\n"));

        Table table = new Table(5);
        table.addHeaderCell("Acc_No");
        table.addHeaderCell("Type");
        table.addHeaderCell("Amount");
        table.addHeaderCell("Balance");
        table.addHeaderCell("Time");

        while (rs.next()) {
            table.addCell(String.valueOf(rs.getInt("acc_no")));
            table.addCell(rs.getString("type"));
            table.addCell(String.valueOf(rs.getInt("amt")));
            table.addCell(String.valueOf(rs.getInt("balance")));
            table.addCell(rs.getTimestamp("time").toString());
        }

        document.add(table);
        document.close();
        rs.close();
        pstmt.close();
    }

    private static void exportTransactionsToPDF(Connection con) throws Exception {
        String query = "SELECT * FROM transaction";
        PreparedStatement pstmt = con.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        PdfWriter writer = new PdfWriter("Admin_Transaction_Report.pdf");
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        Paragraph title = new Paragraph("Transaction Report")
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20);

        document.add(title);

        document.add(new Paragraph("\n"));

        Table table = new Table(5);
        table.addHeaderCell("Acc_No");
        table.addHeaderCell("Type");
        table.addHeaderCell("Amount");
        table.addHeaderCell("Balance");
        table.addHeaderCell("Time");

        while (rs.next()) {
            table.addCell(String.valueOf(rs.getInt("acc_no")));
            table.addCell(rs.getString("type"));
            table.addCell(String.valueOf(rs.getInt("amt")));
            table.addCell(String.valueOf(rs.getInt("balance")));
            table.addCell(rs.getTimestamp("time").toString());
        }

        document.add(table);
        document.close();
        rs.close();
        pstmt.close();
    }

    private static void sendEmail(String to, String subject, String body) {
        String from = "sakthisiva31122000@gmail.com"; // replace with your email
        String password = "nqjg pbyu nxnf drje";   // replace with your email password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Email sent successfully.");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
