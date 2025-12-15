package com.university.eventmanagement;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

public class MainFrame extends JFrame {
    private static final Color PRIMARY = new Color(33, 150, 243);
    private static final Color PRIMARY_DARK = new Color(0, 33, 66);
    private static final Color SURFACE = new Color(0, 0, 0);
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(255, 255, 255);
    private static final Color DIVIDER = new Color(0, 0, 0);
    private static final Color SUCCESS = new Color(0, 62, 2);
    private static final Color WARNING = new Color(255, 193, 7);
    private static final Color ACCENT = new Color(156, 39, 176);

    private final DefaultTableModel tableModel;
    private final JTable eventTable;
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Upcoming", "Today", "Completed"});

    private final JLabel totalEventsLabel = new JLabel("0");
    private final JLabel participantsLabel = new JLabel("0");
    private final JLabel upcomingLabel = new JLabel("0");
    private final JLabel monthLabel = new JLabel("0");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(cardLayout);

    private final JButton dashboardBtn = new JButton("Dashboard");
    private final JButton eventsBtn = new JButton("Events");
    private final JButton reportsBtn = new JButton("Reports");
    private final JButton analyticsBtn = new JButton("Analytics");
    private final JButton settingsBtn = new JButton("Settings");

    public MainFrame(String username) {
        super("Event Management - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JSplitPane split = new JSplitPane();
        split.setDividerSize(2);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(SURFACE);

        JPanel sidebar = buildSidebar(username);
        JPanel content = buildContent();

        split.setLeftComponent(sidebar);
        split.setRightComponent(content);

        getContentPane().setBackground(SURFACE);
        add(split, BorderLayout.CENTER);

        String[] columns = {"ID", "Event Name", "Date", "Venue", "Organizer", "Participants", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        eventTable = createTable(tableModel);

        JPanel eventsCard = buildEventsCard();
        JPanel dashboardCard = buildDashboardCard();
        centerCards.add(dashboardCard, "dashboard");
        centerCards.add(eventsCard, "events");
        centerCards.add(buildReportsCard(), "reports");

        centerCards.add(buildAnalyticsCard(), "analytics");

        centerCards.add(buildSettingsCard(), "settings");


        cardLayout.show(centerCards, "dashboard");

        addListeners();
        refreshStatsAndTable();

        pack();
        setVisible(true);
    }

    private JPanel buildAnalyticsCard() {
        JPanel panel = new RoundedPanel(10, Color.WHITE);
        panel.setLayout(new GridLayout(2, 2, 16, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<Event> events = DataManager.getInstance().getAllEvents();

        long upcoming = events.stream()
                .filter(e -> !e.getDate().isBefore(LocalDate.now()))
                .count();

        int totalParticipants = events.stream()
                .mapToInt(Event::getParticipantCount)
                .sum();

        panel.add(makeStatCard("Total Events", new JLabel(String.valueOf(events.size())), PRIMARY));
        panel.add(makeStatCard("Upcoming Events", new JLabel(String.valueOf(upcoming)), WARNING));
        panel.add(makeStatCard("Participants", new JLabel(String.valueOf(totalParticipants)), SUCCESS));
        panel.add(makeStatCard("Avg Participants",
                new JLabel(events.isEmpty() ? "0" :
                        String.valueOf(totalParticipants / events.size())),
                ACCENT));

        return panel;
    }


    private JPanel buildSettingsCard() {
        JPanel panel = new RoundedPanel(10, Color.WHITE);
        panel.setLayout(new GridLayout(3, 1, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(createMaterialButton(
                "Backup Database",
                PRIMARY,
                e -> {
                    DataManager.getInstance().createBackup();
                    JOptionPane.showMessageDialog(this, "Backup created successfully");
                }
        ));

        panel.add(createMaterialButton(
                "Export CSV",
                SUCCESS,
                e -> DataManager.getInstance().exportToCSV("events_export.csv")
        ));

        panel.add(createMaterialButton(
                "Logout",
                new Color(244, 67, 54),
                e -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
        ));

        return panel;
    }



    private JPanel buildReportsCard() {
        JPanel panel = new RoundedPanel(10, Color.WHITE);
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        StringBuilder sb = new StringBuilder();
        List<Event> events = DataManager.getInstance().getAllEvents();

        sb.append("EVENT REPORT SUMMARY\n");
        sb.append("====================\n\n");
        sb.append("Total Events: ").append(events.size()).append("\n");

        int totalParticipants = events.stream()
                .mapToInt(Event::getParticipantCount)
                .sum();

        sb.append("Total Participants: ").append(totalParticipants).append("\n\n");

        events.forEach(e -> {
            sb.append(e.getEventName()).append("\n");
            sb.append(" Date: ").append(e.getDate()).append("\n");
            sb.append(" Venue: ").append(e.getVenue()).append("\n");
            sb.append(" Participants: ").append(e.getParticipantCount()).append("\n\n");
        });

        area.setText(sb.toString());

        JButton export = createMaterialButton(
                "Export CSV",
                SUCCESS,
                e -> DataManager.getInstance().exportToCSV("events_report.csv")
        );

        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        panel.add(export, BorderLayout.SOUTH);

        return panel;
    }



    private JPanel buildSidebar(String username) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, DIVIDER));
        root.setPreferredSize(new Dimension(260, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 16));
        top.setBackground(Color.WHITE);
        JLabel title = new JLabel("<html><span style='font-weight:700;font-size:18px;color:#212121;'>Event Manager</span></html>");
        top.add(title);

        JPanel menu = new JPanel();
        menu.setBackground(Color.WHITE);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        styleSidebarButton(dashboardBtn, true);
        styleSidebarButton(eventsBtn, false);
        styleSidebarButton(reportsBtn, false);
        styleSidebarButton(analyticsBtn, false);
        styleSidebarButton(settingsBtn, false);

        menu.add(dashboardBtn);
        menu.add(eventsBtn);
        menu.add(reportsBtn);
        menu.add(analyticsBtn);
        menu.add(settingsBtn);
        menu.add(Box.createVerticalGlue());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel user = new JLabel("<html><b>" + username + "</b><br/><span style='color:#757575;font-size:11px;'>Organizer</span></html>");
        bottom.add(user, BorderLayout.WEST);

        root.add(top, BorderLayout.NORTH);
        root.add(menu, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        dashboardBtn.addActionListener(e -> { selectMenu(dashboardBtn); cardLayout.show(centerCards, "dashboard"); });
        eventsBtn.addActionListener(e -> { selectMenu(eventsBtn); cardLayout.show(centerCards, "events"); });
        reportsBtn.addActionListener(e -> { selectMenu(reportsBtn); cardLayout.show(centerCards, "reports"); });
        analyticsBtn.addActionListener(e -> { selectMenu(analyticsBtn); cardLayout.show(centerCards, "analytics"); });
        settingsBtn.addActionListener(e -> { selectMenu(settingsBtn); cardLayout.show(centerCards, "settings"); });

        return root;
    }

    private void styleSidebarButton(JButton btn, boolean selected) {
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (selected) {
            btn.setForeground(PRIMARY_DARK);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, PRIMARY),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            btn.setOpaque(true);
            btn.setBackground(new Color(255, 255, 255));
        } else {
            btn.setForeground(TEXT_PRIMARY);
            btn.setOpaque(false);
        }
    }

    private void selectMenu(JButton selected) {
        for (Component c : ((Container) selected.getParent()).getComponents()) {
            if (c instanceof JButton) {
                styleSidebarButton((JButton) c, c == selected);
            }
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel appBar = new JPanel(new BorderLayout());
        appBar.setOpaque(false);
        JLabel heading = new JLabel("Dashboard Overview");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setForeground(TEXT_PRIMARY);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        rightTools.setOpaque(false);
        searchField.setPreferredSize(new Dimension(260, 36));
        styleTextField(searchField);
        statusFilter.setPreferredSize(new Dimension(140, 36));
        statusFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusFilter.setBackground(Color.WHITE);

        rightTools.add(statusFilter);
        rightTools.add(searchField);

        appBar.add(heading, BorderLayout.WEST);
        appBar.add(rightTools, BorderLayout.EAST);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 20, 0));
        statsPanel.add(makeStatCard("Total Events", totalEventsLabel, PRIMARY));
        statsPanel.add(makeStatCard("Participants", participantsLabel, SUCCESS));
        statsPanel.add(makeStatCard("Upcoming", upcomingLabel, WARNING));
        statsPanel.add(makeStatCard("This Month", monthLabel, ACCENT));

        centerCards.setOpaque(false);

        root.add(appBar, BorderLayout.NORTH);
        root.add(statsPanel, BorderLayout.BEFORE_FIRST_LINE);
        root.add(centerCards, BorderLayout.CENTER);

        return root;
    }

    private JPanel makeStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new RoundedPanel(10, Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        card.setLayout(new BorderLayout());
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(TEXT_SECONDARY);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel circle = new JLabel("\u25CF");
        circle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        circle.setForeground(accent);
        top.add(circle, BorderLayout.WEST);
        top.add(valueLabel, BorderLayout.EAST);

        card.add(top, BorderLayout.CENTER);
        card.add(t, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildDashboardCard() {
        JPanel panel = new RoundedPanel(10, Color.WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(eventTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildEventsCard() {
        JPanel root = new RoundedPanel(10, Color.WHITE);
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Events Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        actions.add(createMaterialButton("+ Add", SUCCESS, e -> addEvent()));
        actions.add(createMaterialButton("Edit", PRIMARY, e -> updateEvent()));
        actions.add(createMaterialButton("Delete", new Color(244, 67, 54), e -> deleteEvent()));
        actions.add(createMaterialButton("Register", ACCENT, e -> registerParticipant()));
        actions.add(createMaterialButton("Report", WARNING, e -> generateEnhancedReport()));

        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(eventTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildPlaceholderCard(String text) {
        JPanel p = new RoundedPanel(10, Color.WHITE);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 18));
        l.setForeground(TEXT_SECONDARY);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(44);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(0, 0, 0));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(8, 6));

        table.getTableHeader().setPreferredSize(new Dimension(0, 44));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SURFACE);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        return table;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(DIVIDER, 8),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
    }

    private JButton createMaterialButton(String text, Color bg, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(new RoundedLineBorder(bg.darker(), 16));
        b.setPreferredSize(new Dimension(110, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(action);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(bg.darker()); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private void addListeners() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        statusFilter.addActionListener(e -> filterTable());

        eventTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) updateEvent();
            }
        });
    }

    private void refreshStatsAndTable() {
        List<Event> events = DataManager.getInstance().getAllEvents();
        totalEventsLabel.setText(String.valueOf(events.size()));
        int totalParticipants = events.stream().mapToInt(Event::getParticipantCount).sum();
        participantsLabel.setText(String.valueOf(totalParticipants));
        long upcoming = events.stream().filter(ev -> !ev.getDate().isBefore(LocalDate.now())).count();
        upcomingLabel.setText(String.valueOf(upcoming));
        int thisMonth = (int) events.stream().filter(ev -> ev.getDate().getMonth().equals(LocalDate.now().getMonth())).count();
        monthLabel.setText(String.valueOf(thisMonth));
        filterTable();
    }

    private void filterTable() {
        String q = searchField.getText().trim().toLowerCase();
        String status = statusFilter.getSelectedItem().toString();
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Event event : DataManager.getInstance().getAllEvents()) {
            String evStatus = event.getDate().isBefore(today) ? "Completed" :
                    event.getDate().isEqual(today) ? "Today" : "Upcoming";

            boolean statusOk = status.equals("All") || evStatus.equalsIgnoreCase(status);
            boolean qOk = q.isEmpty() ||
                    event.getEventName().toLowerCase().contains(q) ||
                    event.getVenue().toLowerCase().contains(q) ||
                    event.getOrganizer().toLowerCase().contains(q);

            if (statusOk && qOk) {
                tableModel.addRow(new Object[]{
                        event.getEventId(),
                        event.getEventName(),
                        event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        event.getVenue(),
                        event.getOrganizer(),
                        event.getParticipantCount(),
                        evStatus
                });
            }
        }
    }

    private void addEvent() {
        MaterialFormDialog d = new MaterialFormDialog(this, "Add New Event");
        JTextField name = d.addTextField("Event Name");
        JTextField date = d.addTextField("Date (DD/MM/YYYY)");
        JTextField venue = d.addTextField("Venue");
        JTextField organizer = d.addTextField("Organizer");

        d.setPrimaryAction("Save Event", e -> {
            try {
                String n = name.getText().trim();
                String ds = date.getText().trim();
                String v = venue.getText().trim();
                String o = organizer.getText().trim();
                if (n.isEmpty() || ds.isEmpty() || v.isEmpty() || o.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "All fields are required", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                LocalDate dt = LocalDate.parse(ds, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                List<Event> conflicts = DataManager.getInstance().getConflictingEvents(dt, v);
                if (!conflicts.isEmpty()) {
                    int r = JOptionPane.showConfirmDialog(d, "Event exists same date/venue. Continue?", "Conflict", JOptionPane.YES_NO_OPTION);
                    if (r != JOptionPane.YES_OPTION) return;
                }
                Event ev = new Event(n, dt, v, o);
                DataManager.getInstance().addEvent(ev);
                refreshStatsAndTable();
                d.dispose();
                JOptionPane.showMessageDialog(this, "Event added");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(d, "Invalid date format. Use DD/MM/YYYY", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        d.setVisible(true);
    }

    private Event getSelectedEvent() {
        int row = eventTable.getSelectedRow();
        if (row == -1) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        return DataManager.getInstance().getAllEvents().stream()
                .filter(ev -> ev.getEventId() == id)
                .findFirst().orElse(null);
    }

    private void updateEvent() {
        Event ev = getSelectedEvent();
        if (ev == null) {
            JOptionPane.showMessageDialog(this, "Select an event to edit", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MaterialFormDialog d = new MaterialFormDialog(this, "Update Event");
        JTextField name = d.addTextField("Event Name", ev.getEventName());
        JTextField date = d.addTextField("Date (DD/MM/YYYY)", ev.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField venue = d.addTextField("Venue", ev.getVenue());
        JTextField organizer = d.addTextField("Organizer", ev.getOrganizer());

        d.setPrimaryAction("Update Event", a -> {
            try {
                String n = name.getText().trim();
                String ds = date.getText().trim();
                String v = venue.getText().trim();
                String o = organizer.getText().trim();
                if (n.isEmpty() || ds.isEmpty() || v.isEmpty() || o.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "All fields are required", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                LocalDate dt = LocalDate.parse(ds, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                ev.setEventName(n);
                ev.setDate(dt);
                ev.setVenue(v);
                ev.setOrganizer(o);
                refreshStatsAndTable();
                d.dispose();
                JOptionPane.showMessageDialog(this, "Event updated");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(d, "Invalid date format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        d.setVisible(true);
    }

    private void deleteEvent() {
        Event ev = getSelectedEvent();
        if (ev == null) {
            JOptionPane.showMessageDialog(this, "Select an event to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Delete selected event?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            DataManager.getInstance().removeEvent(ev);
            refreshStatsAndTable();
            JOptionPane.showMessageDialog(this, "Event removed");
        }
    }

    private void registerParticipant() {
        Event ev = getSelectedEvent();
        if (ev == null) {
            JOptionPane.showMessageDialog(this, "Select an event to register", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MaterialFormDialog d = new MaterialFormDialog(this, "Register Participant");
        JTextField name = d.addTextField("Participant Name");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Student", "Staff"});
        typeBox.setPreferredSize(new Dimension(260, 36));
        d.addComponentRow("Type", typeBox);

        d.setPrimaryAction("Register", e -> {
            String n = name.getText().trim();
            if (n.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Enter participant name", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ev.addParticipant(n + " - " + typeBox.getSelectedItem());
            refreshStatsAndTable();
            d.dispose();
            JOptionPane.showMessageDialog(this, "Participant added");
        });

        d.setVisible(true);
    }

    private void generateEnhancedReport() {
        JDialog dialog = new JDialog(this, "Event Report", true);
        dialog.setMinimumSize(new Dimension(900, 640));
        dialog.setLocationRelativeTo(this);

        JPanel main = new RoundedPanel(10, Color.WHITE);
        main.setLayout(new BorderLayout(14, 14));
        main.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        reportArea.setMargin(new Insets(12, 12, 12, 12));

        StringBuilder rep = new StringBuilder();
        rep.append("UNIVERSITY EVENT MANAGEMENT REPORT\n");
        rep.append("Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");

        List<Event> events = DataManager.getInstance().getAllEvents();
        rep.append("Total Events: ").append(events.size()).append("\n\n");
        rep.append("UPCOMING EVENTS\n");
        rep.append("----------------\n");
        events.stream()
                .filter(e -> !e.getDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Event::getDate))
                .forEach(e -> {
                    rep.append(e.getEventName()).append(" (").append(e.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append(")\n");
                    rep.append("Venue: ").append(e.getVenue()).append(" | Organizer: ").append(e.getOrganizer()).append("\n");
                    rep.append("Participants: ").append(e.getParticipantCount()).append("\n\n");
                });

        int totalParticipants = events.stream().mapToInt(Event::getParticipantCount).sum();
        rep.append("Total Participants (All Events): ").append(totalParticipants).append("\n");
        if (!events.isEmpty()) {
            rep.append("Average Participants per Event: ").append(String.format("%.1f", (double) totalParticipants / events.size())).append("\n");
        }

        reportArea.setText(rep.toString());
        reportArea.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(reportArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel qrPanel = new RoundedPanel(8, new Color(250, 250, 250));
        qrPanel.setLayout(new BorderLayout(8, 8));
        qrPanel.setPreferredSize(new Dimension(260, 260));
        qrPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel qrTitle = new JLabel("QR for Report", SwingConstants.CENTER);
        qrTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        qrPanel.add(qrTitle, BorderLayout.NORTH);

        BufferedImage qrImage = generateSimpleQRCode("Report:" + events.size() + ":" + LocalDate.now(), 220, 220);
        JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrPanel.add(qrLabel, BorderLayout.CENTER);

        JPanel qrBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        qrBtns.setOpaque(false);
        JButton save = createMaterialButton("Save Report", SUCCESS, e -> saveReportToFile(rep.toString(), qrImage));
        JButton saveQr = createMaterialButton("Save QR", PRIMARY, e -> saveQRCode(qrImage));
        qrBtns.add(save);
        qrBtns.add(saveQr);
        qrPanel.add(qrBtns, BorderLayout.SOUTH);

        main.add(scroll, BorderLayout.CENTER);
        main.add(qrPanel, BorderLayout.EAST);

        dialog.add(main);
        dialog.setVisible(true);
    }

    private BufferedImage generateSimpleQRCode(String data, int width, int height) {
        BufferedImage qr = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = qr.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        int blocks = 20;
        int blockSize = Math.max(4, width / blocks);
        for (int i = 0; i < Math.min(data.length(), blocks * blocks); i++) {
            int x = (i % blocks) * blockSize;
            int y = (i / blocks) * blockSize;
            if (data.charAt(i % data.length()) % 2 == 0) {
                g.setColor(Color.BLACK);
                g.fillRect(x, y, blockSize, blockSize);
            }
        }
        g.dispose();
        return qr;
    }

    private void saveReportToFile(String reportText, BufferedImage qrImage) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Event_Report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".txt"));
        int sel = chooser.showSaveDialog(this);
        if (sel == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(file)) { writer.write(reportText); }
                String qrFileName = file.getAbsolutePath().replace(".txt", "_QR.png");
                ImageIO.write(qrImage, "png", new File(qrFileName));
                JOptionPane.showMessageDialog(this, "Report and QR saved", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveQRCode(BufferedImage qrImage) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Event_Report_QR_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".png"));
        int sel = chooser.showSaveDialog(this);
        if (sel == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                ImageIO.write(qrImage, "png", file);
                JOptionPane.showMessageDialog(this, "QR saved: " + file.getName(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving QR: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg) { this.radius = radius; this.bg = bg; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        RoundedLineBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }
    }

    static class MaterialFormDialog extends JDialog {
        private final JPanel body = new JPanel();
        private final JButton primaryBtn = new JButton();
        private final JButton cancelBtn = new JButton("Cancel");

        MaterialFormDialog(JFrame parent, String title) {
            super(parent, title, true);
            setMinimumSize(new Dimension(480, 360));
            setLocationRelativeTo(parent);

            body.setLayout(new GridBagLayout());
            body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            body.setBackground(Color.WHITE);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(body, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            actions.setOpaque(false);

            primaryBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
            primaryBtn.setBackground(PRIMARY);
            primaryBtn.setForeground(Color.WHITE);
            primaryBtn.setBorder(new RoundedLineBorder(PRIMARY.darker(), 16));
            primaryBtn.setPreferredSize(new Dimension(140, 36));
            primaryBtn.setFocusPainted(false);

            cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
            cancelBtn.addActionListener(e -> dispose());

            actions.add(cancelBtn);
            actions.add(primaryBtn);
            getContentPane().add(actions, BorderLayout.SOUTH);
        }

        public JTextField addTextField(String labelText) { return addTextField(labelText, ""); }

        public JTextField addTextField(String labelText, String initial) {
            JLabel lbl = new JLabel(labelText);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lbl.setForeground(TEXT_PRIMARY);
            JTextField tf = new JTextField(initial);
            tf.setPreferredSize(new Dimension(320, 36));
            tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
            tf.setBorder(new RoundedLineBorder(DIVIDER, 10));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0; gbc.gridy = body.getComponentCount(); gbc.weightx = 1;
            body.add(lbl, gbc);
            gbc.gridx = 0; gbc.gridy++;
            body.add(tf, gbc);
            return tf;
        }

        public void addComponentRow(String labelText, JComponent comp) {
            JLabel lbl = new JLabel(labelText);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lbl.setForeground(TEXT_PRIMARY);
            comp.setPreferredSize(new Dimension(320, 36));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0; gbc.gridy = body.getComponentCount(); gbc.weightx = 1;
            body.add(lbl, gbc);
            gbc.gridx = 0; gbc.gridy++;
            body.add(comp, gbc);
        }

        public void setPrimaryAction(String text, java.awt.event.ActionListener action) {
            primaryBtn.setText(text);
            for (java.awt.event.ActionListener l : primaryBtn.getActionListeners()) primaryBtn.removeActionListener(l);
            primaryBtn.addActionListener(action);
        }
    }
}