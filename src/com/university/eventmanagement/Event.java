package com.university.eventmanagement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Event {
    // Variables (Properties) - These store information about each event
    private static int idCounter = 1;  // Automatically generates unique IDs
    private int eventId;               // Each event gets a unique number
    private String eventName;          // Name like "AI Workshop"
    private LocalDate date;            // Date of the event
    private String venue;              // Location like "Room 101"
    private String organizer;          // Who's organizing it
    private List<String> participants; // List of people attending

    // Constructor - This runs when you create a new Event
    public Event(String eventName, LocalDate date, String venue, String organizer) {
        this.eventId = idCounter++;      // Give this event the next ID number
        this.eventName = eventName;
        this.date = date;
        this.venue = venue;
        this.organizer = organizer;
        this.participants = new ArrayList<>();  // Start with empty participant list
    }

    // Getters - These let you READ the information
    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getVenue() {
        return venue;
    }

    public String getOrganizer() {
        return organizer;
    }

    public List<String> getParticipants() {
        return participants;
    }

    // Setters - These let you CHANGE the information
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    // Special methods
    public void addParticipant(String participant) {
        participants.add(participant);  // Add someone to the event
    }

    public int getParticipantCount() {
        return participants.size();  // Count how many people registered
    }
}