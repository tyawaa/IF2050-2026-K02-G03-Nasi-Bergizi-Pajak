package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class Notification {
    private final String type;
    private final String message;
    private final String severity;
    private final LocalDate date;

    public Notification(String type, String message, String severity) {
        this.type = type;
        this.message = message;
        this.severity = severity;
        this.date = LocalDate.now();
    }

    public Notification(String type, String message, String severity, LocalDate date) {
        this.type = type;
        this.message = message;
        this.severity = severity;
        this.date = date;
    }

    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return "[" + severity.toUpperCase() + "] " + type + ": " + message;
    }
}
