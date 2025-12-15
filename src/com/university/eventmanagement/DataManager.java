package com.university.eventmanagement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    private List<Event> events;
    private List<User> users;

    private DataManager() {
        DatabaseManager.initialize();

        events = DatabaseManager.loadEvents();
        users = DatabaseManager.loadUsers();

        if (events.isEmpty()) {
            addSampleEvents();
            saveData();
        }

        if (users.isEmpty()) {
            users.add(new User("admin", "admin123"));
            users.add(new User("groupfive", "BIT1201"));
            saveData();
        }

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void addSampleEvents() {
        Event e1 = new Event("AI Workshop", LocalDate.of(2024, 12, 15), "Room 101", "Dr. Smith");
        e1.addParticipant("John Doe - Student");
        e1.addParticipant("Jane Smith - Staff");

        Event e2 = new Event("Cultural Show", LocalDate.of(2024, 12, 20), "Main Hall", "Prof. Johnson");
        e2.addParticipant("Alice Brown - Student");

        events.add(e1);
        events.add(e2);
    }

    public boolean authenticate(String username, String password) {
        return users.stream().anyMatch(
                u -> u.getUsername().equals(username) && u.getPassword().equals(password)
        );
    }

    public void addEvent(Event event) {
        events.add(event);
        saveData();
    }

    public void removeEvent(Event event) {
        events.remove(event);
        saveData();
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    public List<Event> getConflictingEvents(LocalDate date, String venue) {
        return events.stream()
                .filter(e -> e.getDate().equals(date) &&
                        e.getVenue().equalsIgnoreCase(venue))
                .toList();
    }

    public void saveData() {
        DatabaseManager.saveEvents(events);
        DatabaseManager.saveUsers(users);
    }

    public void exportToCSV(String filename) {
        DatabaseManager.exportToCSV(events, filename);
    }

    public void createBackup() {
        DatabaseManager.createBackup();
    }
}

