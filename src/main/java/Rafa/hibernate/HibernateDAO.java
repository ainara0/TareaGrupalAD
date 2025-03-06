package Rafa.hibernate;

import DAO.Department;
import DAO.Employee;
import DAO.IDAO;
import Utils.*;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public class HibernateDAO implements IDAO {
    SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
    private EntityManager entityManager = sessionFactory.createEntityManager();

    @Override
    public List<Employee> findAllEmployees() {
        System.out.println("");
        System.out.println("Has escodigo la opcion de listar empleados");
        System.out.println("");
        List<Employee> employees = new ArrayList<>();
        List<EmployeeJPA> employeesJAR = entityManager.createQuery("SELECT e FROM EmployeeJPA e", EmployeeJPA.class).getResultList();
        for (EmployeeJPA employeeJPA : employeesJAR) {
            Employee employee = new Employee(
                    employeeJPA.getId(),
                    employeeJPA.getName(),
                    employeeJPA.getJob(),
                    ConvertersJPA.convertToEntity(employeeJPA.getDepno())
            );
            employees.add(employee);
        }

        return employees;
    }

    @Override
    public Employee findEmployeeById(Object id) {
        EmployeeJPA employeeJPA = entityManager.find(EmployeeJPA.class, id);
        if (employeeJPA != null) {
            Employee employee = new Employee(
                    employeeJPA.getId(),
                    employeeJPA.getName(),
                    employeeJPA.getJob(),
                    findDepartmentById(employeeJPA.getDepno().getId())
            );
            System.out.println(employee);
            return employee;
        } else {
            return null;
        }
    }

    @Override
    public void addEmployee(Employee employee) {
        EmployeeJPA employeeJPA = new EmployeeJPA(
                employee.getName(),
                employee.getJob(),
                ConvertersJPA.convertToJPA(employee.getDepartment())
        );
        entityManager.getTransaction().begin();
        entityManager.persist(employeeJPA);
        entityManager.getTransaction().commit();
    }


    @Override
    public Employee updateEmployee(Object id) {
        entityManager.getTransaction().begin();
        if (!(id instanceof Employee employee)) {
            return null;
        }
        EmployeeJPA employeeJpa = entityManager.find(EmployeeJPA.class, employee.getId());

        if (employeeJpa == null) {
            entityManager.getTransaction().rollback();
            return null;
        }

        int option;

        menuUpdateEmployee();
        option = Utils.Ask.askForNumber(1, 5);

        switch (option) {
            case 1 -> {
                if (askForNameFromEmployee(employeeJpa)) return null;
            }
            case 2 -> {
                if (askForJobFromEmployee(employeeJpa)) return null;
            }
            case 3 -> {
                if (askForDepartmentFromEmployee(employeeJpa)) return null;
            }
            case 4 -> {
                if (askForNameFromEmployee(employeeJpa)) return null;

                if (askForJobFromEmployee(employeeJpa)) return null;

                if (askForDepartmentFromEmployee(employeeJpa)) return null;
            }
            case 5 -> {
                entityManager.getTransaction().rollback();
                return null;
            }

        }


        entityManager.merge(employeeJpa);
        entityManager.getTransaction().commit();
        System.out.println("Empleado actualizado correctamente.");

        return new Employee(
                employeeJpa.getId(),
                employeeJpa.getName(),
                employeeJpa.getJob(),
                findDepartmentById(employeeJpa.getDepno().getId())
        );
    }

    private static void menuUpdateEmployee() {
        System.out.println("\n--- Actualizar Empleado ---");
        System.out.println("1. Cambiar nombre");
        System.out.println("2. Cambiar puesto");
        System.out.println("3. Cambiar departamento");
        System.out.println("4. Cambiar todo");
        System.out.println("5. Atrás");
        System.out.print("Seleccione una opción: ");
    }

    private boolean askForDepartmentFromEmployee(EmployeeJPA employee) {
        System.out.print("Ingrese el id de departamento: ");
        int deptId = Utils.Ask.askForNumber();

        DepartmentJPA deptJpa = entityManager.find(DepartmentJPA.class, deptId);
        if (deptJpa == null) {
            System.out.println("El departamento no existe.");
            entityManager.getTransaction().rollback();
            return true;
        }
        employee.setDepno(deptJpa);
        return false;
    }

    private boolean askForJobFromEmployee(EmployeeJPA employee) {
        System.out.print("Ingrese el nuevo puesto: ");
        String job = Utils.Ask.askForString();
        if (job == null) {
            entityManager.getTransaction().rollback();
            return true;
        }
        employee.setJob(job);
        return false;
    }

    private boolean askForNameFromEmployee(EmployeeJPA employee) {
        System.out.print("Ingrese el nuevo apellido: ");
        String name = Utils.Ask.askForString();
        if (name == null) {
            entityManager.getTransaction().rollback();
            return true;
        }
        employee.setName(name);
        return false;
    }


    @Override
    public boolean deleteEmployee(Object id) {
        entityManager.getTransaction().begin();
        EmployeeJPA employee = entityManager.find(EmployeeJPA.class, id);
        if (employee != null) {
            System.out.println("\n Empleado eliminado con éxito. \n");
            entityManager.remove(employee);
            entityManager.getTransaction().commit();
            return true;
        } else {
            System.err.println("El empleado no existe");
            entityManager.getTransaction().rollback();
        }
        return false;
    }

    @Override
    public List<Department> findAllDepartments() {
        List<DepartmentJPA> departmentsJPA = entityManager.createQuery("SELECT e FROM DepartmentJPA e", DepartmentJPA.class).getResultList();
        List<Department> departments = new ArrayList<>();
        for (DepartmentJPA departmentJPA : departmentsJPA) {
            departments.add(new Department(
                    departmentJPA.getId(),
                    departmentJPA.getName(),
                    departmentJPA.getCity()
            ));
        }
        return departments;
    }

    @Override
    public Department findDepartmentById(Object id) {
        DepartmentJPA departmentJPA = entityManager.find(DepartmentJPA.class, id);
        if (departmentJPA != null) {
            return new Department(
                    departmentJPA.getId(),
                    departmentJPA.getName(),
                    departmentJPA.getCity()
            );
        } else{
            entityManager.getTransaction().rollback();
            return null;
        }
    }

    @Override
    public boolean addDepartment(Department department) {
        DepartmentJPA departmentJPA = Utils.ConvertersJPA.convertToJPA(department);
        if (entityManager.find(DepartmentJPA.class, departmentJPA.getId()) == null) {
            entityManager.getTransaction().begin();
            entityManager.persist(departmentJPA);
            entityManager.getTransaction().commit();
            return true;
        } else {
            entityManager.getTransaction().rollback();
            return false;
        }
    }

    @Override
    public Department updateDepartment(Object id) {
        entityManager.getTransaction().begin();

        if (!(id instanceof Employee employee)){
            return null;
        }
        DepartmentJPA departmentJPA = entityManager.find(DepartmentJPA.class, employee.getId());

        if (departmentJPA == null) {
            entityManager.getTransaction().rollback();
            return null;
        }

        int option;

        System.out.println("\n--- Actualizar Departamento ---");
        System.out.println("1. Cambiar nombre");
        System.out.println("2. Cambiar ubicacion");
        System.out.println("3. Cambiar Ambos");
        System.out.println("4. Atrás");
        System.out.print("Seleccione una opción: ");
        option = Ask.askForNumber(1,4);


        switch (option) {

            case 1 -> {
                if (askForNameFromDepartment(departmentJPA)) return null;
            }
            case 2 -> {
                if (askForCityFromDepartment(departmentJPA)) return null;
            }
            case 3 -> {

                if (askForNameFromDepartment(departmentJPA)) return null;

                if (askForCityFromDepartment(departmentJPA)) return null;
            }
            case 4 -> {
                System.out.println("Regresando al menú principal...");
                entityManager.getTransaction().rollback();
                return null;
            }

        }

        entityManager.merge(departmentJPA);
        entityManager.getTransaction().commit();
        System.out.println("Empleado actualizado correctamente.");

        return new Department(
                departmentJPA.getId(),
                departmentJPA.getName(),
                departmentJPA.getCity()
        );
    }

    private boolean askForCityFromDepartment(DepartmentJPA department) {
        System.out.print("Ingrese la nueva ubicacion: ");
        String city = Utils.Ask.askForString();
        if (city == null) {
            entityManager.getTransaction().rollback();
            return true;
        }
        department.setCity(city);
        return false;
    }

    private boolean askForNameFromDepartment(DepartmentJPA department) {
        System.out.print("Ingrese el nuevo nombre: ");
        String name = Utils.Ask.askForString();
        if (name == null) {
            entityManager.getTransaction().rollback();
            return true;
        }
        department.setName(name);
        return false;
    }

    private boolean askForIdFromDepartment(DepartmentJPA department) {
        System.out.print("Ingrese el nuevo Id: ");
        int deptId = Utils.Ask.askForNumber();
        Department dept = findDepartmentById(deptId);
        if (dept == null) {
            department.setId(deptId);
            return false;
        } else {
            System.err.println("Ese id de departamento ya existe");
            entityManager.getTransaction().rollback();
            return true;
        }
    }

    @Override
    public Department deleteDepartment(Object id) {
        entityManager.getTransaction().begin();
        DepartmentJPA departmentJPA = entityManager.find(DepartmentJPA.class, id);
        if (departmentJPA != null) {
            entityManager.remove(departmentJPA);
            entityManager.getTransaction().commit();
            return new Department(
                    departmentJPA.getId(),
                    departmentJPA.getName(),
                    departmentJPA.getCity()
            );
        }
        return null;
    }

    @Override
    public List<Employee> findEmployeesByDept(Object idDept) {
        List<Employee> employees = new ArrayList<>();
        List<EmployeeJPA> employeesJPA = entityManager.createQuery("SELECT e FROM EmployeeJPA e WHERE e.depno.id = :deptId", EmployeeJPA.class)
                .setParameter("deptId", idDept)
                .getResultList();

        for (EmployeeJPA employeeJPA : employeesJPA) {
            employees.add(new Employee(
                    employeeJPA.getId(),
                    employeeJPA.getName(),
                    employeeJPA.getJob(),
                    findDepartmentById(employeeJPA.getDepno().getId())
            ));
        }
        return employees;
    }

}
