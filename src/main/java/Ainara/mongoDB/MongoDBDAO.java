package Ainara.mongoDB;

import DAO.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;


/**
 * MongoDB Data Access Object (DAO) implementation for handling MongoDB database operations.
 */
public class MongoDBDAO implements Closeable, IDAO {
    static MongoClient mongoClient;
    static MongoDatabase db;

    /**
     * Initializes the MongoDB connection with a specified connection string.
     */
    public MongoDBDAO() {
        String connectionString = "mongodb+srv://admin:0000@cluster0.0pqj1.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("GroupProject");
    }

    /**
     * Closes the MongoDB connection.
     */
    @Override
    public void close() {
        mongoClient.close();
    }

    /**
     * Adds a new department to the MongoDB collection.
     * @param department The department to be added.
     * @return true if the operation is successful.
     */
    @Override
    public boolean addDepartment(Department department) {
        // todo se duplica si hay exactamente los mismos datos? se comprueba que existe?
        MongoCollection<Document> departmentsCollection = db.getCollection("Department");
        Document document = new Document()
                .append("_id", department.getId())
                .append("name", department.getName())
                .append("location", department.getLocation());
        BsonValue id = departmentsCollection.insertOne(document).getInsertedId();
        return id != null;
    }

    /**
     * Adds a new employee to the MongoDB collection.
     * @param employee The employee to be added.
     */
    @Override
    public void addEmployee(Employee employee) {
        MongoCollection<Document> employeesCollection = db.getCollection("Employee");
        Document document = new Document()
                .append("_id", getLastEmployeeId() + 1)
                .append("name", employee.getName())
                .append("job", employee.getJob())
                .append("department_id", employee.getDepartment().getId())
                ;
        employeesCollection.insertOne(document);
    }

    /**
     * Retrieves all employees from the MongoDB collection.
     * @return A list of all employees.
     */
    @Override
    public List<Employee> findAllEmployees() {
        MongoCollection<Document> employeesCollection = db.getCollection("Employee");
        List<Employee> employees = new ArrayList<>();
        try (MongoCursor<Document> cursor = employeesCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                employees.add(toEmployee(document));
            }
        }
        return employees;
    }

    /**
     * Finds an employee by their ID.
     * @param id The ID of the employee.
     * @return The Employee object if found, otherwise null.
     */
    @Override
    public Employee findEmployeeById(Object id) {
        MongoCollection<Document> departmentsCollection = db.getCollection("Employee");
        try (MongoCursor<Document> cursor = departmentsCollection.find(Filters.eq("_id", id)).iterator()) {
            if (cursor.hasNext()) {
                Document document = cursor.next();
                return toEmployee(document);
            }
        }
        return null;
    }

    /**
     * Updates an employee's information in the MongoDB collection.
     * @return The updated Employee object if successful, otherwise null.
     */
    @Override
    public Employee updateEmployee(Object employeeObject) {
        if (!(employeeObject instanceof Employee employee)){
            return null;
        }
        Bson updates;
        System.out.println("Seleccione el campo a actualizar:");
        System.out.println("1. Nombre");
        System.out.println("2. Localidad");
        System.out.println("3. Departamento");
        int choice = Utils.Ask.askForNumber();
        switch (choice) {
            case 1:
                System.out.println("Ingrese el nuevo nombre: ");
                String name = Utils.Ask.askForString();
                updates = Updates.set("name", name);
                break;
            case 2:
                System.out.println("Ingrese el nuevo puesto: ");
                String job = Utils.Ask.askForString();
                updates = Updates.set("job", job);
                break;
            case 3:
                System.out.println("Ingrese el departamento: ");
                int departmentId = Utils.Ask.askForNumber();
                if (findDepartmentById(departmentId) != null) {
                    updates = Updates.set("department_id", departmentId);
                } else {
                    System.out.println("No existe el departamento.");
                    return null;
                }
                break;
            default:
                System.out.println("Opción no valida.");
                return null;
        }
        MongoCollection<Document> employeesCollection = db.getCollection("Employee");
        Bson query = Filters.eq("_id", employee.getId());
        Document updatedEmployee = employeesCollection.findOneAndUpdate(query, updates);
        return updatedEmployee != null ? toEmployee(updatedEmployee) : null;
    }

    /**
     * Deletes an employee from the MongoDB collection.
     * @param id The ID of the employee to be deleted.
     * @return true if the employee was deleted, otherwise false.
     */
    @Override
    public boolean deleteEmployee(Object id) {
        MongoCollection<Document> employeesCollection = db.getCollection("Employee");
        Document employee = employeesCollection.findOneAndDelete(Filters.eq("_id", id));
        return employee != null;
    }

    /**
     * Retrieves all departments from the MongoDB collection.
     * @return A list of all departments.
     */
    @Override
    public List<Department> findAllDepartments() {
        MongoCollection<Document> departmentsCollection = db.getCollection("Department");
        List<Department> departments = new ArrayList<>();
        try (MongoCursor<Document> cursor = departmentsCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                departments.add(toDepartment(document));
            }
        }
        return departments;
    }

    /**
     * Finds a department by its ID.
     * @param id The ID of the department.
     * @return The Department object if found, otherwise null.
     */
    @Override
    public Department findDepartmentById(Object id) {
        MongoCollection<Document> departmentsCollections = db.getCollection("Department");
        Document departmentDocument = departmentsCollections.find(Filters.eq("_id", id)).first();
        return departmentDocument != null ? toDepartment(departmentDocument) : null;
    }

    /**
     * Updates a department's information in the MongoDB collection.
     * @param departmentObject The department object with updated data.
     * @return The updated Department object if successful, otherwise null.
     */
    @Override
    public Department updateDepartment(Object departmentObject) {
        if (!(departmentObject instanceof Department department)) {
            return null;
        }
        System.out.println("Seleccione el campo a actualizar:");
        System.out.println("1. Nombre");
        System.out.println("2. Localizacion");
        int option = Utils.Ask.askForNumber();
        Bson updates;
        switch (option){
            case 1:
                System.out.println("Ingrese el nuevo nombre: ");
                String newName = Utils.Ask.askForString();
                department.setName(newName);
                updates = Updates.set("name", newName);
                break;
            case 2:
                System.out.println("Ingrese la nueva localizacion: ");
                String newLocation = Utils.Ask.askForString();
                department.setLocation(newLocation);
                updates = Updates.set("location", newLocation);
                break;
            default:
                System.out.println("Opción no valida");
                return null;

        }
        MongoCollection<Document> departmentsCollection = db.getCollection("Department");
        Bson query = Filters.eq("_id", department.getId());
        Document updatedDepartment = departmentsCollection.findOneAndUpdate(query, updates);
        return updatedDepartment != null ? toDepartment(updatedDepartment) : null;
    }


    /**
     * Deletes a department from the MongoDB collection.
     * @param id The ID of the department to be deleted.
     * @return The deleted Department object if successful, otherwise null.
     */
    @Override
    public Department deleteDepartment(Object id) {
        MongoCollection<Document> departmentsCollection = db.getCollection("Department");
        Document department = departmentsCollection.findOneAndDelete(Filters.eq("_id", id));
        return department != null ? toDepartment(department) : null;
    }

    /**
     * Finds employees by their department ID.
     * @param idDept The ID of the department.
     * @return A list of employees belonging to the department.
     */
    @Override
    public List<Employee> findEmployeesByDept(Object idDept) {
        MongoCollection<Document> employeesCollection = db.getCollection("Employee");
        List<Employee> employees = new ArrayList<>();
        Bson query = Filters.eq("department_id",idDept);
        try (MongoCursor<Document> cursor = employeesCollection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                employees.add(toEmployee(document));
            }
        }
        return employees;
    }


    /**
     * Converts a MongoDB Document into a Department object.
     * @param document The MongoDB Document.
     * @return A Department object.
     */
    private Department toDepartment(Document document) {
        return new Department(
                document.getInteger("_id"),
                document.getString("name"),
                document.getString("location")
        );
    }

    /**
     * Converts a MongoDB Document into an Employee object.
     * @param document The MongoDB Document.
     * @return An Employee object.
     */
    private Employee toEmployee(Document document) {
        return new Employee(
                document.getInteger("_id"),
                document.getString("name"),
                document.getString("job"),
                findDepartmentById(document.getInteger("department_id"))
        );
    }

    private int getLastEmployeeId() {
        List<Employee> employees = findAllEmployees();
        if (employees == null || employees.isEmpty()) return -1;
        employees.sort(Comparator.comparingInt(Employee::getId));
        return employees.getLast().getId();
    }
}