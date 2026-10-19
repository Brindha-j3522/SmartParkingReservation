
package com.smartparking;

import com.smartparking.model.Reservation;
import com.smartparking.repository.ReservationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {

    private final ReservationRepository reservationRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public HomeController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    private boolean isValidSlot(String slot) {
        return slot != null && slot.matches("A([1-9]|1[0-5])");
    }

    private boolean isValidVehicleType(String vehicleType) {
        return vehicleType != null
                && (vehicleType.equals("2 Wheeler")
                || vehicleType.equals("3 Wheeler")
                || vehicleType.equals("4 Wheeler"));
    }

    private double calculateAmount(String vehicleType, int parkingHours) {
        double rate;

        switch (vehicleType) {
            case "2 Wheeler" -> rate = 10;
            case "3 Wheeler" -> rate = 20;
            case "4 Wheeler" -> rate = 30;
            default -> throw new IllegalArgumentException(
                    "Invalid vehicle category");
        }

        return rate * parkingHours;
    }

    private String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // A reservation occupies its slot only until endTime.
    private boolean isActive(Reservation reservation) {
        return reservation.getEndTime() != null
                && reservation.getEndTime().isAfter(LocalDateTime.now());
    }

    private Reservation getActiveReservation(String slot) {
        return reservationRepository.findAll().stream()
                .filter(r -> slot.equals(r.getSlot()))
                .filter(this::isActive)
                .max(Comparator.comparing(
                        Reservation::getStartTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    private String pageStyle() {
        return """
                <style>
                    * { box-sizing: border-box; }
                    body {
                        font-family: Arial, sans-serif;
                        background: #f1f5f9;
                        margin: 0;
                        padding: 25px;
                        color: #1e293b;
                    }
                    h1 { color: #0f766e; text-align: center; }
                    .container {
                        max-width: 1100px;
                        margin: 20px auto;
                        background: white;
                        padding: 25px;
                        border-radius: 14px;
                        box-shadow: 0 5px 20px #cbd5e1;
                    }
                    a, button {
                        display: inline-block;
                        padding: 10px 15px;
                        background: #0f766e;
                        color: white;
                        text-decoration: none;
                        border: 0;
                        border-radius: 7px;
                        cursor: pointer;
                        margin: 5px;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns:
                            repeat(auto-fit, minmax(140px, 1fr));
                        gap: 14px;
                        margin: 25px 0;
                    }
                    .slot {
                        padding: 16px;
                        border-radius: 10px;
                        text-align: center;
                    }
                    .available {
                        background: #dcfce7;
                        border: 2px solid #16a34a;
                    }
                    .reserved {
                        background: #fee2e2;
                        border: 2px solid #dc2626;
                    }
                    .cards {
                        display: grid;
                        grid-template-columns:
                            repeat(auto-fit, minmax(180px, 1fr));
                        gap: 15px;
                    }
                    .metric {
                        background: #f0fdfa;
                        border: 1px solid #99f6e4;
                        padding: 20px;
                        border-radius: 10px;
                    }
                    .metric h2 { color: #0f766e; }
                    .table-wrap { overflow-x: auto; }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 15px;
                    }
                    th, td {
                        padding: 11px;
                        border-bottom: 1px solid #e2e8f0;
                        text-align: left;
                        white-space: nowrap;
                    }
                    th { background: #ccfbf1; }
                    label {
                        display: block;
                        margin-top: 14px;
                        font-weight: bold;
                    }
                    input, select {
                        width: 100%;
                        padding: 11px;
                        margin-top: 6px;
                        border: 1px solid #cbd5e1;
                        border-radius: 7px;
                    }
                    .form { max-width: 460px; margin: auto; }
                    .muted { color: #64748b; }
                    @media(max-width: 500px) {
                        body { padding: 12px; }
                        .container { padding: 15px; }
                    }
                </style>
                """;
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return """
                <!DOCTYPE html>
                <html><head><title>Smart Parking</title>%s</head>
                <body>
                    <div class="container" style="text-align:center">
                        <h1>Smart Parking System</h1>
                        <p>Book your parking slot easily.</p>
                        <a href="/slots">View Parking Slots</a>
                        <a href="/reserve">Reserve a Slot</a>
                        <a href="/admin">Admin Dashboard</a>
                    </div>
                </body></html>
                """.formatted(pageStyle());
    }

    @GetMapping("/slots")
    @ResponseBody
    public String slots() {
        StringBuilder cards = new StringBuilder();
        int available = 0;
        int reserved = 0;

        for (int i = 1; i <= 15; i++) {
            String slot = "A" + i;
            Reservation booking = getActiveReservation(slot);

            if (booking == null) {
                available++;
                cards.append("""
                        <div class="slot available">
                            <h3>%s</h3>
                            <p>Available</p>
                            <a href="/reserve">Book Now</a>
                        </div>
                        """.formatted(slot));
            } else {
                reserved++;
                cards.append("""
                        <div class="slot reserved">
                            <h3>%s</h3>
                            <p>Reserved</p>
                            <p>Until: %s</p>
                        </div>
                        """.formatted(
                        slot,
                        booking.getEndTime().format(FORMATTER)));
            }
        }

        return """
                <!DOCTYPE html>
                <html><head><title>Parking Slots</title>%s</head>
                <body>
                    <div class="container">
                        <h1>Parking Slot Availability</h1>
                        <h3 style="text-align:center">
                            Available: %d | Reserved: %d | Total: 15
                        </h3>
                        <div class="grid">%s</div>
                        <a href="/">Home</a>
                        <a href="/reserve">Reserve</a>
                        <a href="/admin">Admin Dashboard</a>
                    </div>
                </body></html>
                """.formatted(pageStyle(), available, reserved,
                cards.toString());
    }

    @GetMapping("/reserve")
    @ResponseBody
    public String reserve() {
        StringBuilder options = new StringBuilder();

        for (int i = 1; i <= 15; i++) {
            String slot = "A" + i;

            if (getActiveReservation(slot) == null) {
                options.append("""
                        <option value="%s">%s</option>
                        """.formatted(slot, slot));
            }
        }

        if (options.isEmpty()) {
            return """
                    <!DOCTYPE html>
                    <html><head><title>No Slots</title>%s</head>
                    <body><div class="container">
                        <h1>All slots are currently reserved!</h1>
                        <p>Please check again after a reservation expires.</p>
                        <a href="/slots">View Slots</a>
                        <a href="/">Home</a>
                    </div></body></html>
                    """.formatted(pageStyle());
        }

        return """
                <!DOCTYPE html>
                <html><head><title>Reserve Parking</title>%s</head>
                <body>
                    <div class="container">
                        <h1>Reserve Your Slot</h1>
                        <form class="form" action="/book" method="post">
                            <label>Your Name</label>
                            <input name="name" maxlength="100" required>

                            <label>Vehicle Number</label>
                            <input name="vehicle" maxlength="20" required>

                            <label>Vehicle Category</label>
                            <select id="vehicleType" name="vehicleType" required>
                                <option value="">Select category</option>
                                <option value="2 Wheeler">2 Wheeler</option>
                                <option value="3 Wheeler">3 Wheeler</option>
                                <option value="4 Wheeler">4 Wheeler</option>
                            </select>

                            <label>Parking Duration</label>
                            <select id="parkingHours" name="parkingHours" required>
                                <option value="">Select hours</option>
                                <option value="1">1 Hour</option>
                                <option value="2">2 Hours</option>
                                <option value="3">3 Hours</option>
                                <option value="4">4 Hours</option>
                                <option value="5">5 Hours</option>
                                <option value="6">6 Hours</option>
                                <option value="7">7 Hours</option>
                                <option value="8">8 Hours</option>
                                <option value="9">9 Hours</option>
                                <option value="10">10 Hours</option>
                                <option value="11">11 Hours</option>
                                <option value="12">12 Hours</option>
                            </select>

                            <p class="metric">
                                Estimated Fee: ₹<strong id="amount">0.00</strong>
                            </p>

                            <label>Parking Slot</label>
                            <select name="slot" required>%s</select>

                            <button type="submit">Confirm Reservation</button>
                        </form>
                        <p style="text-align:center">
                            <a href="/slots">View Slots</a>
                            <a href="/">Home</a>
                        </p>
                    </div>
                    <script>
                        function updateFee() {
                            const rates = {
                                '2 Wheeler': 10,
                                '3 Wheeler': 20,
                                '4 Wheeler': 30
                            };
                            const type =
                                document.getElementById('vehicleType').value;
                            const hours = Number(
                                document.getElementById('parkingHours').value
                            ) || 0;
                            document.getElementById('amount').textContent =
                                ((rates[type] || 0) * hours).toFixed(2);
                        }
                        document.getElementById('vehicleType')
                            .addEventListener('change', updateFee);
                        document.getElementById('parkingHours')
                            .addEventListener('change', updateFee);
                    </script>
                </body></html>
                """.formatted(pageStyle(), options.toString());
    }

    @PostMapping("/book")
    @ResponseBody
    public String book(
            @RequestParam String name,
            @RequestParam String vehicle,
            @RequestParam String vehicleType,
            @RequestParam int parkingHours,
            @RequestParam String slot) {

        if (name.isBlank() || vehicle.isBlank()
                || !isValidVehicleType(vehicleType)
                || parkingHours < 1 || parkingHours > 12
                || !isValidSlot(slot)) {
            return """
                    <!DOCTYPE html>
                    <html><body>
                        <h1>Invalid details</h1>
                        <a href="/reserve">Try Again</a>
                    </body></html>
                    """;
        }

        name = name.trim();
        vehicle = vehicle.trim();

        // Recheck availability when the form is submitted.
        if (getActiveReservation(slot) != null) {
            return """
                    <!DOCTYPE html>
                    <html><head>%s</head><body>
                    <div class="container">
                        <h1>Slot Already Reserved!</h1>
                        <p>Please choose another available slot.</p>
                        <a href="/slots">View Slots</a>
                        <a href="/reserve">Try Again</a>
                    </div></body></html>
                    """.formatted(pageStyle());
        }

        double amount = calculateAmount(vehicleType, parkingHours);

        Reservation reservation = new Reservation(
                name, vehicle, vehicleType, slot, parkingHours, amount);

        reservationRepository.save(reservation);

        return """
                <!DOCTYPE html>
                <html><head><title>Booking Successful</title>%s</head>
                <body><div class="container" style="text-align:center">
                    <h1>Reservation Successful!</h1>
                    <p>Thank you, %s!</p>
                    <p>Vehicle Number: %s</p>
                    <p>Vehicle Category: %s</p>
                    <p>Parking Duration: %d hour(s)</p>
                    <p>Parking Slot: %s</p>
                    <p>Start Time: %s</p>
                    <p>End Time: %s</p>
                    <h2>Total Fee: ₹%.2f</h2>
                    <a href="/slots">View Slots</a>
                    <a href="/admin">Admin Dashboard</a>
                    <a href="/">Home</a>
                </div></body></html>
                """.formatted(
                pageStyle(),
                escapeHtml(name),
                escapeHtml(vehicle),
                escapeHtml(vehicleType),
                parkingHours,
                escapeHtml(slot),
                reservation.getStartTime().format(FORMATTER),
                reservation.getEndTime().format(FORMATTER),
                amount);
    }

    @GetMapping("/admin")
    @ResponseBody
    public String adminDashboard() {
        List<Reservation> allBookings = reservationRepository.findAll();

        int activeCount = 0;
        double totalRevenue = 0;

        for (Reservation booking : allBookings) {
            if (isActive(booking)) {
                activeCount++;
            }
            if (booking.getAmount() != 0) {
                totalRevenue += booking.getAmount();
            }
        }

        int availableCount = 15 - activeCount;

        StringBuilder history = new StringBuilder();

        allBookings.stream()
                .sorted(Comparator.comparing(
                        Reservation::getStartTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .forEach(booking -> {
                    boolean active = isActive(booking);

                    String start = booking.getStartTime() == null
                            ? "N/A"
                            : booking.getStartTime().format(FORMATTER);

                    String end = booking.getEndTime() == null
                            ? "N/A"
                            : booking.getEndTime().format(FORMATTER);

                    String status = active ? "Reserved" : "Expired";

                    history.append("""
                            <tr>
                                <td>%s</td>
                                <td>%s</td>
                                <td>%s</td>
                                <td>%s</td>
                                <td>%s</td>
                                <td>%s</td>
                                <td>₹%.2f</td>
                                <td>%s</td>
                            </tr>
                            """.formatted(
                            escapeHtml(booking.getName()),
                            escapeHtml(booking.getVehicle()),
                            escapeHtml(booking.getVehicleType()),
                            escapeHtml(booking.getSlot()),
                            start,
                            end,
                            booking.getAmount(),
                            status));
                });

        if (history.isEmpty()) {
            history.append("""
                    <tr><td colspan="8">No bookings yet.</td></tr>
                    """);
        }

        return """
                <!DOCTYPE html>
                <html><head><title>Admin Dashboard</title>%s</head>
                <body>
                    <div class="container">
                        <h1>Smart Parking Admin Dashboard</h1>

                        <div class="cards">
                            <div class="metric">
                                <p>Total Parking Slots</p><h2>15</h2>
                            </div>
                            <div class="metric">
                                <p>Available Slots</p><h2>%d</h2>
                            </div>
                            <div class="metric">
                                <p>Currently Reserved</p><h2>%d</h2>
                            </div>
                            <div class="metric">
                                <p>Total Recorded Revenue</p>
                                <h2>₹%.2f</h2>
                            </div>
                        </div>

                        <h2>Booking History</h2>
                        <p class="muted">
                            Expired bookings remain here for record keeping.
                        </p>
                        <div class="table-wrap">
                            <table>
                                <thead><tr>
                                    <th>Name</th>
                                    <th>Vehicle</th>
                                    <th>Category</th>
                                    <th>Slot</th>
                                    <th>Start</th>
                                    <th>End</th>
                                    <th>Fee</th>
                                    <th>Status</th>
                                </tr></thead>
                                <tbody>%s</tbody>
                            </table>
                        </div>
                        <a href="/">Home</a>
                        <a href="/slots">View Slots</a>
                        <a href="/reserve">New Booking</a>
                    </div>
                </body></html>
                """.formatted(
                pageStyle(),
                availableCount,
                activeCount,
                totalRevenue,
                history.toString());
    }
}
