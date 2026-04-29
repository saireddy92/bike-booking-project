package com.bikebooking;

import com.bikebooking.model.*;
import com.bikebooking.service.*;
import com.bikebooking.exception.BookingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Scanner scanner = new Scanner(System.in);
    private static BikeService bikeService = new BikeService();
    private static UserService userService = new UserService();
    private static BookingService bookingService = new BookingService();
    private static User currentUser = null;

    static {
        // Inject dependencies
        bookingService.setBikeService(bikeService);
        bookingService.setUserService(userService);
    }

    public static void main(String[] args) {
        initializeDemoData();
        logger.info("Bike Booking Application started");
        
        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else {
                showUserMenu();
            }
        }
    }

    // Rest of the methods remain the same as previous Main.java
    // Just update the package name and import statements
    private static void showMainMenu() {
        System.out.println("\n===== BIKE BOOKING APPLICATION (INDIA) =====");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. View Available Bikes");
        System.out.println("4. Exit");
        System.out.print("Choose option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                registerUser();
                break;
            case 2:
                loginUser();
                break;
            case 3:
                bikeService.displayAvailableBikes();
                break;
            case 4:
                System.out.println("Thank you for using Bike Booking App!");
                logger.info("Application closed");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void showUserMenu() {
        System.out.println("\n===== Welcome " + currentUser.getName() + " =====");
        System.out.println("1. View All Bikes");
        System.out.println("2. Book a Bike");
        System.out.println("3. View My Bookings");
        System.out.println("4. Cancel Booking");
        System.out.println("5. Logout");
        System.out.print("Choose option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                bikeService.displayAllBikes();
                break;
            case 2:
                bookBike();
                break;
            case 3:
                viewMyBookings();
                break;
            case 4:
                cancelBooking();
                break;
            case 5:
                currentUser = null;
                System.out.println("Logged out successfully!");
                break;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void registerUser() {
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        try {
            User user = userService.registerUser(name, email, phone, password);
            System.out.println("Registration successful! User ID: " + user.getUserId());
            logger.info("New user registered: {}", email);
        } catch (BookingException e) {
            System.out.println("Registration failed: " + e.getMessage());
            logger.error("Registration failed: {}", e.getMessage());
        }
    }

    private static void loginUser() {
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        try {
            currentUser = userService.loginUser(email, password);
            System.out.println("Login successful! Welcome " + currentUser.getName());
            logger.info("User logged in: {}", email);
        } catch (BookingException e) {
            System.out.println("Login failed: " + e.getMessage());
            logger.error("Login failed: {}", e.getMessage());
        }
    }

    private static void bookBike() {
        bikeService.displayAvailableBikes();
        System.out.print("Enter Bike ID to book: ");
        String bikeId = scanner.nextLine();
        
        System.out.print("Enter number of hours: ");
        int hours = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Enter pickup location (City): ");
        String pickupLocation = scanner.nextLine();
        
        try {
            Booking booking = bookingService.createBooking(bikeId, currentUser.getUserId(), hours, pickupLocation);
            System.out.println("Booking successful!");
            System.out.println("Booking ID: " + booking.getBookingId());
            System.out.println("Total Amount: ₹" + booking.getTotalAmount());
            System.out.println("Pickup Location: " + booking.getPickupLocation());
            logger.info("Booking created: {} for user {}", booking.getBookingId(), currentUser.getUserId());
        } catch (BookingException e) {
            System.out.println("Booking failed: " + e.getMessage());
            logger.error("Booking failed: {}", e.getMessage());
        }
    }

    private static void viewMyBookings() {
        List<Booking> userBookings = bookingService.getUserBookings(currentUser.getUserId());
        if (userBookings.isEmpty()) {
            System.out.println("No bookings found!");
            return;
        }
        
        System.out.println("\n===== MY BOOKINGS =====");
        for (Booking booking : userBookings) {
            Bike bike = bikeService.getBikeById(booking.getBikeId());
            System.out.println("----------------------------------------");
            System.out.println("Booking ID: " + booking.getBookingId());
            System.out.println("Bike: " + (bike != null ? bike.getBrand() + " " + bike.getModel() : "N/A"));
            System.out.println("Hours: " + booking.getHours());
            System.out.println("Total Amount: ₹" + booking.getTotalAmount());
            System.out.println("Status: " + booking.getStatus());
            System.out.println("Pickup: " + booking.getPickupLocation());
            System.out.println("Booking Date: " + booking.getBookingDate());
        }
    }

    private static void cancelBooking() {
        viewMyBookings();
        System.out.print("Enter Booking ID to cancel: ");
        String bookingId = scanner.nextLine();
        
        try {
            bookingService.cancelBooking(bookingId, currentUser.getUserId());
            System.out.println("Booking cancelled successfully!");
            logger.info("Booking cancelled: {}", bookingId);
        } catch (BookingException e) {
            System.out.println("Cancellation failed: " + e.getMessage());
            logger.error("Cancellation failed: {}", e.getMessage());
        }
    }

    private static void initializeDemoData() {
        // Adding demo bikes
        bikeService.addBike(new Bike("B001", "Hero", "Splendor Plus", 50.0, "Petrol", true, "Delhi"));
        bikeService.addBike(new Bike("B002", "Honda", "Shine", 60.0, "Petrol", true, "Mumbai"));
        bikeService.addBike(new Bike("B003", "Bajaj", "Pulsar 150", 70.0, "Petrol", true, "Bangalore"));
        bikeService.addBike(new Bike("B004", "TVS", "Apache RTR 160", 75.0, "Petrol", true, "Chennai"));
        bikeService.addBike(new Bike("B005", "Royal Enfield", "Classic 350", 100.0, "Petrol", true, "Delhi"));
        bikeService.addBike(new Bike("B006", "Yamaha", "FZ V3", 80.0, "Petrol", true, "Mumbai"));
        bikeService.addBike(new Bike("B007", "Suzuki", "Access 125", 45.0, "Petrol", true, "Pune"));
        bikeService.addBike(new Bike("B008", "Ola", "S1 Pro", 90.0, "Electric", true, "Bangalore"));
        
        System.out.println("Demo data initialized!");
        logger.info("Demo data initialized with {} bikes", bikeService.getTotalBikes());
    }
}
