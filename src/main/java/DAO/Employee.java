package DAO;

public class Employee {
    private int id;
    private String name;
    private String job;
    private Department department;

    public Employee() {
    }

    public Employee(int id, String name, String job, Department department) {
        this.id = id;
        this.name = name;
        this.job = job;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department deptId) {
        this.department = deptId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Employee:" + "\n ID=" + id + ", \n Name=" + name + ",\n Job=" + job + "\n DeptID=" + department.getId() + " \n ____________________________________";
    }

}