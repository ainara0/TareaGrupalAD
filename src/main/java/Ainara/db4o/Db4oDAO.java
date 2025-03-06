package Ainara.db4o;

import DAO.Department;
import DAO.Employee;
import DAO.IDAO;
import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

import java.io.Closeable;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements the {@link IDAO} interface and provides database operations using Db4o.
 * It allows operations such as adding, updating, deleting, and retrieving {@link Employee} and {@link Department} objects.
 * Implements {@link Closeable} to ensure proper resource management.
 */
public class Db4oDAO implements Closeable, IDAO {

    /**
     * The Db4o object container for database operations.
     */
    private ObjectContainer container;

    /**
     * Constructor that initializes the database connection.
     */
    public Db4oDAO() {
        connectToDatabase();
    }

    /**
     * Retrieves the Db4o object container.
     *
     * @return the current ObjectContainer instance.
     */
    public ObjectContainer getContainer() {
        return container;
    }

    /**
     * Establishes a connection to the database.
     *
     * @return true if the connection is successful, false otherwise.
     */
    public boolean connectToDatabase() {
        try {
            container = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "GroupProject");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Closes the database connection.
     */
    @Override
    public void close() {
        container.close();
    }

    /**
     * Retrieves all employees from the database.
     *
     * @return a list of employees or null if no employees are found.
     */
    @Override
    public List<Employee> findAllEmployees() {
        ObjectSet<Employee> result = container.query(Employee.class);
        return result.stream().toList();
    }

    /**
     * Finds an employee by ID.
     *
     * @param id the employee ID.
     * @return the corresponding Employee object, or null if not found.
     */
    @Override
    public Employee findEmployeeById(Object id) {
        if (!isNumeric(id)) return null;
        ObjectSet<Employee> result = container.query(new Predicate<>() {
            public boolean match(Employee employee) {
                return employee.getId() == Integer.parseInt(id.toString());
            }
        });
        return result.getFirst();
    }

    /**
     * Adds an employee to the database.
     *
     * @param employee the employee object to add.
     */
    @Override
    public void addEmployee(Employee employee) {
        employee.setId(getLastEmployeeId() + 1);
        container.store(employee);
    }

    /**
     * Updates an existing employee record.
     *
     * @param employeeObject the employee object with updated information.
     * @return the updated Employee object, or null if the update fails.
     */
    @Override
    public Employee updateEmployee(Object employeeObject) {
        if (!(employeeObject instanceof Employee employee)) return null;
        try {
            container.store(employee);
        } catch (Exception e) {
            return null;
        }
        return employee;
    }

    /**
     * Deletes an employee by ID.
     *
     * @param id the employee ID.
     * @return true if deletion was successful, false otherwise.
     */
    @Override
    public boolean deleteEmployee(Object id) {
        Employee employee = findEmployeeById(id);
        if (employee == null) return false;
        try {
            container.delete(employee);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Retrieves all departments from the database.
     *
     * @return a list of departments or null if no departments are found.
     */
    @Override
    public List<Department> findAllDepartments() {
        ObjectSet<Department> result = container.query(Department.class);
        return result.stream().toList();
    }

    /**
     * Finds a department by ID.
     *
     * @param id the department ID.
     * @return the corresponding Department object, or null if not found.
     */
    @Override
    public Department findDepartmentById(Object id) {
        if (!isNumeric(id)) return null;
        ObjectSet<Department> result = container.query(new Predicate<>() {
            public boolean match(Department department) {
                return department.getId() == Integer.parseInt(id.toString());
            }
        });
        return result.getFirst();
    }

    /**
     * Adds a department to the database.
     *
     * @param department the department object to add.
     * @return true if the department was added successfully, false otherwise.
     */
    @Override
    public boolean addDepartment(Department department) {
        try {
            container.store(department);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Updates an existing department record.
     *
     * @param departmentObject the department object with updated information.
     * @return the updated Department object, or null if the update fails.
     */
    @Override
    public Department updateDepartment(Object departmentObject) {
        if (!(departmentObject instanceof Department department)) {
            return null;
        }


        System.out.println("¿Qué desea actualizar del departamento?");
        System.out.println("1. Nombre");
        System.out.println("2. Ubicación");
        System.out.println("3. Ambos");

        int opcion = Utils.Ask.askForNumber();
        switch (opcion) {
            case 1:
                System.out.println("Ingrese el nuevo nombre:");
                String nuevoNombre = Utils.Ask.askForString();
                department.setName(nuevoNombre);
                break;
            case 2:
                System.out.println("Ingrese la nueva ubicación:");
                String nuevaUbicacion = Utils.Ask.askForString();
                department.setLocation(nuevaUbicacion);
                break;
            default:
                System.out.println("Opción no válida.");
                return null;
        }

        try {
            container.store(department);
        } catch (Exception e) {
            return null;
        }

        return department;
    }


    /**
     * Deletes a department by ID.
     *
     * @param id the department ID.
     * @return the deleted Department object, or null if deletion fails.
     */
    @Override
    public Department deleteDepartment(Object id) {
        Department department = findDepartmentById(id);
        if (department == null) return null;
        try {
            container.delete(department);
        } catch (Exception e) {
            return null;
        }
        return department;
    }

    /**
     * Finds employees by department ID.
     *
     * @param idDept the department ID.
     * @return a list of employees in the specified department, or null if no employees are found.
     */
    @Override
    public List<Employee> findEmployeesByDept(Object idDept) {
        if (!isNumeric(idDept)) return null;
        ObjectSet<Employee> result = container.query(new Predicate<>() {
            public boolean match(Employee employee) {
                return employee.getDepartment().getId() == Integer.parseInt(idDept.toString());
            }
        });
        return result.stream().toList();
    }

    /**
     * Checks if a given object is a numeric value.
     *
     * @param num the object to check.
     * @return true if the object is numeric, false otherwise.
     */
    private static boolean isNumeric(Object num) {
        try {
            Integer.parseInt(num.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private int getLastEmployeeId() {
        List<Employee> employees = findAllEmployees();
        if (employees == null || employees.isEmpty()) return -1;
        employees.sort(Comparator.comparingInt(Employee::getId));
        return employees.getLast().getId();
    }


}