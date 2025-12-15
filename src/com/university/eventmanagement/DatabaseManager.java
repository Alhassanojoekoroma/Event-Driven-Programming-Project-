package com.university.eventmanagement;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles data persistence using file storage
 * Saves events and users to local files
 */
public class DatabaseManager {
    private static final String DATA_DIR = "eventmanagement_data";
    private static final String EVENTS_FILE = DATA_DIR + "/events.txt";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Initialize database (create folders and files if they don't exist)
     */
    public static void initialize() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File eventsFile = new File(EVENTS_FILE);
        File usersFile = new File(USERS_FILE);

        try {
            if (!eventsFile.exists()) eventsFile.createNewFile();
            if (!usersFile.exists()) usersFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Save all events to file
     */
    public static void saveEvents(List<Event> events) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EVENTS_FILE))) {
            for (Event event : events) {
                // Format: ID|Name|Date|Venue|Organizer|Participant1,Participant2,...
                StringBuilder line = new StringBuilder();
                line.append(event.getEventId()).append("|");
                line.append(event.getEventName()).append("|");
                line.append(event.getDate().format(DATE_FORMATTER)).append("|");
                line.append(event.getVenue()).append("|");
                line.append(event.getOrganizer()).append("|");

                // Join participants with comma
                String participants = String.join(",", event.getParticipants());
                line.append(participants);

                writer.write(line.toString());
                writer.newLine();
            }
            System.out.println("Events saved successfully!");
        } catch (IOException e) {
            System.err.println("Error saving events: " + e.getMessage());
        }
    }

    /**
     * Load all events from file
     */
    public static List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        File file = new File(EVENTS_FILE);

        if (!file.exists() || file.length() == 0) {
            return events; // Return empty list if file doesn't exist or is empty
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(EVENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|", -1); // -1 to keep empty strings
                if (parts.length >= 5) {
                    try {
                        // int id = Integer.parseInt(parts[0]); // We'll let Event class auto-generate IDs
                        String name = parts[1];
                        LocalDate date = LocalDate.parse(parts[2], DATE_FORMATTER);
                        String venue = parts[3];
                        String organizer = parts[4];

                        Event event = new Event(name, date, venue, organizer);

                        // Add participants if they exist
                        if (parts.length > 5 && !parts[5].isEmpty()) {
                            String[] participants = parts[5].split(",");
                            for (String participant : participants) {
                                if (!participant.trim().isEmpty()) {
                                    event.addParticipant(participant.trim());
                                }
                            }
                        }

                        events.add(event);
                    } catch (Exception e) {
                        System.err.println("Error parsing event line: " + line);
                    }
                }
            }
            System.out.println("Loaded " + events.size() + " events from database");
        } catch (IOException e) {
            System.err.println("Error loading events: " + e.getMessage());
        }

        return events;
    }

    /**
     * Save all users to file
     */
    public static void saveUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                // Format: Username|Password
                writer.write(user.getUsername() + "|" + user.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Load all users from file
     */
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);

        if (!file.exists() || file.length() == 0) {
            // Return default users if file doesn't exist
            users.add(new User("admin", "admin123"));
            users.add(new User("coordinator", "coord123"));
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Export events to CSV format
     */
    public static void exportToCSV(List<Event> events, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write CSV header
            writer.write("Event ID,Event Name,Date,Venue,Organizer,Total Participants");
            writer.newLine();

            // Write data
            for (Event event : events) {
                writer.write(String.format("%d,%s,%s,%s,%s,%d",
                        event.getEventId(),
                        escapeCsv(event.getEventName()),
                        event.getDate().format(DATE_FORMATTER),
                        escapeCsv(event.getVenue()),
                        escapeCsv(event.getOrganizer()),
                        event.getParticipantCount()
                ));
                writer.newLine();
            }

            System.out.println("Exported to CSV: " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
        }
    }

    /**
     * Escape CSV values (handle commas and quotes)
     */
    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Backup current database
     */
    public static void createBackup() {
        try {
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String backupDir = DATA_DIR + "/backups";
            new File(backupDir).mkdirs();

            // Copy events file
            copyFile(EVENTS_FILE, backupDir + "/events_" + timestamp + ".txt");

            // Copy users file
            copyFile(USERS_FILE, backupDir + "/users_" + timestamp + ".txt");

            System.out.println("Backup created successfully!");
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }

    private static void copyFile(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        if (!sourceFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(source));
             BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}