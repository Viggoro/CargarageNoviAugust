package com.garage.config;

import com.garage.model.*;
import com.garage.model.enums.PaymentStatus;
import com.garage.model.enums.RepairStatus;
import com.garage.model.enums.Role;
import com.garage.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final double VAT_RATE = 0.21;

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;
    private final PartRepository partRepository;
    private final StockRepository stockRepository;
    private final RepairActionRepository repairActionRepository;
    private final RepairRepository repairRepository;
    private final InspectionRepository inspectionRepository;
    private final ReceiptRepository receiptRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        if (employeeRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }

        log.info("Initializing garage database with sample data...");

        List<Employee> employees = initializeEmployees();
        List<Customer> customers = initializeCustomers();
        List<Car> cars = initializeCars(customers);
        List<Part> parts = initializeParts();
        initializeStock(parts);
        List<RepairAction> repairActions = initializeRepairActions();
        initializeSampleWorkOrders(employees, cars, parts, repairActions);
        initializeAuditLogs(employees);

        log.info("Database initialization completed successfully.");
        log.info("Sample logins: admin/admin123 | backoffice.jane/jane123 | mechanic.john/john123 | cashier.sarah/sarah123");
    }

    private List<Employee> initializeEmployees() {
        List<Employee> employees = new ArrayList<>();

        AdminEmployee admin = new AdminEmployee();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@cargarage.com");
        admin.setName("Admin User");
        admin.setRole(Role.ADMIN);
        admin.setIsActive(true);
        employees.add(admin);

        BackOfficeEmployee backOffice = new BackOfficeEmployee();
        backOffice.setUsername("backoffice.jane");
        backOffice.setPasswordHash(passwordEncoder.encode("jane123"));
        backOffice.setEmail("jane@cargarage.com");
        backOffice.setName("Jane Backoffice");
        backOffice.setRole(Role.BACK_OFFICE);
        backOffice.setIsActive(true);
        employees.add(backOffice);

        Mechanic mechanicJohn = new Mechanic();
        mechanicJohn.setUsername("mechanic.john");
        mechanicJohn.setPasswordHash(passwordEncoder.encode("john123"));
        mechanicJohn.setEmail("john@cargarage.com");
        mechanicJohn.setName("John Mechanic");
        mechanicJohn.setRole(Role.MECHANIC);
        mechanicJohn.setIsActive(true);
        employees.add(mechanicJohn);

        Mechanic mechanicMike = new Mechanic();
        mechanicMike.setUsername("mechanic.mike");
        mechanicMike.setPasswordHash(passwordEncoder.encode("mike123"));
        mechanicMike.setEmail("mike@cargarage.com");
        mechanicMike.setName("Mike Mechanic");
        mechanicMike.setRole(Role.MECHANIC);
        mechanicMike.setIsActive(true);
        employees.add(mechanicMike);

        Cashier cashierSarah = new Cashier();
        cashierSarah.setUsername("cashier.sarah");
        cashierSarah.setPasswordHash(passwordEncoder.encode("sarah123"));
        cashierSarah.setEmail("sarah@cargarage.com");
        cashierSarah.setName("Sarah Cashier");
        cashierSarah.setRole(Role.CASHIER);
        cashierSarah.setIsActive(true);
        employees.add(cashierSarah);

        Cashier cashierLisa = new Cashier();
        cashierLisa.setUsername("cashier.lisa");
        cashierLisa.setPasswordHash(passwordEncoder.encode("lisa123"));
        cashierLisa.setEmail("lisa@cargarage.com");
        cashierLisa.setName("Lisa Cashier");
        cashierLisa.setRole(Role.CASHIER);
        cashierLisa.setIsActive(true);
        employees.add(cashierLisa);

        return employeeRepository.saveAll(employees);
    }

    private List<Customer> initializeCustomers() {
        List<Customer> customers = new ArrayList<>();

        customers.add(createCustomer("Jan", "Jansen", "Hoofdstraat 1, 1000 AA Amsterdam", "+31 6 12345678", "jan.jansen@email.com"));
        customers.add(createCustomer("Piet", "Pietersen", "Kerkstraat 15, 2000 BB Rotterdam", "+31 6 23456789", "piet.pietersen@email.com"));
        customers.add(createCustomer("Anna", "Bakker", "Molenweg 42, 3000 CC Den Haag", "+31 6 34567890", "anna.bakker@email.com"));
        customers.add(createCustomer("Klaas", "Visser", "Dijkstraat 7, 4000 DD Utrecht", "+31 6 45678901", "klaas.visser@email.com"));
        customers.add(createCustomer("Maria", "Smit", "Bosweg 23, 5000 EE Eindhoven", "+31 6 56789012", "maria.smit@email.com"));
        customers.add(createCustomer("Henk", "Mulder", "Schoolstraat 8, 6000 FF Tilburg", "+31 6 67890123", "henk.mulder@email.com"));
        customers.add(createCustomer("Liesbeth", "De Boer", "Stationsweg 12, 7000 GG Groningen", "+31 6 78901234", "liesbeth.deboer@email.com"));
        customers.add(createCustomer("Gerard", "Meijer", "Kanaalstraat 33, 8000 HH Almere", "+31 6 89012345", "gerard.meijer@email.com"));
        customers.add(createCustomer("Petra", "Bos", "Parklaan 5, 9000 II Breda", "+31 6 90123456", "petra.bos@email.com"));
        customers.add(createCustomer("Frank", "Vos", "Industrieweg 18, 6500 JJ Nijmegen", "+31 6 01234567", "frank.vos@email.com"));

        return customerRepository.saveAll(customers);
    }

    private Customer createCustomer(String firstName, String lastName, String address, String phone, String email) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAddress(address);
        customer.setPhoneNumber(phone);
        customer.setEmail(email);
        return customer;
    }

    private List<Car> initializeCars(List<Customer> customers) {
        List<Car> cars = new ArrayList<>();

        cars.add(createCar("AB-123-C", "Toyota", "Corolla", 2020, "1HGBH41JXMN109186", customers.get(0)));
        cars.add(createCar("CD-456-D", "Honda", "Civic", 2019, "2T1BURHE0JC123456", customers.get(0)));
        cars.add(createCar("EF-789-E", "Volkswagen", "Golf", 2021, "3VWDX7AJ5DM123456", customers.get(1)));
        cars.add(createCar("GH-012-F", "BMW", "3 Series", 2018, "4T1B11HK5JU123456", customers.get(2)));
        cars.add(createCar("IJ-345-G", "Mercedes", "C-Class", 2022, "5NPE34AF4FH123456", customers.get(2)));
        cars.add(createCar("KL-678-H", "Audi", "A4", 2020, "6G1ZT51806L123456", customers.get(3)));
        cars.add(createCar("MN-901-I", "Ford", "Focus", 2019, "7FARW2H84BE123456", customers.get(4)));
        cars.add(createCar("OP-234-J", "Opel", "Astra", 2021, "8XJDF4G25NA123456", customers.get(4)));
        cars.add(createCar("QR-567-K", "Peugeot", "308", 2018, "9BWDE21J924123456", customers.get(5)));
        cars.add(createCar("ST-890-L", "Renault", "Clio", 2020, "1ZVBP8CF4E5123456", customers.get(6)));
        cars.add(createCar("UV-123-M", "Fiat", "500", 2021, "2T2ZZMCA5CC123456", customers.get(7)));
        cars.add(createCar("WX-456-N", "Skoda", "Octavia", 2019, "3VWLL7AJ5BM123456", customers.get(8)));

        return carRepository.saveAll(cars);
    }

    private Car createCar(String licensePlate, String brand, String model, int year, String vin, Customer owner) {
        Car car = new Car();
        car.setLicensePlate(licensePlate);
        car.setBrand(brand);
        car.setModel(model);
        car.setYear(year);
        car.setVin(vin);
        car.setOwner(owner);
        return car;
    }

    private List<Part> initializeParts() {
        List<Part> parts = new ArrayList<>();

        parts.add(createPart("Oil Filter", 12.50, 50));
        parts.add(createPart("Air Filter", 8.75, 40));
        parts.add(createPart("Fuel Filter", 15.25, 30));
        parts.add(createPart("Spark Plugs (Set of 4)", 24.99, 25));
        parts.add(createPart("Timing Belt", 89.99, 15));
        parts.add(createPart("Water Pump", 45.50, 20));
        parts.add(createPart("Brake Pads (Front)", 35.99, 30));
        parts.add(createPart("Brake Pads (Rear)", 32.50, 30));
        parts.add(createPart("Brake Discs (Front)", 68.75, 20));
        parts.add(createPart("Brake Discs (Rear)", 62.25, 20));
        parts.add(createPart("Brake Fluid", 8.99, 40));
        parts.add(createPart("Shock Absorbers (Front)", 89.99, 15));
        parts.add(createPart("Shock Absorbers (Rear)", 79.99, 15));
        parts.add(createPart("Battery", 89.99, 20));
        parts.add(createPart("Alternator", 125.50, 10));
        parts.add(createPart("Starter Motor", 95.75, 8));
        parts.add(createPart("Clutch Kit", 189.99, 8));
        parts.add(createPart("Radiator", 145.99, 10));
        parts.add(createPart("Catalytic Converter", 299.99, 5));
        parts.add(createPart("Cabin Air Filter", 14.99, 35));

        return partRepository.saveAll(parts);
    }

    private Part createPart(String name, double price, int stock) {
        Part part = new Part();
        part.setName(name);
        part.setPrice(price);
        part.setStock(stock);
        return part;
    }

    private void initializeStock(List<Part> parts) {
        for (Part part : parts) {
            Stock stock = new Stock();
            stock.setPart(part);
            stock.setQuantity(part.getStock());
            stock.setUpdatedAt(LocalDateTime.now());
            stockRepository.save(stock);
        }
    }

    private List<RepairAction> initializeRepairActions() {
        List<RepairAction> actions = new ArrayList<>();

        actions.add(createRepairAction("Oil Change", 45.00));
        actions.add(createRepairAction("Air Filter Replacement", 25.00));
        actions.add(createRepairAction("Fuel Filter Replacement", 35.00));
        actions.add(createRepairAction("Spark Plug Replacement", 40.00));
        actions.add(createRepairAction("Brake Pad Replacement (Front)", 120.00));
        actions.add(createRepairAction("Brake Pad Replacement (Rear)", 110.00));
        actions.add(createRepairAction("Brake Disc Replacement (Front)", 180.00));
        actions.add(createRepairAction("Brake Disc Replacement (Rear)", 170.00));
        actions.add(createRepairAction("Brake Fluid Change", 60.00));
        actions.add(createRepairAction("Shock Absorber Replacement (Front)", 200.00));
        actions.add(createRepairAction("Battery Replacement", 30.00));
        actions.add(createRepairAction("Alternator Replacement", 150.00));
        actions.add(createRepairAction("Clutch Replacement", 350.00));
        actions.add(createRepairAction("Radiator Replacement", 200.00));
        actions.add(createRepairAction("Safety Inspection", 50.00));
        actions.add(createRepairAction("Emissions Test", 40.00));
        actions.add(createRepairAction("Computer Diagnostics", 35.00));
        actions.add(createRepairAction("Wheel Alignment", 65.00));
        actions.add(createRepairAction("Tire Rotation", 30.00));
        actions.add(createRepairAction("Cabin Air Filter Replacement", 25.00));

        return repairActionRepository.saveAll(actions);
    }

    private RepairAction createRepairAction(String name, double price) {
        RepairAction action = new RepairAction();
        action.setName(name);
        action.setPrice(price);
        return action;
    }

    private void initializeSampleWorkOrders(List<Employee> employees, List<Car> cars,
                                            List<Part> parts, List<RepairAction> repairActions) {
        LocalDateTime now = LocalDateTime.now();

        Mechanic mechanicJohn = employees.stream()
                .filter(e -> e instanceof Mechanic && "mechanic.john".equals(e.getUsername()))
                .map(e -> (Mechanic) e)
                .findFirst()
                .orElseThrow();

        Mechanic mechanicMike = employees.stream()
                .filter(e -> e instanceof Mechanic && "mechanic.mike".equals(e.getUsername()))
                .map(e -> (Mechanic) e)
                .findFirst()
                .orElseThrow();

        Repair inProgressRepair = new Repair();
        inProgressRepair.setStatus(RepairStatus.IN_PROGRESS);
        inProgressRepair.setScheduledAt(now.minusDays(2));
        inProgressRepair.setPerformedBy(mechanicJohn);
        inProgressRepair.setCar(cars.get(0));
        inProgressRepair.addAction(repairActions.get(0));
        inProgressRepair.addAction(repairActions.get(1));
        inProgressRepair.addPart(parts.get(0), 1);
        inProgressRepair.addPart(parts.get(1), 1);
        inProgressRepair.calculateTotalCost();
        repairRepository.save(inProgressRepair);

        Repair completedRepair = new Repair();
        completedRepair.setStatus(RepairStatus.COMPLETED);
        completedRepair.setScheduledAt(now.minusDays(5));
        completedRepair.setCompletedAt(now.minusDays(4));
        completedRepair.setPerformedBy(mechanicMike);
        completedRepair.setCar(cars.get(2));
        completedRepair.addAction(repairActions.get(4));
        completedRepair.addAction(repairActions.get(6));
        completedRepair.addPart(parts.get(6), 1);
        completedRepair.addPart(parts.get(8), 1);
        completedRepair.calculateTotalCost();
        completedRepair = repairRepository.save(completedRepair);

        CustomRepairAction customAction = new CustomRepairAction();
        customAction.setDescription("Extra rust treatment under chassis");
        customAction.setPrice(75.00);
        completedRepair.addCustomAction(customAction);
        completedRepair.calculateTotalCost();
        completedRepair = repairRepository.save(completedRepair);

        Inspection passedInspection = new Inspection();
        passedInspection.setStatus(RepairStatus.COMPLETED);
        passedInspection.setScheduledAt(now.minusDays(1));
        passedInspection.setCompletedAt(now.minusHours(6));
        passedInspection.setPerformedBy(mechanicJohn);
        passedInspection.setCar(cars.get(1));
        passedInspection.setFindings("Regular safety inspection completed. All systems functioning properly.");
        passedInspection.setInspectionResult("PASSED");
        passedInspection.setApproved(true);
        passedInspection.addDefect("Minor wear on brake pads");
        passedInspection = inspectionRepository.save(passedInspection);

        Inspection failedInspection = new Inspection();
        failedInspection.setStatus(RepairStatus.COMPLETED);
        failedInspection.setScheduledAt(now.minusDays(3));
        failedInspection.setCompletedAt(now.minusDays(3).plusHours(2));
        failedInspection.setPerformedBy(mechanicMike);
        failedInspection.setCar(cars.get(4));
        failedInspection.setFindings("Annual inspection with several issues found.");
        failedInspection.setInspectionResult("FAILED");
        failedInspection.setApproved(false);
        failedInspection.addDefect("Exhaust system needs replacement");
        failedInspection.addDefect("Suspension bushings worn");
        failedInspection.addDefect("Headlight alignment incorrect");
        inspectionRepository.save(failedInspection);

        Inspection scheduledInspection = new Inspection();
        scheduledInspection.setStatus(RepairStatus.SCHEDULED);
        scheduledInspection.setScheduledAt(now.plusDays(2));
        scheduledInspection.setPerformedBy(mechanicJohn);
        scheduledInspection.setCar(cars.get(3));
        inspectionRepository.save(scheduledInspection);

        createPaidReceiptForRepair(completedRepair, "CARD", now.minusDays(4));
        createPaidReceiptForInspection(passedInspection, 50.00, "CASH", now.minusHours(5));
    }

    private void createPaidReceiptForRepair(Repair repair, String method, LocalDateTime paidAt) {
        double total = repair.getTotalCost();
        double vat = total * VAT_RATE;

        Payment payment = new Payment();
        payment.setAmount(total + vat);
        payment.setVatAmount(vat);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(paidAt);

        Receipt receipt = new Receipt();
        receipt.setRepair(repair);
        receipt.setTotal(total);
        receipt.setVat(vat);
        receipt.setPayment(payment);
        payment.setReceipt(receipt);

        receiptRepository.save(receipt);
    }

    private void createPaidReceiptForInspection(Inspection inspection, double fee, String method, LocalDateTime paidAt) {
        double vat = fee * VAT_RATE;

        Payment payment = new Payment();
        payment.setAmount(fee + vat);
        payment.setVatAmount(vat);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(paidAt);

        Receipt receipt = new Receipt();
        receipt.setInspection(inspection);
        receipt.setTotal(fee);
        receipt.setVat(vat);
        receipt.setPayment(payment);
        payment.setReceipt(receipt);

        receiptRepository.save(receipt);
    }

    private void initializeAuditLogs(List<Employee> employees) {
        Employee admin = employees.get(0);
        Employee mechanic = employees.stream()
                .filter(e -> e instanceof Mechanic)
                .findFirst()
                .orElse(admin);

        List<AuditLog> logs = new ArrayList<>();
        logs.add(createAuditLog("LOGIN", admin, "Employee", admin.getId(), "Admin logged in"));
        logs.add(createAuditLog("CREATE", admin, "Customer", 1L, "Created sample customers"));
        logs.add(createAuditLog("CREATE", admin, "Car", 1L, "Registered sample cars"));
        logs.add(createAuditLog("CREATE", admin, "Part", 1L, "Initialized inventory"));
        logs.add(createAuditLog("CREATE", mechanic, "Repair", 1L, "Started oil change repair"));
        logs.add(createAuditLog("COMPLETE", mechanic, "Repair", 2L, "Completed brake replacement"));
        logs.add(createAuditLog("CREATE", mechanic, "Inspection", 1L, "Completed safety inspection"));
        logs.add(createAuditLog("UPDATE", mechanic, "Inspection", 2L, "Inspection failed with defects"));

        auditLogRepository.saveAll(logs);
    }

    private AuditLog createAuditLog(String action, Employee performedBy, String entity, Long entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setPerformedBy(performedBy);
        auditLog.setEntity(entity);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        return auditLog;
    }
}
