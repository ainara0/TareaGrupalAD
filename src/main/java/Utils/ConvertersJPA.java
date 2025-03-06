package Utils;

import DAO.Department;
import Rafa.hibernate.DepartmentJPA;

public class ConvertersJPA {
    public static DepartmentJPA convertToJPA(Department department) {
        DepartmentJPA departmentJPA = new DepartmentJPA();
        departmentJPA.setId(department.getId());
        departmentJPA.setName(department.getName());
        departmentJPA.setCity(department.getLocation());
        return departmentJPA;
    }
    public static Department convertToEntity(DepartmentJPA departmentJPA) {
        Department department = new Department();
        department.setId(departmentJPA.getId());
        department.setName(departmentJPA.getName());
        department.setLocation(departmentJPA.getCity());
        return department;
    }
}