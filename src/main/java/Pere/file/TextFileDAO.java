package Pere.file;

import DAO.Department;
import DAO.Employee;
import DAO.IDAO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Implementation of the IDAO interface for file-based data storage.
 * <p>
 * This class reads and writes data from/to a text file (empresa.txt) for both employees and departments.
 * It provides basic CRUD operations and stores data in memory, synchronizing changes with the file.
 * </p>
 * <p>
 * The file is expected to contain lines in the following formats:
 * <ul>
 *   <li>Departments: <code>department(id,name,city)</code></li>
 *   <li>Employees: <code>employee(id,surname,job,department_id)</code></li>
 * </ul>
 * </p>
 */
public class TextFileDAO implements IDAO {

    /** Data file path (make sure it is in the correct directory) */
    private final String filePath = "empresa.txt";

    /** List of departments loaded from the file */
    private List<Department> departments;
    /** List of employees loaded from the file */
    private List<Employee> employees;

    /**
     * Constructor: Loads data from the file upon initialization.
     */
    public TextFileDAO() {
        departments = new ArrayList<>();
        employees = new ArrayList<>();
        loadData();
    }

    /**
     * Loads data from the file into the in-memory lists.
     * <p>
     * First, it reads all department entries, then it re-reads the file to load employee entries.
     * If an employee references a department that is not yet loaded, a default department is created.
     * </p>
     */
    private void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // First, load departments
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines or comments
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                // Process department lines
                if (line.startsWith("department(")) {
                    int start = line.indexOf('(');
                    int end = line.lastIndexOf(')');
                    if (start != -1 && end != -1) {
                        String content = line.substring(start + 1, end);
                        String[] parts = content.split(",");
                        if (parts.length >= 3) {
                            int id = Integer.parseInt(parts[0].trim());
                            String name = parts[1].trim();
                            String city = parts[2].trim();
                            departments.add(new Department(id, name, city));
                        }
                    }
                }
            }
            // Restart the BufferedReader to load employees
            br.close();
            BufferedReader br2 = new BufferedReader(new FileReader(filePath));
            while ((line = br2.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                // Process employee lines
                if (line.startsWith("employee(")) {
                    int start = line.indexOf('(');
                    int end = line.lastIndexOf(')');
                    if (start != -1 && end != -1) {
                        String content = line.substring(start + 1, end);
                        String[] parts = content.split(",");
                        if (parts.length >= 4) {
                            int id = Integer.parseInt(parts[0].trim());
                            String surname = parts[1].trim(); // mapped to name
                            String job = parts[2].trim();
                            int depId = Integer.parseInt(parts[3].trim());
                            // Find the corresponding Department object
                            Department dept = findDepartmentById(depId);
                            // If not found, create a default department and add it to the list
                            if (dept == null) {
                                dept = new Department(depId, "NoNombre", "NoLocacion");
                                departments.add(dept);
                            }
                            employees.add(new Employee(id, surname, job, dept));
                        }
                    }
                }
            }
            br2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current data from the in-memory lists to the file.
     * <p>
     * The file is overwritten with the current department and employee data.
     * </p>
     */
    private void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            // Write departments header
            pw.println("-- departments: department(id,name,city).");
            for (Department d : departments) {
                pw.println("department(" + d.getId() + "," + d.getName() + "," + d.getLocation() + ")");
            }
            pw.println(); // Blank line
            pw.println("-- employees: employee(id,surname,job,department_id).");
            for (Employee e : employees) {
                // Extract the department id from the Department object
                pw.println("employee(" + e.getId() + "," + e.getName() + "," + e.getJob() + "," + e.getDepartment().getId() + ")");
            }
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // IDAO Interface Methods for EMPLOYEES

    /**
     * Retrieves all employees.
     *
     * @return a list of {@link Employee} objects.
     */
    @Override
    public List<Employee> findAllEmployees() {
        return new ArrayList<>(employees);
    }

    /**
     * Finds an employee by its ID.
     *
     * @param id the employee's identifier.
     * @return the {@link Employee} if found, or {@code null} otherwise.
     */
    @Override
    public Employee findEmployeeById(Object id) {
        int empId = (int) id;
        for (Employee e : employees) {
            if (e.getId() == empId) {
                return e;
            }
        }
        return null;
    }

    /**
     * Adds a new employee.
     * <p>
     * Checks if an employee with the same ID already exists and if the referenced department exists.
     * If the checks pass, the employee is added to the list and data is saved to the file.
     * </p>
     *
     * @param employee the {@link Employee} to add.
     */
    @Override
    public void addEmployee(Employee employee) {
        // Check if an employee with the same ID already exists
        for (Employee e : employees) {
            if (e.getId() == employee.getId()) {
                System.out.println("Ya existe un empleado con el ID:" + employee.getId());
                return;
            }
        }
        // Check if the department exists
        boolean deptExists = false;
        for (Department d : departments) {
            if (d.getId() == employee.getDepartment().getId()) {
                deptExists = true;
                break;
            }
        }
        if (!deptExists) {
            System.out.println("El departamento con id " + employee.getDepartment().getId() + " no existe.");
            return;
        }
        employees.add(employee);
        saveData();
    }

    /**
     * Updates an existing employee.
     * <p>
     * Displays an interactive menu to select which field(s) to update:
     * <ul>
     *   <li>1 - Name</li>
     *   <li>2 - Job</li>
     *   <li>3 - Department</li>
     *   <li>4 - All fields</li>
     * </ul>
     * </p>
     *
     * @param id the identifier of the employee to update.
     * @return the updated {@link Employee} object, or {@code null} if not found or if update is canceled.
     */
    @Override
    public Employee updateEmployee(Object id) {
        if (!(id instanceof Employee employee)){
            return null;
        }
        Scanner scanner = new Scanner(System.in);
        for (Employee e : employees) {
            if (e.getId() == employee.getId()) {
                System.out.println("Seleccione el campo que desea actualizar para el empleado:");
                System.out.println("1. Nombre");
                System.out.println("2. Trabajo");
                System.out.println("3. Departamento");
                System.out.println("4. Todos los campos");
                System.out.print("Opciones: ");
                int option;
                try {
                    option = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException ex) {
                    System.out.println("Error: La opción debe ser un número entero.");
                    return null;
                }

                switch (option) {
                    case 1:
                        System.out.print("Introduzca el nuevo nombre: ");
                        String newName = scanner.nextLine();
                        e.setName(newName);
                        break;
                    case 2:
                        System.out.print("Introduzca el nuevo trabajo: ");
                        String newJob = scanner.nextLine();
                        e.setJob(newJob);
                        break;
                    case 3:
                        System.out.print("Introduzca el nuevo ID del departamento: ");
                        int newDepId;
                        try {
                            newDepId = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException ex) {
                            System.out.println("Error: El ID del departamento debe ser un número entero.");
                            return null;
                        }
                        Department newDept = findDepartmentById(newDepId);
                        if (newDept == null) {
                            System.out.println("Departamento no encontrado.");
                            return null;
                        }
                        e.setDepartment(newDept);
                        break;
                    case 4:
                        System.out.print("Introduzca el nuevo nombre: ");
                        String allName = scanner.nextLine();
                        System.out.print("Introduzca el nuevo trabajo: ");
                        String allJob = scanner.nextLine();
                        System.out.print("Introduzca el nuevo ID del departamento: ");
                        int allDepId;
                        try {
                            allDepId = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException ex) {
                            System.out.println("Error: el ID del departamento debe ser un número entero.");
                            return null;
                        }
                        Department allDept = findDepartmentById(allDepId);
                        if (allDept == null) {
                            System.out.println("Departamento no encontrado.");
                            return null;
                        }
                        e.setName(allName);
                        e.setJob(allJob);
                        e.setDepartment(allDept);
                        break;
                    default:
                        System.out.println("Opción inválida.");
                        return null;
                }
                saveData();
                return e;
            }
        }
        return null;
    }

    /**
     * Deletes an employee.
     *
     * @param id the identifier of the employee to delete.
     * @return {@code true} if deletion was successful; {@code false} otherwise.
     */
    @Override
    public boolean deleteEmployee(Object id) {
        int empId = (int) id;
        Employee toRemove = null;
        for (Employee e : employees) {
            if (e.getId() == empId) {
                toRemove = e;
                break;
            }
        }
        if (toRemove != null) {
            employees.remove(toRemove);
            saveData();
            return true;
        }
        return false;
    }

    /**
     * Finds all employees belonging to a specific department.
     *
     * @param idDept the department identifier.
     * @return a list of {@link Employee} objects belonging to that department.
     */
    @Override
    public List<Employee> findEmployeesByDept(Object idDept) {
        int depId = (int) idDept;
        List<Employee> result = new ArrayList<>();
        for (Employee e : employees) {
            if (e.getDepartment().getId() == depId) {
                result.add(e);
            }
        }
        return result;
    }

    // IDAO Interface Methods for DEPARTMENTS

    /**
     * Retrieves all departments.
     *
     * @return a list of {@link Department} objects.
     */
    @Override
    public List<Department> findAllDepartments() {
        return new ArrayList<>(departments);
    }

    /**
     * Finds a department by its ID.
     *
     * @param id the department identifier.
     * @return the {@link Department} object if found; {@code null} otherwise.
     */
    @Override
    public Department findDepartmentById(Object id) {
        int depId = (int) id;
        for (Department d : departments) {
            if (d.getId() == depId) {
                return d;
            }
        }
        return null;
    }

    /**
     * Adds a new department.
     * <p>
     * Checks that no department with the same id already exists. If the check passes,
     * the department is added and data is saved to the file.
     * </p>
     *
     * @param department the {@link Department} to add.
     * @return {@code true} if the insertion was successful; {@code false} otherwise.
     */
    @Override
    public boolean addDepartment(Department department) {
        for (Department d : departments) {
            if (d.getId() == department.getId()) {
                return false;
            }
        }
        departments.add(department);
        saveData();
        return true;
    }

    /**
     * Updates an existing department.
     * <p>
     * Displays an interactive menu to update:
     * <ul>
     *   <li>1 - Name</li>
     *   <li>2 - City</li>
     *   <li>3 - Both</li>
     * </ul>
     * </p>
     *
     * @param id the identifier of the department to update.
     * @return the updated {@link Department} object or {@code null} if not found or update is canceled.
     */
    @Override
    public Department updateDepartment(Object id) {
        if (!(id instanceof Department dept)){
            return null;
        }
        Scanner scanner = new Scanner(System.in);
        for (Department d : departments) {
            if (d.getId() == dept.getId()) {
                System.out.println("Seleccione el campo a actualizar para el departamento:");
                System.out.println("1. Nombre");
                System.out.println("2. Ciudad");
                System.out.println("3. Ambas");
                System.out.print("Opcion: ");
                int option = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (option) {
                    case 1:
                        System.out.print("Introduzca el nuevo nombre: ");
                        String newName = scanner.nextLine();
                        d.setName(newName);
                        break;
                    case 2:
                        System.out.print("Introduzca la nueva ciudad: ");
                        String newCity = scanner.nextLine();
                        d.setLocation(newCity);
                        break;
                    case 3:
                        System.out.print("Introduzca el nuevo nombre: ");
                        String allName = scanner.nextLine();
                        System.out.print("Introduzca la nueva ciudad: ");
                        String allCity = scanner.nextLine();
                        d.setName(allName);
                        d.setLocation(allCity);
                        break;
                    default:
                        System.out.println("Opción inválida.");
                        return null;
                }
                saveData();
                return d;
            }
        }
        return null;
    }

    /**
     * Deletes a department.
     * <p>
     * The department is deleted if it exists and if there are no employees assigned to it.
     * Otherwise, it returns {@code null}.
     * </p>
     *
     * @param id the identifier of the department to delete.
     * @return the deleted {@link Department} object, or {@code null} if not found or deletion is not allowed.
     */
    @Override
    public Department deleteDepartment(Object id) {
        int depId = (int) id;
        Department toRemove = null;
        for (Department d : departments) {
            if (d.getId() == depId) {
                toRemove = d;
                break;
            }
        }
        if (toRemove != null) {
            // Optional: Check if there are employees assigned to the department
            boolean hasEmployees = false;
            for (Employee e : employees) {
                if (e.getDepartment().getId() == depId) {
                    hasEmployees = true;
                    break;
                }
            }
            if (hasEmployees) {
                System.out.println("El departamento no se puede eliminar; tiene empleados asignados a él.");
                return null;
            }
            departments.remove(toRemove);
            saveData();
        }
        return toRemove;
    }
}